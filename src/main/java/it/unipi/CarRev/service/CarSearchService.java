package it.unipi.CarRev.service;

import it.unipi.CarRev.dto.CarSearchResponse;

public interface CarSearchService {

    CarSearchResponse search(String carName,
                             String carBrand,
                             String carModel,
                             String bodyType,
                             Integer engineDisplacement,
                             Integer numberOfCylinders,
                             int page,
                             int size);
}
