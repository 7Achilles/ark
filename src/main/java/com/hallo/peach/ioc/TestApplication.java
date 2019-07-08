package com.hallo.peach.ioc;

import com.hallo.peach.ioc.annotation.EnablePeach;
import com.hallo.peach.ioc.start.PeachApplication;
import com.hallo.peach.ioc.test.Target;


import java.util.concurrent.ConcurrentHashMap;

import static com.hallo.peach.ioc.core.PeachApplicationContext.getBeans;

@EnablePeach(packageName = "com.hallo.peach.ioc")
public class TestApplication {

    public static void main(String[] args) {

        PeachApplication.getInstance(TestApplication.class);
        ConcurrentHashMap<String, Object> beans = getBeans();
        Target aopTest = (Target)beans.get("aopTest");
        aopTest.point();

    }



}
