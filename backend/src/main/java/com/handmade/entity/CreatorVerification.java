package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("creator_verification")
public class CreatorVerification {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String portfolioLinks;

    private String creationExperience;

    private String expertiseField;

    private String realName;

    private String contactInfo;

    private String additionalMaterials;

    private Integer status;

    private String reviewRemark;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
