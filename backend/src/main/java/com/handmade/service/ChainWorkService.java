package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.ChainWork;
import com.handmade.vo.ChainWorkVO;

import java.util.List;

public interface ChainWorkService extends IService<ChainWork> {
    IPage<ChainWorkVO> getChainWorks(Long chainActivityId, Integer page, Integer size, String sortBy);

    IPage<ChainWorkVO> getMyChainWorks(Long userId, Integer page, Integer size);

    ChainWorkVO getChainWorkDetail(Long id);

    Long submitChainWork(Long chainActivityId, Long workId, Long userId, Long parentChainWorkId, String inspirationRemark);

    boolean auditChainWork(Long id, Integer auditStatus, String auditRemark, Long auditorId);

    int getUserSubmitCount(Long chainActivityId, Long userId);

    List<ChainWork> getChildrenChainWorks(Long parentChainWorkId);

    boolean incrementBranchCount(Long chainWorkId);
}
