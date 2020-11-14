package io.github.hdfg159.game

import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
import io.github.hdfg159.game.constant.GameConsts
import io.reactivex.Completable
import io.vertx.core.Verticle
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
		this.@vertx.fileSystem()
				.rxReadFile(GameConsts.COMPONENT_PATH)
				.map({
					new YamlSlurper().parseText(it.toString())
				})
				.flatMapCompletable(this.&createComponents)
				.doOnComplete({
					log.info "deploy ${this.class.simpleName} complete"
				})
	}
	
	@Override
	Completable rxStop() {
		Completable.complete()
	}
	
	def createComponents(components) {
		Completable.defer({
			def dataManagersOrderMap = new TreeMap<String, List<String>>(components.'data-managers')
			def servicesOrderMap = new TreeMap<String, List<String>>(components.services)
			
			def dataManagers = buildVerticleCompletes(dataManagersOrderMap)
			def services = buildVerticleCompletes(servicesOrderMap)
			
			Completable.concatArray(dataManagers, services)
		})
	}
	
	def buildVerticleCompletes(verticleOrderMap) {
		Completable.concat(verticleOrderMap.collect {order, names ->
			Completable.merge(names.collect {name ->
				this.@vertx.rxDeployVerticle((("${name}" as Class).getInstance()) as Verticle).ignoreElement()
			})
		})
	}
}
