package io.github.hdfg159.web.enumeration;

/**
 * Project:project
 * <p>
 * Package:com.sevenbillion.common.enumeration
 * <p>
 *
 * @author zhangzhenyu* @date 2020/7/1 9:47
 */
enum ErrorCodeEnums {
	/**
	 * 预留编码表 -1 ~ 9999 通用编码
	 */
	FAIL(0),
	SUCCESS(1),
	PARAM_ERROR(2),
	UNKNOWN_ERROR(3),
	
	long errorCode
	
	ErrorCodeEnums(long errorCode) {
		this.errorCode = errorCode
	}
}
