import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get market prices (mock for now, will be dynamic)
router.get('/market', async (req, res) => {
  try {
    // Mock market prices
    const marketPrices = {
      resources: {
        DIAMOND: { buyPrice: 50, sellPrice: 40 },
        GOLD: { buyPrice: 30, sellPrice: 25 },
        IRON: { buyPrice: 5, sellPrice: 3 },
        COAL: { buyPrice: 2, sellPrice: 1 },
        FOOD: { buyPrice: 10, sellPrice: 8 }
      },
      lastUpdated: new Date().toISOString()
    };

    res.json(marketPrices);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch market prices' });
  }
});

// Get recent transactions
router.get('/transactions', async (req, res) => {
  try {
    const limit = parseInt(req.query.limit as string) || 50;

    const transactions = await prisma.transaction.findMany({
      take: limit,
      orderBy: { timestamp: 'desc' },
      include: {
        fromPlayer: {
          select: {
            username: true
          }
        }
      }
    });

    res.json({ transactions });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch transactions' });
  }
});

// Get player transactions
router.get('/transactions/:uuid', async (req, res) => {
  try {
    const transactions = await prisma.transaction.findMany({
      where: {
        fromPlayerId: req.params.uuid
      },
      orderBy: { timestamp: 'desc' },
      take: 100
    });

    res.json({ transactions });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch player transactions' });
  }
});

export default router;
