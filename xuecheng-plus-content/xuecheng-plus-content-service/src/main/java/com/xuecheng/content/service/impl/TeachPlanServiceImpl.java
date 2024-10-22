package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanErrorDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachPlanService;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class TeachPlanServiceImpl implements TeachPlanService {
    @Resource
    private TeachplanMapper teachplanMapper;
    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;

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
            if (orderBy != null) {
                Integer order = orderBy.getOrderby();
                teachplan.setOrderby(order + 1);
            }else {
                teachplan.setOrderby(1);
            }
            teachplanMapper.insert(teachplan);
        }else {
            //修改
            Teachplan teachplan = teachplanMapper.selectById(teachPlanDto.getId());
            BeanUtils.copyProperties(teachPlanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    @Transactional
    public TeachPlanErrorDto deleteTeachPlanById(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        if (teachplan == null){
            return new TeachPlanErrorDto("120409","课程计划不存在");
        }

        Long parentId = teachplan.getParentid();
        if (parentId == 0){
            // 父级
            QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
            queryWrapper.select().eq("parentid",teachplan.getId());
            Teachplan childrenPlan = teachplanMapper.selectOne(queryWrapper);
            if (childrenPlan != null){
                return new TeachPlanErrorDto("120409","课程计划存在子级，无法删除");
            }
            int result = teachplanMapper.deleteById(id);
            if (result <= 0){
                return new TeachPlanErrorDto("120409","删除失败");
            }
        }else {
            // 子级
            teachplanMapper.deleteById(id);
            QueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new QueryWrapper<>();
            teachplanMediaQueryWrapper.select().eq("teachplan_id",id);
            int delete = teachplanMediaMapper.delete(teachplanMediaQueryWrapper);
            if (delete < 0){
                return new TeachPlanErrorDto("120409","删除失败");
            }
        }
        return null;
    }

    @Override
    public void moveTeachPlan(String moveType, Long id) {
        //select * from teachplan where parentid = 294 and course_id = 1 order by orderby
        Teachplan teachPlanOrigin = teachplanMapper.selectById(id);
        //当前节点id位置
        int flag = 0;
        //按顺序查询当前节点
        QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentid",teachPlanOrigin.getParentid()).eq("course_id",teachPlanOrigin.getCourseId()).orderByAsc("orderby");
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        // 转换为数组
        Long[] array = teachplans.stream()
                .map(Teachplan::getId)
                .toArray(Long[]::new);
        //找到当前节点前一位和后一位的id
        for (int i = 0; i < array.length; i++) {
            if (Objects.equals(array[i], id)) {
                flag = i;
                break;
            }
        }

        if ("moveup".equals(moveType)){
            //上移
            if (flag == 0){
                XueChengPlusException.cast("当前小节已经首位，无法上移");
            }
            //获取前一位的id
            Long up = array[flag - 1];
            Teachplan teachPlanUp = new Teachplan();
            teachplans.forEach(v -> {
                if (v.getId().equals(up)){
                    BeanUtils.copyProperties(v, teachPlanUp);
                }
            });

            int orderBy = teachPlanOrigin.getOrderby();
            teachPlanOrigin.setOrderby(teachPlanUp.getOrderby());
            teachPlanUp.setOrderby(orderBy);
            teachplanMapper.updateById(teachPlanOrigin);
            teachplanMapper.updateById(teachPlanUp);

        }else if ("movedown".equals(moveType)){
            //下移
            if (flag == array.length - 1){
                XueChengPlusException.cast("当前小节已经末位，无法下移");
            }
            //获取前一位的id
            Long up = array[flag + 1];
            Teachplan teachPlanUp = new Teachplan();
            teachplans.forEach(v -> {
                if (v.getId().equals(up)){
                    BeanUtils.copyProperties(v, teachPlanUp);
                }
            });

            int orderBy = teachPlanOrigin.getOrderby();
            teachPlanOrigin.setOrderby(teachPlanUp.getOrderby());
            teachPlanUp.setOrderby(orderBy);
            teachplanMapper.updateById(teachPlanOrigin);
            teachplanMapper.updateById(teachPlanUp);
        }


    }
}
