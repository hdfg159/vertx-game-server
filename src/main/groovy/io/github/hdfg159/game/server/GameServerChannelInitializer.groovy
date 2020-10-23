package io.github.hdfg159.game.server

import io.github.hdfg159.game.config.ServerConfig
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.handler.*
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.handler.timeout.IdleStateHandler
import io.vertx.reactivex.core.Vertx

import java.util.concurrent.TimeUnit

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game
 * <p>
 * 游戏服务端通道初始化
 * @date 2020/7/15 10:30
 * @author zhangzhenyu
 */
class GameServerChannelInitializer extends ChannelInitializer<Channel> {
	private Vertx vertx
	private ServerConfig config
	private GameMessageDispatcher dispatcher
	
	GameServerChannelInitializer(Vertx vertx, ServerConfig config) {
		this.vertx = vertx
		this.config = config
		this.dispatcher = new GameMessageDispatcher(vertx)
	}
	
	@Override
	protected void initChannel(Channel ch) throws Exception {
		def pipeline = ch.pipeline()
		pipeline.addLast(new IdleStateHandler(10 * 60, 0, 0, TimeUnit.SECONDS))
		
		if (config.websocket) {
			pipeline.addLast(new HttpServerCodec())
					.addLast(new HttpObjectAggregator(65536))
					
					.addLast(new WebSocketServerCompressionHandler())
					.addLast(new WebSocketServerProtocolHandler(config.websocketPath, null))
					.addLast(new WebSocketBinaryMessageOutHandler())
					.addLast(new WebSocketBinaryMessageInHandler())
		}
		
		pipeline.addLast(new ProtobufVarint32FrameDecoder())
				.addLast(new ProtobufDecoder(GameMessage.Message.getDefaultInstance()))
				.addLast(new ProtobufVarint32LengthFieldPrepender())
				.addLast(new ProtobufEncoder())
		
		if (config.log) {
			pipeline.addLast(new LogHandler())
		}
		
		pipeline.addLast(new ConnectionHandler(config.maxConnection, dispatcher))
				.addLast(new MessageHandler(vertx, dispatcher))
	}
}
