package it.unipi.CarRev.service.Impl;

import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.dto.CarUpdateRequestDTO;
import it.unipi.CarRev.mapper.CarMapper;
import it.unipi.CarRev.model.Car;
import it.unipi.CarRev.service.Neo4jUpdateService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateCarServiceImpl {

    private final CarDAO carDAO;
    private final CarMapper carMapper;
    private final Neo4jUpdateService neo4jUpdateService;

    public UpdateCarServiceImpl(CarDAO carDAO, CarMapper carMapper, Neo4jUpdateService neo4jUpdateService) {
        this.carDAO = carDAO;
        this.carMapper = carMapper;
        this.neo4jUpdateService = neo4jUpdateService;
    }

    /**
     * Return codes:
     *  0  ok
     * -1  car not found in mongo
     * -2  mongo save error
     * -3  neo4j update error (mongo already updated)
     */
    public int updateCar(CarUpdateRequestDTO dto) {
        Optional<Car> opt = carDAO.findById(dto.getId());
        if (opt.isEmpty()) return -1;

        Car car = opt.get();


        try {
            carMapper.updateCarFromDto(dto, car);
            carDAO.save(car);
        } catch (Exception e) {
            System.out.println("mongo update failed: " + e.getMessage());
            return -2;
        }


        try {
            neo4jUpdateService.updateCarByMongoId(dto.getId(), car);

        } catch (Exception e) {
            System.out.println("neo4j update failed (mongo already updated): " + e.getMessage());
            return -3;
        }

        return 0;
    }
}