import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { query } from '../db/connection.js';

export interface AuthRequest extends Request {
  user?: {
    id: number;
    uuid: string;
    username: string;
    email: string;
    role: string;
  };
}

export interface JWTPayload {
  userId: number;
  email: string;
  role: string;
}

/**
 * Middleware pour vérifier le JWT token
 */
export async function authenticateToken(
  req: AuthRequest,
  res: Response,
  next: NextFunction
) {
  try {
    const authHeader = req.headers['authorization'];
    const token = authHeader && authHeader.split(' ')[1]; // Format: "Bearer TOKEN"

    if (!token) {
      return res.status(401).json({ error: 'Token manquant' });
    }

    const secret = process.env.JWT_SECRET;
    if (!secret) {
      console.error('JWT_SECRET not configured!');
      return res.status(500).json({ error: 'Configuration serveur invalide' });
    }

    // Verify token
    const decoded = jwt.verify(token, secret) as JWTPayload;

    // Get user from database
    const result = await query(
      `SELECT id, uuid, username, email, role, is_active
       FROM launcher_users
       WHERE id = $1`,
      [decoded.userId]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Utilisateur introuvable' });
    }

    const user = result.rows[0];

    if (!user.is_active) {
      return res.status(403).json({ error: 'Compte désactivé' });
    }

    // Attach user to request
    req.user = {
      id: user.id,
      uuid: user.uuid,
      username: user.username,
      email: user.email,
      role: user.role,
    };

    next();
  } catch (error: any) {
    if (error.name === 'TokenExpiredError') {
      return res.status(401).json({ error: 'Token expiré' });
    }
    if (error.name === 'JsonWebTokenError') {
      return res.status(401).json({ error: 'Token invalide' });
    }
    console.error('Auth middleware error:', error);
    return res.status(500).json({ error: 'Erreur serveur' });
  }
}

/**
 * Middleware pour vérifier le rôle admin
 */
export function requireAdmin(
  req: AuthRequest,
  res: Response,
  next: NextFunction
) {
  if (!req.user) {
    return res.status(401).json({ error: 'Non authentifié' });
  }

  if (req.user.role !== 'admin') {
    return res.status(403).json({ error: 'Accès réservé aux administrateurs' });
  }

  next();
}

/**
 * Génère un JWT token
 */
export function generateToken(userId: number, email: string, role: string): string {
  const secret = process.env.JWT_SECRET;
  if (!secret) {
    throw new Error('JWT_SECRET not configured');
  }

  const payload: JWTPayload = {
    userId,
    email,
    role,
  };

  const expiresIn = process.env.JWT_EXPIRES_IN || '7d';

  return jwt.sign(payload, secret, { expiresIn });
}
