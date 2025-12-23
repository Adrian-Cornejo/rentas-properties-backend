-- V10__update_images_per_property_limits.sql
-- Actualizar límites de imágenes por propiedad: 0, 5, 10, 15

-- STARTER: 0 imágenes (sin fotografías)
UPDATE subscription_plans
SET images_per_property = 0
WHERE plan_code = 'STARTER';

-- BASICO: 5 imágenes
UPDATE subscription_plans
SET images_per_property = 5
WHERE plan_code = 'BASICO';

-- PROFESIONAL: 10 imágenes
UPDATE subscription_plans
SET images_per_property = 10
WHERE plan_code = 'PROFESIONAL';

-- EMPRESARIAL: 15 imágenes
UPDATE subscription_plans
SET images_per_property = 15
WHERE plan_code = 'EMPRESARIAL';