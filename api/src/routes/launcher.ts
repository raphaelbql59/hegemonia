import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get latest launcher version
router.get('/version', async (req, res) => {
  try {
    const latestVersion = await prisma.launcherVersion.findFirst({
      where: { isLatest: true },
      orderBy: { releasedAt: 'desc' }
    });

    if (!latestVersion) {
      return res.json({
        version: '1.0.0',
        downloadUrl: '',
        releaseNotes: 'Initial version',
        isMandatory: false
      });
    }

    res.json(latestVersion);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch version' });
  }
});

// Get list of mods with versions and hashes
router.get('/mods', async (req, res) => {
  try {
    const mods = await prisma.modFile.findMany({
      orderBy: { category: 'asc' }
    });

    res.json({
      mods: mods.map(mod => ({
        fileName: mod.fileName,
        version: mod.version,
        hash: mod.fileHash,
        size: mod.fileSize,
        downloadUrl: mod.downloadUrl,
        isRequired: mod.isRequired,
        category: mod.category
      }))
    });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch mods' });
  }
});

// Download specific mod
router.get('/mods/:fileName/download', async (req, res) => {
  try {
    const mod = await prisma.modFile.findUnique({
      where: { fileName: req.params.fileName }
    });

    if (!mod) {
      return res.status(404).json({ error: 'Mod not found' });
    }

    res.redirect(mod.downloadUrl);
  } catch (error) {
    res.status(500).json({ error: 'Failed to download mod' });
  }
});

export default router;
