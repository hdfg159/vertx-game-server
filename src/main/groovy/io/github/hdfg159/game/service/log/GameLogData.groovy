package io.github.hdfg159.game.service.log

import groovy.util.logging.Slf4j
import groovy.yaml.YamlSlurper
import io.github.hdfg159.common.util.IdUtils
import io.github.hdfg159.game.constant.GameConsts
import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.BulkOperation
import io.vertx.ext.mongo.MongoClientBulkWriteResult
import io.vertx.reactivex.core.AbstractVerticle
import io.vertx.reactivex.ext.mongo.MongoClient

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.LongAdder

import static io.github.hdfg159.game.constant.GameConsts.LOG_MONGO_DATA_SOURCE

/**
 * 日志数据管理
 */
@Slf4j
@Singleton
class GameLogData extends AbstractVerticle {
	private static final int BATCH_LOG_LIMIT = 20
	private static final int SAVE_LOG_LIMIT_SECONDS = 10
	
	def successCount = new LongAdder()
	def errorCount = new LongAdder()
	
	def client
	def shutdown = false
	def shutdownLatch = new CountDownLatch(1)
	def logQueue = new LinkedBlockingQueue<GameLog>()
	def executor = Executors.newSingleThreadExecutor()
	
	@Override
	Completable rxStart() {
		this.@vertx.fileSystem().rxReadFile(GameConsts.CONFIG_PATH)
				.map({buffer ->
					def config = new JsonObject(new YamlSlurper().parseText(buffer.toString()).database.log)
					this.client = MongoClient.createShared(this.@vertx, config, LOG_MONGO_DATA_SOURCE)
					log.info "create db log mongo client,config:${config},result:[${client != null}]"
					this.client
				})
				.ignoreElement()
				.concatWith(Completable.fromRunnable({saveLogTask()}))
				.doOnComplete({
					log.info "deploy data manager complete : ${this.class.simpleName}"
				})
	}
	
	@Override
	Completable rxStop() {
		Completable.fromRunnable({
			this.@shutdown = true
			executor.shutdownNow()
			shutdownLatch.await()
		})
	}
	
	def saveLogTask() {
		Flowable.<GameLog> create({
			for (; ;) {
				try {
					it.onNext(logQueue.take())
				} catch (InterruptedException ignored) {
					log.error "interrupted emmit game log,next on complete"
					break
				}
			}
			
			it.onComplete()
		}, BackpressureStrategy.BUFFER)
				.subscribeOn(Schedulers.from(executor))
				.buffer(SAVE_LOG_LIMIT_SECONDS, TimeUnit.SECONDS, BATCH_LOG_LIMIT)
				.flatMapMaybe({logs ->
					rxBatchSaveDB(logs).doOnSuccess({
						successCount.add(logs.size())
					}).doOnError({
						errorCount.add(logs.size())
						log.error "batch save log error", it
					}).onErrorReturn({
						new MongoClientBulkWriteResult()
					})
				})
				.subscribe({
					log.info "batch save log success:${it.toJson()}"
				}, {
					log.error "save game log process error", it
				}, {
					log.info "batch save log complete,success:[{}],error:[{}]", successCount.sum(), errorCount.sum()
					shutdownLatch.countDown()
				})
	}
	
	static def collectionName() {
		// 覆盖集合名称，使用表名+日期
		"${GameLog.class.simpleName}_${LocalDate.now().format("yyyyMMdd")}"
	}
	
	Maybe<MongoClientBulkWriteResult> rxBatchSaveDB(Collection<GameLog> data) {
		data ? client.rxBulkWrite(collectionName(), data.collect {
			BulkOperation.createInsert(JsonObject.mapFrom(it))
		}) : Maybe.<MongoClientBulkWriteResult> empty()
	}
	
	def log(GameLog log) {
		if (!shutdown && log) {
			log.id = IdUtils.idStr
			log.createTime = LocalDateTime.now()
			logQueue.add(log)
		}
	}
}
