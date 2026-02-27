const { v4: uuidv4 } = require('uuid');
const db = require('../config/database');

// Get all notes with pagination
exports.getAllNotes = async (req, res) => {
  const page = parseInt(req.query.page) || 1;
  const limit = parseInt(req.query.limit) || 20;
  const offset = (page - 1) * limit;
  const userId = req.userId;

  try {
    // Get notes with user info
    const [notes] = await db.query(`
      SELECT 
        n.*,
        u.username,
        u.avatar as user_avatar,
        (SELECT COUNT(*) FROM user_likes WHERE note_id = n.id AND user_id = ?) as is_liked,
        (SELECT COUNT(*) FROM user_collects WHERE note_id = n.id AND user_id = ?) as is_collected
      FROM notes n
      JOIN users u ON n.user_id = u.id
      ORDER BY n.created_at DESC
      LIMIT ? OFFSET ?
    `, [userId, userId, limit, offset]);

    // Get images for each note
    for (let note of notes) {
      const [images] = await db.query(
        'SELECT image_url FROM note_images WHERE note_id = ? ORDER BY sort_order',
        [note.id]
      );
      note.images = images.map(img => img.image_url);

      const [topics] = await db.query(
        'SELECT topic FROM note_topics WHERE note_id = ?',
        [note.id]
      );
      note.topics = topics.map(t => t.topic);

      note.is_liked = note.is_liked > 0;
      note.is_collected = note.is_collected > 0;
    }

    // Get total count
    const [countResult] = await db.query('SELECT COUNT(*) as total FROM notes');
    const total = countResult[0].total;

    res.json({
      success: true,
      data: {
        notes,
        pagination: {
          page,
          limit,
          total,
          totalPages: Math.ceil(total / limit)
        }
      }
    });

  } catch (error) {
    console.error('获取笔记列表错误:', error);
    res.status(500).json({
      success: false,
      message: '获取笔记列表失败',
      error: error.message
    });
  }
};

// Get note by ID
exports.getNoteById = async (req, res) => {
  const { id } = req.params;
  const userId = req.userId;

  try {
    const [notes] = await db.query(`
      SELECT 
        n.*,
        u.username,
        u.avatar as user_avatar,
        (SELECT COUNT(*) FROM user_likes WHERE note_id = n.id AND user_id = ?) as is_liked,
        (SELECT COUNT(*) FROM user_collects WHERE note_id = n.id AND user_id = ?) as is_collected
      FROM notes n
      JOIN users u ON n.user_id = u.id
      WHERE n.id = ?
    `, [userId, userId, id]);

    if (notes.length === 0) {
      return res.status(404).json({
        success: false,
        message: '笔记不存在'
      });
    }

    const note = notes[0];

    // Get images
    const [images] = await db.query(
      'SELECT image_url FROM note_images WHERE note_id = ? ORDER BY sort_order',
      [note.id]
    );
    note.images = images.map(img => img.image_url);

    // Get topics
    const [topics] = await db.query(
      'SELECT topic FROM note_topics WHERE note_id = ?',
      [note.id]
    );
    note.topics = topics.map(t => t.topic);

    note.is_liked = note.is_liked > 0;
    note.is_collected = note.is_collected > 0;

    // Increment views
    await db.query(
      'UPDATE notes SET views_count = views_count + 1 WHERE id = ?',
      [id]
    );

    res.json({
      success: true,
      data: note
    });

  } catch (error) {
    console.error('获取笔记详情错误:', error);
    res.status(500).json({
      success: false,
      message: '获取笔记详情失败',
      error: error.message
    });
  }
};

// Create note
exports.createNote = async (req, res) => {
  const { title, content, location, topics } = req.body;
  const userId = req.userId;

  try {
    if (!title || !content) {
      return res.status(400).json({
        success: false,
        message: '请提供标题和内容'
      });
    }

    if (!req.files || req.files.length === 0) {
      return res.status(400).json({
        success: false,
        message: '请至少上传一张图片'
      });
    }

    const noteId = uuidv4();
    const imageUrls = req.files.map(file => `${process.env.BASE_URL}/${file.path}`);
    const coverImage = imageUrls[0];

    // Insert note
    await db.query(
      'INSERT INTO notes (id, user_id, title, content, cover_image, location) VALUES (?, ?, ?, ?, ?, ?)',
      [noteId, userId, title, content, coverImage, location || '']
    );

    // Insert images
    for (let i = 0; i < imageUrls.length; i++) {
      await db.query(
        'INSERT INTO note_images (id, note_id, image_url, sort_order) VALUES (?, ?, ?, ?)',
        [uuidv4(), noteId, imageUrls[i], i]
      );
    }

    // Insert topics
    if (topics) {
      const topicList = Array.isArray(topics) ? topics : JSON.parse(topics);
      for (let topic of topicList) {
        await db.query(
          'INSERT INTO note_topics (id, note_id, topic) VALUES (?, ?, ?)',
          [uuidv4(), noteId, topic]
        );
      }
    }

    // Update user notes count
    await db.query(
      'UPDATE users SET notes_count = notes_count + 1 WHERE id = ?',
      [userId]
    );

    // Get created note
    const [notes] = await db.query(`
      SELECT n.*, u.username, u.avatar as user_avatar
      FROM notes n
      JOIN users u ON n.user_id = u.id
      WHERE n.id = ?
    `, [noteId]);

    const note = notes[0];
    note.images = imageUrls;
    note.topics = topics ? (Array.isArray(topics) ? topics : JSON.parse(topics)) : [];

    res.status(201).json({
      success: true,
      message: '发布成功',
      data: note
    });

  } catch (error) {
    console.error('创建笔记错误:', error);
    res.status(500).json({
      success: false,
      message: '发布失败',
      error: error.message
    });
  }
};

// Delete note
exports.deleteNote = async (req, res) => {
  const { id } = req.params;
  const userId = req.userId;

  try {
    // Check if note exists and belongs to user
    const [notes] = await db.query(
      'SELECT user_id FROM notes WHERE id = ?',
      [id]
    );

    if (notes.length === 0) {
      return res.status(404).json({
        success: false,
        message: '笔记不存在'
      });
    }

    if (notes[0].user_id !== userId) {
      return res.status(403).json({
        success: false,
        message: '无权删除此笔记'
      });
    }

    // Delete note (cascade will delete related records)
    await db.query('DELETE FROM notes WHERE id = ?', [id]);

    // Update user notes count
    await db.query(
      'UPDATE users SET notes_count = notes_count - 1 WHERE id = ?',
      [userId]
    );

    res.json({
      success: true,
      message: '删除成功'
    });

  } catch (error) {
    console.error('删除笔记错误:', error);
    res.status(500).json({
      success: false,
      message: '删除失败',
      error: error.message
    });
  }
};

// Like/Unlike note
exports.toggleLike = async (req, res) => {
  const { id } = req.params;
  const userId = req.userId;

  try {
    // Check if already liked
    const [likes] = await db.query(
      'SELECT id FROM user_likes WHERE user_id = ? AND note_id = ?',
      [userId, id]
    );

    if (likes.length > 0) {
      // Unlike
      await db.query(
        'DELETE FROM user_likes WHERE user_id = ? AND note_id = ?',
        [userId, id]
      );
      await db.query(
        'UPDATE notes SET likes_count = likes_count - 1 WHERE id = ?',
        [id]
      );
      
      res.json({
        success: true,
        message: '取消点赞',
        data: { is_liked: false }
      });
    } else {
      // Like
      await db.query(
        'INSERT INTO user_likes (id, user_id, note_id) VALUES (?, ?, ?)',
        [uuidv4(), userId, id]
      );
      await db.query(
        'UPDATE notes SET likes_count = likes_count + 1 WHERE id = ?',
        [id]
      );
      
      res.json({
        success: true,
        message: '点赞成功',
        data: { is_liked: true }
      });
    }

  } catch (error) {
    console.error('点赞操作错误:', error);
    res.status(500).json({
      success: false,
      message: '操作失败',
      error: error.message
    });
  }
};

// Collect/Uncollect note
exports.toggleCollect = async (req, res) => {
  const { id } = req.params;
  const userId = req.userId;

  try {
    // Check if already collected
    const [collects] = await db.query(
      'SELECT id FROM user_collects WHERE user_id = ? AND note_id = ?',
      [userId, id]
    );

    if (collects.length > 0) {
      // Uncollect
      await db.query(
        'DELETE FROM user_collects WHERE user_id = ? AND note_id = ?',
        [userId, id]
      );
      await db.query(
        'UPDATE notes SET collects_count = collects_count - 1 WHERE id = ?',
        [id]
      );
      
      res.json({
        success: true,
        message: '取消收藏',
        data: { is_collected: false }
      });
    } else {
      // Collect
      await db.query(
        'INSERT INTO user_collects (id, user_id, note_id) VALUES (?, ?, ?)',
        [uuidv4(), userId, id]
      );
      await db.query(
        'UPDATE notes SET collects_count = collects_count + 1 WHERE id = ?',
        [id]
      );
      
      res.json({
        success: true,
        message: '收藏成功',
        data: { is_collected: true }
      });
    }

  } catch (error) {
    console.error('收藏操作错误:', error);
    res.status(500).json({
      success: false,
      message: '操作失败',
      error: error.message
    });
  }
};

// Get user's notes
exports.getUserNotes = async (req, res) => {
  const { userId } = req.params;
  const currentUserId = req.userId;

  try {
    const [notes] = await db.query(`
      SELECT 
        n.*,
        u.username,
        u.avatar as user_avatar,
        (SELECT COUNT(*) FROM user_likes WHERE note_id = n.id AND user_id = ?) as is_liked,
        (SELECT COUNT(*) FROM user_collects WHERE note_id = n.id AND user_id = ?) as is_collected
      FROM notes n
      JOIN users u ON n.user_id = u.id
      WHERE n.user_id = ?
      ORDER BY n.created_at DESC
    `, [currentUserId, currentUserId, userId]);

    // Get images for each note
    for (let note of notes) {
      const [images] = await db.query(
        'SELECT image_url FROM note_images WHERE note_id = ? ORDER BY sort_order',
        [note.id]
      );
      note.images = images.map(img => img.image_url);

      const [topics] = await db.query(
        'SELECT topic FROM note_topics WHERE note_id = ?',
        [note.id]
      );
      note.topics = topics.map(t => t.topic);

      note.is_liked = note.is_liked > 0;
      note.is_collected = note.is_collected > 0;
    }

    res.json({
      success: true,
      data: notes
    });

  } catch (error) {
    console.error('获取用户笔记错误:', error);
    res.status(500).json({
      success: false,
      message: '获取用户笔记失败',
      error: error.message
    });
  }
};

// Search notes
exports.searchNotes = async (req, res) => {
  const { q } = req.query;
  const userId = req.userId;

  try {
    if (!q) {
      return res.status(400).json({
        success: false,
        message: '请提供搜索关键词'
      });
    }

    const searchTerm = `%${q}%`;

    const [notes] = await db.query(`
      SELECT DISTINCT
        n.*,
        u.username,
        u.avatar as user_avatar,
        (SELECT COUNT(*) FROM user_likes WHERE note_id = n.id AND user_id = ?) as is_liked,
        (SELECT COUNT(*) FROM user_collects WHERE note_id = n.id AND user_id = ?) as is_collected
      FROM notes n
      JOIN users u ON n.user_id = u.id
      LEFT JOIN note_topics nt ON n.id = nt.note_id
      WHERE n.title LIKE ? OR n.content LIKE ? OR u.username LIKE ? OR nt.topic LIKE ?
      ORDER BY n.created_at DESC
    `, [userId, userId, searchTerm, searchTerm, searchTerm, searchTerm]);

    // Get images for each note
    for (let note of notes) {
      const [images] = await db.query(
        'SELECT image_url FROM note_images WHERE note_id = ? ORDER BY sort_order',
        [note.id]
      );
      note.images = images.map(img => img.image_url);

      const [topics] = await db.query(
        'SELECT topic FROM note_topics WHERE note_id = ?',
        [note.id]
      );
      note.topics = topics.map(t => t.topic);

      note.is_liked = note.is_liked > 0;
      note.is_collected = note.is_collected > 0;
    }

    res.json({
      success: true,
      data: notes
    });

  } catch (error) {
    console.error('搜索笔记错误:', error);
    res.status(500).json({
      success: false,
      message: '搜索失败',
      error: error.message
    });
  }
};
