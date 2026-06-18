package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.*;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraineeMapper {
    TraineeResponseDTO toDTO(Trainee trainee);

    Trainee toEntity(NewTraineeRequestDTO dto);

    void updateFromDTO(UpdateTraineeRequestDTO dto, @MappingTarget Trainee trainee);
}
