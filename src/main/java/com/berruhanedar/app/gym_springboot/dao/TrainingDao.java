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
        return entityManager.createQuery(
                        "SELECT t FROM Training t", Training.class)
                .getResultList();
    }

    public void delete(Training training) {
        entityManager.remove(training);
    }

    public List<Training> findByTraineeUsernameAndCriteria(
            String traineeUsername,
            LocalDate fromDate,
            LocalDate toDate,
            String trainerName,
            String trainingType
    ) {
        StringBuilder jpql = new StringBuilder("""
            SELECT tr
            FROM Training tr
            WHERE tr.trainee.username = :traineeUsername
            """);

        if (fromDate != null) {
            jpql.append(" AND tr.trainingDate >= :fromDate ");
        }

        if (toDate != null) {
            jpql.append(" AND tr.trainingDate <= :toDate ");
        }

        if (trainerName != null && !trainerName.isBlank()) {
            jpql.append("""
                 AND LOWER(CONCAT(tr.trainer.firstName, ' ', tr.trainer.lastName))
                LIKE LOWER(:trainerName)
                """);
        }

        if (trainingType != null && !trainingType.isBlank()) {
            jpql.append("""
                 AND LOWER(tr.trainingType.trainingTypeName) = LOWER(:trainingType)
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
            query.setParameter("trainerName", "%" + trainerName + "%");
        }

        if (trainingType != null && !trainingType.isBlank()) {
            query.setParameter("trainingType", trainingType);
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
            SELECT tr
            FROM Training tr
            WHERE tr.trainer.username = :trainerUsername
            """);

        if (fromDate != null) {
            jpql.append(" AND tr.trainingDate >= :fromDate ");
        }

        if (toDate != null) {
            jpql.append(" AND tr.trainingDate <= :toDate ");
        }

        if (traineeName != null && !traineeName.isBlank()) {
            jpql.append("""
                 AND LOWER(CONCAT(tr.trainee.firstName, ' ', tr.trainee.lastName))
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
            query.setParameter("traineeName", "%" + traineeName + "%");
        }

        return query.getResultList();
    }
}