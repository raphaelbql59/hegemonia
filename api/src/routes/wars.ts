import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get all active wars
router.get('/', async (req, res) => {
  try {
    const wars = await prisma.war.findMany({
      where: { status: 'ACTIVE' },
      include: {
        attacker: {
          select: { id: true, name: true, tag: true }
        },
        defender: {
          select: { id: true, name: true, tag: true }
        }
      },
      orderBy: { startDate: 'desc' }
    });

    res.json({ wars });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch wars' });
  }
});

// Get war by ID
router.get('/:id', async (req, res) => {
  try {
    const war = await prisma.war.findUnique({
      where: { id: req.params.id },
      include: {
        attacker: true,
        defender: true,
        participants: {
          include: {
            player: {
              select: {
                uuid: true,
                username: true
              }
            }
          }
        }
      }
    });

    if (!war) {
      return res.status(404).json({ error: 'War not found' });
    }

    res.json(war);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch war' });
  }
});

// Get war history
router.get('/history/:nationId', async (req, res) => {
  try {
    const wars = await prisma.war.findMany({
      where: {
        OR: [
          { attackerId: req.params.nationId },
          { defenderId: req.params.nationId }
        ],
        status: 'ENDED'
      },
      include: {
        attacker: {
          select: { name: true, tag: true }
        },
        defender: {
          select: { name: true, tag: true }
        }
      },
      orderBy: { endDate: 'desc' },
      take: 20
    });

    res.json({ wars });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch war history' });
  }
});

export default router;
