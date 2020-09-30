package io.github.hdfg159.game.handler

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.service.avatar.ChannelData
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.vertx.reactivex.core.Vertx

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game.handler
 * <p>
 * 消息处理器
 * @date 2020/7/15 15:22
 * @author zhangzhenyu
 */
@Slf4j
@ChannelHandler.Sharable
class MessageHandler extends SimpleChannelInboundHandler<GameMessage.Message> {
	private Vertx vertx
	private GameMessageDispatcher dispatcher
	private ChannelData channelData = ChannelData.instance
	
	MessageHandler(Vertx vertx, GameMessageDispatcher dispatcher) {
		this.vertx = vertx
		this.dispatcher = dispatcher
	}
	
	@Override
	void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx)
	}
	
	@Override
	void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error "message handle error", cause
		
		channelData.remove(ctx.channel())
		ctx.close()
		
		super.channelInactive(ctx)
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, GameMessage.Message msg) throws Exception {
		dispatcher.request(ctx.channel(), msg)
	}
}
