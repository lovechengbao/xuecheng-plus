package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.model.dto.TeachPlanDto;
import com.xuecheng.content.model.po.Teachplan;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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


    @Test
    void testMove(){
        //select * from teachplan where parentid = 294 order by orderby
        int flag = 0;
        Teachplan teachPlanOrigin = teachplanMapper.selectById(299);
        QueryWrapper<Teachplan> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parentid",294).eq("course_id",teachPlanOrigin.getCourseId()).orderByAsc("orderby");
        List<Teachplan> teachplans = teachplanMapper.selectList(queryWrapper);
        Long[] array = teachplans.stream()
                .map(Teachplan::getId)
                .toArray(Long[]::new);
        for (int i = 0; i < array.length; i++) {
            if (array[i] == 299) {
                flag = i;
                break;
            }
        }
        Long up = array[flag - 1];
        Long down = array[flag + 1];
        System.out.println(up);
        System.out.println(down);
    }
}
