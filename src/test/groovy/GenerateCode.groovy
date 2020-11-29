import io.github.hdfg159.game.enumeration.EventEnums
import io.github.hdfg159.game.enumeration.ProtocolEnums

import java.time.LocalDateTime

/**
 * Project:hgt-game-server
 * Package:
 * Created by hdfg159 on 2020/11/28 23:49.
 */
class GenerateCode {
	static main(args) {
		// 系统英文名 小写
		def moduleName = "test"
		// 系统所在文件夹绝对路径
		def dirPath = "${new File(".").getCanonicalPath()}\\src\\main\\groovy\\io\\github\\hdfg159\\game\\service\\"
		// 作者
		def author = "zhangzhenyu"
		// 数据管理名称
		def dataArray = ["Test1", "Test2"]
		// 生成协议名称
		def protocolEnums = [
				ProtocolEnums.REQ_HEART_BEAT,
				ProtocolEnums.REQ_OFFLINE
		]
		// 生成事件名称
		def eventEnums = [
				EventEnums.ONLINE,
				EventEnums.OFFLINE
		]
		
		dataArray.each {
			def dataParams = [
					moduleName: moduleName,
					dataName  : it,
					author    : author,
					dirPath   : dirPath
			]
			generateData(dataParams)
		}
		
		// 生成服务类，需要配置协议枚举
		def serviceParams = [
				dataArray : dataArray,
				moduleName: moduleName,
				author    : author,
				dirPath   : dirPath,
				protocol  : protocolEnums,
				event     : eventEnums
		]
		generateService(serviceParams)
		
		// 生成配置文件，生成后手动添加进去
		generateComponentConfig(serviceParams)
	}
	
	static void generateData(param) {
		def dataManagerName = "${param.dataName}Data"
		def dataName = "${param.dataName}"
		def moduleName = "${param.moduleName}"
		def createFileDateTime = "${LocalDateTime.now().format("yyyy-MM-dd HH:mm:ss")}"
		
		def dataTemplate = """package io.github.hdfg159.game.service.${moduleName}

import groovy.transform.Canonical
import io.github.hdfg159.game.data.TData

/**
 * [${moduleName.capitalize()}] 数据类:${dataName}
 *
 * @author ${param.author}
 * @date ${createFileDateTime}
 */
@Canonical
class ${dataName} implements TData<String> {

}
"""
		
		def dataManagerTemplate = """package io.github.hdfg159.game.service.${moduleName}

import io.github.hdfg159.game.data.AbstractDataManager

/**
 * [${moduleName.capitalize()}] 数据管理:${dataManagerName}
 *
 * @author ${param.author}
 * @date ${createFileDateTime}
 */
@Singleton
class ${dataManagerName} extends AbstractDataManager<${dataName}> {
	@Override
	Class<${dataName}> clazz() {
		${dataName}.class
	}
}
"""
		new FileTreeBuilder(new File(param.dirPath)).dir("${moduleName}") {
			file("${dataManagerName}.groovy", dataManagerTemplate)
			file("${dataName}.groovy", dataTemplate)
		}
		
		println "生成 数据管理类 成功,参数:${param}"
	}
	
	static void generateService(param) {
		def moduleName = "${param.moduleName}"
		def createFileDateTime = "${LocalDateTime.now().format("yyyy-MM-dd HH:mm:ss")}"
		
		def declareDataInstance = ""
		param.dataArray.each {
			declareDataInstance << """def ${it.toLowerCase()}Data = ${it}Data.getInstance()
"""
		}
		
		def declareProtocol = ""
		def declareRequestClosure = ""
		
		def declareEvent = ""
		def declareEventClosure = ""
		
		param.protocol.each {
			def protocol = it as ProtocolEnums
			def protocolNames = protocol.name().split("_").collect {it.toLowerCase().capitalize()}
			if (protocolNames.size() > 0) {
				protocolNames[0] = protocolNames[0].toLowerCase()
			}
			def protocolMethodName = protocolNames.join("")
			
			declareProtocol += """		ProtocolEnums.${protocol.name().toUpperCase()}.handle(this, ${protocolMethodName})
"""
			declareRequestClosure += """
	def ${protocolMethodName} = {headers, params ->
		def aid = getHeaderAvatarId(headers)
		def req = params as ${protocol.requestClass.getCanonicalName()}

		// TODO:根据需要响应
		// return ProtocolEnums.${protocol.name().replace("REQ", "RES")}.sucRes(...)
	}
"""
		}
		
		param.event.each {
			def event = it as EventEnums
			def eventNames = event.name().split("_").collect {"${it.toLowerCase().capitalize()}"}
			if (eventNames.size() > 0) {
				eventNames[0] = eventNames[0].toLowerCase()
			}
			def eventMethodName = "${eventNames.join("")}Event"
			
			declareEvent += """		EventEnums.${event.name()}.handle(this, ${eventMethodName})
"""
			declareEventClosure += """
	def ${eventMethodName} = {headers, params ->
		def event = params as ${event.clazz.getCanonicalName()}
	}
"""
		}
		
		def serviceTemplate = """package io.github.hdfg159.game.service.${moduleName}

import io.reactivex.Completable
import groovy.util.logging.Slf4j
import io.github.hdfg159.game.enumeration.EventEnums
import io.github.hdfg159.game.service.AbstractService
import io.github.hdfg159.game.enumeration.ProtocolEnums

/**
 * ${moduleName} 系统
 *
 * @author ${param.author}
 * @date ${createFileDateTime}
 */
@Slf4j
@Singleton
class ${moduleName.capitalize()}Service extends AbstractService {
	${declareDataInstance}
	@Override
	Completable init() {
		// Request
${declareProtocol}
		// Event
${declareEvent}
		Completable.complete()
	}
	
	@Override
	Completable destroy() {
		Completable.complete()
	}

	// Request
	${declareRequestClosure}
	// Event
	${declareEventClosure}
	// Private

}
"""
		new FileTreeBuilder(new File(param.dirPath)).dir("${moduleName}") {
			file("${moduleName.capitalize()}Service.groovy", serviceTemplate)
		}
		
		println "生成 业务类 成功,参数:${param}"
	}
	
	static void generateComponentConfig(param) {
		def moduleName = "${param.moduleName}"
		def component = ""
		param.dataArray.each {
			component += """'io.github.hdfg159.game.service.${moduleName}.${it}Data'

"""
		}
		
		component += """'io.github.hdfg159.game.service.${moduleName}.${moduleName.capitalize()}Service'

"""
		println "生成 组件配置 成功,请在配置文件加入以下组件部署:\n${component}"
	}
}
