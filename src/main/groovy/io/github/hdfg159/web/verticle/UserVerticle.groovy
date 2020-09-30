package io.github.hdfg159.web.verticle

import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle

/**
 * Project:starter
 * Package:io.github.hdfg159.web.verticle
 * Created by hdfg159 on 2020/7/11 21:06.
 */
@Slf4j
@Singleton
class UserVerticle extends AbstractVerticle {
	@Override
	Completable rxStart() {
		Completable.fromCallable({
			log.info "deploy ${this.class.simpleName}"
		})
	}
	
	@Override
	Completable rxStop() {
		Completable.fromCallable({
			log.info "undeploy ${this.class.simpleName}"
		})
	}
}
