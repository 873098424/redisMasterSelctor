package com.ycb.lock;

import com.ycb.constant.SystemConstant;
import com.ycb.heartbeat.JVMProcessHeartbeat;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
public class ProcessLock {

    /**
     * 当前进程的唯一ID
     */
    private static final String JVM_UNIQUE_KEY = SystemConstant.NODE_ID;

    /**
     * redis操作类
     */
    private RedissonClient redissonClient;

    /**
     * 锁名称
     */
    private String lockName;


    /**
     * 构造方法
     *
     * @param redissonClient
     * @param lockName
     */
    public ProcessLock(RedissonClient redissonClient, String lockName) {
        this.redissonClient = redissonClient;
        this.lockName = lockName;
    }


    /**
     * 用于进程获取锁，如果锁未被占用，则设为自己持有
     * 如果已被占用，则判断是否是当前节点持有，如果是则延长TTL，如果不是，则判断持有锁的节点是否存活，如果不是存活，则通过 cas将其设为自己持有
     * 本锁无需释放，到期自动释放，类似watch dog的功能
     *
     * @param timeToLive 锁存活时间
     * @param timeUnit   时间单位
     * @return
     */
    public boolean tryLock(long timeToLive, TimeUnit timeUnit) {
        RBucket<String> bucket = redissonClient.getBucket(lockName);
        String hasLockedJVM = bucket.get();
        boolean result;
        if (StringUtils.isBlank(hasLockedJVM)) {
            result = bucket.trySet(JVM_UNIQUE_KEY, timeToLive, timeUnit);
        } else {
            if (JVM_UNIQUE_KEY.equals(hasLockedJVM)) {
                //如果本来就是自己持有的话，则续约
                result = bucket.expire(timeToLive, timeUnit);
            } else {
                result = false;
                JVMProcessHeartbeat jvmProcessHeartbeat = JVMProcessHeartbeat.getInstance(redissonClient);
                //检查持有锁的节点是否在线，如果不在线的话，将其改成自己持有，防止进程挂了产生死锁
                if (!jvmProcessHeartbeat.checkOnline(hasLockedJVM)) {
                    result = bucket.compareAndSet(hasLockedJVM, JVM_UNIQUE_KEY);
                    if (result) {
                        bucket.expire(timeToLive, timeUnit);
                    }
                }
            }
        }
        return result;
    }


}
