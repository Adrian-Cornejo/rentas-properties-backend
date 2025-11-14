-- ============================================
-- V4: Agregar soporte multi-tenant con organizaciones
-- ============================================
CREATE TABLE organizations (
                               id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),

                               name VARCHAR(255) NOT NULL,
                               description TEXT,

                               logo_url TEXT,
                               logo_public_id VARCHAR(255),
                               primary_color VARCHAR(7) DEFAULT '#3B82F6', -- Hex color
                               secondary_color VARCHAR(7) DEFAULT '#10B981',

                               invitation_code VARCHAR(8) UNIQUE NOT NULL, -- ABC-12D3
                               code_is_reusable BOOLEAN DEFAULT TRUE,

                               owner_id UUID REFERENCES users(id) ON DELETE SET NULL,

                               max_users INTEGER DEFAULT 3,
                               max_properties INTEGER DEFAULT 3,
                               current_users_count INTEGER DEFAULT 0,
                               current_properties_count INTEGER DEFAULT 0,

                               subscription_status VARCHAR(50) DEFAULT 'trial', 
                               subscription_plan VARCHAR(50) DEFAULT 'free',
                               trial_ends_at TIMESTAMP,
                               subscription_started_at TIMESTAMP,
                               subscription_ends_at TIMESTAMP,

                               is_active BOOLEAN DEFAULT TRUE,

                               created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                               created_by UUID REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_organizations_invitation_code ON organizations(invitation_code);
CREATE INDEX idx_organizations_owner ON organizations(owner_id);
CREATE INDEX idx_organizations_subscription_status ON organizations(subscription_status);
CREATE INDEX idx_organizations_is_active ON organizations(is_active);

-- ============================================
-- 2. AGREGAR COLUMNA organization_id A USERS
-- ============================================
ALTER TABLE users
    ADD COLUMN organization_id UUID REFERENCES organizations(id) ON DELETE SET NULL,
    ADD COLUMN organization_joined_at TIMESTAMP,
    ADD COLUMN account_status VARCHAR(50) DEFAULT 'pending';

CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_users_account_status ON users(account_status);

-- ============================================
-- 3. AGREGAR COLUMNA organization_id A LOCATIONS
-- ============================================
ALTER TABLE locations
    ADD COLUMN organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE;

CREATE INDEX idx_locations_organization ON locations(organization_id);

-- ============================================
-- 4. AGREGAR COLUMNA organization_id A PROPERTIES
-- ============================================
ALTER TABLE properties
    ADD COLUMN organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE;

CREATE INDEX idx_properties_organization ON properties(organization_id);

-- ============================================
-- 5. AGREGAR COLUMNA organization_id A TENANTS
-- ============================================
ALTER TABLE tenants
    ADD COLUMN organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE;

CREATE INDEX idx_tenants_organization ON tenants(organization_id);

-- ============================================
-- 6. AGREGAR COLUMNA organization_id A CONTRACTS
-- ============================================
ALTER TABLE contracts
    ADD COLUMN organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE;

CREATE INDEX idx_contracts_organization ON contracts(organization_id);

-- ============================================
-- 7. AGREGAR COLUMNA organization_id A MAINTENANCE_RECORDS
-- ============================================
ALTER TABLE maintenance_records
    ADD COLUMN organization_id UUID REFERENCES organizations(id) ON DELETE CASCADE;

CREATE INDEX idx_maintenance_organization ON maintenance_records(organization_id);

