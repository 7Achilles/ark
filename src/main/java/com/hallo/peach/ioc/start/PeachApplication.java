package com.hallo.peach.ioc.start;


import com.hallo.peach.ioc.annotation.EnablePeach;
import com.hallo.peach.ioc.core.PeachApplicationContext;

/**
 * 启动类
 *
 */
public class PeachApplication {

    /**
     * 单例模式
     */
    private static PeachApplication instance;

    private PeachApplication (Class<?> clazz){

        EnablePeach enablePeach =  clazz.getAnnotation(EnablePeach.class);

        if(enablePeach != null){

            String packageName = enablePeach.packageName();

            //构建ioc容器
            run(packageName);

        }

    }

    private PeachApplication(){}

    public static PeachApplication getInstance(Class<?> clazz) {

        if (instance == null) {

            synchronized (PeachApplication.class) {
                if (instance == null) {

                    instance = new PeachApplication(clazz);

                }
            }
        }
        return instance;
    }

    private void run(String packageName){

        PeachApplicationContext.getInstance(packageName);


    }


}
