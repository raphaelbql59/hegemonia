import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get all technologies
router.get('/', async (req, res) => {
  try {
    const era = req.query.era as string | undefined;

    const technologies = await prisma.technology.findMany({
      where: era ? { era: era as any } : undefined,
      orderBy: [
        { era: 'asc' },
        { tier: 'asc' }
      ]
    });

    res.json({ technologies });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch technologies' });
  }
});

// Get nation's unlocked technologies
router.get('/nation/:nationId', async (req, res) => {
  try {
    const unlocks = await prisma.technologyUnlock.findMany({
      where: { nationId: req.params.nationId },
      include: {
        technology: true
      },
      orderBy: { unlockedAt: 'desc' }
    });

    res.json({ unlocks });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch unlocked technologies' });
  }
});

export default router;
