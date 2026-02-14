package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.CarDAO;
import it.unipi.CarRev.model.Car;
import org.springframework.stereotype.Service;
import it.unipi.CarRev.service.Neo4jCarDeleteService;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

@Service
public class DeleteACarServiceImpl {
    private final CarDAO carDAO;
    private final Neo4jCarDeleteService neo4jCarDeleteService ;
    public DeleteACarServiceImpl(CarDAO carDAO,Neo4jCarDeleteService neo4jCarDeleteService){
        this.carDAO=carDAO;
        this.neo4jCarDeleteService = neo4jCarDeleteService;
    }

    @Transactional("mongoTransactionManager")
    public int deleteCar(String id){
        try {
            if(!carDAO.existsById(id)){
                System.out.println("car not found");
                return -1;
            }
            Car oldCar = carDAO.findById(id).orElse(null);
            if (oldCar == null) {
                System.out.println("car not found (race condition)");
                return -1;
            }
            int deleted = neo4jCarDeleteService.deleteCarProjection(oldCar.getId());
            if (deleted != 1) {
                System.out.println("neo4j mismatch on delete: deleted=" + deleted);
                return -2; //
            }
            carDAO.deleteById(id);
            int result=deleteInRedis(id);
            return result;

        }
        catch(Exception e){

            System.out.println("an error has occurred during the delete of a car");
            e.printStackTrace();
            return -2;
        }
    }
    private int deleteInRedis(String id){
        final String CARKEY="Car:"+id+":views";
        try(Jedis jedis= RedisConfig.getJedis()){
            jedis.del(CARKEY);
            return 0;
        }
        catch(Exception e){
            System.out.print("error during the delete of cached views of a car in redis");
            return -2;
        }
    }
}
