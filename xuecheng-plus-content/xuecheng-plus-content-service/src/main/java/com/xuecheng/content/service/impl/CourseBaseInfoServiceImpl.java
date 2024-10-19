package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.injector.methods.SelectPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Service
@Slf4j
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {
    @Resource
    private CourseBaseMapper courseBaseMapper;
    @Resource
    private CourseMarketMapper courseMarketMapper;
    @Resource
    private CourseCategoryMapper courseCategoryMapper;


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
        //参数的合法性校验
        if (StringUtils.isBlank(addCourseDto.getName())) {
            throw new RuntimeException("课程名称为空");
        }

        if (StringUtils.isBlank(addCourseDto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(addCourseDto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(addCourseDto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(addCourseDto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(addCourseDto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }


        //向课程基本信息表course_base写入数据
        CourseBase courseBase = new CourseBase();
        BeanUtils.copyProperties(addCourseDto,courseBase);
        courseBase.setCompanyId(companyId);
        courseBase.setCreateDate(LocalDateTime.now());
        courseBase.setAuditStatus("202002");
        courseBase.setStatus("203001");
        int insert = courseBaseMapper.insert(courseBase);
        if (insert <= 0){
            throw new RuntimeException("添加课程失败");
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

    public int saveCourseMarket(CourseMarket courseMarket) {
        //收费规则
        String charge = courseMarket.getCharge();
        if(StringUtils.isBlank(charge)){
            throw new RuntimeException("收费规则没有选择");
        }
        //收费规则为收费
        if(charge.equals("201001")){
            if(courseMarket.getPrice() == null || courseMarket.getPrice().floatValue()<=0){
                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
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
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            return null;
        }

        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null){
            return null;
        }

        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);

        //查询mtname
        CourseCategory st = courseCategoryMapper.selectById(courseBase.getSt());
        CourseCategory mt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setStName(st.getName());
        courseBaseInfoDto.setStName(mt.getName());

        return courseBaseInfoDto;

    }
}
