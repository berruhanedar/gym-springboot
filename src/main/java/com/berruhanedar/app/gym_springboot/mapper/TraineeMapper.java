package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    TraineeResponseDTO toDTO(Trainee trainee);

    RegistrationResponseDTO toRegistrationResponseDTO(Trainee trainee);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    Trainee toEntity(NewTraineeRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "trainers", ignore = true)
    @Mapping(target = "trainings", ignore = true)
    void updateFromDTO(UpdateTraineeRequestDTO dto, @MappingTarget Trainee trainee);
}