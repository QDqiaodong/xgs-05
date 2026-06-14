package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("chain_work")
public class ChainWork {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long chainActivityId;

    private Long workId;

    private Long userId;

    private Long parentChainWorkId;

    private Long rootChainWorkId;

    private String inspirationRemark;

    private Integer chainLevel;

    private Integer branchCount;

    private Integer likeCount;

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
