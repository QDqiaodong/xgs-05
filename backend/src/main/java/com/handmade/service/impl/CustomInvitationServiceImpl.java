package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.CustomInvitation;
import com.handmade.entity.CustomInvitationMessage;
import com.handmade.entity.User;
import com.handmade.mapper.CustomInvitationMapper;
import com.handmade.mapper.CustomInvitationMessageMapper;
import com.handmade.service.CustomInvitationService;
import com.handmade.service.UserService;
import com.handmade.vo.InvitationDetailVO;
import com.handmade.vo.InvitationListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomInvitationServiceImpl extends ServiceImpl<CustomInvitationMapper, CustomInvitation> implements CustomInvitationService {

    @Autowired
    private CustomInvitationMessageMapper messageMapper;

    @Autowired
    private UserService userService;

    @Override
    public IPage<InvitationListVO> getClientInvitations(Long clientId, Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<CustomInvitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomInvitation::getClientId, clientId);
        if (status != null) {
            wrapper.eq(CustomInvitation::getStatus, status);
        }
        wrapper.orderByDesc(CustomInvitation::getCreateTime);
        IPage<CustomInvitation> invitationPage = this.page(new Page<>(page, size), wrapper);
        return convertToVOList(invitationPage);
    }

    @Override
    public IPage<InvitationListVO> getCreatorInvitations(Long creatorId, Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<CustomInvitation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomInvitation::getCreatorId, creatorId);
        if (status != null) {
            wrapper.eq(CustomInvitation::getStatus, status);
        }
        wrapper.orderByDesc(CustomInvitation::getCreateTime);
        IPage<CustomInvitation> invitationPage = this.page(new Page<>(page, size), wrapper);
        return convertToVOList(invitationPage);
    }

    private IPage<InvitationListVO> convertToVOList(IPage<CustomInvitation> invitationPage) {
        Page<InvitationListVO> result = new Page<>(invitationPage.getCurrent(), invitationPage.getSize());
        result.setTotal(invitationPage.getTotal());
        List<InvitationListVO> records = invitationPage.getRecords().stream().map(inv -> {
            InvitationListVO vo = new InvitationListVO();
            vo.setInvitation(inv);
            vo.setClient(userService.getById(inv.getClientId()));
            vo.setCreator(userService.getById(inv.getCreatorId()));
            return vo;
        }).collect(Collectors.toList());
        result.setRecords(records);
        return result;
    }

    @Override
    public InvitationDetailVO getInvitationDetail(Long invitationId) {
        CustomInvitation invitation = this.getById(invitationId);
        if (invitation == null) {
            return null;
        }
        InvitationDetailVO vo = new InvitationDetailVO();
        vo.setInvitation(invitation);
        vo.setClient(userService.getById(invitation.getClientId()));
        vo.setCreator(userService.getById(invitation.getCreatorId()));
        vo.setMessages(getMessages(invitationId));
        return vo;
    }

    @Override
    public Long createInvitation(CustomInvitation invitation) {
        invitation.setStatus(0);
        this.save(invitation);
        return invitation.getId();
    }

    @Override
    public boolean acceptInvitation(Long invitationId) {
        CustomInvitation invitation = new CustomInvitation();
        invitation.setId(invitationId);
        invitation.setStatus(1);
        return this.updateById(invitation);
    }

    @Override
    public boolean rejectInvitation(Long invitationId, String rejectReason) {
        CustomInvitation invitation = new CustomInvitation();
        invitation.setId(invitationId);
        invitation.setStatus(2);
        invitation.setRejectReason(rejectReason);
        return this.updateById(invitation);
    }

    @Override
    public boolean startProgress(Long invitationId) {
        CustomInvitation invitation = new CustomInvitation();
        invitation.setId(invitationId);
        invitation.setStatus(3);
        return this.updateById(invitation);
    }

    @Override
    public boolean completeInvitation(Long invitationId) {
        CustomInvitation invitation = new CustomInvitation();
        invitation.setId(invitationId);
        invitation.setStatus(4);
        return this.updateById(invitation);
    }

    @Override
    public boolean cancelInvitation(Long invitationId) {
        CustomInvitation invitation = new CustomInvitation();
        invitation.setId(invitationId);
        invitation.setStatus(5);
        return this.updateById(invitation);
    }

    @Override
    public CustomInvitationMessage sendMessage(Long invitationId, Long senderId, String content, String images) {
        CustomInvitationMessage message = new CustomInvitationMessage();
        message.setInvitationId(invitationId);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setImages(images);
        messageMapper.insert(message);
        return message;
    }

    @Override
    public List<CustomInvitationMessage> getMessages(Long invitationId) {
        LambdaQueryWrapper<CustomInvitationMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomInvitationMessage::getInvitationId, invitationId);
        wrapper.orderByAsc(CustomInvitationMessage::getCreateTime);
        return messageMapper.selectList(wrapper);
    }
}
