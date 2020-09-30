package io.github.hdfg159.common.util;

/**
 * Project:starter
 * Package:io.github.hdfg159.common.util
 *
 * @author hdfg159
 * @date 2020/7/19 21:42
 */
public class IdUtils {
	private static final Sequence IDENTIFIER_GENERATOR = new Sequence();
	
	/**
	 * 获取唯一ID
	 *
	 * @return id
	 */
	public static long getId() {
		return IDENTIFIER_GENERATOR.nextId();
	}
	
	/**
	 * 获取唯一ID
	 *
	 * @return id
	 */
	public static String getIdStr() {
		return String.valueOf(IDENTIFIER_GENERATOR.nextId());
	}
}
