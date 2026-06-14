package com.handmade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.handmade.entity.*;
import com.handmade.mapper.ChainWorkMapper;
import com.handmade.service.*;
import com.handmade.vo.ChainWorkVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChainWorkServiceImpl extends ServiceImpl<ChainWorkMapper, ChainWork> implements ChainWorkService {

    @Autowired
    @Lazy
    private ChainActivityService chainActivityService;

    @Autowired
    private WorkService workService;

    @Autowired
    private UserService userService;

    @Override
    public IPage<ChainWorkVO> getChainWorks(Long chainActivityId, Integer page, Integer size, String sortBy) {
        LambdaQueryWrapper<ChainWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainWork::getChainActivityId, chainActivityId);
        wrapper.eq(ChainWork::getAuditStatus, 1);
        if ("like".equals(sortBy)) {
            wrapper.orderByDesc(ChainWork::getLikeCount);
        } else if ("level".equals(sortBy)) {
            wrapper.orderByAsc(ChainWork::getChainLevel);
        } else {
            wrapper.orderByDesc(ChainWork::getSubmitTime);
        }
        IPage<ChainWork> chainWorkPage = this.page(new Page<>(page, size), wrapper);
        return convertToVOPage(chainWorkPage);
    }

    @Override
    public IPage<ChainWorkVO> getMyChainWorks(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<ChainWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainWork::getUserId, userId);
        wrapper.orderByDesc(ChainWork::getSubmitTime);
        IPage<ChainWork> chainWorkPage = this.page(new Page<>(page, size), wrapper);
        return convertToVOPage(chainWorkPage);
    }

    private IPage<ChainWorkVO> convertToVOPage(IPage<ChainWork> chainWorkPage) {
        IPage<ChainWorkVO> voPage = new Page<>(chainWorkPage.getCurrent(), chainWorkPage.getSize(), chainWorkPage.getTotal());
        List<ChainWorkVO> voList = new ArrayList<>();
        if (!chainWorkPage.getRecords().isEmpty()) {
            List<Long> workIds = chainWorkPage.getRecords().stream().map(ChainWork::getWorkId).collect(Collectors.toList());
            List<Long> userIds = chainWorkPage.getRecords().stream().map(ChainWork::getUserId).collect(Collectors.toList());
            List<Long> parentChainWorkIds = chainWorkPage.getRecords().stream()
                    .map(ChainWork::getParentChainWorkId).filter(Objects::nonNull).collect(Collectors.toList());

            Map<Long, Work> workMap = workService.listByIds(workIds).stream().collect(Collectors.toMap(Work::getId, w -> w));
            Map<Long, User> userMap = userService.listByIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u));

            Map<Long, ChainWork> parentChainWorkMap = new HashMap<>();
            Map<Long, Work> parentWorkMap = new HashMap<>();
            Map<Long, User> parentUserMap = new HashMap<>();

            if (!parentChainWorkIds.isEmpty()) {
                List<ChainWork> parentChainWorks = this.listByIds(parentChainWorkIds);
                parentChainWorkMap = parentChainWorks.stream().collect(Collectors.toMap(ChainWork::getId, cw -> cw));
                List<Long> parentWorkIds = parentChainWorks.stream().map(ChainWork::getWorkId).collect(Collectors.toList());
                List<Long> parentUserIds = parentChainWorks.stream().map(ChainWork::getUserId).collect(Collectors.toList());
                if (!parentWorkIds.isEmpty()) {
                    parentWorkMap = workService.listByIds(parentWorkIds).stream().collect(Collectors.toMap(Work::getId, w -> w));
                }
                if (!parentUserIds.isEmpty()) {
                    parentUserMap = userService.listByIds(parentUserIds).stream().collect(Collectors.toMap(User::getId, u -> u));
                }
            }

            for (ChainWork cw : chainWorkPage.getRecords()) {
                ChainWorkVO vo = new ChainWorkVO();
                vo.setChainWork(cw);
                vo.setWork(workMap.get(cw.getWorkId()));
                vo.setUser(userMap.get(cw.getUserId()));
                if (cw.getParentChainWorkId() != null) {
                    vo.setParentChainWork(parentChainWorkMap.get(cw.getParentChainWorkId()));
                    ChainWork parentCW = parentChainWorkMap.get(cw.getParentChainWorkId());
                    if (parentCW != null) {
                        vo.setParentWork(parentWorkMap.get(parentCW.getWorkId()));
                        vo.setParentUser(parentUserMap.get(parentCW.getUserId()));
                    }
                }
                voList.add(vo);
            }
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ChainWorkVO getChainWorkDetail(Long id) {
        ChainWork cw = this.getById(id);
        if (cw == null) return null;
        ChainWorkVO vo = new ChainWorkVO();
        vo.setChainWork(cw);
        vo.setWork(workService.getById(cw.getWorkId()));
        vo.setUser(userService.getById(cw.getUserId()));
        if (cw.getParentChainWorkId() != null) {
            ChainWork parentCW = this.getById(cw.getParentChainWorkId());
            vo.setParentChainWork(parentCW);
            if (parentCW != null) {
                vo.setParentWork(workService.getById(parentCW.getWorkId()));
                vo.setParentUser(userService.getById(parentCW.getUserId()));
            }
        }
        return vo;
    }

    @Override
    public Long submitChainWork(Long chainActivityId, Long workId, Long userId, Long parentChainWorkId, String inspirationRemark) {
        ChainActivity activity = chainActivityService.getById(chainActivityId);
        if (activity == null || activity.getStatus() != 1) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            return null;
        }
        Work work = workService.getById(workId);
        if (work == null || !work.getUserId().equals(userId)) {
            return null;
        }
        if (activity.getCategoryId() != null && !activity.getCategoryId().equals(work.getCategoryId())) {
            return null;
        }
        LambdaQueryWrapper<ChainWork> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(ChainWork::getChainActivityId, chainActivityId);
        existWrapper.eq(ChainWork::getWorkId, workId);
        if (this.count(existWrapper) > 0) {
            return null;
        }

        int chainLevel = 1;
        Long rootChainWorkId = null;
        if (parentChainWorkId != null) {
            ChainWork parentCW = this.getById(parentChainWorkId);
            if (parentCW == null || !parentCW.getChainActivityId().equals(chainActivityId)) {
                return null;
            }
            chainLevel = parentCW.getChainLevel() + 1;
            if (activity.getMaxChainDepth() != null && activity.getMaxChainDepth() > 0 && chainLevel > activity.getMaxChainDepth()) {
                return null;
            }
            rootChainWorkId = parentCW.getRootChainWorkId() != null ? parentCW.getRootChainWorkId() : parentCW.getId();
            incrementBranchCount(parentChainWorkId);
        }

        ChainWork chainWork = new ChainWork();
        chainWork.setChainActivityId(chainActivityId);
        chainWork.setWorkId(workId);
        chainWork.setUserId(userId);
        chainWork.setParentChainWorkId(parentChainWorkId);
        chainWork.setRootChainWorkId(rootChainWorkId);
        chainWork.setInspirationRemark(inspirationRemark);
        chainWork.setChainLevel(chainLevel);
        chainWork.setBranchCount(0);
        chainWork.setLikeCount(0);
        chainWork.setSubmitTime(now);
        chainWork.setAuditStatus(1);
        this.save(chainWork);

        if (rootChainWorkId == null && parentChainWorkId != null) {
            chainWork.setRootChainWorkId(chainWork.getId());
        } else if (rootChainWorkId == null) {
            chainWork.setRootChainWorkId(chainWork.getId());
            this.updateById(chainWork);
        }

        chainActivityService.incrementWorkCount(chainActivityId);

        int userSubmitCountBefore = getUserSubmitCount(chainActivityId, userId);
        if (userSubmitCountBefore == 0) {
            chainActivityService.incrementParticipantCount(chainActivityId);
        }

        return chainWork.getId();
    }

    @Override
    public boolean auditChainWork(Long id, Integer auditStatus, String auditRemark, Long auditorId) {
        ChainWork chainWork = new ChainWork();
        chainWork.setId(id);
        chainWork.setAuditStatus(auditStatus);
        chainWork.setAuditRemark(auditRemark);
        chainWork.setAuditorId(auditorId);
        chainWork.setAuditTime(LocalDateTime.now());
        return this.updateById(chainWork);
    }

    @Override
    public int getUserSubmitCount(Long chainActivityId, Long userId) {
        LambdaQueryWrapper<ChainWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainWork::getChainActivityId, chainActivityId);
        wrapper.eq(ChainWork::getUserId, userId);
        return (int) this.count(wrapper);
    }

    @Override
    public List<ChainWork> getChildrenChainWorks(Long parentChainWorkId) {
        LambdaQueryWrapper<ChainWork> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChainWork::getParentChainWorkId, parentChainWorkId);
        wrapper.eq(ChainWork::getAuditStatus, 1);
        wrapper.orderByDesc(ChainWork::getSubmitTime);
        return this.list(wrapper);
    }

    @Override
    public boolean incrementBranchCount(Long chainWorkId) {
        LambdaQueryWrapper<ChainWork> updateWrapper = new LambdaQueryWrapper<>();
        return this.lambdaUpdate()
                .eq(ChainWork::getId, chainWorkId)
                .setSql("branch_count = branch_count + 1")
                .update();
    }
}
