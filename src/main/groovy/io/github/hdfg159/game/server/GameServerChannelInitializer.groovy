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
 * 游戏服务端通道初始化
 * @date 2020/7/15 10:30
 * @author zhangzhenyu
 */
class GameServerChannelInitializer extends ChannelInitializer<Channel> {

	private static final LogHandler LOG_HANDLER = new LogHandler()

	private static final ProtobufEncoder PROTOBUF_ENCODER = new ProtobufEncoder()
	private static final ProtobufDecoder PROTOBUF_DECODER = new ProtobufDecoder(GameMessage.Message.getDefaultInstance())

	private static final WebSocketBinaryMessageInHandler WEBSOCKET_BINARY_IN_HANDLER = new WebSocketBinaryMessageInHandler()
	private static final WebSocketBinaryMessageOutHandler WEBSOCKET_BINARY_OUT_HANDLER = new WebSocketBinaryMessageOutHandler()

	private static final ProtobufVarint32LengthFieldPrepender PROTOBUF_LENGTH_FIELD_PREPENDER = new ProtobufVarint32LengthFieldPrepender()

	private final Vertx vertx
	private final ServerConfig config
	private final GameMessageDispatcher dispatcher

	private final MessageHandler messageHandler
	private final ConnectionHandler connectionHandler

	GameServerChannelInitializer(Vertx vertx, ServerConfig config) {
		this.vertx = vertx
		this.config = config
		this.dispatcher = new GameMessageDispatcher(vertx)
		this.connectionHandler = new ConnectionHandler(config.maxConnection, dispatcher)
		this.messageHandler = new MessageHandler(this.vertx, dispatcher)
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
					.addLast(WEBSOCKET_BINARY_OUT_HANDLER)
					.addLast(WEBSOCKET_BINARY_IN_HANDLER)
		} else {
			pipeline.addLast(new ProtobufVarint32FrameDecoder())
		}

		pipeline.addLast(PROTOBUF_DECODER)

		if (!config.websocket) {
			pipeline.addLast(PROTOBUF_LENGTH_FIELD_PREPENDER)
		}

		pipeline.addLast(PROTOBUF_ENCODER)

		if (config.log) {
			pipeline.addLast(LOG_HANDLER)
		}

		pipeline.addLast(connectionHandler).addLast(messageHandler)
	}
}
