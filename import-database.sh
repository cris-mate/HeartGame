#!/bin/bash
# HeartGame Database Import Script
# Imports backup data (useful for testing or restoring)

DB_NAME="heartgame"
DB_USER="root"
DB_PASS="password123"
BACKUP_FILE="heartgame_backup.sql"

echo "==========================================="
echo "HeartGame Database Import Tool"
echo "==========================================="
echo ""

# Check if backup file exists
if [ ! -f "$BACKUP_FILE" ]; then
    echo "❌ Error: Backup file not found: $BACKUP_FILE"
    echo "Please run ./export-database.sh first."
    exit 1
fi

echo "⚠️  WARNING: This will overwrite all data in database: $DB_NAME"
echo ""
read -p "Continue? (yes/no): " confirm

if [ "$confirm" != "yes" ]; then
    echo "Import cancelled."
    exit 0
fi

echo ""
echo "Importing from: $BACKUP_FILE"

# Import the backup
mysql -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$BACKUP_FILE"

if [ $? -eq 0 ]; then
    echo "✅ Database imported successfully!"
else
    echo "❌ Import failed! Check MySQL connection and backup file."
    exit 1
fi
