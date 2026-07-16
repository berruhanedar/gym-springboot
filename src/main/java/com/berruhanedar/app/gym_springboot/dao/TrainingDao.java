package com.berruhanedar.app.gym_springboot.dao;

import com.berruhanedar.app.gym_springboot.entity.Training;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
        return entityManager.createQuery("SELECT training FROM Training training", Training.class)
                .getResultList();
    }

    public void delete(Training training) {
        Training managedTraining = entityManager.contains(training)
                ? training
                : entityManager.merge(training);

        entityManager.remove(managedTraining);
    }

    public List<Training> findByTraineeUsernameAndCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT training
                FROM Training training
                WHERE training.trainee.username = :traineeUsername
                """);

        if (fromDate != null) {
            jpql.append(" AND training.trainingDate >= :fromDate ");
        }

        if (toDate != null) {
            jpql.append(" AND training.trainingDate <= :toDate ");
        }

        if (trainerName != null && !trainerName.isBlank()) {
            jpql.append("""
                    AND LOWER(CONCAT(training.trainer.firstName, ' ', training.trainer.lastName))
                    LIKE LOWER(:trainerName)
                    """);
        }

        if (trainingType != null && !trainingType.isBlank()) {
            jpql.append("""
                    AND LOWER(training.trainingType.trainingTypeName) = LOWER(:trainingType)
                    """);
        }

        var query = entityManager.createQuery(jpql.toString(), Training.class);
        query.setParameter("traineeUsername", traineeUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }

        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }

        if (trainerName != null && !trainerName.isBlank()) {
            query.setParameter("trainerName", "%" + trainerName.trim() + "%");
        }

        if (trainingType != null && !trainingType.isBlank()) {
            query.setParameter("trainingType", trainingType.trim());
        }

        return query.getResultList();
    }

    public List<Training> findByTrainerUsernameAndCriteria(
            String trainerUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String traineeName
    ) {
        StringBuilder jpql = new StringBuilder("""
                SELECT training
                FROM Training training
                WHERE training.trainer.username = :trainerUsername
                """);

        if (fromDate != null) {
            jpql.append(" AND training.trainingDate >= :fromDate ");
        }

        if (toDate != null) {
            jpql.append(" AND training.trainingDate <= :toDate ");
        }

        if (traineeName != null && !traineeName.isBlank()) {
            jpql.append("""
                    AND LOWER(CONCAT(training.trainee.firstName, ' ', training.trainee.lastName))
                    LIKE LOWER(:traineeName)
                    """);
        }

        var query = entityManager.createQuery(jpql.toString(), Training.class);
        query.setParameter("trainerUsername", trainerUsername);

        if (fromDate != null) {
            query.setParameter("fromDate", fromDate);
        }

        if (toDate != null) {
            query.setParameter("toDate", toDate);
        }

        if (traineeName != null && !traineeName.isBlank()) {
            query.setParameter("traineeName", "%" + traineeName.trim() + "%");
        }

        return query.getResultList();
    }

    public long count() {
        return entityManager.createQuery("SELECT COUNT(e) FROM Training e", Long.class)
                .getSingleResult();
    }
}