package io.github.hdfg159.game.handler

import com.google.protobuf.TextFormat
import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.enumeration.ProtocolEnums
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
		if ((msg instanceof GameMessage.Message) && logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, formatProtoBuf(ctx, "READ", (GameMessage.Message) msg))
			ctx.fireChannelRead(msg)
		} else {
			super.channelRead(ctx, msg)
		}
	}
	
	@Override
	void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		if ((msg instanceof GameMessage.Message) && logger.isEnabled(internalLevel)) {
			logger.log(internalLevel, formatProtoBuf(ctx, "WRITE", (GameMessage.Message) msg))
			ctx.write(msg, promise)
		} else {
			super.write(ctx, msg, promise)
		}
	}
	
	private static String formatProtoBuf(ChannelHandlerContext ctx, String eventName, GameMessage.Message msg) {
		def protocol = ProtocolEnums.valOf(msg.protocol)
		def data = msg.data
		if (protocol && protocol.requestClass) {
			def unpackData = data.unpack(protocol.requestClass)
			return "${ctx.channel()} [${eventName}][${msg.protocol}][${msg.code}]:\n${TextFormat.printer().escapingNonAscii(false).printToString(unpackData)}"
		}
		
		return "${ctx.channel()} ${eventName} : not support data"
	}
	
}
