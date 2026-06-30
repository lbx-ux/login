package com.fjq.login.common.context;


public class BaseContext {

    // 内部使用 Object 来接收任意类型的数据
    private static ThreadLocal<Object> threadLocal = new ThreadLocal<>();

    /**
     * 存入数据
     * @param data 要存入的数据
     * @param <T>  泛型标记
     */
    public static <T> void setCurrent(T data) {
        threadLocal.set(data);
    }

    /**
     * 获取数据
     * @param <T> 泛型标记，通过外层接收时的类型自动推导并强转
     * @return 存入的数据
     */
    @SuppressWarnings("unchecked")
    public static <T> T getCurrent() {
        return (T) threadLocal.get();
    }

    /**
     * 移除数据（极度重要，千万不能忘！）
     */
    public static void removeCurrent() {
        threadLocal.remove();
    }
}