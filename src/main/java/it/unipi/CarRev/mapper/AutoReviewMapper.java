package it.unipi.CarRev.mapper;

import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.dto.ReviewUpdateRequestDTO;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.model.Review;
import org.mapstruct.*;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AutoReviewMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateReviewFromDto(ReviewUpdateRequestDTO reviewUpdateRequestDTO, @MappingTarget Review review);
}
