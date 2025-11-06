package com.rentas.properties.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de QueryDSL para Type-Safe Queries
 *
 * Proporciona el JPAQueryFactory para crear queries type-safe
 * sin usar strings de JPQL o SQL
 */
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Bean de JPAQueryFactory para inyectar en repositorios y servicios
     *
     * Uso:
     * <pre>
     * {@code
     * @Autowired
     * private JPAQueryFactory queryFactory;
     *
     * QProperty property = QProperty.property;
     * List<Property> results = queryFactory
     *     .selectFrom(property)
     *     .where(property.status.eq("DISPONIBLE"))
     *     .fetch();
     * }
     * </pre>
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}