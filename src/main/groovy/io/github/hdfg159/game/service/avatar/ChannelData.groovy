package io.github.hdfg159.game.service.avatar

import groovy.util.logging.Slf4j
import io.netty.channel.Channel

import java.util.concurrent.ConcurrentHashMap

import static io.github.hdfg159.game.constant.GameConsts.ATTR_AVATAR

/**
 * Project:starter
 * Package:io.github.hdfg159.game.service.avatar
 * Created by hdfg159 on 2020/7/19 0:20.
 */
@Slf4j
@Singleton
class ChannelData {
	/**
	 * [通道ID:通道]
	 */
	Map<String, Channel> channelMap = new ConcurrentHashMap<>()
	
	/**
	 * 添加通道
	 * @param channel 通道
	 * @return 通道
	 */
	Channel add(Channel channel) {
		channel ? channelMap.put(channel.id().asLongText(), channel) : null
	}
	
	/**
	 * 移除通道
	 * @param channel 通道
	 * @return 通道
	 */
	Channel remove(Channel channel) {
		// 顺带清除通道的玩家属性值
		channel?.attr(ATTR_AVATAR)?.set(null)
		channel ? channelMap.remove(channel.id().asLongText()) : null
	}
	
	/**
	 * 移除通道
	 * @param channelId 通道ID
	 * @return 通道
	 */
	Channel remove(String channelId) {
		def channel = channelMap.get(channelId)
		remove(channel)
	}
}
