package com.rentas.properties.dao.repository;

import com.rentas.properties.dao.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findByEntityTypeAndEntityId(String entityType, UUID entityId);

    List<Document> findByEntityType(String entityType);

    List<Document> findByDocumentType(String documentType);

    List<Document> findByIsActiveTrue();

    void deleteByEntityTypeAndEntityId(String entityType, UUID entityId);
}