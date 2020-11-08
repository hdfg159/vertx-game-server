package io.github.hdfg159.game.service.log

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.service.AbstractService
import io.reactivex.Completable

/**
 * 日志系统
 */
@Slf4j
@Singleton
class LogService extends AbstractService {
	def logData = GameLogData.getInstance()
	
	@Override
	Completable init() {
		Completable.complete()
	}
	
	@Override
	Completable destroy() {
		Completable.complete()
	}
	
	def log(GameLog log) {
		logData.log(log)
	}
}
