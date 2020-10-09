package io.github.hdfg159.game.data

import com.github.benmanes.caffeine.cache.*
import groovy.util.logging.Slf4j
import io.github.hdfg159.common.util.IdUtils
import io.github.hdfg159.game.constant.GameConsts
import io.reactivex.Completable
import io.reactivex.Maybe
import io.vertx.core.json.JsonObject
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.mongo.MongoClient

import static io.github.hdfg159.game.constant.GameConsts.MONGO_CONFIG
import static io.github.hdfg159.game.constant.GameConsts.MONGO_DATA_SOURCE
import static io.reactivex.schedulers.Schedulers.io

/**
 * 数据抽象 管理
 * Project:starter
 * Package:io.github.hdfg159.game.data
 * Created by hdfg159 on 2020/7/19 10:03.
 */
@Slf4j
abstract class AbstractDataManager<D extends TData<String>> extends AbstractVerticle {
	MongoClient client
	LoadingCache<String, D> cache = cacheBuilder()
	
	/**
	 * mongodb 集合名称
	 * @return 集合名称
	 */
	String collectionName() {
		clazz().simpleName
	}
	
	/**
	 * 构建缓存
	 * @return
	 */
	LoadingCache<String, D> cacheBuilder() {
		defaultCacheBuilder()
	}
	
	/**
	 * 数据所属类
	 * @return
	 */
	abstract Class<D> clazz()
	
	@Override
	Completable rxStart() {
		log.info "deploy data manager ${this.class.simpleName}"
		
		this.@vertx.fileSystem()
				.rxReadFile(MONGO_CONFIG)
				.map({buffer ->
					// 创建共享 mongodb 客户端
					def config = new JsonObject(buffer.delegate)
					this.client = MongoClient.createShared(this.@vertx, config, MONGO_DATA_SOURCE)
					log.info "create mongo client,config:${config},client:${client}"
					this.client
				})
				.ignoreElement()
	}
	
	@Override
	Completable rxStop() {
		log.info "undeploy data manager ${this.class.name}"
		rxSaveAll()
	}
	
	/**
	 * 默认缓存构建
	 * @return
	 */
	LoadingCache<String, D> defaultCacheBuilder() {
		Caffeine.newBuilder()
				.scheduler(Scheduler.systemScheduler())
				.writer(new CacheWriter<String, D>() {
					void write(String key, D value) {}
					
					void delete(String key, D value, RemovalCause cause) {
						rxSaveDB(value)
								.doOnSubscribe({
									log.debug("flush [${cause}] data:[${value.hashCode()}][${key}][${value.class.name}]")
								})
								.blockingGet()
					}
				})
				.expireAfterAccess(GameConsts.CACHE_ACCESS_TIME_OUT)
				.recordStats()
				.build({rxLoad(it).blockingGet()})
	}
	
	/**
	 * 更新保存
	 * @param data 数据
	 * @return 数据
	 */
	D saveCache(D data) {
		if (data) {
			if (data.id) {
				cache.put(data.id, data)
			} else {
				def id = IdUtils.idStr
				data.id = id
				cache.put(id, data)
			}
		}
		data
	}
	
	/**
	 * 获取数据
	 * @param id id
	 * @return 数据
	 */
	D getById(String id) {
		if (id) {
			return cache.get(id)
		}
		return null
	}
	
	/**
	 * 强制更新缓存并刷写到数据库
	 * @param data 数据
	 * @return 数据
	 */
	D updateForce(D data) {
		if (data) {
			cache.put(data.id, data)
			cache.invalidate(data.id)
			cache.cleanUp()
		}
		data
	}
	
	/**
	 * 根据 ID 强制更新 缓存 到数据库
	 * @param data 数据
	 */
	void updateForceById(String id) {
		if (id) {
			cache.invalidate(id)
			cache.cleanUp()
		}
	}
	
	/**
	 * 更新所有缓存数据到数据库
	 */
	Completable rxSaveAll() {
		// 缓存设置全部过期，清除缓存刷新到数据库，增加 subscribeOn 用线程池调度
		// invalidateAll() & cleanUp() 同步操作关联 com.github.benmanes.caffeine.cache.CacheWriter.delete
		Completable.fromAction({
			cache.invalidateAll()
			cache.cleanUp()
		}).subscribeOn(io())
	}
	
	/**
	 * id查询用户数据
	 * @param id
	 */
	protected Maybe<D> rxLoad(String id) {
		def query = new JsonObject(["_id": id])
		client.rxFindOne(collectionName(), query, new JsonObject())
				.map({
					it.mapTo(clazz())
				})
	}
	
	/**
	 * 保存数据
	 * @param data
	 * @return
	 */
	protected Maybe<String> rxSaveDB(D data) {
		client.rxSave(collectionName(), JsonObject.mapFrom(data))
	}
}
