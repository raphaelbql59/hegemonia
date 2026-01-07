import express, { Request, Response } from 'express';
import bcrypt from 'bcrypt';
import { z } from 'zod';
import { query } from '../db/connection.js';
import { authenticateToken, generateToken, AuthRequest } from '../middleware/auth.js';

const router = express.Router();

// ============================================================================
// Validation Schemas
// ============================================================================

const LoginSchema = z.object({
  email: z.string().email('Email invalide'),
  password: z.string().min(6, 'Le mot de passe doit contenir au moins 6 caractères'),
});

const RegisterSchema = z.object({
  username: z.string().min(3).max(16),
  email: z.string().email(),
  password: z.string().min(8),
});

// ============================================================================
// Routes
// ============================================================================

/**
 * POST /api/auth/login
 * Connexion avec email + password
 */
router.post('/login', async (req: Request, res: Response) => {
  try {
    // Validate input
    const { email, password } = LoginSchema.parse(req.body);

    // Find user
    const result = await query(
      `SELECT id, uuid, username, email, password_hash, role, is_active, minecraft_uuid
       FROM launcher_users
       WHERE email = $1`,
      [email.toLowerCase()]
    );

    if (result.rows.length === 0) {
      return res.status(401).json({ error: 'Email ou mot de passe incorrect' });
    }

    const user = result.rows[0];

    // Check if account is active
    if (!user.is_active) {
      return res.status(403).json({ error: 'Votre compte a été désactivé. Contactez un administrateur.' });
    }

    // Verify password
    const passwordMatch = await bcrypt.compare(password, user.password_hash);
    if (!passwordMatch) {
      return res.status(401).json({ error: 'Email ou mot de passe incorrect' });
    }

    // Update last login
    await query(
      'UPDATE launcher_users SET last_login = NOW() WHERE id = $1',
      [user.id]
    );

    // Generate JWT token
    const token = generateToken(user.id, user.email, user.role);

    // Return user data + token
    res.json({
      token,
      user: {
        id: user.id,
        uuid: user.uuid,
        username: user.username,
        email: user.email,
        role: user.role,
        minecraftUuid: user.minecraft_uuid,
      },
    });

  } catch (error: any) {
    if (error instanceof z.ZodError) {
      return res.status(400).json({ error: error.errors[0].message });
    }
    console.error('Login error:', error);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

/**
 * POST /api/auth/register
 * DÉSACTIVÉ - Inscription uniquement via le site web
 */
router.post('/register', (req: Request, res: Response) => {
  res.status(403).json({
    error: 'Les inscriptions se font uniquement sur le site web',
    websiteUrl: 'https://hegemonia.fr/register'
  });
});

/**
 * GET /api/auth/me
 * Récupère les infos de l'utilisateur connecté
 */
router.get('/me', authenticateToken, async (req: AuthRequest, res: Response) => {
  try {
    if (!req.user) {
      return res.status(401).json({ error: 'Non authentifié' });
    }

    // Get full user data
    const result = await query(
      `SELECT id, uuid, username, email, display_name, avatar_url, role,
              minecraft_uuid, created_at, last_login, settings
       FROM launcher_users
       WHERE id = $1`,
      [req.user.id]
    );

    if (result.rows.length === 0) {
      return res.status(404).json({ error: 'Utilisateur introuvable' });
    }

    const user = result.rows[0];

    res.json({
      id: user.id,
      uuid: user.uuid,
      username: user.username,
      email: user.email,
      displayName: user.display_name,
      avatarUrl: user.avatar_url,
      role: user.role,
      minecraftUuid: user.minecraft_uuid,
      createdAt: user.created_at,
      lastLogin: user.last_login,
      settings: user.settings,
    });

  } catch (error) {
    console.error('Get user error:', error);
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

/**
 * POST /api/auth/logout
 * Déconnexion (côté client, supprime juste le token)
 */
router.post('/logout', authenticateToken, (req: Request, res: Response) => {
  // En JWT stateless, le logout est géré côté client
  // On pourrait implémenter une blacklist de tokens si nécessaire
  res.json({ message: 'Déconnecté avec succès' });
});

export default router;
