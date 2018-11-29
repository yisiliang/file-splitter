package com.github.yisiliang.service;

import com.github.yisiliang.timer.SplitJob;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author YiSiliang
 * @date 2018/11/9 16:01
 */
@Service
public class SplitService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplitJob.class);

    public List<File> split(File file, int number, String outPath, String bakPath) throws Exception {
        LOGGER.info("Divide {} into {} parts, outPath = {}, bakPath = {}", file, number, outPath, bakPath);
        List<File> smallFileList = new ArrayList<File>(number);
        if (number <= 1) {
            smallFileList.add(file);
            return smallFileList;
        }
        long fileSize = file.length();
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
        FileChannel fileChannel = randomAccessFile.getChannel();

        String baseName = FilenameUtils.getBaseName(file.getName());
        String extName = FilenameUtils.getExtension(file.getName());
        long startPos = 0;
        long smallSize = (fileSize / number);
        for (int i = 1; i <= number; i++) {
            if (i == number) {
                smallSize = (int) (fileSize - (long) startPos);
            }
            LOGGER.info("startPos = {}, smallSize = {}", startPos, smallSize);
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPos, smallSize);
            long fixSize = smallSize;
            if (i != number) {
                while (fixSize > 0) {
                    byte b = mappedByteBuffer.get((int) fixSize - 1);
                    if (b == '\n') {
                        break;
                    } else {
                        fixSize--;
                    }
                }
                LOGGER.info("find LF in {}", fixSize);
            } else {
                LOGGER.info("last part, no need to find LF");
            }

            File smallFile = new File(outPath, baseName + "_" + i + "." + extName);
            smallFileList.add(smallFile);
            RandomAccessFile target = new RandomAccessFile(smallFile, "rw");
            target.setLength(0);
            FileChannel out = target.getChannel();

            MappedByteBuffer mbbo = out.map(FileChannel.MapMode.READ_WRITE, 0, fixSize);
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startPos, fixSize);
            LOGGER.info("file {} : startPos = {}, fixSize = {}", smallFile, startPos, fixSize);
            for (int cnt = 0; cnt < fixSize; cnt++) {
                byte b = mappedByteBuffer.get(cnt);
                mbbo.put(cnt, b);
            }
            target.close();
            startPos = startPos + fixSize;
        }
        randomAccessFile.close();
        FileUtils.deleteQuietly(new File(bakPath, file.getName()));
        FileUtils.moveFileToDirectory(file, new File(bakPath), true);
        return smallFileList;
    }
}
