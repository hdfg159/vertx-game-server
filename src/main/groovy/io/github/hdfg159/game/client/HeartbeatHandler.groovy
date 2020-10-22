package io.github.hdfg159.game.client

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.enumeration.ProtocolEnums
import io.github.hdfg159.game.util.GameUtils
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game.handler
 * <p>
 * 心跳处理
 * @date 2020/7/16 16:33
 * @author zhangzhenyu
 */
@Slf4j
class HeartbeatHandler extends ChannelInboundHandlerAdapter {
	@Override
	void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (!(evt instanceof IdleStateEvent)) {
			super.userEventTriggered(ctx, evt)
			return
		}
		
		def event = evt as IdleStateEvent
		if (event.state() == IdleState.ALL_IDLE) {
			// 发送心跳维持
			def message = GameUtils.reqMsg(ProtocolEnums.REQ_HEART_BEAT, null)
			
			ctx.channel()
					.writeAndFlush(message)
					.addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
		} else {
			super.userEventTriggered(ctx, evt)
		}
	}
}
