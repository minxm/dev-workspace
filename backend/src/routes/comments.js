const express = require('express');
const router = express.Router();
const commentController = require('../controllers/commentController');
const authMiddleware = require('../middleware/auth');

router.get('/note/:noteId', authMiddleware, commentController.getCommentsByNoteId);
router.post('/note/:noteId', authMiddleware, commentController.createComment);
router.delete('/:id', authMiddleware, commentController.deleteComment);
router.post('/:id/like', authMiddleware, commentController.toggleCommentLike);

module.exports = router;
