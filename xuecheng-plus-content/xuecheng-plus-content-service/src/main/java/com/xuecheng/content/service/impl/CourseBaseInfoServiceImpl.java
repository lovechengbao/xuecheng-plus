package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.*;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.EditCourseDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.*;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Resource
    private CourseBaseMapper courseBaseMapper;
    @Resource
    private CourseMarketMapper courseMarketMapper;
    @Resource
    private CourseCategoryMapper courseCategoryMapper;
    @Resource
    private TeachplanMapper teachplanMapper;
    @Resource
    private TeachplanMediaMapper teachplanMediaMapper;
    @Resource
    private CourseTeacherMapper courseTeacherMapper;


    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        //封装查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName,courseParamsDto.getCourseName());
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()),CourseBase::getAuditStatus,courseParamsDto.getAuditStatus());
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()),CourseBase::getStatus,courseParamsDto.getPublishStatus());
        //分页查询
        Page<CourseBase> page = new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        Page<CourseBase> courseBasePage = courseBaseMapper.selectPage(page, queryWrapper);

        //封装结果
        PageResult<CourseBase> pageResult = new PageResult<>();
        pageResult.setItems(courseBasePage.getRecords());
        pageResult.setPage(courseBasePage.getCurrent());
        pageResult.setCounts(courseBasePage.getTotal());
        pageResult.setPageSize(courseBasePage.getPages());

        return pageResult;
    }

    @Override
    @Transactional
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto addCourseDto) {

        //向课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0){
            XueChengPlusException.cast("添加课程失败");
        }

        //向课程营销表course_market写入数据
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(addCourseDto,courseMarket);
        Long courseId = courseBase.getId();
        courseMarket.setId(courseId);
        //保存营销信息
        int result = saveCourseMarket(courseMarket);

        return getCourseBaseInfo(courseId);
    }

    @Override
    public CourseBaseInfoDto queryCourseById(Long courseId) {
        return getCourseBaseInfo(courseId);
    }

    @Override
    @Transactional
    public CourseBaseInfoDto updateCourseBase(EditCourseDto editCourseDto) {
        Long courseId = editCourseDto.getId();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        Long companyId = editCourseDto.getCompanyId();
        if (1232141425L != companyId) {
            XueChengPlusException.cast("只允许本机构修改本课程机构不匹配");
        }

        BeanUtils.copyProperties(editCourseDto,courseBase);
        courseBase.setChangeDate(LocalDateTime.now());
        int update = courseBaseMapper.updateById(courseBase);
        if (update <= 0){
            XueChengPlusException.cast("更新课程失败");
        }
        //更新营销信息
        CourseMarket courseMarket = new CourseMarket();
        BeanUtils.copyProperties(editCourseDto,courseMarket);
        int updateMarket = saveCourseMarket(courseMarket);
        if (updateMarket <= 0){
            XueChengPlusException.cast("更新营销信息失败");
        }


        //向课程基本信息表course_base写入数据

        return getCourseBaseInfo(courseId);
    }

    @Override
    @Transactional
    public void deleteCourseById(Long courseId) {
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null) {
            XueChengPlusException.cast("课程不存在");
        }

        if (!courseBase.getCompanyId().equals(1232141425L)){
            XueChengPlusException.cast("只允许本机构删除本课程机构不匹配");
        }

        if (!Objects.equals("202002", courseBase.getAuditStatus())){
            XueChengPlusException.cast("课程的审核状态为未提交时方可删除");
        }

        //删除课程信息
        courseBaseMapper.deleteById(courseId);
        //删除课程营销信息
        LambdaQueryWrapper<CourseMarket> courseMarketQueryWrapper = new LambdaQueryWrapper<>();
        courseMarketQueryWrapper.eq(CourseMarket::getId, courseId);
        courseMarketMapper.delete(courseMarketQueryWrapper);
        //删除课程计划信息
        LambdaQueryWrapper<Teachplan> teachplanQueryWrapper = new LambdaQueryWrapper<>();
        teachplanQueryWrapper.eq(Teachplan::getCourseId, courseId);
        teachplanMapper.delete(teachplanQueryWrapper);
        //删除课程计划媒资信息
        LambdaQueryWrapper<TeachplanMedia> teachplanMediaQueryWrapper = new LambdaQueryWrapper<>();
        teachplanMediaQueryWrapper.eq(TeachplanMedia::getCourseId, courseId);
        teachplanMediaMapper.delete(teachplanMediaQueryWrapper);
        //删除课程教师信息
        LambdaQueryWrapper<CourseTeacher> courseTeacherQueryWrapper = new LambdaQueryWrapper<>();
        courseTeacherQueryWrapper.eq(CourseTeacher::getCourseId, courseId);
        courseTeacherMapper.delete(courseTeacherQueryWrapper);

    }

    public int saveCourseMarket(CourseMarket courseMarket) {
        //收费规则
        String charge = courseMarket.getCharge();
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() <=0){
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
            }
        }
        //收费规则为免费
        if(charge.equals("201000")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice() !=0){
                XueChengPlusException.cast("课程为收费价格不能为空且必须等于0");
            }
        }
        //根据id从课程营销表查询
        CourseMarket courseMarketObj = courseMarketMapper.selectById(courseMarket.getId());
        if(courseMarketObj == null){
            return courseMarketMapper.insert(courseMarket);
        }else{
            BeanUtils.copyProperties(courseMarket,courseMarketObj);
            courseMarketObj.setId(courseMarket.getId());
            return courseMarketMapper.updateById(courseMarketObj);
        }
    }

    public CourseBaseInfoDto getCourseBaseInfo(Long courseId){
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            XueChengPlusException.cast("课程信息不存在");
        }
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }

        //查询mtname
        CourseCategory st = courseCategoryMapper.selectById(courseBase.getSt());
        CourseCategory mt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setStName(st.getName());
        courseBaseInfoDto.setMtName(mt.getName());

        return courseBaseInfoDto;

    }
}
