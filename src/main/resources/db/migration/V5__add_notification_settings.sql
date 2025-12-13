-- ============================================
-- V5: Agregar configuración de notificaciones a organizations
-- ============================================

ALTER TABLE organizations
    ADD COLUMN notification_enabled BOOLEAN DEFAULT FALSE,
    ADD COLUMN notification_channel VARCHAR(20),
    ADD COLUMN notifications_sent_this_month INTEGER DEFAULT 0,
    ADD COLUMN notification_limit INTEGER DEFAULT 0,
    ADD COLUMN last_notification_reset DATE,
    ADD COLUMN admin_notifications BOOLEAN DEFAULT TRUE;


CREATE INDEX idx_organizations_notification_enabled ON organizations(notification_enabled);

ALTER TABLE notifications
    ADD COLUMN retry_count INTEGER DEFAULT 0,
    ADD COLUMN last_retry_at TIMESTAMP;

CREATE INDEX idx_notifications_pending_retry ON notifications(status, created_at) WHERE status = 'PENDING';
