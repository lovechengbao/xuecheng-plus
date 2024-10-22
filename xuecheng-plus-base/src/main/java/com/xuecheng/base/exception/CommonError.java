
package com.xuecheng.base.exception;


import lombok.Getter;

/**
 * @description 通用错误信息
 * @author Mr.M
 * @date 2022/9/6 11:29
 * @version 1.0
 */
@Getter
public enum CommonError {

	UNKOWN_ERROR("执行过程异常，请重试。"),
	PARAMS_ERROR("非法参数"),
	OBJECT_NULL("对象为空"),
	QUERY_NULL("查询结果为空"),
	REQUEST_NULL("请求参数为空"),
	DUPLICATE_KEY("教师姓名不能重复重复");

	private String errMessage;

    private CommonError( String errMessage) {
		this.errMessage = errMessage;
	}

}
