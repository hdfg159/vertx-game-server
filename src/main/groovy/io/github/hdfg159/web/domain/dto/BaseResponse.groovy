package io.github.hdfg159.web.domain.dto

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import groovy.transform.builder.Builder
import io.vertx.core.json.Json
import io.vertx.reactivex.ext.web.RoutingContext

import java.time.LocalDateTime

import static io.github.hdfg159.web.enumeration.ErrorCodeEnums.FAIL
import static io.github.hdfg159.web.enumeration.ErrorCodeEnums.SUCCESS

/**
 * @author zhangzhenyu* @date 2019-6-10 11:56
 */
@Builder
@ToString
@EqualsAndHashCode
class BaseResponse<T> implements Serializable {
	public static final String STR_SUCCESS = "SUCCESS"
	public static final String STR_FAIL = "FAIL"
	
	/**
	 * 通用返回格式的数据字段
	 */
	T data
	/**
	 * 通用返回格式的信息部分
	 */
	String message
	/**
	 * 实体类属性使用包装类型
	 * 通用返回格式的代码
	 */
	Long code
	/**
	 * 错误异常名称
	 */
	String exception
	/**
	 * 系统时间
	 */
	LocalDateTime time
	
	/**
	 * 成功时返回的JSON数据
	 *
	 * @return {@code BaseResponse<String>}
	 */
	static BaseResponse<String> success() {
		builder()
				.code(SUCCESS.getErrorCode())
				.message(STR_SUCCESS)
				.time(LocalDateTime.now())
				.build()
	}
	
	/**
	 * 成功时返回的JSON数据
	 *
	 * @param data
	 * 		数据
	 * @param < T >
	 * 		类型
	 *
	 * @return {@code BaseResponse<                                             ?                                             >}
	 */
	static <T> BaseResponse<T> success(T data) {
		builder()
				.code(SUCCESS.getErrorCode())
				.message(STR_SUCCESS)
				.data(data)
				.time(LocalDateTime.now())
				.build()
	}
	
	/**
	 * 失败时返回的JSON数据
	 *
	 * @param data
	 * 		数据
	 * @param < T >
	 * 		类型
	 *
	 * @return {@code BaseResponse<                                             ?                                             >}
	 */
	static <T> BaseResponse<T> fail(T data) {
		builder()
				.code(FAIL.getErrorCode())
				.message(STR_FAIL)
				.data(data)
				.time(LocalDateTime.now())
				.build()
	}
	
	/**
	 * 失败时返回的JSON数据，携带业务错误码、错误信息 message，不带数据
	 *
	 * @param code
	 * 		错误返回码
	 * @param message
	 * 		错误异常信息 message
	 * @param exception
	 * 		错误异常名称
	 *
	 * @return {@code BaseResponse<String>}
	 */
	static BaseResponse<String> fail(Long code, String message, String exception) {
		builder()
				.code(code)
				.message(message)
				.exception(exception)
				.time(LocalDateTime.now())
				.build()
	}
	
	/**
	 * 失败时返回的JSON数据，携带其他通用错误码、错误信息 message，不带数据
	 *
	 * @param message
	 * 		错误信息message
	 * @param exception
	 * 		错误异常名称
	 *
	 * @return {@code BaseResponse<String>}
	 */
	static BaseResponse<String> fail(String message, String exception) {
		builder()
				.code(FAIL.getErrorCode())
				.message(message)
				.exception(exception)
				.time(LocalDateTime.now())
				.build()
	}
	
	def response(RoutingContext context, int statusCode) {
		context.response()
				.putHeader("content-type", "application/json")
				.setStatusCode(statusCode)
				.end(Json.encode(this))
	}
}
