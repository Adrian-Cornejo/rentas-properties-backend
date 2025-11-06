package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio para la entidad User
 * Gestiona usuarios del sistema (administradores y familia)
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /**
     * Buscar usuario por email
     * Usado para login y validación de unicidad
     */
    Optional<User> findByEmail(String email);

    /**
     * Buscar usuario por email ignorando mayúsculas/minúsculas
     */
    Optional<User> findByEmailIgnoreCase(String email);

    /**
     * Verificar si existe un email
     */
    boolean existsByEmail(String email);

    /**
     * Verificar si existe un email ignorando mayúsculas/minúsculas
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Buscar todos los usuarios activos
     */
    List<User> findByIsActiveTrue();

    /**
     * Buscar todos los usuarios inactivos
     */
    List<User> findByIsActiveFalse();

    /**
     * Buscar usuarios por rol
     */
    List<User> findByRole(String role);

    /**
     * Buscar usuarios activos por rol
     */
    List<User> findByRoleAndIsActiveTrue(String role);

    /**
     * Buscar todos los administradores activos
     */
    @Query("SELECT u FROM User u WHERE u.role = 'ADMIN' AND u.isActive = true")
    List<User> findActiveAdmins();

    /**
     * Buscar usuarios por nombre (búsqueda parcial)
     */
    List<User> findByFullNameContainingIgnoreCase(String name);

    /**
     * Buscar usuarios que hicieron login después de una fecha
     */
    List<User> findByLastLoginAfter(LocalDateTime date);

    /**
     * Buscar usuarios que nunca han hecho login
     */
    List<User> findByLastLoginIsNull();

    /**
     * Contar usuarios por rol
     */
    long countByRole(String role);

    /**
     * Contar usuarios activos
     */
    long countByIsActiveTrue();

    /**
     * Buscar usuario por email y verificar que esté activo
     */
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    /**
     * Obtener todos los usuarios ordenados por fecha de creación (más recientes primero)
     */
    List<User> findAllByOrderByCreatedAtDesc();

    /**
     * Actualizar último login
     */
    @Query("UPDATE User u SET u.lastLogin = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") LocalDateTime loginTime);
}