package com.rentas.properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "Aplicación funcionando con Gradle + Supabase");
        response.put("timestamp", LocalDateTime.now());
        response.put("buildTool", "Gradle");
        return response;
    }

    @GetMapping("/db-connection")
    public Map<String, Object> testDbConnection() {
        Map<String, Object> response = new HashMap<>();

        try {
            if (jdbcTemplate != null) {
                String version = jdbcTemplate.queryForObject(
                        "SELECT version()",
                        String.class
                );
                Integer tableCount = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public'",
                        Integer.class
                );

                String testMessage = jdbcTemplate.queryForObject(
                        "SELECT message FROM test_connection LIMIT 1",
                        String.class
                );

                response.put("status", "CONNECTED");
                response.put("message", "Conexión exitosa a Supabase");
                response.put("postgresVersion", version);
                response.put("tablesCount", tableCount);
                response.put("testMessage", testMessage);
                response.put("host", "db.fkgphpvhderresebpdat.supabase.co");
            } else {
                response.put("status", "ERROR");
                response.put("message", "JdbcTemplate no disponible");
            }
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("message", "Error: " + e.getMessage());
            e.printStackTrace();
        }

        return response;
    }
}