package com.ycb.lock;

import org.redisson.api.RedissonClient;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
public class ProcessLockFactory {

    private RedissonClient redissonClient;

    /**
     * 构建方法
     *
     * @param redissonClient
     */
    public ProcessLockFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 获取锁
     *
     * @param lockName
     * @return
     */
    public ProcessLock getLock(String lockName) {
        return new ProcessLock(redissonClient, lockName);
    }
}
