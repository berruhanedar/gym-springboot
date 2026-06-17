package com.berruhanedar.app.gym_springboot.mapper;

import com.berruhanedar.app.gym_springboot.dto.NewTraineeRequestDTO;
import com.berruhanedar.app.gym_springboot.dto.TraineeResponseDTO;
import com.berruhanedar.app.gym_springboot.dto.UpdateTraineeRequestDTO;
import com.berruhanedar.app.gym_springboot.entity.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TraineeMapper {

    TraineeResponseDTO toDTO(Trainee trainee);

    Trainee toEntity(NewTraineeRequestDTO dto);

    void updateTraineeFromDTO(UpdateTraineeRequestDTO dto, @MappingTarget Trainee trainee);
}