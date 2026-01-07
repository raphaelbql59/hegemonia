import express, { Request, Response } from 'express';
import { query } from '../db/connection.js';
import { authenticateToken, requireAdmin, AuthRequest } from '../middleware/auth.js';

const router = express.Router();

/**
 * GET /api/news
 * Récupère toutes les actualités publiées
 */
router.get('/', async (req: Request, res: Response) => {
  try {
    const result = await query(
      `SELECT
        n.id, n.title, n.content, n.thumbnail_url,
        n.published_at, n.tags, n.views_count,
        u.username as author_username,
        u.display_name as author_display_name,
        u.avatar_url as author_avatar
       FROM launcher_news n
       LEFT JOIN launcher_users u ON n.author_id = u.id
       WHERE n.is_published = true
       ORDER BY n.published_at DESC
       LIMIT 20`
    );

    res.json(result.rows);
  } catch (error) {
    console.error('Get news error:', error);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

/**
 * GET /api/news/:id
 * Récupère une actualité spécifique
 */
router.get('/:id', async (req: Request, res: Response) => {
  try {
    const { id } = req.params;

    const result = await query(
      `SELECT
        n.id, n.title, n.content, n.thumbnail_url,
        n.published_at, n.tags, n.views_count,
        u.username as author_username,
        u.display_name as author_display_name,
        u.avatar_url as author_avatar
       FROM launcher_news n
       LEFT JOIN launcher_users u ON n.author_id = u.id
       WHERE n.id = $1 AND n.is_published = true`,
      [id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Actualité introuvable' });
    }

    // Increment views count
    await query(
      'UPDATE launcher_news SET views_count = views_count + 1 WHERE id = $1',
      [id]
    );

    res.json(result.rows[0]);
  } catch (error) {
    console.error('Get news by ID error:', error);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

/**
 * POST /api/news
 * Créer une nouvelle actualité (Admin only)
 */
router.post('/', authenticateToken, requireAdmin, async (req: AuthRequest, res: Response) => {
  try {
    const { title, content, thumbnailUrl, tags } = req.body;

    if (!title || !content) {
      return res.status(400).json({ error: 'Titre et contenu requis' });
    }

    const result = await query(
      `INSERT INTO launcher_news (title, content, author_id, thumbnail_url, tags)
       VALUES ($1, $2, $3, $4, $5)
       RETURNING *`,
      [title, content, req.user!.id, thumbnailUrl || null, tags || []]
    );

    res.status(201).json(result.rows[0]);
  } catch (error) {
    console.error('Create news error:', error);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

export default router;
