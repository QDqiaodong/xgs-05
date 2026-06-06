package com.handmade.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    public Result<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadFile(file);
            if (url != null) {
                urls.add(url);
            }
        }
        return Result.success(urls);
    }

    @PostMapping("/upload/webp")
    public Result<List<String>> uploadFilesToWebp(@RequestParam("files") MultipartFile[] files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            String url = uploadAndConvertToWebp(file);
            if (url != null) {
                urls.add(url);
            }
        }
        return Result.success(urls);
    }

    private String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String fileName = IdUtil.simpleUUID() + "." + suffix;
        String datePath = java.time.LocalDate.now().toString().replace("-", "/");
        File dir = new File(uploadPath + "/" + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File destFile = new File(dir, fileName);
        file.transferTo(destFile);
        return urlPrefix + "/" + datePath + "/" + fileName;
    }

    private String uploadAndConvertToWebp(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        String datePath = java.time.LocalDate.now().toString().replace("-", "/");
        File dir = new File(uploadPath + "/" + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = IdUtil.simpleUUID() + ".webp";
        File destFile = new File(dir, fileName);

        BufferedImage image = ImageIO.read(file.getInputStream());
        if (image != null) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("webp");
            if (writers.hasNext()) {
                ImageWriter writer = writers.next();
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.75f);

                try (ImageOutputStream ios = ImageIO.createImageOutputStream(new FileOutputStream(destFile))) {
                    writer.setOutput(ios);
                    writer.write(null, new IIOImage(image, null, null), param);
                }
                writer.dispose();
            } else {
                ImageIO.write(image, "png", destFile);
            }
        }
        return urlPrefix + "/" + datePath + "/" + fileName;
    }
}
