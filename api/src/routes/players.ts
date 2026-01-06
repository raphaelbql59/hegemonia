import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get player by UUID
router.get('/:uuid', async (req, res) => {
  try {
    const player = await prisma.player.findUnique({
      where: { uuid: req.params.uuid },
      include: {
        nation: {
          select: {
            id: true,
            name: true,
            tag: true
          }
        }
      }
    });

    if (!player) {
      return res.status(404).json({ error: 'Player not found' });
    }

    res.json(player);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch player' });
  }
});

// Get player stats
router.get('/:uuid/stats', async (req, res) => {
  try {
    const player = await prisma.player.findUnique({
      where: { uuid: req.params.uuid },
      include: {
        warParticipations: true,
        transactions: true
      }
    });

    if (!player) {
      return res.status(404).json({ error: 'Player not found' });
    }

    const totalWarKills = player.warParticipations.reduce((sum, w) => sum + w.kills, 0);
    const totalWarDeaths = player.warParticipations.reduce((sum, w) => sum + w.deaths, 0);
    const kdr = totalWarDeaths > 0 ? (totalWarKills / totalWarDeaths).toFixed(2) : totalWarKills.toString();

    res.json({
      username: player.username,
      level: player.level,
      profession: player.profession,
      balance: player.balance,
      playtime: player.playtime,
      kills: player.kills,
      deaths: player.deaths,
      kdr,
      warsParticipated: player.warParticipations.length
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch stats' });
  }
});

// Get leaderboard
router.get('/leaderboard/:type', async (req, res) => {
  try {
    const { type } = req.params;
    const limit = parseInt(req.query.limit as string) || 10;

    let orderBy: any = { balance: 'desc' };

    switch (type) {
      case 'wealth':
        orderBy = { balance: 'desc' };
        break;
      case 'playtime':
        orderBy = { playtime: 'desc' };
        break;
      case 'kills':
        orderBy = { kills: 'desc' };
        break;
      case 'level':
        orderBy = { level: 'desc' };
        break;
      default:
        return res.status(400).json({ error: 'Invalid leaderboard type' });
    }

    const players = await prisma.player.findMany({
      take: limit,
      orderBy,
      select: {
        uuid: true,
        username: true,
        balance: true,
        playtime: true,
        kills: true,
        deaths: true,
        level: true,
        nation: {
          select: {
            name: true,
            tag: true
          }
        }
      }
    });

    res.json({ leaderboard: players });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch leaderboard' });
  }
});

export default router;
