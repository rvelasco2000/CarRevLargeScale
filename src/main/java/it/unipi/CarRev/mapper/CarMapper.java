package it.unipi.CarRev.mapper;


import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.model.Car;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CarMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCarFromDto(CarUpdateRequestDTO carUpdateRequestDTO, @MappingTarget Car car);
}
