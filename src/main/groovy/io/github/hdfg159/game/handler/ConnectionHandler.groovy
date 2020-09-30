package io.github.hdfg159.game.handler

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.service.avatar.ChannelData
import io.github.hdfg159.game.util.GameUtils
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

import java.util.concurrent.atomic.AtomicInteger

import static io.github.hdfg159.game.constant.GameConsts.ATTR_AVATAR
import static io.github.hdfg159.game.enumeration.CodeEnums.MAX_CONNECTION_LIMIT
import static io.github.hdfg159.game.enumeration.ProtocolEnums.*

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game.handler
 * <p>
 * 连接处理器
 * @date 2020/7/15 19:13
 * @author zhangzhenyu
 */
@Slf4j
@ChannelHandler.Sharable
class ConnectionHandler extends SimpleChannelInboundHandler<GameMessage.Message> {
	private int maxConnections
	private GameMessageDispatcher dispatcher
	private ChannelData channelData = ChannelData.instance
	private AtomicInteger connections = new AtomicInteger(0)
	
	ConnectionHandler(int maxConnections, GameMessageDispatcher dispatcher) {
		this.dispatcher = dispatcher
		this.maxConnections = maxConnections
	}
	
	@Override
	void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (connections.incrementAndGet() > this.@maxConnections) {
			// 达到最大连接数
			log.error "server max connections limit:[${maxConnections}]"
			def active = ctx.channel().isActive()
			if (active) {
				// 响应关掉channel
				channelData.remove(ctx.channel())
				
				def message = GameUtils.resMsg(RES_PUSH, MAX_CONNECTION_LIMIT)
				ctx.writeAndFlush(message)
				ctx.close()
				return
			}
		}
		
		channelData.add(ctx.channel())
		
		log.debug("channel active:${ctx.channel()},current connections:[${connections.get()}]")
		super.channelActive(ctx)
	}
	
	@Override
	void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// 移除连接
		def connection = this.@connections.decrementAndGet()
		// 清除用户信息
		def attrAvatar = ctx.channel().attr(ATTR_AVATAR)
		def avatar = attrAvatar.get()
		if (avatar) {
			log.info "发送下线请求,下线玩家:${avatar}"
			def offlineReq = GameMessage.OfflineReq.newBuilder()
					.setUserId(avatar)
					.build()
			dispatcher.request(ctx.channel(), GameUtils.reqMsg(REQ_OFFLINE, offlineReq))
		}
		
		log.debug("channel inactive:${ctx.channel()},current connections:[${connection}],avatar:${avatar}")
		
		super.channelInactive(ctx)
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, GameMessage.Message msg) throws Exception {
		def avatar = ctx.channel().attr(ATTR_AVATAR).get()
		if (avatar
				|| msg.protocol == REQ_LOGIN.protocol
				|| msg.protocol == REQ_HEART_BEAT.protocol
				|| msg.protocol == REQ_REGISTER.protocol) {
			ctx.fireChannelRead(msg)
			return
		}
		
		channelData.remove(ctx.channel())
		ctx.close()
		
		log.debug("avatar not login，avatar:${avatar},close channel:${ctx.channel()}")
	}
	
	@Override
	void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			log.debug "${evt},avatarId:${ctx.channel().attr(ATTR_AVATAR)?.get()}"
			def event = evt as IdleStateEvent
			if (event.state() == IdleState.READER_IDLE) {
				ctx.close()
			} else {
				super.userEventTriggered(ctx, evt)
			}
		}
		super.userEventTriggered(ctx, evt)
	}
}
