package io.github.hdfg159.game.data

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Project:starter
 * Package:io.github.hdfg159.game.data
 * Created by hdfg159 on 2020/7/19 10:16.
 */
interface IData<K> extends Serializable {
	/**
	 * 获取ID
	 * @return ID
	 */
	@JsonProperty("_id")
	K getId()
	
	/**
	 * 设置ID
	 * @param id
	 */
	void setId(K id)
}