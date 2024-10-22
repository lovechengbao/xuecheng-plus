package com.xuecheng.content.api;

import com.xuecheng.content.model.po.CourseTeacher;
import com.xuecheng.content.service.CourseTeacherService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController()
@RequestMapping("/courseTeacher")
public class CourseTeacherController {
    @Resource
    private CourseTeacherService courseTeacherService;

    @GetMapping("/list/{id}")
    public List<CourseTeacher> list(@PathVariable Integer id) {
        return courseTeacherService.getById(id);
    }

    @PostMapping
    public CourseTeacher add(@RequestBody CourseTeacher courseTeacher) {
        return courseTeacherService.add(courseTeacher);
    }

    @DeleteMapping("course/{courseId}/{teacherId}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Integer courseId, @PathVariable Integer teacherId) {
        courseTeacherService.delete(courseId, teacherId);
    }

}
