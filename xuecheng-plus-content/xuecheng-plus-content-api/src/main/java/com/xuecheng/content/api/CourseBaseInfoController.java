package com.xuecheng.content.api;

import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.service.CourseBaseInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@Api("课程信息管理接口")
public class CourseBaseInfoController {
    @Resource
    private CourseBaseInfoService courseBaseInfoService;

    @ApiOperation("课程查询接口")
    @PostMapping("/course/list")
    public PageResult<CourseBase> list(PageParams pageParams, @RequestBody(required = false) QueryCourseParamsDto queryCourseParamsDto){
        return courseBaseInfoService.queryCourseBaseList(pageParams,queryCourseParamsDto);
    }

    @ApiOperation("新增课程")
    @PostMapping("/course")
    public CourseBaseInfoDto createCourseBase(@RequestBody AddCourseDto addCourseDto){
        Long companyId = 1L;
        return courseBaseInfoService.createCourseBase(companyId,addCourseDto);
    }
}
