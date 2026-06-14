package com.handmade.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.handmade.entity.ChainActivity;
import com.handmade.vo.ChainActivityDetailVO;
import com.handmade.vo.ChainGenealogyVO;

public interface ChainActivityService extends IService<ChainActivity> {
    IPage<ChainActivity> getChainActivityList(Integer page, Integer size, Integer status);

    IPage<ChainActivity> getOngoingChainActivities(Integer page, Integer size);

    ChainActivityDetailVO getChainActivityDetail(Long id);

    Long createChainActivity(ChainActivity chainActivity);

    boolean updateChainActivity(Long id, ChainActivity chainActivity);

    boolean deleteChainActivity(Long id);

    boolean incrementViewCount(Long id);

    ChainGenealogyVO getChainGenealogy(Long chainActivityId);

    boolean incrementWorkCount(Long id);

    boolean incrementParticipantCount(Long id);
}
