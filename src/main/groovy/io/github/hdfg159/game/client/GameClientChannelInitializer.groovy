package io.github.hdfg159.game.client

import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.handler.LogHandler
import io.github.hdfg159.game.handler.WebSocketBinaryMessageInHandler
import io.github.hdfg159.game.handler.WebSocketBinaryMessageOutHandler
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.handler.timeout.IdleStateHandler

import java.util.concurrent.TimeUnit

/**
 * 游戏客户端通道初始化
 * @date 2020/7/15 10:30
 * @author zhangzhenyu
 */
class GameClientChannelInitializer extends ChannelInitializer<Channel> {
	private static final LogHandler LOG_HANDLER = new LogHandler()
	private static final HeartbeatHandler HEART_BEAT_HANDLER = new HeartbeatHandler()

	private static final ProtobufEncoder PROTOBUF_ENCODER = new ProtobufEncoder()
	private static final ProtobufDecoder PROTOBUF_DECODER = new ProtobufDecoder(GameMessage.Message.getDefaultInstance())

	private static final ProtobufVarint32LengthFieldPrepender PROTOBUF_LENGTH_FIELD_PREPENDER = new ProtobufVarint32LengthFieldPrepender()

	private static final WebSocketBinaryMessageInHandler WEBSOCKET_BINARY_IN_HANDLER = new WebSocketBinaryMessageInHandler()
	private static final WebSocketBinaryMessageOutHandler WEBSOCKET_BINARY_OUT_HANDLER = new WebSocketBinaryMessageOutHandler()

	@Override
	protected void initChannel(Channel ch) throws Exception {
		def handShaker = WebSocketClientHandshakerFactory.newHandshaker(
				new URI("ws://localhost:9998"),
				WebSocketVersion.V13,
				null,
				false,
				new DefaultHttpHeaders()
		)

		ch.pipeline()
				.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS))

				.addLast(new HttpClientCodec())
				.addLast(new HttpObjectAggregator(65536))

				.addLast(new WebSocketClientProtocolHandler(handShaker))
				.addLast(WEBSOCKET_BINARY_IN_HANDLER)
				.addLast(WEBSOCKET_BINARY_OUT_HANDLER)

		// .addLast(new ProtobufVarint32FrameDecoder())
				.addLast(PROTOBUF_DECODER)

		// .addLast(PROTOBUF_LENGTH_FIELD_PREPENDER)
				.addLast(PROTOBUF_ENCODER)

				.addLast(LOG_HANDLER)

				.addLast(HEART_BEAT_HANDLER)
	}
}
