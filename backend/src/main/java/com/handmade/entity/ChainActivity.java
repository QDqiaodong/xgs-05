package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chain_activity")
public class ChainActivity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String theme;

    private String description;

    private String rules;

    private String coverImage;

    private String bannerImage;

    private Long seedWorkId;

    private Long categoryId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer maxChainDepth;

    private Integer workCount;

    private Integer participantCount;

    private Integer viewCount;

    private Integer status;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
