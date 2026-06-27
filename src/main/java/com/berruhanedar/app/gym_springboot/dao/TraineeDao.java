package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.Trainee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Trainee save(Trainee trainee) {
        entityManager.persist(trainee);
        return trainee;
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(entityManager.find(Trainee.class, id));
    }

    public List<Trainee> findAll() {
        return entityManager.createQuery("SELECT t FROM Trainee t", Trainee.class)
                .getResultList();
    }

    public Trainee update(Trainee trainee) {
        return entityManager.merge(trainee);
    }

    public void delete(Trainee trainee) {
        Trainee managedTrainee = entityManager.contains(trainee)
                ? trainee
                : entityManager.merge(trainee);

        entityManager.remove(managedTrainee);
    }

    public Optional<Trainee> findByUsername(String username) {
        return entityManager.createQuery(
                        "SELECT t FROM Trainee t WHERE t.username = :username",
                        Trainee.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public List<String> findAllUsernames() {
        return entityManager.createQuery(
                        "SELECT t.username FROM Trainee t",
                        String.class)
                .getResultList();
    }
}