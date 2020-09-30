package io.github.hdfg159.game.service.farm

import groovy.util.logging.Slf4j
import io.github.hdfg159.game.domain.dto.EventMessage
import io.github.hdfg159.game.domain.dto.GameMessage
import io.github.hdfg159.game.enumeration.EventEnums
import io.github.hdfg159.game.enumeration.ProtocolEnums
import io.github.hdfg159.game.service.AbstractService
import io.github.hdfg159.game.util.GameUtils
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
		response(ProtocolEnums.REQ_TEST, test)
		
		handleEvent(EventEnums.OFFLINE, offlineEvent)
		handleEvent(EventEnums.ONLINE, onlineEvent)
		return Completable.complete()
	}
	
	@Override
	Completable destroy() {
		return Completable.complete()
	}
	
	def test = {headers, params ->
		// throw new RuntimeException("error test========================")
		def res = GameMessage.TestRes.newBuilder()
				.setStr("teststsadasdasdasd")
				.build()
		GameUtils.sucResMsg(ProtocolEnums.RES_TEST, res)
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
