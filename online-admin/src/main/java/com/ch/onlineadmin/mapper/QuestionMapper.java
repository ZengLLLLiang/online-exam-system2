package com.wzz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wzz.entity.Question;
import org.springframework.stereotype.Repository;


@Repository//代表持久层
public interface QuestionMapper extends BaseMapper<Question> {
}