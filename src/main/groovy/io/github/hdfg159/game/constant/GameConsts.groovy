package io.github.hdfg159.game.constant

import io.netty.util.AttributeKey

import java.time.Duration

/**
 * 游戏常量枚举
 * Project:starter
 * Package:io.github.hdfg159.game.constant
 * Created by hdfg159 on 2020/7/14 23:16.
 */
class GameConsts {
	/**
	 * 用户ID属性值
	 */
	static final String ATTR_NAME_AVATAR = 'ATTR_AVATAR'
	/**
	 * 当前通道ID
	 */
	static final String ATTR_NAME_CHANNEL_ID = 'ATTR_CHANNEL_ID'
	/**
	 * 用户ID
	 */
	static final AttributeKey<String> ATTR_AVATAR = AttributeKey.valueOf(ATTR_NAME_AVATAR)
	
	/**
	 * 协议前缀
	 */
	static final String ADDRESS_PROTOCOL = 'PROTOCOL.'
	/**
	 * 事件前缀
	 */
	static final String ADDRESS_EVENT = 'EVENT.'
	
	/**
	 * 缓存访问过期时间
	 */
	static final Duration CACHE_ACCESS_TIME_OUT = Duration.ofSeconds(60 * 10)
	
	/**
	 * mongodb 共享数据源名称
	 */
	static final String MONGO_DATA_SOURCE = 'common'
	/**
	 * mongodb 配置文件相对路径
	 */
	static final String MONGO_CONFIG = 'config/mongodb.json'
	/**
	 * 服务器配置文件相对路径
	 */
	static final String SERVER_CONFIG = 'config/server.json'
}
