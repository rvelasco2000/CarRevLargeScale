package it.unipi.CarRev.mapper;


import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.model.Car;
import org.mapstruct.*;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CarMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateCarFromDto(CarUpdateRequestDTO carUpdateRequestDTO, @MappingTarget Car car);
}
