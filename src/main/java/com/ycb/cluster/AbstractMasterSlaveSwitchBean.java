package com.ycb.cluster;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
public abstract class AbstractMasterSlaveSwitchBean implements ApplicationContextAware, MasterSlaveSwitchBean {
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        MasterSlaveSwitchBean.supportMasterSlave(applicationContext, this);
    }
}
