package com.ycb.cluster;

import com.ycb.constant.SystemConstant;
import org.springframework.context.ApplicationContext;

import java.util.Optional;

/**
 * @author yanchuanbin
 * @Description: 支持主备切换的bean接口，将其生命周期分为：init、turnMaster、turnSlave
 * @date 2022/6/4
 */
public interface MasterSlaveSwitchBean {

    /**
     * 初始伦
     */
    default void init() {

    }

    /**
     * 切换为master为的触发事件
     */
    default void turnMaster() {

    }

    /**
     * 切换为slave后的触发事件
     */
    default void turnSlave() {

    }

    /**
     * 通过静态方法的方式将其进行注册
     *
     * @param applicationContext
     * @param masterSlaveSwitchBean
     */
    static void supportMasterSlave(ApplicationContext applicationContext, MasterSlaveSwitchBean masterSlaveSwitchBean) {
        ProcessMasterSelector masterSelector = null;
        MasterSlaveNamespace masterSlaveNamespace = masterSlaveSwitchBean.getClass().getAnnotation(MasterSlaveNamespace.class);
        String namespace = Optional.ofNullable(masterSlaveNamespace).map(MasterSlaveNamespace::value).orElse(SystemConstant.DEFAULT_MASTER_SLAVE_NAMESPACE);

        try {
            masterSelector = applicationContext.getBean(namespace, ProcessMasterSelector.class);
        } catch (Exception e) {

        }

        if (masterSelector == null) {
            //如果是非集群状态下，则先init初始化，再运行turnMaster
            masterSlaveSwitchBean.init();
            masterSlaveSwitchBean.turnMaster();
        } else {
            //将其注册上去
            masterSelector.register(new MasterSlaveSwitchBeanDecorator(masterSlaveSwitchBean));
        }
    }

}
