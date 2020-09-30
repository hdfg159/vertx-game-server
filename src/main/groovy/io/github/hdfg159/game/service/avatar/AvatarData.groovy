package io.github.hdfg159.game.service.avatar

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.data.AbstractDataManager
import io.netty.channel.Channel
import io.reactivex.Completable
import io.reactivex.Single
import io.vertx.core.json.JsonObject

import java.util.concurrent.ConcurrentHashMap

import static io.reactivex.schedulers.Schedulers.io

/**
 * Project:starter
 * Package:io.github.hdfg159.game.service.avatar.dao
 * Created by hdfg159 on 2020/7/17 23:54.
 */
@Slf4j
@Singleton
class AvatarData extends AbstractDataManager<Avatar> {
	/**
	 * 通道信息
	 */
	private ChannelData channelData = ChannelData.instance
	/**
	 * [用户ID:通道ID]
	 */
	private Map<String, String> userChannel = new ConcurrentHashMap<>()
	/**
	 * [用户ID:用户名]
	 */
	private Map<String, String> userIdNameMap = new ConcurrentHashMap<>()
	/**
	 * [用户名:用户ID]
	 */
	private Map<String, String> usernameIdMap = new ConcurrentHashMap<>()
	
	/**
	 * 通道上线
	 * @param userId 用户ID
	 * @param channel 通道
	 * @return String
	 */
	String onlineChannel(String userId, String channel) {
		if (!userId || !channel) {
			return null
		}
		
		getById(userId)?.lastLoginTime = new Date()
		
		userChannel.put(userId, channel)
	}
	
	/**
	 * 通道下线
	 * @param userId 用户ID
	 * @return String 用户ID
	 */
	String offlineChannel(String userId) {
		def removeChannelId = userChannel.remove(userId)
		if (removeChannelId) {
			channelData.remove(removeChannelId)
		}
		if (userId) {
			def avatar = getById(userId)
			avatar?.lastOfflineTime = new Date()
		}
		userId
	}
	
	/**
	 * 不要随便调用
	 * @param avatar 玩家信息
	 * @return Avatar
	 */
	Avatar addGlobalCache(Avatar avatar) {
		if (!avatar) {
			return null
		}
		
		def id = avatar.id
		def username = avatar.username
		userIdNameMap.put(id, username)
		usernameIdMap.put(username, id)
		
		avatar
	}
	
	/**
	 * 读取所有用户到全局缓存
	 */
	private Completable loadAllToCache() {
		// defer 重新包装，因为 client 还是空对象，就要 rxFind 包装会出现空指针，延迟包装
		Single
				.defer({
					// defer返回 single 延迟化 rxFind 操作
					client.rxFind(collectionName(), new JsonObject([:]))
				})
				.flattenAsFlowable({it})
				.map({
					it.mapTo(clazz())
				})
				.flatMapCompletable({avatar ->
					// flatMap + subscribeOn 并行操作,10并行
					Completable.fromRunnable({addGlobalCache(avatar)}).subscribeOn(io())
				}, false, 10)
				.doOnComplete({
					log.debug "load user global cache success"
				})
	}
	
	/**
	 * 获取相对应的用户通道
	 * @param userId 用户ID
	 * @return 通道
	 */
	Channel getChannel(String userId) {
		if (!userId) {
			return null
		}
		
		def channelId = userChannel.get(userId)
		if (!channelId) {
			return null
		}
		
		channelData.channelMap.get(channelId)
	}
	
	/**
	 * 获取相对应的用户通道ID
	 * @param userId 用户ID
	 * @return 通道ID
	 */
	String getChannelId(String userId) {
		if (!userId) {
			return null
		}
		userChannel.get(userId)
	}
	
	/**
	 * 通过用户名查询用户数据
	 * @param username 用户名
	 * @return 用户数据
	 */
	Avatar getByUsername(String username) {
		if (username) {
			def id = usernameIdMap.get(username)
			if (id) {
				return cache.get(id)
			}
		}
		return null
	}
	
	@Override
	Class<Avatar> clazz() {
		Avatar.class
	}
	
	@Override
	Completable rxStart() {
		// 等到 rxStart() 完全启动再执行下面 Completable
		super.rxStart().concatWith(loadAllToCache())
	}
	
	@Override
	Completable rxStop() {
		Completable.fromCallable({
			// 设置所有玩家离线时间
			allOnlineIds.each {id ->
				getById(id)?.lastOfflineTime = new Date()
			}
		}).subscribeOn(io()).concatWith(super.rxStop())
	}
	
	/**
	 * 获取所有在线用户ID
	 * @return Set<String>
	 */
	def getAllOnlineIds() {
		this.userChannel.keySet()
	}
	
	/**
	 * 获取所有在线用户对应的连接通道
	 * @return Set<Channel>
	 */
	def getAllOnlineChannels() {
		this.userChannel
				.values()
				.collect {
					channelData.channelMap.get(it)
				}
				.findAll {
					it != null
				}
				.toSet()
	}
	
	/**
	 * 玩家是否在线
	 * @param id id
	 */
	def isOnline(String id) {
		allOnlineIds.contains(id)
	}
}
