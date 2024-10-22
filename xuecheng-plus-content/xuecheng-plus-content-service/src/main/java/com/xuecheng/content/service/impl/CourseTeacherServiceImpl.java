package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class CourseTeacherServiceImpl implements CourseTeacherService {
    @Resource
    private CourseTeacherMapper courseTeacherMapper;
    @Resource
    private CourseBaseMapper courseBaseMapper;

    @Override
    public List<CourseTeacher> getById(Integer id) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, id);
        return courseTeacherMapper.selectList(queryWrapper);
    }

    @Override
    @Transactional
    public CourseTeacher add(CourseTeacher courseTeacher) {
        // 校验机构id是否匹配
        Long courseId = courseTeacher.getCourseId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        Long companyId = courseBase.getCompanyId();
        if (!companyId.equals(1232141425L)) {
            XueChengPlusException.cast("只允许向机构自己的课程中添加老师、删除老师");
        }
        // 插入
        if (courseTeacher.getId() == null) {
            // 插入
            int insert = courseTeacherMapper.insert(courseTeacher);
            if (insert <= 0) {
                XueChengPlusException.cast("插入失败");

            }
        } else {
            // 更新
            int update = courseTeacherMapper.updateById(courseTeacher);
            if (update <= 0) {
                XueChengPlusException.cast("更新失败");
            }
        }
        return courseTeacherMapper.selectById(courseTeacher.getId());
    }

    @Override
    public void delete(Integer courseId, Integer teacherId) {
        LambdaQueryWrapper<CourseTeacher> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CourseTeacher::getCourseId, courseId).eq(CourseTeacher::getId, teacherId);
        int delete = courseTeacherMapper.delete(queryWrapper);
        if (delete <= 0) {
            XueChengPlusException.cast("删除失败");
        }
    }
}
