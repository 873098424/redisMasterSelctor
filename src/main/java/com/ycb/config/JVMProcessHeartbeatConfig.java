package com.ycb.config;

import com.ycb.heartbeat.JVMProcessHeartbeat;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
@Configuration
public class JVMProcessHeartbeatConfig {

    /**
     * 将JVMProcessHeartbeat交由spring管理，同时也可以通过单例的方式进行get
     *
     * @param redissonClient
     * @return
     */
    @Bean
    public JVMProcessHeartbeat createJVMProcessHeartbeat(RedissonClient redissonClient) {
        return JVMProcessHeartbeat.getInstance(redissonClient);
    }
}
