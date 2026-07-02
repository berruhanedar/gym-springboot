package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.TrainingTypeResponseDTO;
import com.berruhanedar.app.gym_springboot.entity.TrainingType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TrainingTypeMapper {

    TrainingTypeResponseDTO toDTO(TrainingType trainingType);
}