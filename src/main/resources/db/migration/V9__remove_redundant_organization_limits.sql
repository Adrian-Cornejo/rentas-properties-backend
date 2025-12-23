-- V9__remove_redundant_organization_limits.sql
-- Eliminar columnas redundantes que ahora vienen del subscription_plan

ALTER TABLE organizations
DROP COLUMN IF EXISTS max_properties,
    DROP COLUMN IF EXISTS max_users,
    DROP COLUMN IF EXISTS notification_limit;