import { Router } from 'express';
import { prisma } from '../utils/prisma';

const router = Router();

// Get all territories with their nations
router.get('/territories', async (req, res) => {
  try {
    const territories = await prisma.territory.findMany({
      include: {
        nation: {
          select: {
            id: true,
            name: true,
            tag: true,
            governmentType: true
          }
        }
      }
    });

    // Convert to GeoJSON format
    const geoJSON = {
      type: 'FeatureCollection',
      features: territories.map(territory => ({
        type: 'Feature',
        properties: {
          regionName: territory.regionName,
          continent: territory.continent,
          nation: territory.nation,
          resourceType: territory.resourceType,
          controlledSince: territory.controlledSince,
          isContested: territory.isContested
        },
        geometry: JSON.parse(territory.coordinates)
      }))
    };

    res.json(geoJSON);
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch territories' });
  }
});

// Get territories by nation
router.get('/territories/:nationId', async (req, res) => {
  try {
    const territories = await prisma.territory.findMany({
      where: { nationId: req.params.nationId }
    });

    res.json({ territories });
  } catch (error) {
    res.status(500).json({ error: 'Failed to fetch territories' });
  }
});

export default router;
