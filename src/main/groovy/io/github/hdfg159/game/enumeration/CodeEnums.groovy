package io.github.hdfg159.game.enumeration

/**
 * 响应码枚举
 * Project:starter
 * Package:io.github.hdfg159.game.enumeration
 * Created by hdfg159 on 2020/7/15 22:53.
 */
enum CodeEnums {
	// 1~1000 预留编码
	SUCCESS(200),
	ERROR(500),
	REQUEST(1),
	MAX_CONNECTION_LIMIT(2),
	HEART_BEAT(3),
	// 1001-2000 玩家
	/**
	 * 注册信息不合法
	 */
	REGISTER_INFO_ILLEGAL(1001),
	/**
	 * 注册成功
	 */
	REGISTER_SUCCESS(1002),
	/**
	 * 登录成功
	 */
	LOGIN_SUCCESS(1003),
	/**
	 * 已经有账号登录成功
	 */
	EXIST_LOGIN(1004),
	/**
	 * 登录失败
	 */
	LOGIN_FAIL(1005),
	/**
	 * 强制下线
	 */
	FORCE_OFFLINE(1006),
	
	// 2001-3000 其他模块
	
	
	long code
	
	CodeEnums(long code) {
		this.code = code
	}
}