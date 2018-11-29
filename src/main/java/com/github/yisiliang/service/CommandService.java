package com.github.yisiliang.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

/**
 * @author YiSiliang
 * @date 2018/11/9 18:22
 */
@Service
public class CommandService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandService.class);
    @Value("${split.file.post.command}")
    private String command;

    public void push(List<File> smallFileList){
        for (File file : smallFileList){
            try {
                String[] cmds = new String[2];
                cmds[0] = command;
                cmds[1] = file.getAbsolutePath();
                Runtime.getRuntime().exec(cmds);
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }

}
