package io.github.hdfg159.game

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.config.ServerConfig
import io.github.hdfg159.game.server.GameServer
import io.github.hdfg159.game.service.avatar.AvatarService
import io.github.hdfg159.game.service.farm.FarmService
import io.reactivex.Completable
import io.vertx.core.json.Json
import io.vertx.reactivex.core.AbstractVerticle

import static io.github.hdfg159.game.constant.GameConsts.SERVER_CONFIG
import static io.reactivex.schedulers.Schedulers.io

/**
 * 游戏 verticle
 * Project:starter
 * Package:io.github.hdfg159.game
 * Created by hdfg159 on 2020/7/14 23:10.
 */
@Slf4j
@Singleton
class GameVerticle extends AbstractVerticle {
	@Override
	Completable rxStart() {
		log.info "deploy ${this.class.simpleName}"
		
		this.@vertx.fileSystem()
				.rxReadFile(SERVER_CONFIG)
				.map({buffer ->
					log.info "server config:${Json.decodeValue(buffer.delegate)}"
					Json.decodeValue(buffer.delegate, ServerConfig.class)
				})
				.flatMapCompletable({config ->
					// 启动服务器耗时，subscribeOn 异步
					Completable.fromCallable({
						GameServer.instance.start(this.@vertx, config)
					}).subscribeOn(io())
				})
				.concatWith(
						// 游戏服务器启动完毕再继续部署其他服务
						// rx 操作已经异步化，不需要指定 subscribeOn
						this.@vertx.rxDeployVerticle(AvatarService.getInstance()).ignoreElement()
								.mergeWith(this.@vertx.rxDeployVerticle(FarmService.getInstance()).ignoreElement())
				)
	}
	
	@Override
	Completable rxStop() {
		// 关闭服务器耗时，subscribeOn 异步
		Completable.fromAction({
			GameServer.instance.stop()
			log.info "undeploy ${this.class.simpleName}"
		}).subscribeOn(io())
	}
}
