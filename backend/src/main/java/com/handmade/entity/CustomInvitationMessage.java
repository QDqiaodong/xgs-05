package com.handmade.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("custom_invitation_message")
public class CustomInvitationMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long invitationId;

    private Long senderId;

    private String content;

    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
