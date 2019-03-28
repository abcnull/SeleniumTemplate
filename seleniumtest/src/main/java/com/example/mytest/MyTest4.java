package com.example.mytest;

import com.example.base.SimpleInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by lei on 2019/1/15.
 */
public class MyTest4  extends SimpleInitiator {
    private static final Logger log = LoggerFactory.getLogger(MyTest4.class);

    @Test
    public void test(){
        System.out.println("********** 第四个测试用例执行 *********");
    }
}
