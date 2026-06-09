package com.handmade.service;

import lombok.Getter;

@Getter
public enum ImageSize {
    SMALL("small", 300, 300, 0.75f),
    MEDIUM("medium", 800, 800, 0.8f),
    LARGE("large", 1600, 1600, 0.85f),
    ORIGINAL("original", 0, 0, 0.9f);

    private final String suffix;
    private final int maxWidth;
    private final int maxHeight;
    private final float quality;

    ImageSize(String suffix, int maxWidth, int maxHeight, float quality) {
        this.suffix = suffix;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.quality = quality;
    }

    public static String getSuffixForSize(String size) {
        for (ImageSize imageSize : values()) {
            if (imageSize.name().equalsIgnoreCase(size)) {
                return imageSize.getSuffix();
            }
        }
        return ORIGINAL.getSuffix();
    }
}
