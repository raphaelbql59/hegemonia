import { readFileSync } from 'fs';
import { join, dirname } from 'path';
import { fileURLToPath } from 'url';
import { pool } from '../db/connection.js';

const __filename = fileURLToPath(import.meta.url);
const __dirname = dirname(__filename);

async function runMigrations() {
  console.log('🔧 Running database migrations...\n');

  try {
    // Read migration file
    const migrationPath = join(__dirname, '../db/migrations/001_create_launcher_tables.sql');
    const migrationSQL = readFileSync(migrationPath, 'utf-8');

    // Execute migration
    await pool.query(migrationSQL);

    console.log('✅ Migration completed successfully!');
    console.log('   - launcher_users table created');
    console.log('   - launcher_news table created');
    console.log('   - launcher_sessions table created');
    console.log('');

  } catch (error) {
    console.error('❌ Migration failed:', error);
    process.exit(1);
  } finally {
    await pool.end();
  }
}

runMigrations();
