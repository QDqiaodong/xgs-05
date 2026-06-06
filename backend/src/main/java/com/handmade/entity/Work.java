package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work")
public class Work {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long categoryId;

    private String title;

    private String description;

    private String coverImage;

    private String images;

    private String materials;

    private String creationIdea;

    private String productionCycle;

    private String steps;

    private Integer viewCount;

    private Integer favoriteCount;

    private Integer likeCount;

    private Integer status;

    private Integer isHot;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
