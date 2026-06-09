package com.handmade.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImageUrlVO {
    private String original;
    private String small;
    private String medium;
    private String large;

    public static ImageUrlVO fromMap(java.util.Map<com.handmade.service.ImageSize, String> map) {
        ImageUrlVO vo = new ImageUrlVO();
        vo.setOriginal(map.get(com.handmade.service.ImageSize.ORIGINAL));
        vo.setSmall(map.get(com.handmade.service.ImageSize.SMALL));
        vo.setMedium(map.get(com.handmade.service.ImageSize.MEDIUM));
        vo.setLarge(map.get(com.handmade.service.ImageSize.LARGE));
        return vo;
    }
}
