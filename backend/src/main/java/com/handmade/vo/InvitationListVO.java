package com.handmade.vo;

import com.handmade.entity.CustomInvitation;
import com.handmade.entity.User;
import lombok.Data;

@Data
public class InvitationListVO {
    private CustomInvitation invitation;
    private User client;
    private User creator;
}
