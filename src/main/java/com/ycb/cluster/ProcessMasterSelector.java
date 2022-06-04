package com.ycb.cluster;

import com.ycb.constant.SystemConstant;
import com.ycb.lock.ProcessLock;
import com.ycb.lock.ProcessLockFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author yanchuanbin
 * @Description:
 * @date 2022/6/4
 */
public class ProcessMasterSelector {

    private static final Logger log = LoggerFactory.getLogger(ProcessMasterSelector.class);


    private static final long TIME_OUT_SECOND = 10L;

    /**
     * 当前主机的状态
     */
    private volatile boolean master = false;

    /**
     * 命名空间
     */
    private String namespace;

    private ProcessLock processLock;

    private RedissonClient redissonClient;


    /**
     * 主从切换事件的执行线程
     */
    private ExecutorService executorService;

    /**
     * 主从状态的检查线程
     */
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 用于储存当前命名空间中注册上来的masterSlaveSwitchBean和其状态信息
     */
    private List<MasterSlaveSwitchBeanDecorator> masterSlaveSwitchBeanDecoratorList = new CopyOnWriteArrayList<>();


    /**
     * 构造方法
     *
     * @param namespace      命名空间
     * @param redissonClient redis操作
     */
    public ProcessMasterSelector(String namespace, RedissonClient redissonClient) {
        this.namespace = namespace;
        this.redissonClient = redissonClient;
        this.init();
    }

    /**
     * 初始化
     */
    private void init() {
        processLock = new ProcessLockFactory(redissonClient).getLock(namespace);

        //主从发生切换后的执行线程
        BasicThreadFactory threadFactory = new BasicThreadFactory.Builder().namingPattern(namespace + "-event").build();
        executorService = Executors.newSingleThreadExecutor(threadFactory);
        keepHeartbeat();

        //定时检查主从状态的线程
        BasicThreadFactory basicThreadFactory = new BasicThreadFactory.Builder().namingPattern(namespace + "-selector").build();
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1, basicThreadFactory);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            keepHeartbeat();
        }, 1, TIME_OUT_SECOND, TimeUnit.SECONDS);
        log.info("Namespace[{}]: master selector init successful", namespace);
    }


    /**
     * 保持心跳的具体方法
     */
    private void keepHeartbeat() {
        boolean target = processLock.tryLock(TIME_OUT_SECOND + 5, TimeUnit.SECONDS);
        boolean originStatus = master;
        master = target;
        traceEvent(originStatus, target);
    }


    /**
     * 跟踪事件，主要是作为日志输出
     *
     * @param origin 之前的状态
     * @param target 切换过后的状态，有可能与last一致，则代表未切换
     */
    private void traceEvent(boolean origin, boolean target) {
        if (!origin && target) {
            log.info("Namespace[{}]: become master node................", namespace);
        }
        log.info("Namespace[{}]: NODE_ID[{}] status[{}]", namespace, SystemConstant.NODE_ID, getMasterStatus());
        executorService.submit(() -> handleEvent(origin, target));
    }

    /**
     * 具体的执行事件
     *
     * @param origin
     * @param target
     */
    private void handleEvent(boolean origin, boolean target) {
        Iterator<MasterSlaveSwitchBeanDecorator> iterator = masterSlaveSwitchBeanDecoratorList.iterator();
        while (iterator.hasNext()) {
            MasterSlaveSwitchBeanDecorator masterSlaveSwitchBean = iterator.next();
            try {

                if (target) {
                    //当为master状态时，如果未被初始化，则先进行初始化
                    if (!masterSlaveSwitchBean.isInited()) {
                        masterSlaveSwitchBean.init();
                    }
                    //运行turnMaster事件
                    masterSlaveSwitchBean.turnMaster();
                } else {
                    masterSlaveSwitchBean.turnSlave();
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * 注册上来
     *
     * @param masterSlaveSwitchBeanDecorator
     */
    public void register(MasterSlaveSwitchBeanDecorator masterSlaveSwitchBeanDecorator) {
        this.masterSlaveSwitchBeanDecoratorList.add(masterSlaveSwitchBeanDecorator);
    }


    /**
     * 获取当前节点的master状态
     *
     * @return master or slave
     */
    private String getMasterStatus() {
        return this.master ? "master" : "slave";
    }


    /**
     * 判断当前节点是否为master
     *
     * @return
     */
    public boolean isMaster() {
        return this.master;
    }


}
