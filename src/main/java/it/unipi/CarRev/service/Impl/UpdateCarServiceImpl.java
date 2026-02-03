package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.mapper.CarMapper;
import it.unipi.CarRev.model.Car;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateCarServiceImpl{
    private final CarDAO carDAO;
    private final CarMapper carMapper;
    public UpdateCarServiceImpl(CarDAO carDAO,CarMapper carMapper){
        this.carDAO=carDAO;
        this.carMapper=carMapper;
    }
    public int updateCar(CarUpdateRequestDTO carUpdateRequestDTO){
        Optional<Car> car=carDAO.findById(carUpdateRequestDTO.getId());
        if(car.isEmpty()){
            System.out.println("the car does not exists");
            return -1;
        }
        Car updatedCar=car.get();
        try {
            carMapper.updateCarFromDto(carUpdateRequestDTO, updatedCar);
            carDAO.save(updatedCar);
            return 0;
        }
        catch (Exception e){
            System.out.println("Error during the update of a car:"+e.getMessage());
            return -2;
        }

    }
}
