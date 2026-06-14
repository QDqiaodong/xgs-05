package com.handmade.vo;

import com.handmade.entity.CustomInvitation;
import com.handmade.entity.CustomInvitationMessage;
import com.handmade.entity.User;
import lombok.Data;
import java.util.List;

@Data
public class InvitationDetailVO {
    private CustomInvitation invitation;
    private User client;
    private User creator;
    private List<CustomInvitationMessage> messages;
}
