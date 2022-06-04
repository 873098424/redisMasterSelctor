package com.ycb.cluster;

import java.lang.annotation.*;

/**
 * @author yanchuanbin
 * @Description: use for {@link MasterSlaveSwitchBean}
 * @date 2022/6/4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MasterSlaveNamespace {
    String value();
}
