# Hegemonia - Deployment Guide

## 📋 Prerequisites

- OVH VPS with 64GB RAM
- Ubuntu 20.04+ or Debian 11+
- Root access (sudo)
- Domain name (optional but recommended)

## 🚀 Quick Deployment

### 1. Connect to your VPS

```bash
ssh root@your-vps-ip
```

### 2. Clone the repository

```bash
cd /opt
git clone <your-repo-url> hegemonia
cd hegemonia
```

### 3. Run deployment script

```bash
chmod +x deploy.sh
sudo ./deploy.sh
```

The script will:
- Install Docker & Docker Compose
- Configure firewall (UFW)
- Setup automatic backups
- Start all services

### 4. Configure passwords

**⚠️ IMPORTANT:** Change default passwords in `docker-compose.yml` before production!

```yaml
# PostgreSQL
POSTGRES_PASSWORD: your_secure_password_here

# Redis
command: redis-server --requirepass your_redis_password_here

# RCON
RCON_PASSWORD: your_rcon_password_here

# JWT
JWT_SECRET: your_very_long_random_secret_here
```

### 5. Start services

```bash
docker-compose up -d
```

## 📦 Services Overview

After deployment, you'll have:

| Service | Port | URL |
|---------|------|-----|
| Minecraft Server | 25565 | `your-ip:25565` |
| API | 3000 | `http://your-ip:3000` |
| PostgreSQL | 5432 | Internal |
| Redis | 6379 | Internal |
| Nginx | 80/443 | `http://your-ip` |

## 🔧 Post-Deployment Configuration

### 1. Setup SSL (Recommended)

```bash
# Install certbot
apt install certbot python3-certbot-nginx

# Get certificate
certbot --nginx -d your-domain.com

# Auto-renewal
certbot renew --dry-run
```

Then uncomment HTTPS section in `nginx/nginx.conf`.

### 2. Configure Minecraft Server

```bash
# Edit server properties
nano server/server.properties

# Restart server
docker-compose restart minecraft
```

### 3. Add players to whitelist

```bash
docker exec hegemonia-minecraft rcon-cli whitelist add PlayerName
```

### 4. Database migrations

```bash
cd api
docker-compose exec api npm run prisma:push
```

## 📊 Monitoring

### View logs

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f minecraft
docker-compose logs -f api
```

### Check status

```bash
docker-compose ps
```

### Resource usage

```bash
docker stats
```

## 🔄 Maintenance

### Backup

Backups run automatically daily at 4 AM.

Manual backup:
```bash
/opt/hegemonia/backup.sh
```

Backups location: `/opt/hegemonia/backups/`

### Update server

```bash
cd /opt/hegemonia
git pull
docker-compose build
docker-compose up -d
```

### Restart services

```bash
# All services
docker-compose restart

# Specific service
docker-compose restart minecraft
```

### Stop services

```bash
docker-compose down
```

## 🐛 Troubleshooting

### Minecraft won't start

```bash
# Check logs
docker-compose logs minecraft

# Check if port is in use
netstat -tulpn | grep 25565

# Restart
docker-compose restart minecraft
```

### API errors

```bash
# Check logs
docker-compose logs api

# Check database connection
docker-compose exec api npm run prisma:studio

# Restart
docker-compose restart api
```

### Out of memory

```bash
# Check memory usage
free -h

# Reduce Minecraft RAM in docker-compose.yml
MEMORY: "12G"  # Instead of 16G
```

## 📈 Performance Optimization

### 1. Pre-generate world

```bash
docker exec hegemonia-minecraft rcon-cli chunky world world
docker exec hegemonia-minecraft rcon-cli chunky radius 10000
docker exec hegemonia-minecraft rcon-cli chunky start
```

### 2. Optimize PostgreSQL

Edit `docker-compose.yml`:

```yaml
postgres:
  command:
    - "postgres"
    - "-c"
    - "shared_buffers=2GB"
    - "-c"
    - "effective_cache_size=6GB"
    - "-c"
    - "max_connections=200"
```

### 3. Redis persistence

For better performance, disable persistence in production:

```yaml
redis:
  command: redis-server --requirepass your_pass --save ""
```

## 🔐 Security Checklist

- [ ] Change all default passwords
- [ ] Enable firewall (UFW)
- [ ] Setup SSL certificates
- [ ] Enable fail2ban
- [ ] Disable root SSH login
- [ ] Setup SSH key authentication
- [ ] Regular backups
- [ ] Update system regularly

## 📞 Support

If you encounter issues:

1. Check logs: `docker-compose logs -f`
2. Verify all services are running: `docker-compose ps`
3. Check system resources: `htop`
4. Review configuration files

## 🎮 Next Steps

1. Configure your launcher to point to your server IP
2. Add mods to `/opt/hegemonia/mods/`
3. Configure nations, technologies, etc. via API
4. Test with friends!
5. Go public! 🚀
