package io.github.hdfg159.game.enumeration

import com.google.protobuf.Message
import io.github.hdfg159.game.constant.GameConsts
import io.github.hdfg159.game.domain.dto.EventMessage

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game.enumeration
 * <p>
 * 事件源枚举
 *
 * @date 2020/7/16 11:46
 * @author zhangzhenyu
 */
enum EventEnums {
	/**
	 * 玩家下线时间
	 */
	OFFLINE(1, EventMessage.Offline.class),
	/**
	 * 上线
	 */
	ONLINE(2, EventMessage.Online.class),
	
	/**
	 * 事件值
	 */
	long event
	/**
	 * 事件对应class
	 */
	Class<? extends Message> clazz
	
	EventEnums(long event, Class clazz) {
		this.event = event
		this.clazz = clazz
	}
	
	String address() {
		"${GameConsts.ADDRESS_EVENT}${event}"
	}
}