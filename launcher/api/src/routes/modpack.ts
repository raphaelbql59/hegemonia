import express from 'express';
import path from 'path';
import fs from 'fs';

const router = express.Router();

// Mod storage directory - use local modpack folder
const MODS_DIR = process.env.MODS_DIR || path.join(__dirname, '../../modpack/mods');
const RESOURCEPACKS_DIR = process.env.RESOURCEPACKS_DIR || path.join(__dirname, '../../modpack/resourcepacks');

// Modpack manifest
const MODPACK_MANIFEST = {
  version: '1.1.0',
  minecraft_version: '1.20.4',
  fabric_version: '0.15.6',
  mods: [
    {
      id: 'fabric-api',
      name: 'Fabric API',
      version: '0.96.4+1.20.4',
      file_name: 'fabric-api-0.96.4+1.20.4.jar',
      url: 'https://cdn.modrinth.com/data/P7dR8mSH/versions/BPX6fK06/fabric-api-0.97.3%2B1.20.4.jar',
      sha256: '',
      size: 2187523,
      required: true,
    },
    {
      id: 'hegemonia-client',
      name: 'Hegemonia Client',
      version: '1.0.0',
      file_name: 'hegemonia-client-1.0.0.jar',
      url: null, // Served from local API
      sha256: '',
      size: 76513,
      required: true,
    },
    {
      id: 'cloth-config',
      name: 'Cloth Config API',
      version: '13.0.121',
      file_name: 'cloth-config-13.0.121-fabric.jar',
      url: 'https://cdn.modrinth.com/data/9s6osm5g/versions/PbB23vRL/cloth-config-13.0.121-fabric.jar',
      sha256: '',
      size: 1245678,
      required: true,
    },
    {
      id: 'sodium',
      name: 'Sodium',
      version: '0.5.8+mc1.20.4',
      file_name: 'sodium-fabric-0.5.8+mc1.20.4.jar',
      url: 'https://cdn.modrinth.com/data/AANobbMI/versions/4GyXKCLd/sodium-fabric-0.5.8%2Bmc1.20.4.jar',
      sha256: '',
      size: 1134567,
      required: true,
    },
    {
      id: 'lithium',
      name: 'Lithium',
      version: '0.12.1+mc1.20.4',
      file_name: 'lithium-fabric-mc1.20.4-0.12.1.jar',
      url: 'https://cdn.modrinth.com/data/gvQqBUqZ/versions/nMhjKWVE/lithium-fabric-mc1.20.4-0.12.1.jar',
      sha256: '',
      size: 598432,
      required: true,
    },
    {
      id: 'worldmap',
      name: "Xaero's World Map",
      version: '1.37.8',
      file_name: 'XaerosWorldMap_1.37.8_Fabric_1.20.4.jar',
      url: 'https://cdn.modrinth.com/data/NcUtCpym/versions/hy3cqOH4/XaerosWorldMap_1.37.8_Fabric_1.20.4.jar',
      sha256: '',
      size: 1023456,
      required: true,
    },
    {
      id: 'minimap',
      name: "Xaero's Minimap",
      version: '24.0.3',
      file_name: 'Xaeros_Minimap_24.0.3_Fabric_1.20.4.jar',
      url: 'https://cdn.modrinth.com/data/1bokaNcj/versions/xnCFTkHC/Xaeros_Minimap_24.0.3_Fabric_1.20.4.jar',
      sha256: '',
      size: 876543,
      required: true,
    },
    {
      id: 'iris',
      name: 'Iris Shaders',
      version: '1.6.17+mc1.20.4',
      file_name: 'iris-mc1.20.4-1.6.17.jar',
      url: 'https://cdn.modrinth.com/data/YL57xq9U/versions/LZKZqvZN/iris-mc1.20.4-1.6.17.jar',
      sha256: '',
      size: 2567890,
      required: false,
    },
  ],
  resource_pack: {
    name: 'Hegemonia Resource Pack',
    version: '1.0.0',
    file_name: 'hegemonia.zip',
    url: null, // Will be served from API
    sha256: '',
    size: 5000000,
  },
};

/**
 * GET /api/modpack/manifest
 * Returns the modpack manifest with all mod information
 */
router.get('/manifest', (req, res) => {
  res.json(MODPACK_MANIFEST);
});

/**
 * GET /api/modpack/mods
 * Returns list of available mods
 */
router.get('/mods', (req, res) => {
  res.json(MODPACK_MANIFEST.mods);
});

/**
 * GET /api/modpack/mods/:filename
 * Serve a mod file
 */
router.get('/mods/:filename', (req, res) => {
  const { filename } = req.params;

  // Security: prevent directory traversal
  if (filename.includes('..') || filename.includes('/')) {
    return res.status(400).json({ error: 'Invalid filename' });
  }

  const filePath = path.join(MODS_DIR, filename);

  if (fs.existsSync(filePath)) {
    res.sendFile(filePath);
  } else {
    // If local file doesn't exist, redirect to Modrinth CDN
    const mod = MODPACK_MANIFEST.mods.find(m => m.file_name === filename);
    if (mod?.url) {
      res.redirect(mod.url);
    } else {
      res.status(404).json({ error: 'Mod not found' });
    }
  }
});

/**
 * GET /api/modpack/resourcepacks/:filename
 * Serve a resource pack file
 */
router.get('/resourcepacks/:filename', (req, res) => {
  const { filename } = req.params;

  // Security: prevent directory traversal
  if (filename.includes('..') || filename.includes('/')) {
    return res.status(400).json({ error: 'Invalid filename' });
  }

  const filePath = path.join(RESOURCEPACKS_DIR, filename);

  if (fs.existsSync(filePath)) {
    res.sendFile(filePath);
  } else {
    res.status(404).json({ error: 'Resource pack not found' });
  }
});

/**
 * GET /api/modpack/check
 * Check if modpack files are available
 */
router.get('/check', async (req, res) => {
  const modsAvailable: string[] = [];
  const modsMissing: string[] = [];

  for (const mod of MODPACK_MANIFEST.mods) {
    const filePath = path.join(MODS_DIR, mod.file_name);
    if (fs.existsSync(filePath)) {
      modsAvailable.push(mod.id);
    } else if (mod.url) {
      // File not cached locally but available from CDN
      modsAvailable.push(mod.id);
    } else {
      modsMissing.push(mod.id);
    }
  }

  const resourcePackPath = path.join(RESOURCEPACKS_DIR, MODPACK_MANIFEST.resource_pack.file_name);
  const resourcePackAvailable = fs.existsSync(resourcePackPath);

  res.json({
    version: MODPACK_MANIFEST.version,
    mods_available: modsAvailable,
    mods_missing: modsMissing,
    resource_pack_available: resourcePackAvailable,
    ready: modsMissing.length === 0,
  });
});

export default router;
