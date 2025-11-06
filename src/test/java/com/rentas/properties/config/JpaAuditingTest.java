//package com.rentas.properties.config;
//
//import com.rentas.properties.dao.entity.Location;
//import com.rentas.properties.dao.repository.LocationRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Test de integración para verificar que JPA Auditing funciona correctamente
// *
// * Este test verifica que:
// * - Los campos @CreatedDate se llenan automáticamente
// * - Los campos @LastModifiedDate se actualizan automáticamente
// * - Los campos @CreatedBy se llenan con el usuario actual
// *
// * NOTA: Ejecuta este test después de configurar JpaAuditingConfig y AuditorAwareImpl
// */
//@SpringBootTest
//@ActiveProfiles("test") // Usa application-test.properties si existe
//@Transactional // Rollback automático después de cada test
//class JpaAuditingTest {
//
//    @Autowired
//    private LocationRepository locationRepository;
//
//    @Test
//    void testCreatedDateIsSetAutomatically() {
//        // Arrange
//        Location location = Location.builder()
//                .name("Test Location")
//                .city("Test City")
//                .isActive(true)
//                .build();
//
//        // Act
//        Location saved = locationRepository.save(location);
//
//        // Assert
//        assertNotNull(saved.getCreatedAt(), "createdAt debe ser llenado automáticamente");
//        assertTrue(saved.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)),
//                "createdAt debe ser una fecha reciente");
//
//        System.out.println("✅ createdAt: " + saved.getCreatedAt());
//    }
//
//    @Test
//    void testUpdatedDateIsSetAutomatically() {
//        // Arrange
//        Location location = Location.builder()
//                .name("Test Location")
//                .city("Test City")
//                .isActive(true)
//                .build();
//
//        // Act
//        Location saved = locationRepository.save(location);
//
//        // Assert
//        assertNotNull(saved.getUpdatedAt(), "updatedAt debe ser llenado automáticamente");
//        assertEquals(saved.getCreatedAt(), saved.getUpdatedAt(),
//                "updatedAt debe ser igual a createdAt en el primer guardado");
//
//        System.out.println("✅ updatedAt: " + saved.getUpdatedAt());
//    }
//
//    @Test
//    void testCreatedByIsSetAutomatically() {
//        // Arrange
//        Location location = Location.builder()
//                .name("Test Location")
//                .city("Test City")
//                .isActive(true)
//                .build();
//
//        // Act
//        Location saved = locationRepository.save(location);
//
//        // Assert
//        assertNotNull(saved.getCreatedBy(), "createdBy debe ser llenado automáticamente");
//
//        System.out.println("✅ createdBy: " + saved.getCreatedBy());
//    }
//
//    @Test
//    void testUpdatedDateIsUpdatedOnModification() throws InterruptedException {
//        // Arrange - Crear entidad
//        Location location = Location.builder()
//                .name("Test Location")
//                .city("Test City")
//                .isActive(true)
//                .build();
//        Location saved = locationRepository.save(location);
//
//        LocalDateTime originalCreatedAt = saved.getCreatedAt();
//        LocalDateTime originalUpdatedAt = saved.getUpdatedAt();
//
//        // Esperar un momento para que la fecha sea diferente
//        Thread.sleep(100);
//
//        // Act - Modificar entidad
//        saved.setCity("Updated City");
//        Location updated = locationRepository.save(saved);
//
//        // Assert
//        assertEquals(originalCreatedAt, updated.getCreatedAt(),
//                "createdAt NO debe cambiar en actualizaciones");
//        assertTrue(updated.getUpdatedAt().isAfter(originalUpdatedAt),
//                "updatedAt DEBE actualizarse en modificaciones");
//
//        System.out.println("✅ createdAt (sin cambios): " + updated.getCreatedAt());
//        System.out.println("✅ updatedAt (actualizado): " + updated.getUpdatedAt());
//    }
//
//    @Test
//    void testAllAuditFieldsWorkTogether() {
//        // Arrange
//        Location location = Location.builder()
//                .name("Complete Test Location")
//                .address("Test Address 123")
//                .city("Test City")
//                .state("Test State")
//                .postalCode("12345")
//                .isActive(true)
//                .build();
//
//        // Act
//        Location saved = locationRepository.save(location);
//
//        // Assert - Verificar que todos los campos de auditoría estén presentes
//        assertNotNull(saved.getId(), "ID debe ser generado");
//        assertNotNull(saved.getCreatedAt(), "createdAt debe estar presente");
//        assertNotNull(saved.getUpdatedAt(), "updatedAt debe estar presente");
//        assertNotNull(saved.getCreatedBy(), "createdBy debe estar presente");
//
//        // Imprimir resumen
//        System.out.println("\n========================================");
//        System.out.println("✅ AUDITORÍA JPA FUNCIONANDO CORRECTAMENTE");
//        System.out.println("========================================");
//        System.out.println("ID:         " + saved.getId());
//        System.out.println("Created At: " + saved.getCreatedAt());
//        System.out.println("Updated At: " + saved.getUpdatedAt());
//        System.out.println("Created By: " + saved.getCreatedBy());
//        System.out.println("========================================\n");
//    }
//}