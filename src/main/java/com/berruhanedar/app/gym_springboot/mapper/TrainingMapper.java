package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    @Mapping(source = "trainingType.trainingTypeName", target = "trainingTypeName")
    @Mapping(target = "trainerName", expression = "java(training.getTrainer().getFirstName() + \" \" + training.getTrainer().getLastName())")
    @Mapping(target = "traineeName", expression = "java(training.getTrainee().getFirstName() + \" \" + training.getTrainee().getLastName())")
    TrainingResponseDTO toDTO(Training training);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "trainer", ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    Training toEntity(NewTrainingRequestDTO dto);
}