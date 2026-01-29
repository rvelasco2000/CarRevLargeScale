package it.unipi.CarRev.mapper;


import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Review;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.data.mongodb.core.mapping.Document;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void mapReviewFromDto(Document document, @MappingTarget Review review);
}
