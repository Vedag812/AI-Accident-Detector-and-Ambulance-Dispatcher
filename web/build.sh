#!/usr/bin/env bash
set -o errexit

pip install -r requirements.txt

# Initialize PostgreSQL database
if [ -n "$DATABASE_URL" ]; then
    echo "Initializing PostgreSQL..."
    python -c "
import psycopg2, os
conn = psycopg2.connect(os.environ['DATABASE_URL'])
cur = conn.cursor()
with open('init_pg.sql', 'r') as f:
    cur.execute(f.read())
conn.commit()
cur.close()
conn.close()
print('DB initialized!')
"
fi
