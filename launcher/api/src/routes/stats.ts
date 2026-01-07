import express, { Request, Response } from 'express';
import { query } from '../db/connection.js';

const router = express.Router();

/**
 * GET /api/stats/:uuid
 * Récupère les statistiques d'un joueur Minecraft
 */
router.get('/:uuid', async (req: Request, res: Response) => {
  try {
    const { uuid } = req.params;

    // Get player stats from hegemonia_players table
    const playerResult = await query(
      `SELECT
        p.uuid, p.username, p.first_join, p.last_seen, p.play_time,
        p.balance, p.reputation, p.kills, p.deaths,
        p.blocks_placed, p.blocks_destroyed,
        n.id as nation_id, n.name as nation_name, n.tag as nation_tag,
        n.color as nation_color,
        nm.role as nation_role, nm.joined_at as nation_joined_at,
        nm.contribution as nation_contribution
       FROM hegemonia_players p
       LEFT JOIN nation_members nm ON p.uuid = nm.player_id
       LEFT JOIN nations n ON nm.nation_id = n.id
       WHERE p.uuid = $1`,
      [uuid]
    );

    if (playerResult.rows.length === 0) {
      return res.status(404).json({ error: 'Joueur introuvable' });
    }

    const player = playerResult.rows[0];

    // Calculate K/D ratio
    const kdRatio = player.deaths > 0
      ? (player.kills / player.deaths).toFixed(2)
      : player.kills.toString();

    // Format playtime (milliseconds to hours)
    const playtimeHours = Math.floor(player.play_time / (1000 * 60 * 60));
    const playtimeMinutes = Math.floor((player.play_time % (1000 * 60 * 60)) / (1000 * 60));

    res.json({
      player: {
        uuid: player.uuid,
        username: player.username,
        firstJoin: player.first_join,
        lastSeen: player.last_seen,
        playtime: {
          total: player.play_time,
          hours: playtimeHours,
          minutes: playtimeMinutes,
          formatted: `${playtimeHours}h ${playtimeMinutes}m`,
        },
        balance: player.balance,
        reputation: player.reputation,
      },
      combat: {
        kills: player.kills,
        deaths: player.deaths,
        kdRatio: parseFloat(kdRatio),
      },
      building: {
        blocksPlaced: player.blocks_placed,
        blocksDestroyed: player.blocks_destroyed,
      },
      nation: player.nation_id ? {
        id: player.nation_id,
        name: player.nation_name,
        tag: player.nation_tag,
        color: player.nation_color,
        role: player.nation_role,
        joinedAt: player.nation_joined_at,
        contribution: player.nation_contribution,
      } : null,
    });

  } catch (error) {
    console.error('Get stats error:', error);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

export default router;
