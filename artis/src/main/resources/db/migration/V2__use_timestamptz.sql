-- Altera as colunas para TIMESTAMP WITH TIME ZONE.
-- Isso garante que o banco não perca a informação de UTC (o "Z" no final do JSON).
-- O USING deixa explícito que os valores existentes estão em UTC.

ALTER TABLE event
ALTER COLUMN event_time TYPE TIMESTAMPTZ USING event_time AT TIME ZONE 'UTC',
    ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';

ALTER TABLE audit_log
ALTER COLUMN created_at TYPE TIMESTAMPTZ USING created_at AT TIME ZONE 'UTC';