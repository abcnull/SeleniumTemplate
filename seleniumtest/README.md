## seleniumTest

### 一.结构
- .idea
- src
  - main
    - java
    - resource
      - drivers
      - config.properties
      - log4j.properties
      - README.md
      - testNg.xml
  - test
    - java
- test-output
    - html
    - screenshots
    - xml
- pom.xml
- README.md
- seleniumTest.iml

### 二.介绍
    seleniumTest 项目主要分为三大部分：src 用例代码存放处，res 整个项目配置文件存放处和 test-output 项目结果产出处
    
### 三.项目搭建步骤
1.构建 Idea 的 Maven 项目，在依赖包中导入依赖，注意依赖包版本对应和 selenium 所以依赖的 guava 包需要版本对应

    其他依赖包：guava;junit（junit 在 testng 等其他包中也是存在的）;
    testNg依赖：testng(junit);
    reportNg依赖：reportng(testng;);guice;
    selenium依赖：selenium-java(selenium-support;selenium-api;selenium-remote-driver;selenium-chrome-driver;selenium-firefox-driver;selenium-ie-driver;selenium-safari-driver;selenium-edge-driver;selenium-opera-driver;guava)
                  selenium-server;selenium-htmlunit-driver;
    设备驱动依赖：selenium-iphone-driver;selenium-android-driver;
    log4j依赖：slf4j-api;slf4j-log4j12
    
2.pom 文件中写明 plugins 插件相关信息，具体写法见 pom 文件

    包含配置 report 监听，不使用默认 testNg 的报告，其中还指定了 testNg.xml 文件的位置，需要在 testNg.xml 中来配置监听产生报告
    
3.直到上面，pom 中就基本都写好了，然后开始写 testNg.xml，我把它放在 src 下与 java 文件夹同级的 resource 路径下，testNg.xml 可以配置多线程跑

    主要包含两部分，一个是你的自配的监听报告
    另一个就是你要跑的 test 用例，这个用例具体写法下面有，
    自配监听见项目中 testNg.xml，test 用例以<test></test>标签的形式，其中有<parameter>和<classes>，suit可以分组去跑

4.为了让 log 显示出自己需要的形式，并且可以输出 logs 报告，在 resource 路径下配置 log4j.properties，具体内容见项目中文件，之后的输出就是与 src 同级的logs目录（默认）

5.下载各个浏览器的驱动，放在 resource/drivers 路径下，然后在 resource 路径下添加 config.properties

    config.properties 保存有默认驱动名，浏览器名，各个驱动的路径和打开网站的首页，也可以将测试用例中的用户密码写进去，
    因为以后的测试的基类会使用这个文件中的数据，以后万一用户名，首页网址，驱动路径变了，直接改这个配置文件就行了，达到了配置与代码分离的目的
    
6.写测试类的基类 SimpleInitiator，主要是测试之前准备驱动启动浏览器的过程和测试结束后关闭驱动退出浏览器的过程

    在 suit 中每个 test 用例之前都会执行代码，主要分为 3 步：读取 config.properties 文件中的数据，设置系统驱动，开启浏览器（设置隐式等待）
    在 suit 中每个 test 用例之后都会执行代码，主要分为 2 步：退出驱动程序，关闭浏览器
    
7.写几个简单测试用例，标上 @Test 注解，跑一遍 testNg.xml 后，在 Run → Edit Configurations 中可以看到 testNg 项目的一个 testNg.xml
    
    点击进去，其中可以配置 test-output 的输出路径和 logs 的输出路径，
    在 Configurations 一栏中的 Listener 一栏中可以添加 JunitXMLRportor 和 HTMLReportor，实际上不写也可以，因为在 pom 中和 testNg.xml 中写了配置，之后若是不用 reportNg 的报告，使用默认的，勾选使用默认的勾选框即可
    
8.以上步骤就已经完成了 java + idea + testng + selenium + reportng 的构建，现在就已经可以写测试用例了，但目前缺少截图功能，下面讲解

9.截图功能实现

    思想：每个 @Test 方法的类上标上指明 @Listeners注解，注解括号中包含一个需要被监听的类，这个类需继承 TestListenerAdapter 这个监听类，之后在配置一下 testNg.xml 的截图监听，这样就可以报错时候就 new 一个被监听的类，去执行其中的截图方法
    步骤：
    首先，在每个 @Test 的类上写 @Listener 注解，注意括号中带上被监听的类，为了方便，这个被监听的类就是启动驱动的基类，之后在基类中重写监听器类中的方法即可，
    然后，在 testng.xml 中写好监听器配置，
    最后，在 SimpleInitiator 中去 extends TestListenerAdapter，并且重写 onTestFailure 这个方法
    重写内容：
    获取当前测试类的驱动，截图保存在指定路径（循环判断怕重名），将截图附在报告中

### 四.项目中遇到的问题
**1.运行到System.setProperty()和new一个chromeDriver()出现的问题**
```aidl
// 抛出这个异常
java.lang.NoSuchMethodError: com.google.common.base.Preconditions.checkState(ZLjava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
```
```aidl
// 源代码中出错地方
System.setProperty(TestConstants.CHROME_DRIVER_NAME, file.getAbsolutePath());
this.driver = new ChromeDriver();
```
**解决措施：**

    很有可能是 maven 依赖包版本不对
    1.使用 mvn dependency:tree -Dverbose 命令找到所有传递依赖的问题
    2.通常是导入 guava 版本要修正，因为 selenium 的依赖包中可能要配套相应的 guava 版本，若 guava 版本不对应会包这个错误
    为防止 guava 版本被率先被替代，我把 21.0 的 guava 放在 maven 首部，如下：
    <dependency>
        <groupId>com.google.guava</groupId>
        <artifactId>guava</artifactId>
        <version>21.0</version>
    </dependency>
    3.详情请见网页
    https://blog.csdn.net/u011036200/article/details/80383438
    https://stackoverflow.com/questions/44127226/nosuchmethoderror-for-com-google-common-base-preconditions-checkstate

**2.get()网址需要加 http 协议，www打开会报异常**

**3.文件路径不能包含/或\或:，在截图名字中不能包含这样字符串**

**4.截图监听器中获取正在测试的类的对象**
**解决方案：**
```
Object currentClass = result.getInstance();// 获取当前 Object 类型的测试类
WebDriver currentDriver = ( (SimpleInitiator) (currentClass) ).driver;// 获取当前测试类
```

**5.截图有重复名字**
**解决方案：**
```aidl
// 循环防止截图名字一样时被覆盖
for(int i=2;;i++){
    if(!new File(picPath).exists()){ // 若截图不存在跳出循环
        break;
    }
    else{ // 若文件存在则截图名字后面附上数字并进入下次循环
        picPath = "test-output/screenshots/" + result.getTestClass().getRealClass().getSimpleName() + formatDate + i + ".png";
    }
}
```

**6.截图放在报告中**
**解决方案：**
```
// 将截图放置于 report 报告中
System.setProperty("org.uncommons.reportng.escape-output", "false");// 设置此属性防止前端代码输出成 String
Reporter.log("<a href=\"../../" + picPath + "\" target=_blank>Failed Screen Shot</a>", true);// 在 report 报告中输出截图
```

### last.备注
**到目前需要修改文件：**

    1.项目中pom.xml
    2.res中testNg.xml
    3.src中具体的case用例
    4.res中drivers驱动
    5.src中具体的基类-启动浏览器的类
**到目前的需求**

    1.reportNg产出截图报告，实际上只用到testng报告
    2.优化reportNg报告美观（可以解决）
    4.依赖冲突
    5.testng.xml中配置（可以解决）
    6.驱动更新（可以解决）
    7.linux，mac，windows系统
    8.工厂模式启动驱动
    9.开启驱动方法补全
    9.readme结构路径梳理
    10.jenkins远端地址
    11.报告输出的路径（关键）
**到目前疑问**

    1.run→esit configuration中的logs文件路径保存不对
    2.pom.xml文件中report报告产出路径不对
    3.

    
    

