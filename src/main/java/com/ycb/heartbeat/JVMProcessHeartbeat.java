package com.ycb.heartbeat;

import com.ycb.constant.SystemConstant;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
public class JVMProcessHeartbeat {
    private static final Logger log = LoggerFactory.getLogger(JVMProcessHeartbeat.class);
    /**
     * 进程唯一id
     */
    private static final String NODE_ID = SystemConstant.NODE_ID;

    /**
     * 维护心跳的key值格式
     */
    private static final String FORMAT = "jvm_process_%s_heartbeat";

    /**
     * 根据NODE_ID生成当前进程的key
     */
    private static final String HEARTBEAT_KEY = String.format(FORMAT, NODE_ID);

    /**
     * redis操作工具
     */
    private RedissonClient redissonClient;

    /**
     * 定时维持心跳的线程池
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 单例模式设计心跳
     */
    private static JVMProcessHeartbeat jvmProcessHeartbeat;

    /**
     * DCL单例实现
     *
     * @param redissonClient
     * @return
     */
    public static JVMProcessHeartbeat getInstance(RedissonClient redissonClient) {
        if (jvmProcessHeartbeat == null) {
            synchronized (JVMProcessHeartbeat.class) {
                if (jvmProcessHeartbeat == null) {
                    jvmProcessHeartbeat = new JVMProcessHeartbeat(redissonClient);
                }
            }
        }
        return jvmProcessHeartbeat;
    }

    private JVMProcessHeartbeat(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        init();
    }

    /**
     * 初始化心跳维护线程
     */
    private void init() {
        BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder().namingPattern("heartbeat").build();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1, basicThreadFactory);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            RBucket<String> bucket = redissonClient.getBucket(HEARTBEAT_KEY);
            bucket.set(NODE_ID, 30, TimeUnit.SECONDS);
            log.debug("keep heart by redis,node id = [{}]",NODE_ID);
        }, 1, 15, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new Thread(this::stopHeartbeat));
        log.info("JVMProcessHeartbeat init successful");
    }


    /**
     * 停止心跳，关闭线程池
     */
    private void stopHeartbeat() {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }
        log.info("JVMProcessHeartbeat stop!");
    }


    /**
     * 检查指定的节点是否在线
     *
     * @param nodeId
     * @return
     */
    public boolean checkOnline(final String nodeId) {
        String key = String.format(FORMAT, nodeId);
        RBucket<String> bucket = redissonClient.getBucket(key);
        if (bucket != null && bucket.isExists()) {
            return true;
        }
        return false;
    }

}
