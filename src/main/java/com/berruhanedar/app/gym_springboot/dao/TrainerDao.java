package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.Trainer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class TrainerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Trainer save(Trainer trainer) {
        entityManager.persist(trainer);
        return trainer;
    }

    public Trainer update(Trainer trainer) {
        return entityManager.merge(trainer);
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainer.class, id));
    }

    public List<Trainer> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainer t", Trainer.class)
                .getResultList();
    }

    public void delete(Trainer trainer) {
        entityManager.remove(trainer);
    }

    public Optional<Trainer> findByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT t FROM Trainer t WHERE t.username = :username",
                        Trainer.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public List<String> findAllUsernames() {
        return entityManager.createQuery(
                        "SELECT t.username FROM Trainer t",
                        String.class)
                .getResultList();
    }

    public boolean existsByUsername(String username) {
        Long count = entityManager.createQuery(
                        "SELECT COUNT(t) FROM Trainer t WHERE t.username = :username",
                        Long.class)
                .setParameter("username", username)
                .getSingleResult();

        return count > 0;
    }

    public List<Trainer> findTrainersNotAssignedToTrainee(String traineeUsername) {
        return entityManager.createQuery("""
                        SELECT trainer
                        FROM Trainer trainer
                        WHERE trainer.id NOT IN (
                            SELECT assignedTrainer.id
                            FROM Trainee trainee
                            JOIN trainee.trainers assignedTrainer
                            WHERE trainee.username = :traineeUsername
                        )
                        """, Trainer.class)
                .setParameter("traineeUsername", traineeUsername)
                .getResultList();
    }

    public List<Trainer> findAllByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        return entityManager.createQuery("""
                        SELECT trainer
                        FROM Trainer trainer
                        WHERE trainer.id IN :ids
                        """, Trainer.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Trainer e", Long.class)
                .getSingleResult();
    }

}