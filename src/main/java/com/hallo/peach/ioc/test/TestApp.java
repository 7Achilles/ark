package com.hallo.peach.ioc.test;

import com.hallo.peach.ioc.core.PeachApplicationContext;

public class TestApp {

    public static void main(String[] args) throws Exception {
        PeachApplicationContext app = new PeachApplicationContext("com.hallo.peach.ioc");
        InnerService innerService = (InnerService) app.getBean("innerService");
        innerService.add();
    }

}
