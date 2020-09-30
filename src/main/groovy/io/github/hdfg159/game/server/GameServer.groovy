package io.github.hdfg159.game.server

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.config.ServerConfig
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.vertx.reactivex.core.Vertx

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game
 * <p>
 * 游戏服务器
 * @date 2020/7/15 10:17
 * @author zhangzhenyu
 */
@Slf4j
@Singleton
class GameServer {
	EventLoopGroup group
	EventLoopGroup childGroup
	ChannelFuture startFuture
	
	void start(Vertx vx, ServerConfig config) {
		if (!vx) {
			throw new RuntimeException("vert.x empty")
		}
		if (!config) {
			throw new RuntimeException("server config empty")
		}
		
		group = new NioEventLoopGroup()
		childGroup = new NioEventLoopGroup()
		def bootstrap = new ServerBootstrap()
		bootstrap.group(group, childGroup)
				.channel(NioServerSocketChannel.class)
				.localAddress(config.port)
				.childHandler(new GameServerChannelInitializer(vx, config))
		
		startFuture = bootstrap.bind().sync()
		log.info("${this.class.simpleName} started and listening for connections on ${startFuture.channel().localAddress()}")
	}
	
	void stop() {
		try {
			startFuture.channel().close()
			log.info("${this.class.simpleName} channel stopped success")
		} finally {
			childGroup.shutdownGracefully().sync()
			group.shutdownGracefully().sync()
			log.info("${this.class.simpleName} event loop group stopped success")
		}
		log.info("${this.class.simpleName} stopped success")
	}
}
