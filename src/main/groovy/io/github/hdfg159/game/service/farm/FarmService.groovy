package io.github.hdfg159.game.service.farm

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.EventMessage
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.enumeration.EventEnums
import io.github.hdfg159.game.enumeration.ProtocolEnums
import io.github.hdfg159.game.service.AbstractService
import io.reactivex.Completable

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159.game.service
 * <p>
 * 农场系统
 * @date 2020/7/16 17:13
 * @author zhangzhenyu
 */
@Slf4j
@Singleton
class FarmService extends AbstractService {
	@Override
	Completable init() {
		ProtocolEnums.REQ_TEST.handle(this, test)
		
		EventEnums.OFFLINE.handle(this, offlineEvent)
		EventEnums.ONLINE.handle(this, onlineEvent)
		return Completable.complete()
	}
	
	@Override
	Completable destroy() {
		return Completable.complete()
	}
	
	def test = {headers, params ->
		// throw new RuntimeException("error test========================")
		ProtocolEnums.RES_TEST.sucRes(
				GameMessage.TestRes.newBuilder()
						.setStr("teststsadasdasdasd")
						.build()
		)
	}
	
	def onlineEvent = {headers, params ->
		def event = params as EventMessage.Online
		log.info "${this.class.name} 收到上线通知:${event.username}上线"
	}
	
	def offlineEvent = {headers, params ->
		def event = params as EventMessage.Offline
		def username = event.username
		log.info "${this.class.name} 收到下线通知:${username}下线"
	}
}
