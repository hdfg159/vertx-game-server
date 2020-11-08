package io.github.hdfg159.game.client

import groovy.util.logging.Slf4j
import io.github.hdfg159.common.util.IdUtils
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.enumeration.ProtocolEnums
import io.github.hdfg159.game.util.GameUtils
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
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
		for (; ;) {
			def cmd = System.in.newReader().readLine()
			log.info " CMD:${cmd} ".center(100, "=")
			
			// 下面业务代码
			if (channel.active) {
				switch (cmd) {
					case "1002":
						login(channel)
						break
					case "1003":
						register(channel)
						break
					case "9999999":
						test(channel)
						break
					default:
						break
				}
			}
		}
	}
	
	def static register(Channel channel) {
		// def username = "admin"
		// def password = "admin"
		def str = IdUtils.idStr
		def username = str
		def password = str
		def req = GameMessage.RegisterReq.newBuilder()
				.setUsername(username)
				.setPassword(password)
				.build()
		def reg = GameUtils.reqMsg(ProtocolEnums.REQ_REGISTER, req)
		channel.writeAndFlush(reg)
	}
	
	def static test(Channel channel) {
		def req = GameMessage.TestReq.newBuilder()
				.setStr("test")
				.build()
		def test = GameUtils.reqMsg(ProtocolEnums.REQ_TEST, req)
		channel.writeAndFlush(test)
	}
	
	def static login(channel) {
		def username = "admin"
		def password = "admin"
		def req = GameMessage.LoginReq.newBuilder()
				.setUsername(username)
				.setPassword(password)
				.build()
		def login = GameUtils.reqMsg(ProtocolEnums.REQ_LOGIN, req)
		channel.writeAndFlush(login)
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
