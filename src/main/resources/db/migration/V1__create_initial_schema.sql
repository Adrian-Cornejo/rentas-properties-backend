-- ============================================
-- V1: Esquema inicial - Test de conexión
-- ============================================

-- Extensión para UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla de prueba
CREATE TABLE test_connection (
                                 id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 message VARCHAR(255),
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insertar dato de prueba
INSERT INTO test_connection (message) VALUES ('Conexión exitosa con Supabase desde Gradle!');
