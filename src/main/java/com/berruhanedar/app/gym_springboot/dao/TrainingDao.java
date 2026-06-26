package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Training save(Training training) {
        entityManager.persist(training);
        return training;
    }

    public Training update(Training training) {
        return entityManager.merge(training);
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Training.class, id));
    }

    public List<Training> findAll() {
        return entityManager.createQuery(
                        "SELECT t FROM Training t", Training.class)
                .getResultList();
    }

    public void delete(Training training) {
        entityManager.remove(training);
    }
}