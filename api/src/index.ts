import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import compression from 'compression';
import { createServer } from 'http';
import { Server } from 'socket.io';
import dotenv from 'dotenv';
import rateLimit from 'express-rate-limit';

import { logger } from './utils/logger';
import { errorHandler } from './middlewares/errorHandler';
import { prisma } from './utils/prisma';
import { redis } from './utils/redis';

// Routes
import launcherRouter from './routes/launcher';
import nationsRouter from './routes/nations';
import playersRouter from './routes/players';
import warsRouter from './routes/wars';
import economyRouter from './routes/economy';
import technologiesRouter from './routes/technologies';
import mapsRouter from './routes/maps';
import newsRouter from './routes/news';

dotenv.config();

const app = express();
const httpServer = createServer(app);
const io = new Server(httpServer, {
  cors: {
    origin: process.env.CORS_ORIGIN || '*',
    methods: ['GET', 'POST']
  }
});

const PORT = process.env.PORT || 3000;

// ============================================
// MIDDLEWARE
// ============================================

app.use(helmet());
app.use(cors({
  origin: process.env.CORS_ORIGIN || '*'
}));
app.use(compression());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// Rate limiting
const limiter = rateLimit({
  windowMs: parseInt(process.env.RATE_LIMIT_WINDOW_MS || '900000'), // 15 minutes
  max: parseInt(process.env.RATE_LIMIT_MAX_REQUESTS || '100')
});
app.use('/api/', limiter);

// Request logging
app.use((req, res, next) => {
  logger.info(`${req.method} ${req.path}`, {
    ip: req.ip,
    userAgent: req.get('user-agent')
  });
  next();
});

// ============================================
// ROUTES
// ============================================

app.get('/health', (req, res) => {
  res.json({
    status: 'ok',
    timestamp: new Date().toISOString(),
    uptime: process.uptime()
  });
});

app.use('/api/launcher', launcherRouter);
app.use('/api/nations', nationsRouter);
app.use('/api/players', playersRouter);
app.use('/api/wars', warsRouter);
app.use('/api/economy', economyRouter);
app.use('/api/technologies', technologiesRouter);
app.use('/api/map', mapsRouter);
app.use('/api/news', newsRouter);

// ============================================
// WEBSOCKET
// ============================================

io.on('connection', (socket) => {
  logger.info(`Client connected: ${socket.id}`);

  socket.on('subscribe:nation', (nationId: string) => {
    socket.join(`nation:${nationId}`);
    logger.info(`Socket ${socket.id} subscribed to nation:${nationId}`);
  });

  socket.on('subscribe:war', (warId: string) => {
    socket.join(`war:${warId}`);
    logger.info(`Socket ${socket.id} subscribed to war:${warId}`);
  });

  socket.on('disconnect', () => {
    logger.info(`Client disconnected: ${socket.id}`);
  });
});

// Make io available globally for event emissions
export { io };

// ============================================
// ERROR HANDLING
// ============================================

app.use(errorHandler);

// ============================================
// SERVER STARTUP
// ============================================

async function bootstrap() {
  try {
    // Test database connection
    await prisma.$connect();
    logger.info('✓ Database connected');

    // Test Redis connection
    await redis.ping();
    logger.info('✓ Redis connected');

    // Start server
    httpServer.listen(PORT, () => {
      logger.info(`🚀 Hegemonia API running on port ${PORT}`);
      logger.info(`Environment: ${process.env.NODE_ENV}`);
    });
  } catch (error) {
    logger.error('Failed to start server:', error);
    process.exit(1);
  }
}

// Graceful shutdown
process.on('SIGTERM', async () => {
  logger.info('SIGTERM received, shutting down gracefully...');
  httpServer.close(() => {
    logger.info('HTTP server closed');
  });
  await prisma.$disconnect();
  await redis.quit();
  process.exit(0);
});

bootstrap();
