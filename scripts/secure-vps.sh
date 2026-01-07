#!/bin/bash

# ===========================
# HEGEMONIA - VPS Security Hardening Script
# Pour Debian 11
# ===========================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [[ $EUID -ne 0 ]]; then
   log_error "This script must be run as root"
   exit 1
fi

log_info "Starting VPS security hardening..."

# ===========================
# 1. SYSTÈME À JOUR
# ===========================

log_info "Step 1: Updating system packages..."
apt update
apt upgrade -y
apt autoremove -y
apt autoclean

# ===========================
# 2. CRÉATION UTILISATEUR DÉDIÉ
# ===========================

log_info "Step 2: Creating dedicated minecraft user..."
if id "minecraft" &>/dev/null; then
    log_warn "User 'minecraft' already exists, skipping..."
else
    useradd -m -s /bin/bash minecraft
    log_info "User 'minecraft' created successfully"
fi

# ===========================
# 3. CONFIGURATION SSH
# ===========================

log_info "Step 3: Configuring SSH security..."

# Backup SSH config
cp /etc/ssh/sshd_config /etc/ssh/sshd_config.backup.$(date +%Y%m%d_%H%M%S)

# Get custom SSH port (default 2222)
read -p "Enter custom SSH port (default 2222): " SSH_PORT
SSH_PORT=${SSH_PORT:-2222}

# Configure SSH
sed -i "s/^#*Port.*/Port $SSH_PORT/" /etc/ssh/sshd_config
sed -i 's/^#*PermitRootLogin.*/PermitRootLogin no/' /etc/ssh/sshd_config
sed -i 's/^#*PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
sed -i 's/^#*PubkeyAuthentication.*/PubkeyAuthentication yes/' /etc/ssh/sshd_config
sed -i 's/^#*PermitEmptyPasswords.*/PermitEmptyPasswords no/' /etc/ssh/sshd_config
sed -i 's/^#*X11Forwarding.*/X11Forwarding no/' /etc/ssh/sshd_config
sed -i 's/^#*MaxAuthTries.*/MaxAuthTries 3/' /etc/ssh/sshd_config

# Add SSH key for minecraft user (if provided)
read -p "Add SSH key for minecraft user? (y/N): " ADD_KEY
if [[ $ADD_KEY =~ ^[Yy]$ ]]; then
    mkdir -p /home/minecraft/.ssh
    chmod 700 /home/minecraft/.ssh
    read -p "Paste your public SSH key: " SSH_KEY
    echo "$SSH_KEY" > /home/minecraft/.ssh/authorized_keys
    chmod 600 /home/minecraft/.ssh/authorized_keys
    chown -R minecraft:minecraft /home/minecraft/.ssh
    log_info "SSH key added for minecraft user"
fi

# Restart SSH
systemctl restart sshd
log_info "SSH configured on port $SSH_PORT with key-only authentication"

# ===========================
# 4. FAIL2BAN
# ===========================

log_info "Step 4: Installing and configuring Fail2Ban..."
apt install -y fail2ban

# Create Fail2Ban jail configuration
cat > /etc/fail2ban/jail.local <<EOF
[DEFAULT]
bantime = 3600
findtime = 600
maxretry = 5
destemail = admin@hegemonia.com
sendername = Fail2Ban
action = %(action_mwl)s

[sshd]
enabled = true
port = $SSH_PORT
logpath = %(sshd_log)s
backend = %(sshd_backend)s

[minecraft-bruteforce]
enabled = true
port = 25565
filter = minecraft
logpath = /home/minecraft/hegemonia-project/server/*/logs/latest.log
maxretry = 5
bantime = 7200
EOF

# Create Minecraft filter
mkdir -p /etc/fail2ban/filter.d
cat > /etc/fail2ban/filter.d/minecraft.conf <<EOF
[Definition]
failregex = ^.*\[Server thread/WARN\]: Failed login from <HOST>.*$
            ^.*\[User Authenticator #\d+/INFO\]: UUID of player .* is .*<HOST>.*$
ignoreregex =
EOF

systemctl enable fail2ban
systemctl restart fail2ban
log_info "Fail2Ban installed and configured"

# ===========================
# 5. UFW FIREWALL
# ===========================

log_info "Step 5: Configuring UFW firewall..."
apt install -y ufw

# Default policies
ufw default deny incoming
ufw default allow outgoing

# Allow SSH (custom port)
ufw allow $SSH_PORT/tcp comment 'SSH'

# Allow Minecraft
ufw allow 25565/tcp comment 'Minecraft Velocity'

# Allow HTTP/HTTPS
ufw allow 80/tcp comment 'HTTP'
ufw allow 443/tcp comment 'HTTPS'

# Enable UFW
echo "y" | ufw enable
ufw status verbose

log_info "UFW firewall configured and enabled"

# ===========================
# 6. SWAP OPTIMIZATION
# ===========================

log_info "Step 6: Configuring swap (64GB RAM system)..."

# Check if swap exists
if ! swapon --show | grep -q "/swapfile"; then
    # Create 8GB swap (small, as backup for 64GB RAM)
    fallocate -l 8G /swapfile
    chmod 600 /swapfile
    mkswap /swapfile
    swapon /swapfile
    echo '/swapfile none swap sw 0 0' >> /etc/fstab
    log_info "8GB swap created"
else
    log_warn "Swap already exists, skipping..."
fi

# Optimize swappiness for high RAM system
sysctl vm.swappiness=10
echo 'vm.swappiness=10' >> /etc/sysctl.conf

# ===========================
# 7. KERNEL OPTIMIZATIONS
# ===========================

log_info "Step 7: Applying kernel optimizations for game server..."

cat >> /etc/sysctl.conf <<EOF

# ===========================
# Hegemonia Optimizations
# ===========================

# Network optimizations
net.core.netdev_max_backlog = 5000
net.core.rmem_max = 134217728
net.core.wmem_max = 134217728
net.ipv4.tcp_rmem = 4096 87380 134217728
net.ipv4.tcp_wmem = 4096 65536 134217728
net.ipv4.tcp_congestion_control = bbr
net.core.default_qdisc = fq

# Connection handling
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_fin_timeout = 15
net.ipv4.ip_local_port_range = 1024 65535
net.ipv4.tcp_max_syn_backlog = 8192

# Security
net.ipv4.conf.default.rp_filter = 1
net.ipv4.conf.all.rp_filter = 1
net.ipv4.tcp_syncookies = 1
net.ipv4.icmp_echo_ignore_broadcasts = 1

# File system
fs.file-max = 2097152
fs.inotify.max_user_watches = 524288

# Memory
vm.overcommit_memory = 1
EOF

# Apply sysctl changes
sysctl -p

log_info "Kernel optimizations applied"

# ===========================
# 8. AUTOMATIC SECURITY UPDATES
# ===========================

log_info "Step 8: Configuring automatic security updates..."
apt install -y unattended-upgrades apt-listchanges

cat > /etc/apt/apt.conf.d/50unattended-upgrades <<EOF
Unattended-Upgrade::Allowed-Origins {
    "\${distro_id}:\${distro_codename}-security";
};

Unattended-Upgrade::AutoFixInterruptedDpkg "true";
Unattended-Upgrade::MinimalSteps "true";
Unattended-Upgrade::Mail "admin@hegemonia.com";
Unattended-Upgrade::Automatic-Reboot "false";
EOF

systemctl enable unattended-upgrades
systemctl start unattended-upgrades

log_info "Automatic security updates configured"

# ===========================
# 9. FAIL2BAN STATUS CHECK
# ===========================

log_info "Step 9: Installing monitoring tools..."
apt install -y htop iotop nethogs ncdu

# ===========================
# SUMMARY
# ===========================

clear
echo "=================================="
echo "  VPS SECURITY HARDENING COMPLETE"
echo "=================================="
echo ""
echo "✅ System updated"
echo "✅ User 'minecraft' created"
echo "✅ SSH secured (port $SSH_PORT, key-only)"
echo "✅ Fail2Ban installed and configured"
echo "✅ UFW firewall enabled"
echo "✅ Swap optimized (8GB)"
echo "✅ Kernel optimizations applied"
echo "✅ Automatic security updates enabled"
echo "✅ Monitoring tools installed"
echo ""
echo "⚠️  IMPORTANT NOTES:"
echo "1. SSH port changed to: $SSH_PORT"
echo "2. Root login disabled"
echo "3. Password authentication disabled"
echo "4. Only SSH key authentication allowed"
echo ""
echo "Next steps:"
echo "1. Test SSH connection with new port: ssh -p $SSH_PORT minecraft@YOUR_IP"
echo "2. Make sure you have SSH key access before disconnecting!"
echo "3. Run: sudo su - minecraft"
echo "4. Run: ./scripts/install-hegemonia.sh"
echo ""
echo "=================================="

log_info "Security hardening completed successfully!"
