package com.sydigit.yzwater.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目的启动类
 */
@SuppressWarnings("SpringComponentScan") // 忽略 IDEA 无法识别 ${yz.info.base-package}
@SpringBootApplication(scanBasePackages = {"${yz.info.base-package}.server", "${yz.info.base-package}.module"})
public class YzServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YzServerApplication.class, args);
    }

}
