import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get all nations
router.get('/', async (req, res) => {
  try {
    const nations = await prisma.nation.findMany({
      where: { isActive: true },
      include: {
        _count: {
          select: {
            members: true,
            territories: true
          }
        }
      },
      orderBy: { treasury: 'desc' }
    });

    res.json({
      nations: nations.map(nation => ({
        id: nation.id,
        name: nation.name,
        tag: nation.tag,
        governmentType: nation.governmentType,
        treasury: nation.treasury,
        memberCount: nation._count.members,
        territoryCount: nation._count.territories,
        foundedAt: nation.foundedAt
      }))
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch nations' });
  }
});

// Get nation by ID
router.get('/:id', async (req, res) => {
  try {
    const nation = await prisma.nation.findUnique({
      where: { id: req.params.id },
      include: {
        members: {
          select: {
            uuid: true,
            username: true,
            role: true,
            balance: true
          }
        },
        territories: true,
        researchesDone: {
          include: {
            technology: true
          }
        }
      }
    });

    if (!nation) {
      return res.status(404).json({ error: 'Nation not found' });
    }

    res.json(nation);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch nation' });
  }
});

// Get nation statistics
router.get('/:id/stats', async (req, res) => {
  try {
    const nation = await prisma.nation.findUnique({
      where: { id: req.params.id },
      include: {
        members: true,
        territories: true,
        warsAsAttacker: {
          where: { status: 'ACTIVE' }
        },
        warsAsDefender: {
          where: { status: 'ACTIVE' }
        }
      }
    });

    if (!nation) {
      return res.status(404).json({ error: 'Nation not found' });
    }

    const totalPlaytime = nation.members.reduce((sum, m) => sum + m.playtime, 0);
    const averageBalance = nation.members.reduce((sum, m) => sum + m.balance, 0) / nation.members.length;

    res.json({
      nationId: nation.id,
      memberCount: nation.members.length,
      territoryCount: nation.territories.length,
      treasury: nation.treasury,
      activeWars: nation.warsAsAttacker.length + nation.warsAsDefender.length,
      totalPlaytime,
      averageBalance: Math.round(averageBalance)
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch stats' });
  }
});

export default router;
