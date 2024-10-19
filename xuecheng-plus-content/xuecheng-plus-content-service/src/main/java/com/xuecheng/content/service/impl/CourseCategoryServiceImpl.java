package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.service.CourseCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Resource
    private CourseCategoryMapper courseCategoryMapper;


    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream().filter(c -> !id.equals(c.getId())).collect(Collectors.toMap(CourseCategoryTreeDto::getId, v -> v, (k1, k2) -> k2));
        //
        // List<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();
        // courseCategoryTreeDtos.stream().filter(c -> !id.equals(c.getId())).forEach(item -> {
        //     if (id.equals(item.getParentid())) {
        //         courseCategoryList.add(item);
        //     }
        //     // 找到父节点
        //     CourseCategoryTreeDto courseCategoryTreeDto = mapTemp.get(item.getParentid());
        //     if (courseCategoryTreeDto != null) {
        //         if (courseCategoryTreeDto.getChildrenTreeNodes() == null) {
        //             // 如果父节点的childrenNodes为null,则new一个 放入子节点
        //             courseCategoryTreeDto.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
        //         }
        //
        //         courseCategoryTreeDto.getChildrenTreeNodes().add(item);
        //     }
        //
        // });


        return queryTreeNodesRecursive(id, courseCategoryTreeDtos);
        //return courseCategoryList;
    }


    public static List<CourseCategoryTreeDto> queryTreeNodesRecursive(String id, List<CourseCategoryTreeDto> allNodes) {
        List<CourseCategoryTreeDto> result = new ArrayList<>();
        for (CourseCategoryTreeDto node : allNodes) {
            if (id.equals(node.getParentid())) {
                List<CourseCategoryTreeDto> children = queryTreeNodesRecursive(node.getId(), allNodes);
                node.setChildrenTreeNodes(children.isEmpty() ? null : children);
                result.add(node);
            }
        }
        return result;
    }
}
