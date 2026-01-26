package it.unipi.CarRev.service.Impl;


import it.unipi.CarRev.config.RedisConfig;
import it.unipi.CarRev.dao.mongo.CarDAO;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class DeleteACarServiceImpl {
    private final CarDAO carDAO;
    public DeleteACarServiceImpl(CarDAO carDAO){
        this.carDAO=carDAO;
    }

    public int deleteCar(String id){
        try {
            if(!carDAO.existsById(id)){
                System.out.println("car not found");
                return -1;
            }
            carDAO.deleteById(id);
            int result=deleteInRedis(id);
            return result;

        }
        catch(Exception e){
            System.out.println("an error has occurred during the delete of a car");
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
