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
     * return codes:
     *  0  ok
     * -1  car not found in mongo
     * -2  neo4j: car not found by old superkey (hard fail)
     * -3  neo4j: error executing update
     * -4  mongo: error saving after neo4j ok (rollback neo4j attempted)
     * -5  mongo error + neo4j rollback failed (inconsistency)
     */
    public int updateCar(CarUpdateRequestDTO dto) {
        Optional<Car> opt = carDAO.findById(dto.getId());
        if (opt.isEmpty()) return -1;

        Car oldCar = opt.get();

        Car oldSnapshot = cloneForRollback(oldCar);


        Car newSnapshot = cloneForRollback(oldCar);
        carMapper.updateCarFromDto(dto, newSnapshot);

        try {
            // 1) Neo4j FIRST (match su oldSnapshot, set su newSnapshot)
            Neo4jUpdateService.UpdateOutcome neo = neo4jUpdateService.updateCarByOldSuperKey(oldSnapshot, newSnapshot);

            if (neo == Neo4jUpdateService.UpdateOutcome.NOT_FOUND) {
                return -2;
            }
            if (neo == Neo4jUpdateService.UpdateOutcome.ERROR) {
                return -3;
            }

            carMapper.updateCarFromDto(dto, oldCar);
            carDAO.save(oldCar);

            return 0;

        } catch (Exception mongoEx) {
            //  rollback Neo4j
            try {
                Neo4jUpdateService.UpdateOutcome rb = neo4jUpdateService.rollbackToOld(oldSnapshot, newSnapshot);
                if (rb == Neo4jUpdateService.UpdateOutcome.OK) {
                    return -4;
                } else {
                    return -5;
                }
            } catch (Exception rbEx) {
                return -5;
            }
        }
    }

    private Car cloneForRollback(Car c) {
        // Copia "flat" dei soli campi che ti servono per superchiave + set properties.
        // Non copio liste recensioni ecc perché non servono a Neo4j.
        Car copy = new Car();
        copy.setCarName(c.getCarName());
        copy.setCarBrand(c.getCarBrand());
        copy.setCarModel(c.getCarModel());
        copy.setBodyType(c.getBodyType());
        copy.setDriveWheels(c.getDriveWheels());
        copy.setEngineDisplacement(c.getEngineDisplacement());
        copy.setNumberOfCylinders(c.getNumberOfCylinders());
        copy.setTransmissionType(c.getTransmissionType());
        copy.setHorsePower(c.getHorsePower());
        copy.setFuelType(c.getFuelType());
        copy.setSeatCapacity(c.getSeatCapacity());
        copy.setPriceNew(c.getPriceNew());
        copy.setProduction_year(c.getProduction_year());
        return copy;
    }
}
