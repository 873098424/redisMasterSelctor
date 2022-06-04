package com.ycb.config;

import com.ycb.cluster.ProcessMasterSelector;
import com.ycb.constant.SystemConstant;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
@Configuration
public class MasterSlaveSelectorConfig {

    /**
     * 这里的bean名称需要与namespace保持一致
     *
     * @return
     */
    @Bean(name = SystemConstant.DEFAULT_MASTER_SLAVE_NAMESPACE)
    public ProcessMasterSelector processMasterSelector(RedissonClient redissonClient) {
        return new ProcessMasterSelector(SystemConstant.DEFAULT_MASTER_SLAVE_NAMESPACE, redissonClient);
    }

}
