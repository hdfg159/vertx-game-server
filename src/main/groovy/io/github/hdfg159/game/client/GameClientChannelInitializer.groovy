package io.github.hdfg159.game.client

import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.handler.LogHandler
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.handler.timeout.IdleStateHandler

import java.util.concurrent.TimeUnit

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game
 * <p>
 * 游戏客户端通道初始化
 * @date 2020/7/15 10:30
 * @author zhangzhenyu
 */
class GameClientChannelInitializer extends ChannelInitializer<Channel> {
	
	@Override
	protected void initChannel(Channel ch) throws Exception {
		ch.pipeline()
				.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))
				.addLast(new ProtobufVarint32FrameDecoder())
				.addLast(new ProtobufDecoder(GameMessage.Message.getDefaultInstance()))
				.addLast(new ProtobufVarint32LengthFieldPrepender())
				.addLast(new ProtobufEncoder())
				.addLast(new LogHandler())
				.addLast(new HeartbeatHandler())
	}
}
