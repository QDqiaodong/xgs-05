package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("custom_invitation")
public class CustomInvitation {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long clientId;

    private Long creatorId;

    private Long workId;

    private String title;

    private String requirements;

    private BigDecimal budgetMin;

    private BigDecimal budgetMax;

    private Integer expectedDays;

    private Integer status;

    private String rejectReason;

    private String referenceImages;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
