-- V7__update_notification_columns_and_sync_from_plan.sql
-- Actualizar columnas de notificaciones para alinearse con subscription_plans

ALTER TABLE organizations
    RENAME COLUMN notification_channel TO notification_channels;

-- Ampliar tamaño del campo para soportar valores como 'SMS_OR_WHATSAPP', 'UNLIMITED'
ALTER TABLE organizations
ALTER COLUMN notification_channels TYPE VARCHAR(50);

ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS admin_digest_enabled BOOLEAN DEFAULT FALSE;

-- Migrar datos de admin_notifications a admin_digest_enabled
UPDATE organizations
SET admin_digest_enabled = admin_notifications
WHERE admin_notifications IS NOT NULL;