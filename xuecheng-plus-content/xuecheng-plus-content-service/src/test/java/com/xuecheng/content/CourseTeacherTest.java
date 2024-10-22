package com.xuecheng.content;

import com.xuecheng.content.mapper.CourseTeacherMapper;
import com.xuecheng.content.model.po.CourseTeacher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
public class CourseTeacherTest {
    @Resource
    private CourseTeacherMapper courseTeacherMapper;


    @Test
    void testAdd(){
        CourseTeacher courseTeacher = new CourseTeacher();
        courseTeacher.setCourseId(72L);
        courseTeacher.setTeacherName("傻逼");
        courseTeacher.setIntroduction("hhhh");
        courseTeacher.setPosition("11");

        courseTeacherMapper.insert(courseTeacher);

        Long id = courseTeacher.getId();
        System.out.println(id);
    }
}
