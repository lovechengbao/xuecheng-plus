package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachPlanDto;
import com.xuecheng.content.model.dto.TeachPlanDto;

import java.util.List;

public interface TeachPlanService {
    List<TeachPlanDto> getTreeNodes(Long courseId);

    void saveTeachPlan(SaveTeachPlanDto teachPlan);
}
