package com.hallo.peach.ioc.test;

import com.hallo.peach.ioc.annotation.PAfter;
import com.hallo.peach.ioc.annotation.PAspect;
import com.hallo.peach.ioc.annotation.PBefore;
import com.hallo.peach.ioc.annotation.PPointCut;

@PAspect
public class AopTest{

    @PPointCut(className = "com.hallo.peach.ioc.test.Target",methodName = "point")
    public void pointMethod(){}

    @PBefore(methodName = "point")
    private void before(){
        System.out.println("before");
    }

    @PAfter(methodName = "point")
    private void after(){
        System.out.println("after");
    }


}
