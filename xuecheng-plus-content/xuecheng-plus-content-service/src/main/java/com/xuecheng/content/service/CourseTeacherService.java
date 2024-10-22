package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

public interface CourseTeacherService {
    List<CourseTeacher> getById(Integer id);

    CourseTeacher add(CourseTeacher courseTeacher);

    void delete(Integer courseId, Integer teacherId);
}
