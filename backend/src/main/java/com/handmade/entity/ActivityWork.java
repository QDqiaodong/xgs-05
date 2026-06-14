package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_work")
public class ActivityWork {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long workId;

    private Long userId;

    private String submitRemark;

    private Integer voteCount;

    private Integer rankNum;

    private LocalDateTime submitTime;

    private Integer auditStatus;

    private String auditRemark;

    private Long auditorId;

    private LocalDateTime auditTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
