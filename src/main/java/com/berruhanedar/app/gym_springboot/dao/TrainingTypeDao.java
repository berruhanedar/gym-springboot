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
        return entityManager.createQuery(
                        "SELECT t FROM TrainingType t WHERE LOWER(t.trainingTypeName) = LOWER(:name)",
                        TrainingType.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public List<TrainingType> findAll() {
        return entityManager.createQuery(
                        "SELECT t FROM TrainingType t",
                        TrainingType.class)
                .getResultList();
    }
}