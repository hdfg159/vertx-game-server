package io.github.hdfg159.web

import groovy.util.logging.Slf4j
import io.reactivex.Completable
import io.vertx.reactivex.core.AbstractVerticle

/**
 * Project:starter
 * Package:io.github.hdfg159.web
 * Created by hdfg159 on 2020/7/14 23:09.
 */
@Slf4j
@Singleton
class WebVerticle extends AbstractVerticle {
	
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
