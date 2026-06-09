package com.handmade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.handmade.entity.Work;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WorkMapper extends BaseMapper<Work> {

    @Update("UPDATE work SET favorite_count = GREATEST(0, favorite_count + #{delta}) WHERE id = #{workId} AND status = 1")
    int updateFavoriteCountAtomic(Long workId, int delta);

    @Update("UPDATE work SET view_count = view_count + 1 WHERE id = #{workId} AND status = 1")
    int incrementViewCountAtomic(Long workId);
}
