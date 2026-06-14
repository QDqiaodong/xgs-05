package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.CustomInvitation;
import com.handmade.entity.CustomInvitationMessage;
import com.handmade.vo.InvitationDetailVO;
import com.handmade.vo.InvitationListVO;

import java.util.List;

public interface CustomInvitationService extends IService<CustomInvitation> {
    IPage<InvitationListVO> getClientInvitations(Long clientId, Integer page, Integer size, Integer status);

    IPage<InvitationListVO> getCreatorInvitations(Long creatorId, Integer page, Integer size, Integer status);

    InvitationDetailVO getInvitationDetail(Long invitationId);

    Long createInvitation(CustomInvitation invitation);

    boolean acceptInvitation(Long invitationId);

    boolean rejectInvitation(Long invitationId, String rejectReason);

    boolean startProgress(Long invitationId);

    boolean completeInvitation(Long invitationId);

    boolean cancelInvitation(Long invitationId);

    CustomInvitationMessage sendMessage(Long invitationId, Long senderId, String content, String images);

    List<CustomInvitationMessage> getMessages(Long invitationId);
}
