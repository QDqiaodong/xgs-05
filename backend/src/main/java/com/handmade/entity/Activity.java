package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String rules;

    private String coverImage;

    private String bannerImage;

    private Long categoryId;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime voteStartTime;

    private LocalDateTime voteEndTime;

    private Integer maxSubmitPerUser;

    private Integer maxVotePerUser;

    private Integer allowSameWorkMultivote;

    private Integer workCount;

    private Integer voteCount;

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
