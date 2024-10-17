package com.xuecheng.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;
@Data
@AllArgsConstructor
@ToString
public class PageResult<T> implements Serializable {

    private List<T> items;

    private Long count;

    private Long page;

    private Long pageSize;

}
