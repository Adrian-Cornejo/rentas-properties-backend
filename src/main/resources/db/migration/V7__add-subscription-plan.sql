-- V7__create_subscription_plans_and_add_fk_to_organizations.sql
-- Creación de tabla de planes de suscripción y migración de organizations

-- ============================================
-- PASO 1: CREAR TABLA SUBSCRIPTION_PLANS
-- ============================================

CREATE TABLE subscription_plans (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Identificación
                                    plan_code VARCHAR(50) UNIQUE NOT NULL,
                                    plan_name VARCHAR(100) NOT NULL,
                                    plan_description TEXT,

    -- Pricing
                                    monthly_price DECIMAL(10, 2) NOT NULL,
                                    annual_price DECIMAL(10, 2),
                                    currency VARCHAR(3) DEFAULT 'MXN',

    -- Trial
                                    trial_days INTEGER DEFAULT 0,

    -- Límites de recursos
                                    max_properties INTEGER NOT NULL,
                                    max_users INTEGER NOT NULL,
                                    max_active_contracts INTEGER NOT NULL,
                                    storage_limit_mb INTEGER NOT NULL,
                                    images_per_property INTEGER NOT NULL,
                                    report_history_days INTEGER NOT NULL,

    -- Notificaciones
                                    has_notifications BOOLEAN DEFAULT FALSE,
                                    notification_channels VARCHAR(50),
                                    monthly_notification_limit INTEGER DEFAULT 0,
                                    has_late_reminders BOOLEAN DEFAULT FALSE,
                                    has_admin_digest BOOLEAN DEFAULT FALSE,

    -- Mantenimiento
                                    has_maintenance_scheduling BOOLEAN DEFAULT FALSE,
                                    has_maintenance_photos BOOLEAN DEFAULT FALSE,

    -- Reportes
                                    has_advanced_reports BOOLEAN DEFAULT FALSE,
                                    has_data_export BOOLEAN DEFAULT FALSE,
                                    has_pdf_reports BOOLEAN DEFAULT FALSE,

    -- Funcionalidades avanzadas
                                    has_api_access BOOLEAN DEFAULT FALSE,
                                    has_white_label BOOLEAN DEFAULT FALSE,
                                    white_label_level VARCHAR(20),
                                    has_multi_currency BOOLEAN DEFAULT FALSE,
                                    has_document_management BOOLEAN DEFAULT FALSE,
                                    has_e_signature BOOLEAN DEFAULT FALSE,
                                    has_tenant_portal BOOLEAN DEFAULT FALSE,
                                    has_mobile_app BOOLEAN DEFAULT FALSE,
                                    has_integrations BOOLEAN DEFAULT FALSE,

    -- Soporte
                                    support_level VARCHAR(50) DEFAULT 'email',
                                    support_response_hours INTEGER,
                                    has_onboarding BOOLEAN DEFAULT FALSE,
                                    has_account_manager BOOLEAN DEFAULT FALSE,

    -- Display y estado
                                    display_order INTEGER DEFAULT 0,
                                    is_popular BOOLEAN DEFAULT FALSE,
                                    is_custom BOOLEAN DEFAULT FALSE,
                                    is_active BOOLEAN DEFAULT TRUE,

                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índices para subscription_plans
CREATE INDEX idx_subscription_plans_code ON subscription_plans(plan_code);
CREATE INDEX idx_subscription_plans_active ON subscription_plans(is_active);
CREATE INDEX idx_subscription_plans_order ON subscription_plans(display_order);

-- Tabla de características detalladas (opcional)
CREATE TABLE subscription_features (
                                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                       plan_id UUID REFERENCES subscription_plans(id) ON DELETE CASCADE,

                                       feature_category VARCHAR(100),
                                       feature_name VARCHAR(255) NOT NULL,
                                       feature_description TEXT,
                                       is_included BOOLEAN DEFAULT TRUE,
                                       is_highlight BOOLEAN DEFAULT FALSE,
                                       display_order INTEGER DEFAULT 0,

                                       created_at TIMESTAMP
);

CREATE INDEX idx_subscription_features_plan ON subscription_features(plan_id);
CREATE INDEX idx_subscription_features_category ON subscription_features(feature_category);

-- ============================================
-- PASO 2: INSERTAR PLANES DEFINITIVOS
-- ============================================

INSERT INTO subscription_plans (
    plan_code, plan_name, plan_description,
    monthly_price, annual_price,
    trial_days,
    max_properties, max_users, max_active_contracts,
    storage_limit_mb, images_per_property, report_history_days,
    has_notifications, notification_channels, monthly_notification_limit,
    has_late_reminders, has_admin_digest,
    has_maintenance_scheduling, has_maintenance_photos,
    has_advanced_reports, has_data_export, has_pdf_reports,
    has_api_access, has_white_label, white_label_level,
    has_multi_currency, has_document_management, has_e_signature,
    has_tenant_portal, has_mobile_app, has_integrations,
    support_level, support_response_hours, has_onboarding, has_account_manager,
    display_order, is_popular, is_active
) VALUES

      ('STARTER', 'Starter', 'Ideal para comenzar con pocas propiedades',
       0.00, 0.00,
       0,
       3, 1, 3,
       50, 0, 30,
       FALSE, NULL, 0,
       FALSE, FALSE,
       FALSE, FALSE,
       FALSE, FALSE, FALSE,
       FALSE, FALSE, NULL,
       FALSE, FALSE, FALSE,
       FALSE, FALSE, FALSE,
       'email', 72, FALSE, FALSE,
       1, FALSE, TRUE),


      ('BASICO', 'Básico', 'Para pequeños propietarios con automatización básica',
       299.00, 2990.00,
       15,
       10, 3, 10,
       1024, 8, 90,
       TRUE, 'SMS_OR_WHATSAPP', 60,
       FALSE, FALSE,
       TRUE, FALSE,
       FALSE, FALSE, TRUE,
       FALSE, FALSE, NULL,
       FALSE, FALSE, FALSE,
       FALSE, FALSE, FALSE,
       'email', 36, FALSE, FALSE,
       2, FALSE, TRUE),


      ('PROFESIONAL', 'Profesional', 'Para inmobiliarias y administradores profesionales',
       799.00, 7990.00,
       30,
       50, 10, 50,
       10240, 15, 365,
       TRUE, 'BOTH', 500,
       TRUE, TRUE,
       TRUE, TRUE,
       TRUE, TRUE, TRUE,
       FALSE, TRUE, 'BASIC',
       FALSE, FALSE, FALSE,
       FALSE, FALSE, FALSE,
       'priority', 18, TRUE, FALSE,
       3, TRUE, TRUE),


      ('EMPRESARIAL', 'Empresarial', 'Solución completa para grandes organizaciones',
       1999.00, 19990.00,
       30,
       200, -1, 200,
       51200, 30, -1,
       TRUE, 'UNLIMITED', -1,
       TRUE, TRUE,
       TRUE, TRUE,
       TRUE, TRUE, TRUE,
       TRUE, TRUE, 'FULL',
       TRUE, TRUE, TRUE,
       TRUE, TRUE, TRUE,
       'dedicated', 6, TRUE, TRUE,
       4, FALSE, TRUE);

-- ============================================
-- PASO 3: ACTUALIZAR ORGANIZATIONS - AGREGAR FK
-- ============================================

-- Agregar columna subscription_plan_id
ALTER TABLE organizations
    ADD COLUMN IF NOT EXISTS subscription_plan_id UUID;

-- Crear foreign key constraint
ALTER TABLE organizations
    ADD CONSTRAINT fk_organizations_subscription_plan
        FOREIGN KEY (subscription_plan_id)
            REFERENCES subscription_plans(id)
            ON DELETE SET NULL;

-- Crear índice
CREATE INDEX IF NOT EXISTS idx_organizations_subscription_plan_id
    ON organizations(subscription_plan_id);

-- Migrar datos existentes: Asignar STARTER a todas las organizaciones
UPDATE organizations o
SET subscription_plan_id = (
    SELECT id FROM subscription_plans WHERE plan_code = 'STARTER' LIMIT 1
    )
WHERE subscription_plan_id IS NULL;

-- ============================================
-- PASO 4: SINCRONIZAR LÍMITES DESDE PLAN
-- ============================================

UPDATE organizations o
SET
    max_properties = p.max_properties,
    max_users = p.max_users,
    notification_limit = p.monthly_notification_limit
    FROM subscription_plans p
WHERE o.subscription_plan_id = p.id;

