package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.NewTrainerRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.RegistrationResponseDTO;
import com.berruhanedar.app.gym_springboot.dto.TrainerResponseDTO;
import com.berruhanedar.app.gym_springboot.dto.UpdateTrainerRequestDTO;
import com.berruhanedar.app.gym_springboot.entity.Trainer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TrainerMapper {

    @Mapping(source = "specialization.trainingTypeName", target = "specializationName")
    TrainerResponseDTO toDTO(Trainer trainer);

    RegistrationResponseDTO toRegistrationResponseDTO(Trainer trainer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainer toEntity(NewTrainerRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "specialization", ignore = true)
    @Mapping(target = "trainees", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    void updateFromDTO(UpdateTrainerRequestDTO dto, @MappingTarget Trainer trainer);
}