#!/bin/bash
# HeartGame Database Export Script
# Creates a timestamped backup of the complete database

DB_NAME="heartgame"
DB_USER="root"
DB_PASS="password123"
BACKUP_FILE="heartgame_backup.sql"

echo "==========================================="
echo "HeartGame Database Export Tool"
echo "==========================================="
echo ""
echo "Exporting database: $DB_NAME"
echo "Output file: $BACKUP_FILE"
echo ""

# Export the database with proper formatting
mysqldump -u "$DB_USER" -p"$DB_PASS" \
    --add-drop-table \
    --complete-insert \
    --single-transaction \
    --routines \
    --triggers \
    "$DB_NAME" > "$BACKUP_FILE"

# Check if export was successful
if [ $? -eq 0 ]; then
    echo "✅ Database exported successfully!"
    echo ""
    echo "Backup saved to: $BACKUP_FILE"
    echo "File size: $(du -h "$BACKUP_FILE" | cut -f1)"
    echo ""
    echo "This backup will be automatically loaded on first-time setup."
else
    echo "❌ Export failed! Check MySQL connection and credentials."
    exit 1
fi
