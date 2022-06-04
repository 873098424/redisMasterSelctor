package com.ycb.cluster;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author yanchuanbin
 * @Description: MasterSlaveSwitchBean的包装类
 * @date 2022/6/4
 */
@Slf4j
public class MasterSlaveSwitchBeanDecorator implements MasterSlaveSwitchBean {
    /**
     * 初始化标识
     */
    @Getter
    private boolean inited = false;


    /**
     * 是否已经运行
     */
    @Getter
    private boolean started = false;


    /**
     * 具体的clusterBootstrapBean
     */
    @Getter
    private MasterSlaveSwitchBean masterSlaveSwitchBean;


    public MasterSlaveSwitchBeanDecorator(MasterSlaveSwitchBean masterSlaveSwitchBean) {
        this.masterSlaveSwitchBean = masterSlaveSwitchBean;
    }


    @Override
    public void init() {
        long startTime = System.currentTimeMillis();
        log.info("start handle [{}] init method", masterSlaveSwitchBean.getClass().getSimpleName());
        masterSlaveSwitchBean.init();
        inited = true;
        log.info("end handle [{}] init method,cost time [{}] seconds", masterSlaveSwitchBean.getClass().getSimpleName(), (System.currentTimeMillis() - startTime) / 1000);
    }


    @Override
    public void turnMaster() {
        if (started) {
            return;
        }
        log.info("start handle [{}] turn master method", masterSlaveSwitchBean.getClass().getSimpleName());
        masterSlaveSwitchBean.turnMaster();
        started = true;
    }


    @Override
    public void turnSlave() {
        if (!started) {
            return;
        }
        log.info("start handle [{}] turn slave method", masterSlaveSwitchBean.getClass().getSimpleName());
        masterSlaveSwitchBean.turnSlave();
        started = false;
    }

    @Override
    public String toString() {
        return "MasterSlaveSwitchBeanDecorator{" +
                "beanName=" + masterSlaveSwitchBean.getClass().getSimpleName() +
                ", inited=" + inited +
                ", started=" + started +
                '}';
    }
}
