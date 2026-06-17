package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Training;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TrainingMapper {

    TrainingResponseDTO toDTO(Training training);

    Training toEntity(NewTrainingRequestDTO dto);

    void updateTrainingFromDTO(UpdateTrainingRequestDTO dto, @MappingTarget Training training);
}