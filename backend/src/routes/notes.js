const express = require('express');
const router = express.Router();
const noteController = require('../controllers/noteController');
const authMiddleware = require('../middleware/auth');
const upload = require('../middleware/upload');

router.get('/', authMiddleware, noteController.getAllNotes);
router.get('/search', authMiddleware, noteController.searchNotes);
router.get('/user/:userId', authMiddleware, noteController.getUserNotes);
router.get('/:id', authMiddleware, noteController.getNoteById);
router.post('/', authMiddleware, upload.array('images', 9), noteController.createNote);
router.delete('/:id', authMiddleware, noteController.deleteNote);
router.post('/:id/like', authMiddleware, noteController.toggleLike);
router.post('/:id/collect', authMiddleware, noteController.toggleCollect);

module.exports = router;
