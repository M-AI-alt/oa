package cn.exrick.xboot.common.lock;

/**
 * @author 宁飞
 */
public interface Callback {

    /**
     * 成功获取锁后执行方法
     * @return
     * @throws InterruptedException
     */
    Object onGetLock() throws InterruptedException;

    /**
     * 获取锁超时回调
     * @return
     * @throws InterruptedException
     */
    Object onTimeout() throws InterruptedException;
}
