package com.github.yisiliang;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author YiSiliang
 * @date 2018/11/9 15:43
 */
@SpringBootApplication
@EnableScheduling
public class FileSplitter  {
    public static void main(String[] args) {
        SpringApplication.run(FileSplitter.class, args);
    }
}
