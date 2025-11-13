package com.stater.nova.redis.util;


import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 分布式锁工具类
 *
 * @author tql
 * @date: 2025/11/5
 * @time: 18:52
 * @desc:
 */
@Component
public class DistributeLockUtil {

    @Autowired
    private RedissonClient redissonClient;
}
