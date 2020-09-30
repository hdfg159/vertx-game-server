package io.github.hdfg159.game.enumeration

import com.google.protobuf.Message
import io.github.hdfg159.game.constant.GameConsts
import io.github.hdfg159.game.domain.dto.GameMessage

/**
 * 接口协议枚举
 * Project:starter
 * Package:io.github.hdfg159.game.enumeration
 * Created by hdfg159 on 2020/7/15 22:48.
 */
enum ProtocolEnums {
	// 1-1000 系统预留
	/**
	 * 1~1000预留编码
	 */
	REQ_PUSH(1, Object.class),
	RES_PUSH(-1, Object.class),
	/**
	 * 心跳
	 */
	REQ_HEART_BEAT(2, GameMessage.HeartBeatReq.class),
	RES_HEART_BEAT(-2, GameMessage.HeartBeatRes.class),
	
	// 1001-2000 玩家
	/**
	 * 下线
	 */
	REQ_OFFLINE(1001, GameMessage.OfflineReq.class),
	RES_OFFLINE(-1001, GameMessage.OfflineRes.class),
	/**
	 * 登录
	 */
	REQ_LOGIN(1002, GameMessage.LoginReq.class),
	RES_LOGIN(-1002, GameMessage.LoginRes.class),
	/**
	 * 注册
	 */
	REQ_REGISTER(1003, GameMessage.RegisterReq.class),
	RES_REGISTER(-1003, GameMessage.RegisterReq.class),
	
	/**
	 * 1000~1099 预留编码
	 */
	REQ_TEST(9999999, GameMessage.TestReq.class),
	RES_TEST(-9999999, GameMessage.TestRes.class),
	
	long protocol
	Class<? extends Message> requestClass
	
	ProtocolEnums(long protocol, Class<? extends Message> requestClass) {
		this.protocol = protocol
		this.requestClass = requestClass
	}
	
	String address() {
		"${GameConsts.ADDRESS_PROTOCOL}${protocol}"
	}
}