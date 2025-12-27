package com.wzz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzz.entity.User;
import org.springframework.stereotype.Repository;


@Repository//代表持久层
public interface UserMapper extends BaseMapper<User> {
}
