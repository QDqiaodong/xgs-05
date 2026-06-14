package com.handmade.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.handmade.context.UserContext;
import com.handmade.entity.CustomInvitation;
import com.handmade.entity.CustomInvitationMessage;
import com.handmade.service.CustomInvitationService;
import com.handmade.vo.InvitationDetailVO;
import com.handmade.vo.InvitationListVO;
import com.handmade.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/invitation")
public class CustomInvitationController {

    @Autowired
    private CustomInvitationService customInvitationService;

    @PostMapping
    public Result<Long> createInvitation(@RequestBody CustomInvitation invitation) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        if (invitation.getCreatorId() == null) {
            return Result.error("创作者ID不能为空");
        }
        if (invitation.getCreatorId().equals(currentUserId)) {
            return Result.error("不能向自己发起邀约");
        }
        invitation.setClientId(currentUserId);
        return Result.success(customInvitationService.createInvitation(invitation));
    }

    @GetMapping("/client")
    public Result<IPage<InvitationListVO>> getClientInvitations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(customInvitationService.getClientInvitations(currentUserId, page, size, status));
    }

    @GetMapping("/creator")
    public Result<IPage<InvitationListVO>> getCreatorInvitations(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        return Result.success(customInvitationService.getCreatorInvitations(currentUserId, page, size, status));
    }

    @GetMapping("/{id}")
    public Result<InvitationDetailVO> getInvitationDetail(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getClientId().equals(currentUserId)
                && !detail.getInvitation().getCreatorId().equals(currentUserId)) {
            return Result.error("无权限查看此邀约");
        }
        return Result.success(detail);
    }

    @PutMapping("/{id}/accept")
    public Result<Boolean> acceptInvitation(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getCreatorId().equals(currentUserId)) {
            return Result.error("只有创作者可以接受邀约");
        }
        if (detail.getInvitation().getStatus() != 0) {
            return Result.error("当前状态不可接受");
        }
        return Result.success(customInvitationService.acceptInvitation(id));
    }

    @PutMapping("/{id}/reject")
    public Result<Boolean> rejectInvitation(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getCreatorId().equals(currentUserId)) {
            return Result.error("只有创作者可以拒绝邀约");
        }
        if (detail.getInvitation().getStatus() != 0) {
            return Result.error("当前状态不可拒绝");
        }
        String rejectReason = body != null ? body.get("rejectReason") : null;
        return Result.success(customInvitationService.rejectInvitation(id, rejectReason));
    }

    @PutMapping("/{id}/start")
    public Result<Boolean> startProgress(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getCreatorId().equals(currentUserId)) {
            return Result.error("只有创作者可以开始制作");
        }
        if (detail.getInvitation().getStatus() != 1) {
            return Result.error("当前状态不可开始制作");
        }
        return Result.success(customInvitationService.startProgress(id));
    }

    @PutMapping("/{id}/complete")
    public Result<Boolean> completeInvitation(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getCreatorId().equals(currentUserId)) {
            return Result.error("只有创作者可以完成邀约");
        }
        if (detail.getInvitation().getStatus() != 3) {
            return Result.error("当前状态不可完成");
        }
        return Result.success(customInvitationService.completeInvitation(id));
    }

    @PutMapping("/{id}/cancel")
    public Result<Boolean> cancelInvitation(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getClientId().equals(currentUserId)) {
            return Result.error("只有客户可以取消邀约");
        }
        if (detail.getInvitation().getStatus() != 0 && detail.getInvitation().getStatus() != 1) {
            return Result.error("当前状态不可取消");
        }
        return Result.success(customInvitationService.cancelInvitation(id));
    }

    @PostMapping("/{id}/message")
    public Result<CustomInvitationMessage> sendMessage(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getClientId().equals(currentUserId)
                && !detail.getInvitation().getCreatorId().equals(currentUserId)) {
            return Result.error("无权限在此邀约留言");
        }
        String content = body != null ? body.get("content") : null;
        String images = body != null ? body.get("images") : null;
        if (content == null || content.trim().isEmpty()) {
            return Result.error("消息内容不能为空");
        }
        return Result.success(customInvitationService.sendMessage(id, currentUserId, content, images));
    }

    @GetMapping("/{id}/messages")
    public Result<List<CustomInvitationMessage>> getMessages(@PathVariable Long id) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            return Result.error("请先登录");
        }
        InvitationDetailVO detail = customInvitationService.getInvitationDetail(id);
        if (detail == null) {
            return Result.error("邀约不存在");
        }
        if (!detail.getInvitation().getClientId().equals(currentUserId)
                && !detail.getInvitation().getCreatorId().equals(currentUserId)) {
            return Result.error("无权限查看此邀约消息");
        }
        return Result.success(customInvitationService.getMessages(id));
    }
}
