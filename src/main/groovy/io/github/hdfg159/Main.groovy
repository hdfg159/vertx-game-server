package io.github.hdfg159

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import groovy.jmx.builder.JmxBuilder
import groovy.util.logging.Slf4j
import io.github.hdfg159.game.GameVerticle
import io.github.hdfg159.game.util.GroovyUtils
import io.github.hdfg159.web.WebVerticle
import io.reactivex.Completable
import io.vertx.core.json.jackson.DatabindCodec
import io.vertx.reactivex.core.Vertx

/**
 * Project:starter
 * <p>
 * Package:io.github.hdfg159
 * <p>
 *
 * @date 2020/7/20 18:00
 * @author zhangzhenyu
 */
@Slf4j
class Main {
	static main(args) {
		// vert.x 序列化 json 忽略 null
		DatabindCodec.mapper()
				.setSerializationInclusion(JsonInclude.Include.NON_NULL)
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
				.registerModule(new JavaTimeModule())
		Vertx vx = Vertx.vertx()
		
		jmx()
		
		// 启动 web 服务器和游戏服务器
		// rx 操作已经是异步后包装，不需要 subscribeOn 异步
		def webServer = vx.rxDeployVerticle(WebVerticle.instance).ignoreElement()
		def gameServer = vx.rxDeployVerticle(GameVerticle.instance).ignoreElement()
		// mergeArrayDelayError 延迟到全部完成才发布失败
		Completable.mergeArrayDelayError(webServer, gameServer)
				.subscribe({
					log.info "deploy verticle success"
				}, {
					log.error "deploy verticle error:${it.message}", it
					new Thread({System.exit(0)}).start()
				})
		
		// 注册JVM关闭钩子函数，优雅关闭 vert.x
		Runtime.addShutdownHook {
			log.info("shutdown application,exist verticle count:${vx.deploymentIDs().size()},closing  ...")
			def throwable = vx.rxClose().blockingGet()
			log.info "vert.x close ${throwable ? "fail" : "success"} ${throwable ? throwable.message : ""}"
			log.info "shutdown application success"
		}
	}
	
	/**
	 * jmx
	 */
	private static void jmx() {
		def jmx = new JmxBuilder()
		def beans = jmx.export {
			bean(
					target: new GroovyUtils() {},
					attributes: [],
					operations: "*"
			)
		}
	}
}
