package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingMapper {
    @Mapping(target = "trainingTypeName", source = "trainingType.trainingTypeName")
    TrainingResponseDTO toDTO(Training training);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trainingType", ignore = true)
    Training toEntity(NewTrainingRequestDTO dto);
}
