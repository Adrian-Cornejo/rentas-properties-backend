-- ============================================
-- V3: Agregar índices para optimización
-- ============================================

-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_role ON users(role);

-- Locations
CREATE INDEX idx_locations_name ON locations(name);
CREATE INDEX idx_locations_is_active ON locations(is_active);
CREATE INDEX idx_locations_city ON locations(city);

-- Properties
CREATE INDEX idx_properties_location ON properties(location_id);
CREATE INDEX idx_properties_status ON properties(status);
CREATE INDEX idx_properties_type ON properties(property_type);
CREATE INDEX idx_properties_code ON properties(property_code);
CREATE INDEX idx_properties_slug ON properties(public_url_slug);
CREATE INDEX idx_properties_is_active ON properties(is_active);

-- Property Images
CREATE INDEX idx_property_images_property ON property_images(property_id);
CREATE INDEX idx_property_images_is_main ON property_images(is_main);

-- Tenants
CREATE INDEX idx_tenants_phone ON tenants(phone);
CREATE INDEX idx_tenants_name ON tenants(full_name);
CREATE INDEX idx_tenants_is_active ON tenants(is_active);

-- Contracts
CREATE INDEX idx_contracts_property ON contracts(property_id);
CREATE INDEX idx_contracts_status ON contracts(status);
CREATE INDEX idx_contracts_dates ON contracts(start_date, end_date);
CREATE INDEX idx_contracts_number ON contracts(contract_number);
CREATE INDEX idx_contracts_is_active ON contracts(is_active);

-- Contract Tenants
CREATE INDEX idx_contract_tenants_contract ON contract_tenants(contract_id);
CREATE INDEX idx_contract_tenants_tenant ON contract_tenants(tenant_id);

-- Payments
CREATE INDEX idx_payments_contract ON payments(contract_id);
CREATE INDEX idx_payments_status ON payments(status);
CREATE INDEX idx_payments_dates ON payments(payment_date, due_date);
CREATE INDEX idx_payments_period ON payments(period_year, period_month);
CREATE INDEX idx_payments_type ON payments(payment_type);
CREATE INDEX idx_payments_collected_by ON payments(collected_by);

-- Maintenance Records
CREATE INDEX idx_maintenance_property ON maintenance_records(property_id);
CREATE INDEX idx_maintenance_contract ON maintenance_records(contract_id);
CREATE INDEX idx_maintenance_status ON maintenance_records(status);
CREATE INDEX idx_maintenance_date ON maintenance_records(maintenance_date);
CREATE INDEX idx_maintenance_type ON maintenance_records(maintenance_type);

-- Maintenance Images
CREATE INDEX idx_maintenance_images_maintenance ON maintenance_images(maintenance_id);

-- Notifications
CREATE INDEX idx_notifications_recipient ON notifications(recipient_type, recipient_id);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_type ON notifications(notification_type);
CREATE INDEX idx_notifications_created ON notifications(created_at);
CREATE INDEX idx_notifications_contract ON notifications(related_contract_id);
CREATE INDEX idx_notifications_payment ON notifications(related_payment_id);

-- Documents
CREATE INDEX idx_documents_entity ON documents(entity_type, entity_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_is_active ON documents(is_active);

-- Audit Log
CREATE INDEX idx_audit_user ON audit_log(user_id);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_log(action);
CREATE INDEX idx_audit_created ON audit_log(created_at);