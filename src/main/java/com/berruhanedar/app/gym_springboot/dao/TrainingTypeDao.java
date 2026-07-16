package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingTypeDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<TrainingType> findById(Long id) {
        return Optional.ofNullable(entityManager.find(TrainingType.class, id));
    }

    public Optional<TrainingType> findByName(String name) {
        return entityManager.createQuery("""
                        SELECT trainingType
                        FROM TrainingType trainingType
                        WHERE LOWER(trainingType.trainingTypeName) = LOWER(:name)
                        """, TrainingType.class)
                .setParameter("name", name.trim())
                .getResultStream()
                .findFirst();
    }

    public List<TrainingType> findAll() {
        return entityManager.createQuery("""
                        SELECT trainingType
                        FROM TrainingType trainingType
                        """, TrainingType.class)
                .getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM TrainingType e", Long.class)
                .getSingleResult();
    }
}