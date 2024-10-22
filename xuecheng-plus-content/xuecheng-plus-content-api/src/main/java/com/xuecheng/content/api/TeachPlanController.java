package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanErrorDto;
import com.xuecheng.content.service.TeachPlanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Api(value = "课程计划编辑接口",tags = "课程计划编辑接口")
public class TeachPlanController {
    @Resource
    private TeachPlanService teachPlanService;

    @GetMapping("/teachplan/{courseId}/tree-nodes")
    @ApiOperation("查询课程计划树形结构")
    public List<TeachPlanDto> getTreeNodes(@PathVariable Long courseId){
        return teachPlanService.getTreeNodes(courseId);
    }

    @PostMapping("/teachplan")
    @ApiOperation("查询课程计划树形结构")
    public void saveTeachPlan(@RequestBody SaveTeachPlanDto teachPlan){
        teachPlanService.saveTeachPlan(teachPlan);
    }

    @DeleteMapping("/teachplan/{id}")
    public TeachPlanErrorDto deleteTeachPlan(@PathVariable Long id){
        return teachPlanService.deleteTeachPlanById(id);
    }

    @PostMapping("/teachplan/{moveType}/{id}")
    public void moveTeachPlan(@PathVariable("moveType") String moveType,@PathVariable Long id){
        teachPlanService.moveTeachPlan(moveType,id);
    }
}
