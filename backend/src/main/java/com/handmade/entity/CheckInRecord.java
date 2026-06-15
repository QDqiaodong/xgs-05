package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("check_in_record")
public class CheckInRecord {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate checkInDate;

    private String title;

    private String content;

    private String images;

    private Integer workDuration;

    private Long workId;

    private String moodTag;

    private String weatherTag;

    private Integer viewCount;

    private Integer likeCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
