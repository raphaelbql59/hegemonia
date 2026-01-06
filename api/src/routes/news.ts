import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get all news posts
router.get('/', async (req, res) => {
  try {
    const limit = parseInt(req.query.limit as string) || 10;

    const newsPosts = await prisma.newsPost.findMany({
      take: limit,
      orderBy: [
        { isPinned: 'desc' },
        { publishedAt: 'desc' }
      ]
    });

    res.json({ news: newsPosts });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch news' });
  }
});

// Get news post by ID
router.get('/:id', async (req, res) => {
  try {
    const newsPost = await prisma.newsPost.findUnique({
      where: { id: req.params.id }
    });

    if (!newsPost) {
      return res.status(404).json({ error: 'News post not found' });
    }

    res.json(newsPost);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch news post' });
  }
});

export default router;
