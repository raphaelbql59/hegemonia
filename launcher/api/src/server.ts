import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import dotenv from 'dotenv';
import authRoutes from './routes/auth.js';
import newsRoutes from './routes/news.js';
import statsRoutes from './routes/stats.js';
import serverRoutes from './routes/server.js';
import modpackRoutes from './routes/modpack.js';

// Load environment variables
dotenv.config();

const app = express();
const PORT = process.env.PORT || 3001;

// ============================================================================
// Middleware
// ============================================================================

// Security headers
app.use(helmet());

// CORS for Tauri and external connections
app.use(cors({
  origin: true, // Allow all origins for launcher
  credentials: true,
}));

// Body parsing
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Rate limiting
const limiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 100, // 100 requests per windowMs
  message: 'Trop de requêtes depuis cette adresse IP, veuillez réessayer plus tard.',
});
app.use('/api/', limiter);

// Auth rate limiting (plus strict)
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000,
  max: 5, // 5 login attempts per 15 minutes
  message: 'Trop de tentatives de connexion, veuillez réessayer dans 15 minutes.',
});
app.use('/api/auth/login', authLimiter);

// ============================================================================
// Routes
// ============================================================================

app.use('/api/auth', authRoutes);
app.use('/api/news', newsRoutes);
app.use('/api/stats', statsRoutes);
app.use('/api/server', serverRoutes);
app.use('/api/modpack', modpackRoutes);

// Health check
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({ error: 'Route not found' });
});

// Error handler
app.use((err: any, req: express.Request, res: express.Response, next: express.NextFunction) => {
  console.error('Error:', err);
  res.status(500).json({
    error: process.env.NODE_ENV === 'development' ? err.message : 'Internal server error'
  });
});

// ============================================================================
// Start Server
// ============================================================================

app.listen(Number(PORT), '0.0.0.0', () => {
  console.log('');
  console.log('═══════════════════════════════════════════════════');
  console.log(`🚀 Hegemonia Launcher API`);
  console.log(`📡 Server running on http://localhost:${PORT}`);
  console.log(`🌍 Environment: ${process.env.NODE_ENV}`);
  console.log('═══════════════════════════════════════════════════');
  console.log('');
  console.log('Available routes:');
  console.log('  POST   /api/auth/login');
  console.log('  POST   /api/auth/logout');
  console.log('  GET    /api/auth/me');
  console.log('  GET    /api/news');
  console.log('  GET    /api/stats/:uuid');
  console.log('  GET    /api/server/status');
  console.log('  GET    /health');
  console.log('');
});

export default app;
