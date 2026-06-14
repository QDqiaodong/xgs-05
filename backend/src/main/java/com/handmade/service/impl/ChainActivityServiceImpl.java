package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.*;
import com.handmade.mapper.ChainActivityMapper;
import com.handmade.service.*;
import com.handmade.vo.ChainActivityDetailVO;
import com.handmade.vo.ChainGenealogyNode;
import com.handmade.vo.ChainGenealogyVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChainActivityServiceImpl extends ServiceImpl<ChainActivityMapper, ChainActivity> implements ChainActivityService {

    @Autowired
    @Lazy
    private ChainWorkService chainWorkService;

    @Autowired
    private WorkService workService;

    @Autowired
    private UserService userService;

    @Override
    public IPage<ChainActivity> getChainActivityList(Integer page, Integer size, Integer status) {
        LambdaQueryWrapper<ChainActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ChainActivity::getStatus, status);
        }
        wrapper.orderByDesc(ChainActivity::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public IPage<ChainActivity> getOngoingChainActivities(Integer page, Integer size) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<ChainActivity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainActivity::getStatus, 1);
        wrapper.le(ChainActivity::getStartTime, now);
        wrapper.ge(ChainActivity::getEndTime, now);
        wrapper.orderByDesc(ChainActivity::getCreateTime);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public ChainActivityDetailVO getChainActivityDetail(Long id) {
        ChainActivity activity = this.getById(id);
        if (activity == null) {
            return null;
        }
        incrementViewCount(id);
        ChainActivityDetailVO vo = new ChainActivityDetailVO();
        vo.setChainActivity(activity);
        LocalDateTime now = LocalDateTime.now();
        vo.setIsOngoing(activity.getStatus() == 1 && now.isAfter(activity.getStartTime()) && now.isBefore(activity.getEndTime()));
        vo.setMySubmitCount(0);
        return vo;
    }

    @Override
    public Long createChainActivity(ChainActivity chainActivity) {
        if (chainActivity.getMaxChainDepth() == null) {
            chainActivity.setMaxChainDepth(0);
        }
        if (chainActivity.getWorkCount() == null) {
            chainActivity.setWorkCount(0);
        }
        if (chainActivity.getParticipantCount() == null) {
            chainActivity.setParticipantCount(0);
        }
        if (chainActivity.getViewCount() == null) {
            chainActivity.setViewCount(0);
        }
        if (chainActivity.getStatus() == null) {
            chainActivity.setStatus(1);
        }
        this.save(chainActivity);
        return chainActivity.getId();
    }

    @Override
    public boolean updateChainActivity(Long id, ChainActivity chainActivity) {
        chainActivity.setId(id);
        return this.updateById(chainActivity);
    }

    @Override
    public boolean deleteChainActivity(Long id) {
        return this.removeById(id);
    }

    @Override
    public boolean incrementViewCount(Long id) {
        LambdaUpdateWrapper<ChainActivity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChainActivity::getId, id)
                .setSql("view_count = view_count + 1");
        return this.baseMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public ChainGenealogyVO getChainGenealogy(Long chainActivityId) {
        LambdaQueryWrapper<ChainWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainWork::getChainActivityId, chainActivityId);
        wrapper.eq(ChainWork::getAuditStatus, 1);
        wrapper.orderByAsc(ChainWork::getChainLevel);
        List<ChainWork> allWorks = this.chainWorkService.list(wrapper);

        ChainGenealogyVO vo = new ChainGenealogyVO();
        vo.setChainActivityId(chainActivityId);
        vo.setTotalWorks(allWorks.size());
        Set<Long> participantIds = allWorks.stream().map(ChainWork::getUserId).collect(Collectors.toSet());
        vo.setTotalParticipants(participantIds.size());
        int maxDepth = allWorks.stream().mapToInt(cw -> cw.getChainLevel() != null ? cw.getChainLevel() : 0).max().orElse(0);
        vo.setMaxDepth(maxDepth);

        if (allWorks.isEmpty()) {
            vo.setRoots(new ArrayList<>());
            return vo;
        }

        List<Long> workIds = allWorks.stream().map(ChainWork::getWorkId).collect(Collectors.toList());
        List<Long> userIds = new ArrayList<>(participantIds);
        Map<Long, Work> workMap = workService.listByIds(workIds).stream().collect(Collectors.toMap(Work::getId, w -> w));
        Map<Long, User> userMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

        Map<Long, ChainGenealogyNode> nodeMap = new HashMap<>();
        List<ChainGenealogyNode> roots = new ArrayList<>();

        for (ChainWork cw : allWorks) {
            ChainGenealogyNode node = new ChainGenealogyNode();
            node.setChainWorkId(cw.getId());
            node.setWorkId(cw.getWorkId());
            node.setUserId(cw.getUserId());
            Work w = workMap.get(cw.getWorkId());
            if (w != null) {
                node.setWorkTitle(w.getTitle());
                node.setWorkCoverImage(w.getCoverImage());
            }
            User u = userMap.get(cw.getUserId());
            if (u != null) {
                node.setUserAvatar(u.getAvatar());
                node.setUsername(u.getUsername());
            }
            node.setChainLevel(cw.getChainLevel());
            node.setInspirationRemark(cw.getInspirationRemark());
            node.setBranchCount(cw.getBranchCount());
            node.setLikeCount(cw.getLikeCount());
            node.setChildren(new ArrayList<>());
            nodeMap.put(cw.getId(), node);
        }

        for (ChainWork cw : allWorks) {
            ChainGenealogyNode node = nodeMap.get(cw.getId());
            if (cw.getParentChainWorkId() != null && nodeMap.containsKey(cw.getParentChainWorkId())) {
                nodeMap.get(cw.getParentChainWorkId()).getChildren().add(node);
            } else {
                roots.add(node);
            }
        }

        vo.setRoots(roots);
        return vo;
    }

    @Override
    public boolean incrementWorkCount(Long id) {
        LambdaUpdateWrapper<ChainActivity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChainActivity::getId, id)
                .setSql("work_count = work_count + 1");
        return this.baseMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public boolean incrementParticipantCount(Long id) {
        LambdaUpdateWrapper<ChainActivity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ChainActivity::getId, id)
                .setSql("participant_count = participant_count + 1");
        return this.baseMapper.update(null, updateWrapper) > 0;
    }
}
