package it.unipi.CarRev.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/*this class will keep the information useful to the connection at redis db*/
public class RedisConfig {

    public static final String REDISHOST="localhost";
    public static final Integer REDISPORT=6379;

    /*this create a single jedisPool for our application*/
    private static final JedisPool pool=new JedisPool(REDISHOST,REDISPORT);

    public static Jedis getJedis(){
        return pool.getResource();
    }


}
