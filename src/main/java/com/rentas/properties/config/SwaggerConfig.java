package com.rentas.properties.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Rental Properties Management API",
                version = "1.0.0",
                description = """
                        API REST para sistema de gestión de propiedades en renta.
                        
                        ### Características principales:
                        - **Multi-tenant**: Gestión de múltiples organizaciones
                        - **Autenticación JWT**: Seguridad con tokens Bearer
                        - **Roles**: ADMIN, MANAGER, VIEWER
                        - **Gestión completa**: Propiedades, contratos, arrendatarios, pagos, mantenimiento
                        
                        ### Autenticación
                        1. Registra un usuario con `/api/v1/auth/register`
                        2. Inicia sesión con `/api/v1/auth/login` para obtener el token
                        3. Usa el token en el header: `Authorization: Bearer {token}`
                        4. Haz clic en el botón "Authorize" 🔒 arriba para configurar el token
                        
                        ### Flujo básico:
                        1. Crear organización (solo ADMIN)
                        2. Unirse a organización con código de invitación
                        3. Crear ubicaciones y propiedades
                        4. Registrar arrendatarios
                        5. Crear contratos de renta
                        6. Gestionar pagos y mantenimiento
                        """,
                contact = @Contact(
                        name = "Rental Properties Team",
                        email = "support@rentalproperties.com",
                        url = "https://rentalproperties.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Production",
                        url = "https://api.rentalproperties.com"
                )
        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        description = "JWT token de autenticación. Obténlo desde `/api/v1/auth/login`",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
}