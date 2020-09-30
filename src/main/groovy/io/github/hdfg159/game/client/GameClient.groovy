package io.github.hdfg159.game.client

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.enumeration.ProtocolEnums
import io.github.hdfg159.game.util.GameUtils
import io.netty.bootstrap.Bootstrap
import io.netty.channel.ChannelFuture
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game.domain
 * <p>
 * 游戏客户端
 * @date 2020/7/15 14:21
 * @author zhangzhenyu
 */
@Slf4j
@Singleton
class GameClient {
	ChannelFuture startFuture
	EventLoopGroup group
	
	void start() {
		group = new NioEventLoopGroup()
		def bootstrap = new Bootstrap()
		bootstrap.group(group)
				.channel(NioSocketChannel.class)
				.remoteAddress("127.0.0.1", 9998)
				.handler(new GameClientChannelInitializer())
		
		startFuture = bootstrap.connect().sync()
		log.info("${this.class.simpleName} started and listening for connections on ${startFuture.channel().localAddress()}")
		
		// 关闭 Client
		Runtime.addShutdownHook {
			log.info("shutdown client,closing  ...")
			stop()
			log.info "shutdown client success"
		}
		
		def channel = startFuture.channel()
		def cmd = System.in.newReader().readLine()
		log.info " CMD:${cmd} ".center(100, "=")
		
		// 下面业务代码
		if (channel.active) {
			// (1..1000).each {
			// 	def username = UUID.randomUUID().toString()
			// 	def password = "admin"
			// 	def reg = GameUtils.reqMsg(ProtocolEnums.REQ_REGISTER,
			// 			GameMessage.RegisterReq.newBuilder()
			// 					.setUsername(username)
			// 					.setPassword(password)
			// 					.build())
			// 	channel.writeAndFlush(reg)
			
			def username = "admin"
			def password = "admin"
			// def reg = GameUtils.reqMsg(ProtocolEnums.REQ_REGISTER,
			// 		GameMessage.RegisterReq.newBuilder()
			// 				.setUsername(username)
			// 				.setPassword(password)
			// 				.build())
			// channel.writeAndFlush(reg)
			// Thread.sleep(1000L)
			def login = GameUtils.reqMsg(
					ProtocolEnums.REQ_LOGIN,
					GameMessage.LoginReq.newBuilder()
							.setUsername(username)
							.setPassword(password)
							.build()
			)
			
			(1..999).each {
				channel.writeAndFlush(login)
			}
			
			// Thread.sleep(3000L)
			// def test1 = GameUtils.reqMsg(
			// 		ProtocolEnums.REQ_TEST,
			// 		GameMessage.TestReq.newBuilder()
			// 				.setStr("asdasd")
			// 				.build()
			// )
			// channel.writeAndFlush(test1)
			// }
		}
	}
	
	void stop() {
		try {
			startFuture.channel().closeFuture()
		} finally {
			group.shutdownGracefully().sync()
		}
		log.info("${this.class.simpleName} stopped success")
	}
	
	static void main(String[] args) {
		getInstance().start()
	}
}
