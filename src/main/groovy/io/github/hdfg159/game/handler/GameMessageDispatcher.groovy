package io.github.hdfg159.game.handler

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.GameMessage
import io.netty.channel.Channel
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.reactivex.core.Vertx
import io.vertx.reactivex.core.eventbus.EventBus

import static io.github.hdfg159.game.constant.GameConsts.*

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game
 * <p>
 * 游戏消息分发器
 * @date 2020/7/16 11:09
 * @author zhangzhenyu
 */
@Slf4j
class GameMessageDispatcher {
	Vertx vertx
	EventBus eventBus
	
	GameMessageDispatcher(Vertx vertx) {
		this.@vertx = vertx
		eventBus = this.@vertx.eventBus()
	}
	
	void request(Channel channel, GameMessage.Message message) {
		def channelId = channel.id().asLongText()
		// 事件地址
		def address = "${ADDRESS_PROTOCOL}${message.protocol}"
		
		// 抽取data数据请求
		def data = message.data
		def dataBytes = data.toByteArray()
		
		log.trace "request address:[${address}],data:${data}"
		
		// 填充头部信息
		def option = new DeliveryOptions()
		def attrAvatar = channel.attr(ATTR_AVATAR)
		def userId = attrAvatar.get()
		if (userId) {
			option.addHeader(ATTR_NAME_AVATAR, userId)
		}
		option.addHeader(ATTR_NAME_CHANNEL_ID, channelId)
		
		eventBus.send(address, dataBytes, option)
	}
}
