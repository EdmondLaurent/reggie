package com.edmond.reggie.common;

public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前用户id
     */
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    /**
     * 获取当前登登录的用户 id
     * @return
     */
    public static Long getCurrentId() {
        return threadLocal.get();
    }
}
