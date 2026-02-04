package it.unipi.CarRev.utils;

import it.unipi.CarRev.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.HashSet;
import java.util.Set;

public class RedisUtils {
    public static Set<String> getSring(String key){
        Set<String> allKeys=new HashSet<>();
        String cursor= ScanParams.SCAN_POINTER_START;
        ScanParams scanParams=new ScanParams().match(key).count(100);
        try(Jedis jedis= RedisConfig.getJedis()){
            while(true){
                ScanResult<String> scanResult=jedis.scan(cursor,scanParams);
                allKeys.addAll(scanResult.getResult());
                cursor=scanResult.getCursor();
                if("0".equals(cursor)){
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error during fetch of the keys "+e.getMessage());
        }
        return allKeys;
    }
}

