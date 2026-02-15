package it.unipi.CarRev.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;

import java.util.HashSet;
import java.util.Set;

/*this class will keep the information useful to the connection at redis db*/
public class RedisConfig {
/*
    public static final String REDISHOST="localhost";
    public static final Integer REDISPORT=6379;
 */
    /*this create a single jedisPool for our application*/
    /*
    private static final JedisPool pool=new JedisPool(REDISHOST,REDISPORT);

    public static Jedis getJedis(){
        return pool.getResource();
    }
     */
    private static final String MASTERNAME="mymaster";
    private static final Set<String> SENTINELS;
    static {
        SENTINELS = new HashSet<>();
        SENTINELS.add("10.1.1.89:26379");
        SENTINELS.add("10.1.1.88:26379");
        SENTINELS.add("10.1.1.87:26379");
    }
    private static final String PASSWORD="root";
    private static final JedisSentinelPool pool;
    static {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        pool = new JedisSentinelPool(MASTERNAME, SENTINELS, poolConfig, PASSWORD);
    }
    public static Jedis getJedis(){
        return pool.getResource();
    }

}
