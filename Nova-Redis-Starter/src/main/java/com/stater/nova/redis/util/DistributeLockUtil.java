package com.stater.nova.redis.util;


import cn.hutool.core.util.BooleanUtil;
import com.stater.nova.redis.model.DistributedTaskDTO;
import jodd.util.StringUtil;
import jodd.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 分布式锁工具类
 *
 * @author tql
 * @date: 2025/11/5
 * @time: 18:52
 * @desc:
 */
@Component
@Slf4j
public class DistributeLockUtil {

    @Autowired
    private RedissonClient redissonClient;

    public RLock getLock(String lockKey) {
        return redissonClient.getLock(lockKey);
    }

    public <T, R> R execute(T param, DistributedTaskDTO distributedTask, Function<T, R> business) {
        return execute(param, distributedTask, 0, 0L, business);
    }

    public <T, R> R execute(T param, DistributedTaskDTO distributedTask, Integer retryTimes, Long retryStepMills, Function<T, R> business) {
        RLock lock = redissonClient.getLock(distributedTask.getTaskId());
        tryLock(lock, distributedTask, retryTimes, retryStepMills);
        try {
            return business.apply(param);
        } catch (Exception e) {
            log.warn("分布式任务执行失败", e);
            throw e;
        } finally {
            unlock(distributedTask, lock);
        }
    }

    public <R> R executeWithResult(DistributedTaskDTO distributedTask, Supplier<R> business) {
        return executeWithResult(distributedTask, 0, 0L, business);
    }

    public <R> R executeWithResult(DistributedTaskDTO distributedTask, Integer retryTimes, Long retryStepMills, Supplier<R> business) {
        RLock lock = redissonClient.getLock(distributedTask.getTaskId());
        tryLock(lock, distributedTask, retryTimes, retryStepMills);
        try {
            return business.get();
        } catch (Exception e) {
            log.warn("分布式任务执行失败", e);
            throw e;
        } finally {
            unlock(distributedTask, lock);
        }
    }

    public void execute(String key, Runnable business) {
        execute(DistributedTaskDTO.builder().taskId(key).taskName(key).build(), business);
    }

    public void tryExecute(String key, Runnable business) {
        RLock lock = redissonClient.getLock(key);
        boolean isLocked = lock.tryLock();
        if (!isLocked) {
            log.info("{}-尝试获取锁失败，放弃执行，Thread:{}", key, Thread.currentThread().getName());
            return;
        }
        log.info("{}-尝试获取锁成功", key);
        try {
            business.run();
        } finally {
            lock.unlock();
        }
    }

    public void executeSilently(String key, Runnable business) {
        DistributedTaskDTO distributedTask = DistributedTaskDTO.builder()
                .taskId(key)
                .taskName(key)
                .notPrintErrorLog(true)
                .build();
        execute(distributedTask, business);
    }

    public void execute(DistributedTaskDTO distributedTask, Runnable business) {
        execute(distributedTask, 0, 0L, business);
    }

    public void execute(DistributedTaskDTO distributedTask, Integer retryTimes, Long retryStepMills, Runnable business) {
        RLock lock = redissonClient.getLock(distributedTask.getTaskId());
        tryLock(lock, distributedTask, retryTimes, retryStepMills);
        try {
            business.run();
        } finally {
            unlock(distributedTask, lock);
        }
    }

    /**
     * @param lock
     * @param retryTimes
     * @param retryStepMills
     * @return
     */
    private void tryLock(RLock lock, DistributedTaskDTO distributedTask, Integer retryTimes, Long retryStepMills) {
        boolean isLocked;
        do {
            isLocked = lock.tryLock();
            if (!isLocked) {
                retryTimes--;
                ThreadUtil.sleep(retryStepMills);
                retryStepMills += retryStepMills;
            } else {
                log.info("加锁成功:{}", distributedTask.getTaskName());
                break;
            }
        } while (retryTimes > 0);

        if (!isLocked) {
            if (!BooleanUtil.isTrue(distributedTask.getNotPrintErrorLog())) {
                log.warn("{}还在执行中，无法获取分布式锁,Thread:{}", distributedTask.getTaskName(),
                        Thread.currentThread().getName());
            }
            throw new RuntimeException(StringUtil.isBlank(distributedTask.getErrorMsgCode()) ? "common.warn.operatorBusy" : distributedTask.getErrorMsgCode());
        }
    }

    private void unlock(DistributedTaskDTO distributedTask, RLock lock) {
        lock.unlock();
        log.info("{}执行完毕，释放分布式锁, Thread:{}", distributedTask.getTaskName(), Thread.currentThread().getName());
    }

}
