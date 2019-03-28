package com.example.mytest;

import com.example.base.SimpleInitiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by lei on 2019/1/11.
 */
public class MyTest2  extends SimpleInitiator {
    private static final Logger log = LoggerFactory.getLogger(MyTest2.class);

    @Test
    public void test(){
        System.out.println("********** 第二个测试用例执行 *********");
        throw new RuntimeException("抛错测试2");
    }
}
