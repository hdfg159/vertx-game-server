package io.github.hdfg159.game.util

import groovy.util.logging.Slf4j

/**
 * Project:starter
 * Package:io.github.hdfg159.game.util
 * Created by hdfg159 on 2020/7/25 0:37.
 */
@Slf4j
abstract class GroovyUtils {
	/**
	 * 执行脚本
	 * @param script
	 * @return
	 */
	static def eval(String script) {
		if (script) {
			log.info "eval script:${script}"
			try {
				def object = Eval.me(script)
				log.info "script execute success:${object}"
				return "script execute success"
			} catch (Exception e) {
				def error = "script execute error:${e.message}"
				log.error "${error}"
				return error
			}
		}
		return "script illegal"
	}
}
