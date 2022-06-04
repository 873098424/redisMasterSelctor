package com.ycb.constant;

import java.util.UUID;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
public class SystemConstant {
    /**
     * JVM进程NODE_ID
     */
    public static final String NODE_ID = UUID.randomUUID().toString().replace("-", "");

    /**
     * 默认的主从集群namespace
     */
    public static final String DEFAULT_MASTER_SLAVE_NAMESPACE = "common_master_slave_namespace";
}
