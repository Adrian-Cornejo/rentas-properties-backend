# ============================================
# SCRIPT DE PRUEBA - LOCATION ENDPOINTS
# ============================================

$baseUrl = "http://localhost:8080/api/v1"
$authUrl = "$baseUrl/auth"
$locationsUrl = "$baseUrl/locations"

# Colores para output
function Write-Test { param($msg) Write-Host "🧪 $msg" -ForegroundColor Cyan }
function Write-Success { param($msg) Write-Host "✅ $msg" -ForegroundColor Green }
function Write-Error { param($msg) Write-Host "❌ $msg" -ForegroundColor Red }
function Write-Info { param($msg) Write-Host "📝 $msg" -ForegroundColor Yellow }

Clear-Host
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "     PRUEBAS DE ENDPOINTS - LOCATIONS      " -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host ""

# ============================================
# 1. LOGIN PARA OBTENER TOKEN
# ============================================
Write-Test "1. AUTENTICACIÓN - Login como Admin"

$loginBody = @{
    email = "usuario@example.com"
    password = "Password123)/842#"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$authUrl/login" `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody

    $token = $loginResponse.token
    Write-Success "Login exitoso - Token obtenido"
    Write-Info "Usuario: $($loginResponse.user.email)"
    Write-Info "Rol: $($loginResponse.user.role)"
    Write-Info "Rol: $($loginResponse.user.id)"
} catch {
    Write-Error "Error en login: $_"
    exit 1
}

# Headers con autorización
$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 2. CREAR LOCATION
# ============================================
Write-Test "2. CREATE - Crear nueva ubicación"

$createBody = @{
    name = "Colonia San Miguel"
    address = "Calle Principal #123"
    city = "Toluca"
    state = "Estado de México"
    postalCode = "50000"
    description = "Zona residencial cerca del centro"
} | ConvertTo-Json

try {
    $createResponse = Invoke-RestMethod -Uri $locationsUrl `
        -Method POST `
        -Headers $headers `
        -Body $createBody

    $locationId = $createResponse.id
    Write-Success "Ubicación creada exitosamente"
    Write-Info "ID: $locationId"
    Write-Info "Nombre: $($createResponse.name)"
    Write-Info "Ciudad: $($createResponse.city)"
} catch {
    Write-Error "Error creando ubicación: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 3. CREAR SEGUNDA LOCATION
# ============================================
Write-Test "3. CREATE - Crear segunda ubicación"

$createBody2 = @{
    name = "Colonia Centro"
    address = "Av. Hidalgo #456"
    city = "Toluca"
    state = "Estado de México"
    postalCode = "50010"
    description = "Zona comercial céntrica"
} | ConvertTo-Json

try {
    $createResponse2 = Invoke-RestMethod -Uri $locationsUrl `
        -Method POST `
        -Headers $headers `
        -Body $createBody2

    $locationId2 = $createResponse2.id
    Write-Success "Segunda ubicación creada"
    Write-Info "ID: $locationId2"
    Write-Info "Nombre: $($createResponse2.name)"
} catch {
    Write-Error "Error creando segunda ubicación: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 4. INTENTAR CREAR LOCATION DUPLICADA
# ============================================
Write-Test "4. CREATE - Intentar crear ubicación duplicada (debe fallar)"

$duplicateBody = @{
    name = "Colonia San Miguel"
    address = "Otra dirección"
    city = "Toluca"
    state = "Estado de México"
} | ConvertTo-Json

try {
    $duplicateResponse = Invoke-RestMethod -Uri $locationsUrl `
        -Method POST `
        -Headers $headers `
        -Body $duplicateBody

    Write-Error "No debería permitir duplicados"
} catch {
    Write-Success "Validación correcta - No permite nombres duplicados"
    Write-Info "Error esperado: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 5. OBTENER TODAS LAS LOCATIONS
# ============================================
Write-Test "5. GET ALL - Obtener todas las ubicaciones"

try {
    $allLocations = Invoke-RestMethod -Uri $locationsUrl `
        -Method GET `
        -Headers $headers

    Write-Success "Ubicaciones obtenidas: $($allLocations.Count)"
    foreach ($loc in $allLocations) {
        Write-Info "  - $($loc.name) ($($loc.city))"
    }
} catch {
    Write-Error "Error obteniendo ubicaciones: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 6. OBTENER LOCATION POR ID
# ============================================
Write-Test "6. GET BY ID - Obtener ubicación específica"

try {
    $getByIdResponse = Invoke-RestMethod -Uri "$locationsUrl/$locationId" `
        -Method GET `
        -Headers $headers

    Write-Success "Ubicación obtenida por ID"
    Write-Info "Nombre: $($getByIdResponse.name)"
    Write-Info "Dirección completa: $($getByIdResponse.fullAddress)"
    Write-Info "Propiedades totales: $($getByIdResponse.totalProperties)"
    Write-Info "Propiedades disponibles: $($getByIdResponse.availableProperties)"
} catch {
    Write-Error "Error obteniendo ubicación: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 7. BUSCAR POR CIUDAD
# ============================================
Write-Test "7. GET BY CITY - Buscar ubicaciones por ciudad"

try {
    $cityLocations = Invoke-RestMethod -Uri "$locationsUrl/city/Toluca" `
        -Method GET `
        -Headers $headers

    Write-Success "Ubicaciones en Toluca: $($cityLocations.Count)"
    foreach ($loc in $cityLocations) {
        Write-Info "  - $($loc.name)"
    }
} catch {
    Write-Error "Error buscando por ciudad: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 8. ACTUALIZAR LOCATION
# ============================================
Write-Test "8. UPDATE - Actualizar ubicación"

$updateBody = @{
    description = "Zona residencial actualizada con nuevos servicios"
    postalCode = "50001"
} | ConvertTo-Json

try {
    $updateResponse = Invoke-RestMethod -Uri "$locationsUrl/$locationId" `
        -Method PUT `
        -Headers $headers `
        -Body $updateBody

    Write-Success "Ubicación actualizada"
    Write-Info "Nueva descripción: $($updateResponse.description)"
    Write-Info "Nuevo CP: $($updateResponse.postalCode)"
} catch {
    Write-Error "Error actualizando ubicación: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 9. ACTUALIZAR CON NOMBRE DUPLICADO
# ============================================
Write-Test "9. UPDATE - Intentar actualizar con nombre duplicado (debe fallar)"

$updateDuplicateBody = @{
    name = "Colonia Centro"
} | ConvertTo-Json

try {
    $updateDupResponse = Invoke-RestMethod -Uri "$locationsUrl/$locationId" `
        -Method PUT `
        -Headers $headers `
        -Body $updateDuplicateBody

    Write-Error "No debería permitir nombre duplicado"
} catch {
    Write-Success "Validación correcta - No permite actualizar a nombre existente"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 10. DESACTIVAR LOCATION
# ============================================
Write-Test "10. UPDATE - Desactivar ubicación"

$deactivateBody = @{
    isActive = $false
} | ConvertTo-Json

try {
    $deactivateResponse = Invoke-RestMethod -Uri "$locationsUrl/$locationId2" `
        -Method PUT `
        -Headers $headers `
        -Body $deactivateBody

    Write-Success "Ubicación desactivada"
    Write-Info "Estado activo: $($deactivateResponse.isActive)"
} catch {
    Write-Error "Error desactivando ubicación: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 11. LISTAR CON INACTIVAS
# ============================================
Write-Test "11. GET ALL - Listar incluyendo inactivas"

try {
    $allWithInactive = Invoke-RestMethod -Uri "$($locationsUrl)?includeInactive=true" `
        -Method GET `
        -Headers $headers

    Write-Success "Todas las ubicaciones (incluye inactivas): $($allWithInactive.Count)"
    foreach ($loc in $allWithInactive) {
        $status = if ($loc.isActive) { "Activa" } else { "Inactiva" }
        Write-Info "  - $($loc.name) [$status]"
    }
} catch {
    Write-Error "Error obteniendo ubicaciones: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 12. DELETE (SOFT DELETE)
# ============================================
Write-Test "12. DELETE - Eliminar ubicación (soft delete)"

try {
    Invoke-RestMethod -Uri "$locationsUrl/$locationId" `
        -Method DELETE `
        -Headers $headers

    Write-Success "Ubicación eliminada (soft delete)"
} catch {
    Write-Error "Error eliminando ubicación: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 13. VERIFICAR ELIMINACIÓN
# ============================================
Write-Test "13. GET - Verificar que la ubicación fue desactivada"

try {
    $deletedLocation = Invoke-RestMethod -Uri "$locationsUrl/$locationId" `
        -Method GET `
        -Headers $headers

    if (-not $deletedLocation.isActive) {
        Write-Success "Confirmado - La ubicación está desactivada"
    } else {
        Write-Error "La ubicación sigue activa"
    }
} catch {
    Write-Error "Error verificando eliminación: $_"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 14. PROBAR SIN AUTORIZACIÓN
# ============================================
Write-Test "14. SECURITY - Probar acceso sin token"

try {
    $noAuthResponse = Invoke-RestMethod -Uri $locationsUrl `
        -Method GET

    Write-Error "No debería permitir acceso sin autorización"
} catch {
    Write-Success "Seguridad correcta - Requiere autorización"
}

Write-Host ""
Start-Sleep -Seconds 1

# ============================================
# 15. PROBAR CON ROL USER (NO ADMIN)
# ============================================
Write-Test "15. SECURITY - Crear usuario normal y probar restricciones"

# Registrar usuario normal
$registerBody = @{
    email = "user.test@rentas.com"
    password = "User123!"
    fullName = "Usuario Prueba"
    phone = "5551234567"
    role = "USER"
} | ConvertTo-Json

try {
    $registerResponse = Invoke-RestMethod -Uri "$authUrl/register" `
        -Method POST `
        -ContentType "application/json" `
        -Body $registerBody

    $userToken = $registerResponse.token
    Write-Success "Usuario normal registrado"

    # Intentar crear ubicación con rol USER
    $userHeaders = @{
        "Authorization" = "Bearer $userToken"
        "Content-Type" = "application/json"
    }

    $testBody = @{
        name = "Test Location"
        city = "Test City"
    } | ConvertTo-Json

    try {
        Invoke-RestMethod -Uri $locationsUrl `
            -Method POST `
            -Headers $userHeaders `
            -Body $testBody

        Write-Error "No debería permitir crear con rol USER"
    } catch {
        Write-Success "Autorización correcta - Solo ADMIN puede crear"
    }
} catch {
    Write-Info "Usuario ya existe o error en registro"
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Magenta
Write-Host "        PRUEBAS COMPLETADAS                " -ForegroundColor Magenta
Write-Host "============================================" -ForegroundColor Magenta
Write-Host ""
Write-Success "Todas las pruebas de Location ejecutadas"
Write-Info "Revisa los logs del servidor para más detalles"