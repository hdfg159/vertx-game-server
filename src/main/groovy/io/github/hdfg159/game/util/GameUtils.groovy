package io.github.hdfg159.game.util

import com.google.protobuf.Any
import com.google.protobuf.Message
import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.EventMessage
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.enumeration.CodeEnums
import io.github.hdfg159.game.enumeration.EventEnums
import io.github.hdfg159.game.enumeration.ProtocolEnums
import io.vertx.reactivex.core.eventbus.EventBus

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game.domain.dto
 * <p>
 * 接口响应类
 * @date 2020/7/15 17:26
 * @author zhangzhenyu
 */
@Slf4j
abstract class GameUtils {
	/**
	 * 默认200码的响应数据构造
	 * @param protocolEnums 协议枚举
	 * @param data 消息数据主体
	 * @return 消息
	 */
	static GameMessage.Message sucResMsg(ProtocolEnums protocolEnums, Message data) {
		def builder = GameMessage.Message.newBuilder()
		builder.setProtocol(protocolEnums.protocol)
				.setCode(CodeEnums.SUCCESS.code)
		if (data) {
			builder.setData(Any.pack(data))
		}
		builder.build()
	}
	
	/**
	 * 请求信息构造
	 * @param protocolEnums 协议枚举
	 * @param data 消息数据主体
	 * @return 消息
	 */
	static GameMessage.Message reqMsg(ProtocolEnums protocolEnums, Message data) {
		def builder = GameMessage.Message.newBuilder()
		builder.setProtocol(protocolEnums.protocol)
		if (data) {
			builder.setData(Any.pack(data))
		}
		builder.build()
	}
	
	/**
	 * 响应数据构造
	 * @param protocolEnums 协议枚举
	 * @param codeEnums 响应码枚举
	 * @param data 消息数据主体
	 * @return 消息
	 */
	static GameMessage.Message resMsg(ProtocolEnums protocolEnums, CodeEnums codeEnums, Message data) {
		def builder = GameMessage.Message.newBuilder()
		
		builder.setProtocol(protocolEnums.protocol)
				.setCode(codeEnums.code)
		if (data) {
			builder.setData(Any.pack(data))
		}
		builder.build()
	}
	
	/**
	 * 响应空数据构造
	 * @param protocolEnums 协议枚举
	 * @param codeEnums 响应码枚举
	 * @return 消息
	 */
	static GameMessage.Message resMsg(ProtocolEnums protocolEnums, CodeEnums codeEnums) {
		GameMessage.Message.newBuilder()
				.setProtocol(protocolEnums.protocol)
				.setCode(codeEnums.code)
				.build()
	}
	
	/**
	 * 发布事件
	 * @param enums 事件枚举
	 * @param data 事件数据
	 */
	static void publishEvent(EventBus eventBus, EventEnums enums, Message data) {
		def builder = EventMessage.Event.newBuilder()
		builder.setEvent(enums.event)
		if (data) {
			builder.setData(Any.pack(data))
		}
		def event = builder.build()
		def address = enums.address()
		
		log.trace("${address} publish event:${event},data:${data}")
		
		eventBus.publish(address, event.toByteArray())
	}
}
