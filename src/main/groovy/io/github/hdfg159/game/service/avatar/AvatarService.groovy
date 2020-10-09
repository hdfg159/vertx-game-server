package io.github.hdfg159.game.service.avatar

import com.google.protobuf.Message
import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.EventMessage
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.enumeration.EventEnums
import io.github.hdfg159.game.enumeration.ProtocolEnums
import io.github.hdfg159.game.service.AbstractService
import io.github.hdfg159.game.util.GameUtils
import io.reactivex.Completable
import io.vertx.reactivex.core.MultiMap

import static io.github.hdfg159.game.constant.GameConsts.ATTR_AVATAR
import static io.github.hdfg159.game.constant.GameConsts.ATTR_NAME_CHANNEL_ID
import static io.github.hdfg159.game.enumeration.CodeEnums.*
import static io.github.hdfg159.game.enumeration.ProtocolEnums.RES_LOGIN
import static io.github.hdfg159.game.enumeration.ProtocolEnums.RES_REGISTER

/**
 * Project:starter
 * Package:io.github.hdfg159.game.service.avatar
 * Created by hdfg159 on 2020/7/17 23:53.
 */
@Slf4j
@Singleton
class AvatarService extends AbstractService {
	AvatarData avatarData = AvatarData.instance
	
	@Override
	Completable init() {
		response(ProtocolEnums.REQ_LOGIN, login)
		response(ProtocolEnums.REQ_OFFLINE, offline)
		response(ProtocolEnums.REQ_HEART_BEAT, heartBeat)
		response(ProtocolEnums.REQ_REGISTER, register)
		
		handleEvent(EventEnums.ONLINE, onlineEvent)
		handleEvent(EventEnums.OFFLINE, offlineEvent)
		
		this.@vertx.rxDeployVerticle(avatarData).ignoreElement()
	}
	
	@Override
	Completable destroy() {
		return Completable.complete()
	}
	
	def login = {headers, params ->
		def channelId = (headers as MultiMap)[ATTR_NAME_CHANNEL_ID] as String
		
		def request = params as GameMessage.LoginReq
		def username = request.username
		def password = request.password
		
		def avatar = avatarData.getByUsername(username)
		if (avatar && username == avatar.username && password == avatar.password) {
			synchronized (avatar) {
				def id = avatar.id
				def avatarChannelId = avatarData.getChannelId(id)
				if (avatarData.isOnline(id)) {
					// 查询当前在线玩家的通道
					if (avatarChannelId && avatarChannelId == channelId) {
						// 玩家已经在线，玩家和通道一样，属于重复请求，返回错误码
						log.info "玩家同一通道重复登录:[${id}][${username}][${avatarChannelId}]"
						return GameUtils.resMsg(RES_LOGIN, EXIST_LOGIN)
					} else {
						// 玩家已经在线，玩家当前渠道和这个不一致，踢掉再登录
						log.info "强制下线玩家并重新登录:[${id}][${username}][${avatarChannelId}]"
						forceOffline(id)
						return loginSuccess(channelId, avatar)
					}
				} else {
					log.info "玩家正常登录:[${id}][${username}][${channelId}]"
					return loginSuccess(channelId, avatar)
				}
			}
		} else {
			return GameUtils.resMsg(RES_LOGIN, LOGIN_FAIL)
		}
	}
	
	/**
	 * 登录成功
	 * @param channelId 通道ID
	 * @param avatar 玩家信息
	 * @return 返回
	 */
	private GameMessage.Message loginSuccess(String channelId, Avatar avatar) {
		def id = avatar.id
		def username = avatar.username
		
		avatarData.onlineChannel(id, channelId)
		
		// 设置通道属性，标记已经登录
		avatarData.getChannel(id)?.attr(ATTR_AVATAR)?.set(id)
		
		def event = EventMessage.Online.newBuilder()
				.setUserId(id)
				.setUsername(username)
				.build()
		publishEvent(EventEnums.ONLINE, event)
		
		log.info("玩家登录成功:[${avatar.id}][${avatar.username}][${avatarData.getChannelId(id)}]")
		
		def res = GameMessage.LoginRes.newBuilder()
				.setUsername(username)
				.setUserId(id)
				.build()
		return GameUtils.resMsg(RES_LOGIN, LOGIN_SUCCESS, res)
	}
	
	def offline = {headers, params ->
		def request = params as GameMessage.OfflineReq
		def userId = request.userId
		def channelId = (headers as MultiMap)[ATTR_NAME_CHANNEL_ID]
		
		if (userId) {
			def avatar = avatarData.getById(userId)
			if (avatar) {
				synchronized (avatar) {
					def avatarChannelId = avatarData.getChannelId(userId)
					if (avatarData.isOnline(userId) && avatarChannelId == channelId) {
						log.info "玩家进行正常下线请求:[${userId}][${avatarChannelId}]"
						
						avatarData.offlineChannel(userId)
						
						def event = EventMessage.Offline.newBuilder()
								.setUsername(avatar.username)
								.setUserId(avatar.id)
								.build()
						publishEvent(EventEnums.OFFLINE, event)
					}
				}
			}
		}
		
		return null
	}
	
	def heartBeat = {headers, params ->
		def request = params as GameMessage.HeartBeatReq
		log.info "heart beat ......"
	}
	
	def onlineEvent = {headers, params ->
		def event = params as EventMessage.Online
		def userId = event.userId
		def username = event.username
		def channel = avatarData.getChannel(userId)
		log.info "收到上线通知:[${userId}][${username}]上线,channel:${channel}"
	}
	
	def offlineEvent = {headers, params ->
		def event = params as EventMessage.Offline
		def username = event.username
		def userId = event.userId
		log.info "收到下线通知:[${userId}][${username}]下线"
	}
	
	def register = {headers, params ->
		def request = params as GameMessage.RegisterReq
		def username = request.username
		def password = request.password
		if (!username || !password) {
			return GameUtils.resMsg(RES_REGISTER, REGISTER_INFO_ILLEGAL)
		}
		
		def avatar = avatarData.getByUsername(username)
		if (avatar) {
			return GameUtils.resMsg(RES_REGISTER, REGISTER_INFO_ILLEGAL)
		}
		
		def now = new Date()
		def registerAvatar = new Avatar(
				username: username,
				password: password,
				registerTime: now
		)
		// 保存缓存，全局数据加入相关信息
		avatarData.saveCache(registerAvatar)
		avatarData.addGlobalCache(registerAvatar)
		def res = GameMessage.RegisterRes.newBuilder()
				.setId(registerAvatar.id)
				.setUsername(registerAvatar.username)
				.build()
		return GameUtils.resMsg(RES_REGISTER, REGISTER_SUCCESS, res)
	}
	
	/**
	 * 强制下线
	 * @param userId 用户ID
	 * @return
	 */
	def forceOffline(String userId) {
		if (!userId) {
			return
		}
		
		def avatar = avatarData.getById(userId)
		if (!avatar) {
			return
		}
		
		synchronized (avatar) {
			if (!avatarData.isOnline(avatar.id)) {
				return
			}
			
			def channel = avatarData.getChannel(userId)
			def res = GameUtils.resMsg(ProtocolEnums.RES_OFFLINE, FORCE_OFFLINE)
			if (channel && channel.isActive()) {
				channel.writeAndFlush(res)
				channel.close()
			}
			
			avatarData.offlineChannel(userId)
			
			def event = EventMessage.Offline.newBuilder()
					.setUsername(avatar.username)
					.setUserId(avatar.id)
					.build()
			publishEvent(EventEnums.OFFLINE, event)
		}
	}
	
	/**
	 * 单条在线推送消息
	 * @param userId 用户 ID
	 * @param message 消息
	 * @return
	 */
	def pushMsg(String userId, Message message) {
		def channel = avatarData.getChannel(userId)
		if (channel && channel.isActive()) {
			channel.writeAndFlush(message)
		}
	}
	
	/**
	 * 全部在线推送消息
	 * @param userIds 用户 ID 列表
	 * @param message 消息
	 * @return
	 */
	def pushAllMsg(List<String> userIds, Message message) {
		userIds.collect {avatarData.getChannel(it)}
				.findAll {it && it.isActive()}
				.each {it.writeAndFlush(message)}
	}
}
