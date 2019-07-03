package com.hallo.peach.ioc.test;

import com.hallo.peach.ioc.annotation.PAutowired;
import com.hallo.peach.ioc.annotation.PElement;

@PElement(name = "innerService")
public class InnerServiceImpl implements InnerService{

    @PAutowired
    UserService userService;

    public void add() {
        System.out.println(userService.add(""));
    }
}
