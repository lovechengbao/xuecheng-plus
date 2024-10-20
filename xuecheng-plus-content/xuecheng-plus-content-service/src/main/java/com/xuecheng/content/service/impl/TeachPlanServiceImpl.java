package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Resource
    private TeachplanMapper teachplanMapper;

    @Override
    public List<TeachPlanDto> getTreeNodes(Long courseId) {
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachPlanDtos;
    }

    @Override
    public void saveTeachPlan(SaveTeachPlanDto teachPlanDto) {
        Long teachPlanId = teachPlanDto.getId();
        if (teachPlanId == null) {
            //新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(teachPlanDto, teachplan);
            teachplan.setCreateDate(LocalDateTime.now());
            //确定排序字段   select max(orderby) from teachplan where course_id = 117 and grade = 2
            QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("max(orderby) as orderby").eq("course_id", teachPlanDto.getCourseId()).eq("grade", teachPlanDto.getGrade());
            Teachplan orderBy = teachplanMapper.selectOne(queryWrapper);
            Integer order = orderBy.getOrderby();
            teachplan.setOrderby(order + 1);

            teachplanMapper.insert(teachplan);
        }else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachPlanDto.getId());
            BeanUtils.copyProperties(teachPlanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }
}
