package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
public class TeachPlanMapperTest {
    @Resource
    private TeachplanMapper teachplanMapper;

    @Test
    void testSelectTreeNodes(){
        List<TeachPlanDto> teachPlanDtos = teachplanMapper.selectTreeNodes(117L);
        System.out.println(teachPlanDtos);
    }

    @Test
    void testOrderBy(){
        QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("max(orderby) as orderby").eq("course_id", 117).eq("grade",2);
        Teachplan teachplan = teachplanMapper.selectOne(queryWrapper);
        System.out.println(teachplan.getOrderby());
    }
}
