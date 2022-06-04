package com.ycb.example;

import com.ycb.cluster.AbstractMasterSlaveSwitchBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
@Component
@Slf4j
public class EmailSender extends AbstractMasterSlaveSwitchBean {

    @Override
    public void init() {
        log.info("init");
    }

    @Override
    public void turnMaster() {
        log.info("turnMaster");

    }

    @Override
    public void turnSlave() {
        log.info("turnSlave");
    }
}
