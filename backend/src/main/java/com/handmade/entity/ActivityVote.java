package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("activity_vote")
public class ActivityVote {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long activityWorkId;

    private Long workId;

    private Long userId;

    private Integer voteCount;

    private LocalDateTime voteTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
