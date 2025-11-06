-- ============================================
-- V2: Esquema Completo del Sistema
-- Sistema de Gestión de Propiedades en Renta
-- ============================================

-- ============================================
-- TABLA: USERS
-- ============================================
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(255) NOT NULL,
                       phone VARCHAR(20),
                       role VARCHAR(50) DEFAULT 'USER',
                       is_active BOOLEAN DEFAULT TRUE,
                       last_login TIMESTAMP,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE users IS 'Usuarios del sistema (familia)';

-- ============================================
-- TABLA: LOCATIONS
-- ============================================
CREATE TABLE locations (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           name VARCHAR(255) NOT NULL,
                           address TEXT,
                           city VARCHAR(100),
                           state VARCHAR(100),
                           postal_code VARCHAR(10),
                           description TEXT,
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE locations IS 'Ubicaciones/colonias donde se agrupan las propiedades';

-- ============================================
-- TABLA: PROPERTIES
-- ============================================
CREATE TABLE properties (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            location_id UUID REFERENCES locations(id) ON DELETE SET NULL,
                            property_code VARCHAR(50) UNIQUE NOT NULL,
                            property_type VARCHAR(50) NOT NULL,
                            address TEXT NOT NULL,
                            monthly_rent DECIMAL(10, 2) NOT NULL,
                            water_fee DECIMAL(10, 2) DEFAULT 105.00,
                            status VARCHAR(50) DEFAULT 'DISPONIBLE',

    -- Características físicas
                            floors INTEGER DEFAULT 1,
                            bedrooms INTEGER,
                            bathrooms INTEGER,
                            half_bathrooms INTEGER,
                            has_living_room BOOLEAN DEFAULT FALSE,
                            has_dining_room BOOLEAN DEFAULT FALSE,
                            has_kitchen BOOLEAN DEFAULT FALSE,
                            has_service_area BOOLEAN DEFAULT FALSE,
                            parking_spaces INTEGER DEFAULT 0,
                            total_area_m2 DECIMAL(8, 2),

    -- Servicios incluidos
                            includes_water BOOLEAN DEFAULT FALSE,
                            includes_electricity BOOLEAN DEFAULT FALSE,
                            includes_gas BOOLEAN DEFAULT FALSE,
                            includes_internet BOOLEAN DEFAULT FALSE,

                            notes TEXT,
                            public_url_slug VARCHAR(255) UNIQUE,
                            is_active BOOLEAN DEFAULT TRUE,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE properties IS 'Propiedades disponibles para renta';

-- ============================================
-- TABLA: PROPERTY_IMAGES
-- ============================================
CREATE TABLE property_images (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 property_id UUID REFERENCES properties(id) ON DELETE CASCADE,
                                 image_url TEXT NOT NULL,
                                 image_public_id VARCHAR(255),
                                 description VARCHAR(255),
                                 display_order INTEGER DEFAULT 0,
                                 is_main BOOLEAN DEFAULT FALSE,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE property_images IS 'Galería de imágenes de cada propiedad';

-- ============================================
-- TABLA: TENANTS
-- ============================================
CREATE TABLE tenants (
                         id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         full_name VARCHAR(255) NOT NULL,
                         phone VARCHAR(20) NOT NULL,
                         email VARCHAR(255),
                         ine_number VARCHAR(50),
                         ine_image_url TEXT,
                         ine_public_id VARCHAR(255),
                         number_of_occupants INTEGER DEFAULT 1,
                         notes TEXT,
                         is_active BOOLEAN DEFAULT TRUE,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE tenants IS 'Arrendatarios/inquilinos';

-- ============================================
-- TABLA: CONTRACTS
-- ============================================
CREATE TABLE contracts (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           property_id UUID REFERENCES properties(id) ON DELETE RESTRICT,
                           contract_number VARCHAR(50) UNIQUE NOT NULL,

    -- Fechas
                           start_date DATE NOT NULL,
                           end_date DATE NOT NULL,
                           signed_date DATE,

    -- Montos
                           monthly_rent DECIMAL(10, 2) NOT NULL,
                           water_fee DECIMAL(10, 2) DEFAULT 105.00,
                           advance_payment DECIMAL(10, 2) NOT NULL,
                           deposit_amount DECIMAL(10, 2) NOT NULL,
                           deposit_paid BOOLEAN DEFAULT FALSE,
                           deposit_payment_deadline DATE,

    -- Estado del depósito
                           deposit_status VARCHAR(50) DEFAULT 'PENDIENTE',
                           deposit_return_amount DECIMAL(10, 2),
                           deposit_return_date DATE,
                           deposit_deduction_reason TEXT,

    -- Estado del contrato
                           status VARCHAR(50) DEFAULT 'ACTIVO',

    -- Documentos
                           contract_document_url TEXT,
                           contract_document_public_id VARCHAR(255),

                           notes TEXT,
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE contracts IS 'Contratos de arrendamiento';

-- ============================================
-- TABLA: CONTRACT_TENANTS
-- ============================================
CREATE TABLE contract_tenants (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  contract_id UUID REFERENCES contracts(id) ON DELETE CASCADE,
                                  tenant_id UUID REFERENCES tenants(id) ON DELETE RESTRICT,
                                  is_primary BOOLEAN DEFAULT TRUE,
                                  relationship VARCHAR(100),
                                  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE contract_tenants ADD CONSTRAINT unique_contract_tenant
    UNIQUE (contract_id, tenant_id);

COMMENT ON TABLE contract_tenants IS 'Relación entre contratos y arrendatarios';

-- ============================================
-- TABLA: PAYMENTS
-- ============================================
CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          contract_id UUID REFERENCES contracts(id) ON DELETE RESTRICT,

    -- Tipo de pago
                          payment_type VARCHAR(50) NOT NULL,

    -- Fechas
                          payment_date DATE NOT NULL,
                          due_date DATE NOT NULL,
                          period_month INTEGER NOT NULL,
                          period_year INTEGER NOT NULL,

    -- Montos
                          amount DECIMAL(10, 2) NOT NULL,
                          late_fee DECIMAL(10, 2) DEFAULT 0.00,
                          total_amount DECIMAL(10, 2) NOT NULL,

    -- Estado
                          status VARCHAR(50) DEFAULT 'PENDIENTE',

    -- Método de pago
                          payment_method VARCHAR(50),
                          reference_number VARCHAR(100),

                          notes TEXT,
                          paid_at TIMESTAMP,
                          collected_by UUID REFERENCES users(id),
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE payments IS 'Registro de pagos mensuales';

-- ============================================
-- TABLA: MAINTENANCE_RECORDS
-- ============================================
CREATE TABLE maintenance_records (
                                     id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                     property_id UUID REFERENCES properties(id) ON DELETE RESTRICT,
                                     contract_id UUID REFERENCES contracts(id) ON DELETE SET NULL,

    -- Detalles
                                     title VARCHAR(255) NOT NULL,
                                     description TEXT NOT NULL,
                                     maintenance_type VARCHAR(50) NOT NULL,
                                     category VARCHAR(50),

    -- Fechas
                                     maintenance_date DATE NOT NULL,
                                     completed_date DATE,

    -- Costos
                                     estimated_cost DECIMAL(10, 2),
                                     actual_cost DECIMAL(10, 2),

    -- Estado
                                     status VARCHAR(50) DEFAULT 'PENDIENTE',

                                     assigned_to VARCHAR(255),
                                     notes TEXT,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE maintenance_records IS 'Registro de mantenimiento y reparaciones';

-- ============================================
-- TABLA: MAINTENANCE_IMAGES
-- ============================================
CREATE TABLE maintenance_images (
                                    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    maintenance_id UUID REFERENCES maintenance_records(id) ON DELETE CASCADE,
                                    image_url TEXT NOT NULL,
                                    image_public_id VARCHAR(255),
                                    description VARCHAR(255),
                                    image_type VARCHAR(50) DEFAULT 'EVIDENCIA',
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE maintenance_images IS 'Evidencias fotográficas de mantenimiento';

-- ============================================
-- TABLA: NOTIFICATIONS
-- ============================================
CREATE TABLE notifications (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Destinatario
                               recipient_type VARCHAR(50) NOT NULL,
                               recipient_id UUID,
                               recipient_phone VARCHAR(20) NOT NULL,

    -- Contenido
                               notification_type VARCHAR(50) NOT NULL,
                               title VARCHAR(255) NOT NULL,
                               message TEXT NOT NULL,

    -- Canal
                               channel VARCHAR(50) NOT NULL,

    -- Estado
                               status VARCHAR(50) DEFAULT 'PENDING',
                               sent_at TIMESTAMP,
                               delivered_at TIMESTAMP,

    -- Referencias
                               related_contract_id UUID REFERENCES contracts(id) ON DELETE SET NULL,
                               related_payment_id UUID REFERENCES payments(id) ON DELETE SET NULL,

    -- Provider info
                               provider_message_id VARCHAR(255),
                               error_message TEXT,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE notifications IS 'Log de notificaciones enviadas';

-- ============================================
-- TABLA: DOCUMENTS
-- ============================================
CREATE TABLE documents (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

    -- Polimórfico
                           entity_type VARCHAR(50) NOT NULL,
                           entity_id UUID NOT NULL,

    -- Detalles del documento
                           document_type VARCHAR(50) NOT NULL,
                           file_name VARCHAR(255) NOT NULL,
                           file_url TEXT NOT NULL,
                           file_public_id VARCHAR(255),
                           file_size INTEGER,
                           mime_type VARCHAR(100),

                           description TEXT,
                           is_active BOOLEAN DEFAULT TRUE,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           created_by UUID REFERENCES users(id)
);

COMMENT ON TABLE documents IS 'Documentos generales del sistema';

-- ============================================
-- TABLA: AUDIT_LOG
-- ============================================
CREATE TABLE audit_log (
                           id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                           user_id UUID REFERENCES users(id) ON DELETE SET NULL,

    -- Acción realizada
                           action VARCHAR(100) NOT NULL,
                           entity_type VARCHAR(50) NOT NULL,
                           entity_id UUID,

    -- Detalles
                           description TEXT,
                           old_values JSONB,
                           new_values JSONB,

                           ip_address VARCHAR(45),
                           user_agent TEXT,

                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE audit_log IS 'Log de auditoría de todas las acciones';

-- ============================================
-- TRIGGERS PARA UPDATED_AT
-- ============================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_locations_updated_at
    BEFORE UPDATE ON locations
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_properties_updated_at
    BEFORE UPDATE ON properties
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_tenants_updated_at
    BEFORE UPDATE ON tenants
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_contracts_updated_at
    BEFORE UPDATE ON contracts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_payments_updated_at
    BEFORE UPDATE ON payments
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_maintenance_records_updated_at
    BEFORE UPDATE ON maintenance_records
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();