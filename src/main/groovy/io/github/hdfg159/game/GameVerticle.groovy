package io.github.hdfg159.game

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.service.avatar.AvatarData
import io.github.hdfg159.game.service.avatar.AvatarService
import io.github.hdfg159.game.service.farm.FarmService
import io.github.hdfg159.game.service.log.GameLogData
import io.github.hdfg159.game.service.log.LogService
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
		
		
		Completable.defer({
			def dataManagers = Completable.mergeArray(
					this.@vertx.rxDeployVerticle(GameLogData.instance).ignoreElement(),
					this.@vertx.rxDeployVerticle(AvatarData.instance).ignoreElement(),
			)
			
			def services = Completable.concatArray(
					this.@vertx.rxDeployVerticle(LogService.getInstance()).ignoreElement(),
					Completable.mergeArray(
							this.@vertx.rxDeployVerticle(AvatarService.getInstance()).ignoreElement(),
							this.@vertx.rxDeployVerticle(FarmService.getInstance()).ignoreElement(),
					)
			)
			
			Completable.concatArray(
					dataManagers,
					services
			)
		}).doOnComplete({
			log.info "deploy ${this.class.simpleName} complete"
		})
	}
	
	@Override
	Completable rxStop() {
		Completable.complete()
	}
}
