package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    TrainerResponseDTO toDTO(Trainer trainer);

    Trainer toEntity(NewTrainerRequestDTO dto);

    void updateTrainerFromDTO(UpdateTrainerRequestDTO dto, @MappingTarget Trainer trainer);
}