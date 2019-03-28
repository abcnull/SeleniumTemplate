package com.example.mytest;

import com.example.base.SimpleInitiator;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Created by lei on 2019/1/10.
 */
@Listeners({com.example.base.SimpleInitiator.class})
public class MyTest extends SimpleInitiator {
    private static final Logger log = LoggerFactory.getLogger(MyTest.class);

    /* 一个用于测试的测试用例 */
    @Test
    public void test() throws InterruptedException {
        log.info("********** 第一个测试用例执行 *********");
        log.info("准备延时15s");
//        Thread.sleep(10000);
        log.info("延时15s结束");
        driver.findElement(By.xpath("")).click();
    }
}
