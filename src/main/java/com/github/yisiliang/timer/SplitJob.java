package com.github.yisiliang.timer;

import com.github.yisiliang.service.CommandService;
import com.github.yisiliang.service.SplitService;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author YiSiliang
 * @date 2018/11/9 15:49
 */
@Configuration
public class SplitJob {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitJob.class);
    @Value("${split.file.path}")
    private String splitFilePath;

    @Value("${split.file.out}")
    private String splitFileOut;

    @Value("${split.file.bak}")
    private String splitFileBak;

    @Value("${split.file.pattern}")
    private String splitFilePattern;

    @Value("${split.file.number}")
    private int splitFileNumber;

    @Autowired
    private SplitService splitService;

    @Autowired
    private CommandService commandService;

    @Scheduled(cron = "0/10 * * * * *")
    public void split() {
        File path = new File(splitFilePath);
        if (path.exists() == false || path.isDirectory() == false) {
            LOGGER.error("split.file.path {} is not exist or it is not a folder.", splitFilePath);
            return;
        }
        try {
            Collection<File> files = FileUtils.listFiles(path, null, false);
            if (CollectionUtils.isEmpty(files)) {
                LOGGER.warn("not file in split.file.path {}", splitFilePath);
                return;
            }

            for (File file : files) {
                String fileName = file.getName();
                if (Pattern.matches(splitFilePattern, fileName)) {
                    LOGGER.info("start to split {}", fileName);
                    List<File> smallFileList = splitService.split(file, splitFileNumber, splitFileOut, splitFileBak);
                    LOGGER.info("split {} -> {} ", fileName, smallFileList);
                    commandService.push(smallFileList);
                    LOGGER.info("upload {} ", smallFileList);
                } else {
                    LOGGER.warn("{} does not match {}", fileName, splitFilePattern);
                }
            }

        } catch (Exception e) {
            LOGGER.error("split file in split.file.path {} failed by {}", splitFilePath, e.getMessage());
            LOGGER.error(e.getMessage(), e);
        }
    }
}
