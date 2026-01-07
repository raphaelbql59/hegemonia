import express, { Request, Response } from 'express';
import { status } from 'minecraft-server-util';

const router = express.Router();

const MINECRAFT_IP = process.env.MINECRAFT_SERVER_IP || '51.75.31.173';
const MINECRAFT_PORT = parseInt(process.env.MINECRAFT_SERVER_PORT || '25577');

/**
 * GET /api/server/status
 * Vérifie le statut du serveur Minecraft (Velocity proxy)
 */
router.get('/status', async (req: Request, res: Response) => {
  try {
    const result = await status(MINECRAFT_IP, MINECRAFT_PORT, {
      timeout: 5000,
      enableSRV: false,
    });

    res.json({
      online: true,
      players: {
        online: result.players.online,
        max: result.players.max,
        sample: result.players.sample || [],
      },
      version: result.version.name,
      motd: result.motd.clean,
      favicon: result.favicon || null,
      ping: result.roundTripLatency,
    });

  } catch (error: any) {
    console.error('Server status error:', error.message);
    res.json({
      online: false,
      players: {
        online: 0,
        max: 0,
        sample: [],
      },
      version: 'Unknown',
      motd: 'Serveur hors ligne',
      favicon: null,
      ping: 0,
      error: 'Le serveur est actuellement hors ligne ou inaccessible',
    });
  }
});

export default router;
