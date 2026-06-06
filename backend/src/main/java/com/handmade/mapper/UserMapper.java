package com.handmade.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.handmade.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
