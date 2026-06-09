package com.handmade.service;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@Service
public class ImageService {

    private static final String[] PREFERRED_FORMATS = {"webp", "jpg", "png"};

    public Map<ImageSize, String> generateThumbnails(InputStream inputStream, String originalFileName,
                                                     String uploadPath, String datePath, String urlPrefix) throws IOException {
        Map<ImageSize, String> result = new HashMap<>();

        BufferedImage originalImage = ImageIO.read(inputStream);
        if (originalImage == null) {
            log.warn("无法读取图片: {}", originalFileName);
            return result;
        }

        String format = detectAvailableFormat();
        String ext = format;

        String baseName = FileUtil.mainName(originalFileName);

        File dir = new File(uploadPath + "/" + datePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (ImageSize size : ImageSize.values()) {
            String fileName = baseName + "_" + size.getSuffix() + "." + ext;
            File destFile = new File(dir, fileName);

            BufferedImage targetImage;
            if (size == ImageSize.ORIGINAL) {
                targetImage = originalImage;
            } else {
                targetImage = scaleImage(originalImage, size.getMaxWidth(), size.getMaxHeight());
            }

            writeImage(targetImage, destFile, format, size.getQuality());

            String url = urlPrefix + "/" + datePath + "/" + fileName;
            result.put(size, url);
            log.info("生成缩略图: size={}, url={}", size.getSuffix(), url);
        }

        return result;
    }

    public Map<ImageSize, String> generateThumbnailsFromFile(File originalFile, String datePath,
                                                              String uploadPath, String urlPrefix) throws IOException {
        try (FileInputStream fis = new FileInputStream(originalFile)) {
            return generateThumbnails(fis, originalFile.getName(), uploadPath, datePath, urlPrefix);
        }
    }

    private String detectAvailableFormat() {
        for (String format : PREFERRED_FORMATS) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
            if (writers.hasNext()) {
                return format;
            }
        }
        return "png";
    }

    private BufferedImage scaleImage(BufferedImage original, int maxWidth, int maxHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        if (originalWidth <= maxWidth && originalHeight <= maxHeight) {
            return original;
        }

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int targetWidth = (int) (originalWidth * ratio);
        int targetHeight = (int) (originalHeight * ratio);

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaled.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, targetWidth, targetHeight);
        g2d.drawImage(original, 0, 0, targetWidth, targetHeight, null);
        g2d.dispose();

        return scaled;
    }

    private BufferedImage convertToRGB(BufferedImage image) {
        if (image.getType() == BufferedImage.TYPE_INT_RGB) {
            return image;
        }
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = rgbImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return rgbImage;
    }

    private void writeImage(BufferedImage image, File destFile, String format, float quality) throws IOException {
        BufferedImage rgbImage = convertToRGB(image);

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(format);
        if (writers.hasNext()) {
            ImageWriter writer = writers.next();
            ImageWriteParam param = writer.getDefaultWriteParam();
            try {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            } catch (UnsupportedOperationException e) {
                log.debug("格式 {} 不支持压缩参数设置，使用默认", format);
            }

            try (ImageOutputStream ios = ImageIO.createImageOutputStream(destFile)) {
                writer.setOutput(ios);
                writer.write(null, new IIOImage(rgbImage, null, null), param);
            }
            writer.dispose();
        } else {
            ImageIO.write(rgbImage, "jpg", destFile);
        }
    }

    public static String getSizeUrl(String originalUrl, String size) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }
        String suffix = ImageSize.getSuffixForSize(size);
        int dotIndex = originalUrl.lastIndexOf('.');
        int slashIndex = originalUrl.lastIndexOf('/');
        if (dotIndex > slashIndex) {
            String base = originalUrl.substring(0, dotIndex);
            String ext = originalUrl.substring(dotIndex);
            for (ImageSize s : ImageSize.values()) {
                String existingSuffix = "_" + s.getSuffix();
                if (base.endsWith(existingSuffix)) {
                    base = base.substring(0, base.length() - existingSuffix.length());
                    break;
                }
            }
            return base + "_" + suffix + ext;
        }
        return originalUrl;
    }
}
