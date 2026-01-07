import bcrypt from 'bcrypt';
import { query, closePool } from '../db/connection.js';

const ADMIN_EMAIL = 'admin@hegemonia.fr';
const ADMIN_PASSWORD = 'Hegemonia2024!';
const ADMIN_USERNAME = 'Admin';

async function createAdminAccount() {
  console.log('👑 Creating admin account...\n');

  try {
    // Check if admin already exists
    const existingAdmin = await query(
      'SELECT id FROM launcher_users WHERE email = $1',
      [ADMIN_EMAIL]
    );

    if (existingAdmin.rows.length > 0) {
      console.log('⚠️  Admin account already exists!');
      console.log(`   Email: ${ADMIN_EMAIL}`);
      console.log('   Password: (unchanged)');
      console.log('');
      return;
    }

    // Hash password
    const passwordHash = await bcrypt.hash(ADMIN_PASSWORD, 12);

    // Create admin user
    const result = await query(
      `INSERT INTO launcher_users
       (username, email, password_hash, display_name, role, is_active)
       VALUES ($1, $2, $3, $4, $5, $6)
       RETURNING id, uuid, username, email, role`,
      [ADMIN_USERNAME, ADMIN_EMAIL, passwordHash, 'Administrateur', 'admin', true]
    );

    const admin = result.rows[0];

    console.log('✅ Admin account created successfully!');
    console.log('');
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('   📧 Email:    ' + ADMIN_EMAIL);
    console.log('   🔑 Password: ' + ADMIN_PASSWORD);
    console.log('   🆔 UUID:     ' + admin.uuid);
    console.log('   👤 Username: ' + admin.username);
    console.log('   🎭 Role:     ' + admin.role);
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━');
    console.log('');
    console.log('⚠️  IMPORTANT: Changez ce mot de passe en production !');
    console.log('');

  } catch (error: any) {
    console.error('❌ Failed to create admin account:', error.message);
    process.exit(1);
  } finally {
    await closePool();
  }
}

createAdminAccount();
