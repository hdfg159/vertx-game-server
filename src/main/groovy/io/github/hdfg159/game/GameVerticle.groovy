package io.github.hdfg159.game

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.service.avatar.AvatarService
import io.github.hdfg159.game.service.farm.FarmService
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle

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
		
		this.@vertx.rxDeployVerticle(AvatarService.getInstance()).ignoreElement()
				.mergeWith(this.@vertx.rxDeployVerticle(FarmService.getInstance()).ignoreElement())
	}
	
	@Override
	Completable rxStop() {
		Completable.complete()
	}
}
