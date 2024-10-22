package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanErrorDto;

import java.util.List;

public interface TeachPlanService {
    List<TeachPlanDto> getTreeNodes(Long courseId);

    void saveTeachPlan(SaveTeachPlanDto teachPlan);

    TeachPlanErrorDto deleteTeachPlanById(Long id);

    void moveTeachPlan(String moveType, Long id);
}
