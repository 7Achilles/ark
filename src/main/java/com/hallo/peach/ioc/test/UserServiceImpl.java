package com.hallo.peach.ioc.test;

import com.hallo.peach.ioc.annotation.PElement;

@PElement( name = "userService")
public class UserServiceImpl implements UserService{


    public int add(String a) {
        return 0;
    }


}
