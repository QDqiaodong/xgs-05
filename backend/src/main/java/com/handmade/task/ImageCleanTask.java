package com.handmade.task;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;

@Slf4j
@Component
public class ImageCleanTask {

    @Value("${file.upload.path}")
    private String uploadPath;

    private static final long EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredImages() {
        log.info("开始清理过期图片文件...");
        try {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                return;
            }
            long now = System.currentTimeMillis();
            cleanDirectory(uploadDir, now);
            log.info("过期图片清理完成");
        } catch (Exception e) {
            log.error("清理过期图片失败", e);
        }
    }

    private void cleanDirectory(File dir, long now) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                cleanDirectory(file, now);
                if (file.listFiles() != null && file.listFiles().length == 0) {
                    file.delete();
                }
            } else {
                long lastModified = file.lastModified();
                if (now - lastModified > EXPIRE_TIME) {
                    if (isUnusedImage(file)) {
                        file.delete();
                        log.info("删除过期图片: {}", file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private boolean isUnusedImage(File file) {
        return true;
    }
}
