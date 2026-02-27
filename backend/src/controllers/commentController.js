const { v4: uuidv4 } = require('uuid');
const db = require('../config/database');

// Get comments by note ID
exports.getCommentsByNoteId = async (req, res) => {
  const { noteId } = req.params;
  const userId = req.userId;

  try {
    const [comments] = await db.query(`
      SELECT 
        c.*,
        u.username,
        u.avatar as user_avatar,
        (SELECT COUNT(*) FROM comment_likes WHERE comment_id = c.id AND user_id = ?) as is_liked
      FROM comments c
      JOIN users u ON c.user_id = u.id
      WHERE c.note_id = ?
      ORDER BY c.created_at DESC
    `, [userId, noteId]);

    comments.forEach(comment => {
      comment.is_liked = comment.is_liked > 0;
    });

    res.json({
      success: true,
      data: comments
    });

  } catch (error) {
    console.error('获取评论列表错误:', error);
    res.status(500).json({
      success: false,
      message: '获取评论列表失败',
      error: error.message
    });
  }
};

// Create comment
exports.createComment = async (req, res) => {
  const { noteId } = req.params;
  const { content } = req.body;
  const userId = req.userId;

  try {
    if (!content || content.trim() === '') {
      return res.status(400).json({
        success: false,
        message: '评论内容不能为空'
      });
    }

    // Check if note exists
    const [notes] = await db.query('SELECT id FROM notes WHERE id = ?', [noteId]);
    if (notes.length === 0) {
      return res.status(404).json({
        success: false,
        message: '笔记不存在'
      });
    }

    const commentId = uuidv4();

    // Insert comment
    await db.query(
      'INSERT INTO comments (id, note_id, user_id, content) VALUES (?, ?, ?, ?)',
      [commentId, noteId, userId, content]
    );

    // Update note comments count
    await db.query(
      'UPDATE notes SET comments_count = comments_count + 1 WHERE id = ?',
      [noteId]
    );

    // Get created comment with user info
    const [comments] = await db.query(`
      SELECT c.*, u.username, u.avatar as user_avatar
      FROM comments c
      JOIN users u ON c.user_id = u.id
      WHERE c.id = ?
    `, [commentId]);

    res.status(201).json({
      success: true,
      message: '评论成功',
      data: comments[0]
    });

  } catch (error) {
    console.error('创建评论错误:', error);
    res.status(500).json({
      success: false,
      message: '评论失败',
      error: error.message
    });
  }
};

// Delete comment
exports.deleteComment = async (req, res) => {
  const { id } = req.params;
  const userId = req.userId;

  try {
    // Check if comment exists and belongs to user
    const [comments] = await db.query(
      'SELECT user_id, note_id FROM comments WHERE id = ?',
      [id]
    );

    if (comments.length === 0) {
      return res.status(404).json({
        success: false,
        message: '评论不存在'
      });
    }

    if (comments[0].user_id !== userId) {
      return res.status(403).json({
        success: false,
        message: '无权删除此评论'
      });
    }

    const noteId = comments[0].note_id;

    // Delete comment
    await db.query('DELETE FROM comments WHERE id = ?', [id]);

    // Update note comments count
    await db.query(
      'UPDATE notes SET comments_count = comments_count - 1 WHERE id = ?',
      [noteId]
    );

    res.json({
      success: true,
      message: '删除成功'
    });

  } catch (error) {
    console.error('删除评论错误:', error);
    res.status(500).json({
      success: false,
      message: '删除失败',
      error: error.message
    });
  }
};

// Like/Unlike comment
exports.toggleCommentLike = async (req, res) => {
  const { id } = req.params;
  const userId = req.userId;

  try {
    // Check if already liked
    const [likes] = await db.query(
      'SELECT id FROM comment_likes WHERE user_id = ? AND comment_id = ?',
      [userId, id]
    );

    if (likes.length > 0) {
      // Unlike
      await db.query(
        'DELETE FROM comment_likes WHERE user_id = ? AND comment_id = ?',
        [userId, id]
      );
      await db.query(
        'UPDATE comments SET likes_count = likes_count - 1 WHERE id = ?',
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
        'INSERT INTO comment_likes (id, user_id, comment_id) VALUES (?, ?, ?)',
        [uuidv4(), userId, id]
      );
      await db.query(
        'UPDATE comments SET likes_count = likes_count + 1 WHERE id = ?',
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
