package io.github.hdfg159.game.handler

import groovy.util.logging.Slf4j
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame

/**
 * Project:starter
 * Package:io.github.hdfg159.game.handler
 * Created by hdfg159 on 2020/10/22 23:21.
 */
@Slf4j
@ChannelHandler.Sharable
class WebSocketBinaryMessageInHandler extends SimpleChannelInboundHandler<BinaryWebSocketFrame> {
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, BinaryWebSocketFrame msg) throws Exception {
		def buf = msg.content().retain()
		super.channelRead ctx, buf
	}
}
