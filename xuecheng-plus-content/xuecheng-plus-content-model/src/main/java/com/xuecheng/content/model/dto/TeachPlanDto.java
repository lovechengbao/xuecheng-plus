package com.xuecheng.content.model.dto;

import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import lombok.*;

import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TeachPlanDto extends Teachplan {
    //媒资管理信息
    private TeachplanMedia teachplanMedia;

    //小章节list
    private List<TeachPlanDto> teachPlanTreeNodes;
}
