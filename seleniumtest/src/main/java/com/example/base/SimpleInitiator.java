package com.example.base;

/**
 * Created by lei on 2019/1/14.
 */

import com.example.constants.TestConstants;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.TestListenerAdapter;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * 目前待优化的几个问题：
 *
 * 1.report文档优化问题（需要修改 reportNg 依赖包的源码，再重新打包） ★
 * 2.服务端跑的问题 ☆
 * 3.驱动可能没下载对应的版本，而且驱动目前没有下全 ★
 * 4.火狐驱动包可能有问题，代码需做小调整，火狐来跑可能不行 ★
 * 5.edge 和 safari 启动浏览器代码补全 ★
 */

/**
 * 此类即将被弃用，因为应该有更好的类可以使用，但是依旧可以使用，因为有更好的方式来开启驱动，此类继承了一个 TestListenerAdapter
 * 拥有初始化浏览器驱动开启浏览器的功能，关闭驱动退出浏览器的功能，拥有监听报错产生截图并且可以把截图附在报告上的功能
 *
 * @author lei
 * @version 1.0
 * @deprecated
 */
public class SimpleInitiator extends TestListenerAdapter {
    // Log & Driver
    private static final Logger log = LoggerFactory.getLogger(SimpleInitiator.class);// Log
    private String browser;                         // 浏览器名称
    protected WebDriver driver;                     // 浏览器驱动（需要被继承类使用）
    protected JavascriptExecutor javascriptExecutor;// 驱动脚本（需要被继承类使用）

    // config.properties 变量数据
    private String defaultBrowserP;                 // 默认浏览器
    private String chromeDriverPathP;               // 谷歌驱动路径
    private String ieDriverPathP;                   // IE 驱动路径
    private String edgeDriverPathP;                 // Edge 驱动路径
    private String firefoxDriverPathP;              // 火狐驱动路径
    private String firefoxBinPathP;                 // 火狐 bin 路径
    private String operaDriverPathP;                // Opera 浏览器路径
    private String safariDriverPathP;               // Safari 驱动路径
    private String phantomjsBinaryPathP;            // PhantomJS binary 路径
    private String urlP;                            // url 网站路径

    /* ===================================== 配置好驱动并开启浏览器 ===================================== */
    /**
     * 初始化驱动开启浏览器主体方法
     * 1.读取properties配置文件
     * 2.设置驱动
     * 3.浏览器窗口最大化
     * 4.打开网址
     * 其中@Optional的意思是testNg.xml没有找到Parameters变量，就是用@Optional的值
     * 其中@Parameters的意思是testNg.xml中变量传递过来
     *
     * @param propertiesPath,browserType,browserVersion,remoteIP
     */
    @BeforeTest(groups = {"SimpleInitiator"})
    @Parameters({"propertiesPath", "browserType", "browserVersion", "remoteIP"})
    public void launch(@Optional("src/main/resources/config.properties")String propertiesPath, @Optional("chrome")String browserType, @Optional()String browserVersion, @Optional()String remoteIP){
        // START
        log.info("=== SELENIUM TEST START ===");

        // 打印出四个参数
        log.info("config.properties 文件路径：" + propertiesPath);
        log.info("测试指定浏览器类型：" + browserType);
        log.info("测试指定浏览器版本：" + browserVersion);
        log.info("测试指定远端地址：" + remoteIP);

        /* 1.读取 config.properties 配置文件信息给类中成员变量 */
        log.info("开始读取 config 配置文件...");
        try {
            this.setPropertiesData(propertiesPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* 2.设置浏览器驱动 */
        // 由于有Optional注解，所以 testNg.xml 中的 browserType 要么不存在（此时有Optional默认值），要么在存在，值可能为""
        log.info("开始设置浏览器驱动...");
        this.browser = browserType;
        // 若 testNg.xml 中的 browserType 值为""
        if(StringUtils.isEmpty(this.browser)){
            this.browser = this.defaultBrowserP;// 此类成员变量赋默认值
        }
        // 设置驱动
        try {
            this.setBrowserDriver(this.browser, browserVersion, remoteIP);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        /* 3.窗口最大化 */
        log.info("开启浏览器窗口最大化...");
        driver.manage().window().maximize();

        /* 4.打开网址首页 */
        log.info("正在打开网站首页...");
        driver.get(urlP);

        // 隐式等待10s（会在设置时间内不断循环去找元素，本质是轮询 DOM，作用域是 WebDriver 实例的生存期，时间默认值是 0）
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
    }

    /**
     * 读取properties配置文件，并保存进类中成员变量中
     *
     * @param propertiesPath
     * @throws IOException
     */
    public void setPropertiesData(String propertiesPath) throws IOException {
        // 新建一个拥有 config.properties 相对项目路径的 File 对象
        File propertiesFile = new File(propertiesPath);
        // 初始化输入流 reader，利用相对项目路径生成的 File 来获取其绝对路径，并且以 utf-8 形式读取 properties 配置文件
        InputStreamReader propertiesReader = new InputStreamReader(new FileInputStream(propertiesFile.getAbsolutePath()), "UTF-8");
        // 定义 Properties 对象 properties
        Properties properties = new Properties();
        // 通过 Properties 类的 load 方法来读取 properties 文件中的变量
        properties.load(propertiesReader);

        // 保存 properties 中的数据给成员变量
        this.defaultBrowserP = properties.getProperty("conf.default_browser");           // 保存默认浏览器
        this.chromeDriverPathP = properties.getProperty("conf.chrome_driver_path");      // 谷歌驱动路径
        this.ieDriverPathP = properties.getProperty("conf.ie_driver_path");              // IE 驱动路径
        this.edgeDriverPathP = properties.getProperty("conf.edge_driver_path");          // Edge 驱动路径
        this.firefoxDriverPathP = properties.getProperty("conf.firefox_driver_path");    // 火狐驱动路径
        this.firefoxBinPathP = properties.getProperty("conf.firefox_bin_path");          // 火狐 bin 路径
        this.operaDriverPathP = properties.getProperty("conf.opera_driver_path");        // 欧朋驱动路径
        this.safariDriverPathP = properties.getProperty("conf.safari_driver_path");      // Safari 驱动路径
        this.phantomjsBinaryPathP = properties.getProperty("conf.phantomjs_binary_path");// PhantomJS binary 路径
        this.urlP = properties.getProperty("conf.base_url");                             // 网站地址
    }

    /**
     * 设置系统浏览器驱动，涉及驱动有：chrome，firefox，IE，Edge，Opera，Safari
     *
     * @param browserType,browserVersion,remoteIP
     */
    public void setBrowserDriver(String browserType, String browserVersion, String remoteIP) throws MalformedURLException {
        // 若是 chrome 驱动
        if(browserType.equals("chrome")) {
            log.info("选择 chrome 驱动...");
            // 若是本地直接跑 @Test 用例，没有提供 remoteIP，就是用 ChromeDriver() 方法
            if( remoteIP == null ) {
                log.info("选择使用本机调试用例...");
                // 谷歌驱动文件
                File file = new File(this.chromeDriverPathP);
                // 拿到驱动文件的绝对路径，设置系统驱动属性
                System.setProperty(TestConstants.CHROME_DRIVER_NAME, file.getAbsolutePath());
                // 指定 chrome 驱动
                this.driver = new ChromeDriver();
                // or 使用 chrome 定置驱动
                /*ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments( "--start-maximized");
                this.driver = new ChromeDriver(chromeOptions);*/
            }
            // 若是远端服务器跑 testNg.xml，会提供 remoteIP
            else{
                log.info("选择使用服务端调试用例...");
                DesiredCapabilities desiredCapabilities = DesiredCapabilities.chrome();// 浏览器驱动
                // DesiredCapabilities desiredCaps = new DesiredCapabilities("chrome", browserVersion, Platform.LINUX);// 浏览器驱动
                this.driver = new RemoteWebDriver(new URL("http://" + remoteIP + ":4444/wd/hub/"), desiredCapabilities);// 驱动使用 RemoteWebDriver
            }
        }
        // 若是火狐驱动
        else if(browserType.equals("firefox")){
            log.info("选择 firefox 驱动...");
            // 若是本地直接跑 @Test 用例，没有提供 remoteIP，就是用 ChromeDriver() 方法
            if( remoteIP == null ) {
                log.info("选择使用本机调试用例...");
                // 火狐驱动文件，目前具体的火狐 bin 还没下下来，所以在 drivers 中没有？？？
                File file1 = new File(this.firefoxDriverPathP);
                File file2 = new File(this.firefoxBinPathP);
                // 拿到驱动文件的绝对路径，设置系统驱动属性
                System.setProperty(TestConstants.FIREFOX_DRIVER_NAME, file1.getAbsolutePath());
                System.setProperty(TestConstants.FIREFOX_BIN_NAME, file2.getAbsolutePath());
                // 指定火狐驱动
                this.driver = new FirefoxDriver();
                // or 使用 firefox 定置驱动
                /*FirefoxOptions firefoxOptions = new FirefoxOptions();
                firefoxOptions.addArguments("--start-maximized");
                this.driver = new FirefoxDriver(firefoxOptions);*/
            }
            // 若是远端服务器跑 testNg.xml，会提供 remoteIP
            else{
                log.info("选择使用服务端调试用例...");
                DesiredCapabilities desiredCapabilities = DesiredCapabilities.firefox();// 浏览器驱动
                // DesiredCapabilities desiredCaps = new DesiredCapabilities("firefox", browserVersion, Platform.LINUX);// 浏览器驱动
                this.driver = new RemoteWebDriver(new URL("http://" + remoteIP + ":4444/wd/hub/"), desiredCapabilities);// 驱动使用 RemoteWebDriver
            }
        }
        // 若是 IE 驱动
        else if(browserType.equals("ie")){
            log.info("选择 ie 驱动...");
            // 若是本地直接跑 @Test 用例，没有提供 remoteIP，就是用 ChromeDriver() 方法
            if( remoteIP == null ) {
                log.info("选择使用本机调试用例...");
                // IE 驱动文件
                File file = new File(this.ieDriverPathP);
                // 拿到驱动文件的绝对路径，设置系统驱动属性
                System.setProperty(TestConstants.IE_DRIVER_NAME, file.getAbsolutePath());
                // 指定 IE 驱动
                this.driver = new InternetExplorerDriver();
                // or 使用 IE 定置驱动
                /*DesiredCapabilities desiredCapabilities = DesiredCapabilities.internetExplorer();
                desiredCapabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
                this.driver = new InternetExplorerDriver(desiredCapabilities);*/
            }
            // 若是远端服务器跑 testNg.xml，会提供 remoteIP
            else{
                log.info("选择使用服务端调试用例...");
                DesiredCapabilities desiredCapabilities = DesiredCapabilities.internetExplorer();// 浏览器驱动
                // DesiredCapabilities desiredCaps = new DesiredCapabilities("internetExplorer", browserVersion, Platform.LINUX);// 浏览器驱动
                this.driver = new RemoteWebDriver(new URL("http://" + remoteIP + ":4444/wd/hub/"), desiredCapabilities);// 驱动使用 RemoteWebDriver
            }
        }
        // 若是 Edge 驱动
        else if(browserType.equals("edge")){
            log.info("选择 edge 驱动...");
            // 若是本地直接跑 @Test 用例，没有提供 remoteIP，就是用 ChromeDriver() 方法
            if( remoteIP == null ) {
                log.info("选择使用本机调试用例...");
                // Edge 驱动文件
                File file = new File(this.edgeDriverPathP);
                // 拿到驱动文件的绝对路径，设置系统驱动属性
                System.setProperty(TestConstants.EDGE_DRIVER_NAME, file.getAbsolutePath());
                // 指定 Edge 驱动
                this.driver = new EdgeDriver();
                // or 使用 Edge 定置驱动
                /*代码暂无*/
            }
            // 若是远端服务器跑 testNg.xml，会提供 remoteIP
            else{
                log.info("选择使用服务端调试用例...");
                DesiredCapabilities desiredCapabilities = DesiredCapabilities.edge();// 浏览器驱动
                // DesiredCapabilities desiredCaps = new DesiredCapabilities("edge", browserVersion, Platform.LINUX);// 浏览器驱动
                this.driver = new RemoteWebDriver(new URL("http://" + remoteIP + ":4444/wd/hub/"), desiredCapabilities);// 驱动使用 RemoteWebDriver
            }
        }
        // 若是 Opera 驱动
        else if(browserType.equals("opera")){
            log.info("选择 opera 驱动...");
            // 若是本地直接跑 @Test 用例，没有提供 remoteIP，就是用 ChromeDriver() 方法
            if( remoteIP == null ) {
                log.info("选择使用本机调试用例...");
                // Opera 驱动文件
                File file = new File(this.operaDriverPathP);
                // 拿到驱动文件的绝对路径，设置系统驱动属性
                System.setProperty(TestConstants.OPERA_DRIVER_NAME, file.getAbsolutePath());
                // 指定 Opera 驱动
                this.driver = new OperaDriver();
                // or 使用 opera 定置驱动
                /*OperaOptions operaOptions = new OperaOptions();
                operaOptions.addArguments("--start-maximized");
                this.driver = new OperaDriver(operaOptions);*/
            }
            // 若是远端服务器跑 testNg.xml，会提供 remoteIP
            else{
                log.info("选择使用服务端调试用例...");
                DesiredCapabilities desiredCapabilities = DesiredCapabilities.opera();// 浏览器驱动
                // DesiredCapabilities desiredCaps = new DesiredCapabilities("opera", browserVersion, Platform.LINUX);// 浏览器驱动
                this.driver = new RemoteWebDriver(new URL("http://" + remoteIP + ":4444/wd/hub/"), desiredCapabilities);// 驱动使用 RemoteWebDriver
            }
        }
        // 若是 Safari 驱动
        else if(browserType.equals("safari")){
            log.info("选择 safari 驱动...");
            // 若是本地直接跑 @Test 用例，没有提供 remoteIP，就是用 ChromeDriver() 方法
            if( remoteIP == null ) {
                log.info("选择使用本机调试用例...");
                // Safari 驱动文件
                File file = new File(this.safariDriverPathP);
                // 拿到驱动文件的绝对路径，设置系统驱动属性
                System.setProperty(TestConstants.SAFARI_DRIVER_NAME, file.getAbsolutePath());
                // 指定 Safari 驱动
                this.driver = new SafariDriver();
                // or 使用 safari 定置驱动
                /*代码暂无*/
            }
            // 若是远端服务器跑 testNg.xml，会提供 remoteIP
            else{
                log.info("选择使用服务端调试用例...");
                DesiredCapabilities desiredCapabilities = DesiredCapabilities.safari();// 浏览器驱动
                // DesiredCapabilities desiredCaps = new DesiredCapabilities("safari", browserVersion, Platform.LINUX);// 浏览器驱动
                this.driver = new RemoteWebDriver(new URL("http://" + remoteIP + ":4444/wd/hub/"), desiredCapabilities);// 驱动使用 RemoteWebDriver
            }
        }
        // 若是 PhantomJS 驱动
        else if(browserType.equals("phantomjs")){
            log.info("选择 phantomjs 驱动...");
            // 若是本地直接跑 @Test 用例，没有提供 remoteIP，就是用 ChromeDriver() 方法
            if( remoteIP == null ) {
                log.info("选择使用本机调试用例...");
                // PhantomJS binary
                File file = new File(this.phantomjsBinaryPathP);
                // 拿到驱动文件的绝对路径，设置系统驱动属性
                System.setProperty(TestConstants.PHANTOMJS_BINARY_PATH, file.getAbsolutePath());
                // 指定 PhantomJS binary
                this.driver = new PhantomJSDriver();
                // or 使用 PhantomJS 定置驱动
                /*代码暂无*/
            }
            // 若是远端服务器跑 testNg.xml，会提供 remoteIP
            else{
                log.info("选择使用服务端调试用例...");
                DesiredCapabilities desiredCapabilities = DesiredCapabilities.phantomjs();// 浏览器驱动
                // DesiredCapabilities desiredCaps = new DesiredCapabilities("phantomjs", browserVersion, Platform.LINUX);// 浏览器驱动
                this.driver = new RemoteWebDriver(new URL("http://" + remoteIP + ":4444/wd/hub/"), desiredCapabilities);// 驱动使用 RemoteWebDriver
            }
        }
        // 若无法匹配到传过来的浏览器驱动类型
        else{
            log.error("浏览器暂不支持或者 testNg.xml 中浏览器参数配置出现问题！");
            // 运行时异常，只有在运行到这里时候才会被抛出，否则不被抛出
            throw new RuntimeException("浏览器暂不支持或者 testNg.xml 中浏览器参数配置出现问题！");
        }
        // 此时驱动存在，前面若驱动不存在，会跑出运行异常并且不会被回调方法捕获此类异常，程序会在上一步先报错
        this.javascriptExecutor = ((JavascriptExecutor)driver);
    }

    /* ===================================== 驱动退出并且关闭浏览器 ===================================== */
    /**
     * 驱动退出以及浏览器关闭，driver.quit() 方法拓展，之后关闭浏览器
     *
     * @throws InterruptedException
     */
    @AfterTest
    public void closeBrowser() throws InterruptedException {
        /* 退出 driver 驱动 */
        log.info("该测试用例结束，正在退出驱动...");
        Thread.sleep(3000);// 延时 3s
        // 弹出框显示一个用例测试结束
        javascriptExecutor.executeScript(TestConstants.FINISH_ALERT);
        Thread.sleep(3000);// 延时 3s
        // 驱动退出
        driver.quit();

        /* 杀死浏览器进程 */
        log.info("该测试用例结束，正在关闭浏览器...");
        String command = "taskkill /F /IM";
        // 若 this.browser 变量存在
        if(this.browser != null && !this.browser.isEmpty()){
            if(this.browser.equals("chrome")){         // 若是谷歌浏览器
                command = command + "chromedriver.exe";
            }
            else if(this.browser.equals("firefox")){   // 若是火狐浏览器
                command = command + "geckodriver.exe";
            }
            else if(this.browser.equals("ie")){        // 若是 IE 浏览器
                command = command + "IEDriverServer.exe";
            }
            else if(this.browser.equals("edge")){      // 若是 edge 浏览器
                command = command + "MicrosoftWebDriver.exe";
            }
            else if(this.browser.equals("opera")){     // 若是 opera 浏览器
                command = command + "operadriver.exe";
            }
            else if(this.browser.equals("safari")){    // 若是 safari 浏览器
                command = command + "SafariDriver.safariextz";
            }
            else if(this.browser.equals("phantomjs")){ // 若是 IE 浏览器
                command = command + "phantomjs.exe";
            }
            try {
                // 运行关闭浏览器命令
                Runtime.getRuntime().exec(command);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 若没有 this.browser 就抛出一个异常
        else{
            log.error("测试基类中 this.browser 成员变量不存在！");
            // 运行时异常，只有在运行到这里时候才会被抛出，否则不被抛出
            throw new RuntimeException("测试基类中 this.browser 成员变量不存在！");
        }
        // OVER
        log.info("=== SELENIUM TEST OVER ===");
        System.out.println();
    }

    /* ======================================== 重写监听器方法 ========================================= */
    /**
     * 保存截图并将截图附在 report 报告中
     * 重写 TestListenerAdapter 类的 onTestFailure() 方法，用来截图,用 Report.log 将截图放在报告中
     * 原理是每次出错报异常，新建一个该类，并运行这个重写的方法，目前使用 static driver，优化部分可以从测试类中回溯基类取到 driver
     *
     * @param result
     */
    @Override
    public synchronized void onTestFailure(ITestResult result){
        // 先获取对应测试用例的驱动
        Object currentClass = result.getInstance();// 获取当前 Object 类型的测试类
        WebDriver currentDriver = ( (SimpleInitiator) (currentClass) ).driver;// 获取当前测试类
        log.info("抛出异常时的驱动：" + currentDriver);
        // 若 driver 不为空
        if(currentDriver != null){
            // 得到需要日期格式
            Date currentDate = new Date();// 新建一个 Date，得到当前日期信息
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd(HH-mm-ss)");// 指定日期格式
            String formatDate = simpleDateFormat.format(currentDate);// 样式化当前日期格式
            // 截图功能的实现
            log.info("此处测试报错，正在截图...");
            File srcFile= ((TakesScreenshot)currentDriver).getScreenshotAs(OutputType.FILE);// 测试出错就截图
            String picPath = "test-output/screenshots/" + result.getTestClass().getRealClass().getSimpleName() + formatDate + ".png";// 存放截图的路径，名字带有截图日期
            log.info("截图路径为项目下：" + picPath);
            // 循环防止截图名字一样时被覆盖
            for(int i=2;;i++){
                if(!new File(picPath).exists()){ // 若截图不存在跳出循环
                    break;
                }
                else{ // 若文件存在则截图名字后面附上数字并进入下次循环
                    picPath = "test-output/screenshots/" + result.getTestClass().getRealClass().getSimpleName() + formatDate + i + ".png";
                }
            }
            // 拷贝截图到 test-output/screenshots/ 目录下
            try{
                FileUtils.copyFile(srcFile, new File(picPath));
            }catch(IOException e){
                log.error("截图功能操作出现问题！");
            }
            // 将截图放置于 report 报告中
            System.setProperty("org.uncommons.reportng.escape-output", "false");// 设置此属性防止前端代码输出成 String
            Reporter.log("<a href=\"../../" + picPath + "\" target=_blank>Failed Screen Shot</a>", true);// 在 report 报告中输出截图
        }
    }
}