package io.github.hdfg159.game.handler

import groovy.util.logging.Slf4j
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.handler.logging.LoggingHandler

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game
 * <p>
 *     日志记录处理器
 *
 * @date 2020/7/15 12:23
 * @author zhangzhenyu
 */
@Slf4j
@ChannelHandler.Sharable
class LogHandler extends LoggingHandler {
	@Override
	void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		super.channelRead ctx, msg
	}
	
	@Override
	void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		super.write ctx, msg, promise
	}
}
