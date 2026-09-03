package io.github.lijiajia3515.cairo.auth.modules.dict.biz.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.result.UpdateResult;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.api.client.tenant_app.GetTenantAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.tenant_app.TenantApp;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.TenantAppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.api.client.tenant_app.TenantAppClientApiService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.BizDictMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictItemMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SysDictMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.dict.DictType;
import io.github.lijiajia3515.cairo.auth.domain.message.dict.sys.SyncSysDictMessage;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
import io.github.lijiajia3515.cairo.auth.modules.utils.FilesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

/**
 * 同步业务字典队列处理
 */
@Slf4j
@Component
public class SyncBizDictBySyncSysDictQueueHandler {

	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final TenantAppClientApiService tenantAppClientApiService;
	private final PublicFileCommonService publicFileCommonService;

	public SyncBizDictBySyncSysDictQueueHandler(ObjectMapper objectMapper,
															   MongoTemplate mongoTemplate,
															   TransactionTemplate transactionTemplate,
															   TenantAppClientApiService tenantAppClientApiService,
															   PublicFileCommonService publicFileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.tenantAppClientApiService = tenantAppClientApiService;
		this.publicFileCommonService = publicFileCommonService;
	}


	@RabbitListener(
		queues = {"#{syncBizDictQueue.getName()}"}
	)
	public void syncBizDictQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			SyncSysDictMessage syncSysDictMessage = objectMapper.readValue(payload, SyncSysDictMessage.class);
			log.debug("[SyncBizDict] message handler start");
			//app下所有企业
			List<TenantApp> tenantAppList = tenantAppClientApiService.getTenantAppList(GetTenantAppArgs.builder().appId(syncSysDictMessage.getAppId()).build());
			Optional.ofNullable(tenantAppList).orElse(Collections.emptyList())
				.forEach(tenantApp -> {
					// 系统字典
					Criteria sdCriteria = Criteria
						.where(SysDictMongodb.FIELD.APP_ID).is(syncSysDictMessage.getAppId())
						.and(SysDictMongodb.FIELD.DICT_ID).is(syncSysDictMessage.getDictId());
					Query sdQuery = Query.query(sdCriteria);
					SysDictMongodb sdMongodb = mongoTemplate.findOne(sdQuery, SysDictMongodb.class, MongodbConstants.Collection.SYS_DICT);
					if (sdMongodb != null && DictType.BIZ_TEMPLATE.getTypeValue().equals(sdMongodb.getDictType())) {
						// 应用字典
						Criteria bdCriteria = Criteria
							.where(BizDictMongodb.FIELD.APP_ID).is(syncSysDictMessage.getAppId())
							.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantApp.getTenantId())
							.and(BizDictMongodb.FIELD.DICT_ID).is(syncSysDictMessage.getDictId());
						Query bdQuery = Query.query(bdCriteria);
						BizDictMongodb bdMongodb = mongoTemplate.findOne(bdQuery, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
						//重新生成图标
						String icon = null;
						try {
							if (sdMongodb.getIcon() != null && !sdMongodb.getIcon().isBlank()) {
								String fileName = CoreConstants.nextIdStr();
								MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdMongodb.getIcon(),
									fileName.concat(FilesUtil.getType(sdMongodb.getIcon())));
								if (multipartFile != null) {
									List<String> urls = publicFileCommonService.uploadFile(tenantApp.getTenantId()
											.concat("/")
											.concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ICON)
											.concat("/")
											.concat(fileName)
											.concat(FilesUtil.getType((FilesUtil.getType(sdMongodb.getIcon())))),
										multipartFile);
									if (urls.size() > 2) icon = urls.get(2);
								}
							}
						} catch (Exception e) {
							log.info("[SyncBizDictBySyncSysDict bd uploadPublicFile] error", e);
						}

						// 新增
						if (bdMongodb == null) {
							BizDictMongodb insert = BizDictMongodb.builder()
								.tenantId(tenantApp.getTenantId())
								.dictId(sdMongodb.getDictId())
								.dictName(sdMongodb.getDictName())
								.appId(syncSysDictMessage.getAppId())
								.icon(icon)
								.isCreateItem(sdMongodb.getIsCreateItem())
								.enabled(sdMongodb.getEnabled())
//								.syncVersion(sdMongodb.getVersion())
//								.reductionVersion(sdMongodb.getVersion())
								.reductionDictName(sdMongodb.getDictName())
								.reductionIcon(sdMongodb.getIcon())
								.isSyncIcon(true)
								.build();
							transactionTemplate.executeWithoutResult(status -> mongoTemplate.insert(insert, MongodbConstants.Collection.BIZ_DICT));
						}
						// 修改
						if (bdMongodb != null) {
							Criteria criteria = Criteria
								.where(BizDictMongodb.FIELD.APP_ID).is(bdMongodb.getAppId())
								.and(BizDictMongodb.FIELD.TENANT_ID).is(bdMongodb.getTenantId())
								.and(BizDictMongodb.FIELD.DICT_ID).is(bdMongodb.getDictId());
							Query query = Query.query(criteria);
//							Update update = Update.update(BizDictMongodb.FIELD.SYNC_VERSION, sdMongodb.getVersion())
//								.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);
							Update update =new Update().currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);
							if (sdMongodb.getDictName() != null) {
								update.set(BizDictMongodb.FIELD.DICT_NAME, sdMongodb.getDictName());
							}
							if (sdMongodb.getEnabled() != null) {
								update.set(BizDictMongodb.FIELD.ENABLED, sdMongodb.getEnabled());
							}
							if (sdMongodb.getIcon() != null) {
								update.set(BizDictMongodb.FIELD.ICON, icon);
								update.set(BizDictMongodb.FIELD.IS_SYNC_ICON, true);
							}
							if (sdMongodb.getIsCreateItem() != null) {
								update.set(BizDictMongodb.FIELD.IS_CREATE_ITEM, sdMongodb.getIsCreateItem());
							}
							transactionTemplate.executeWithoutResult(status -> mongoTemplate.updateFirst(query, update, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT));
						}

						// 系统字典项
						Criteria sdiCriteria = Criteria
							.where(SysDictItemMongodb.FIELD.APP_ID).is(syncSysDictMessage.getAppId())
							.and(SysDictItemMongodb.FIELD.DICT_ID).is(syncSysDictMessage.getDictId());
						Query sdiQuery = Query.query(sdiCriteria);
						List<SysDictItemMongodb> sdiMongodbs = mongoTemplate.find(sdiQuery, SysDictItemMongodb.class, MongodbConstants.Collection.SYS_DICT_ITEM);
						// 应用字典项
						Criteria bdiCriteria = Criteria
							.where(BizDictItemMongodb.FIELD.APP_ID).is(syncSysDictMessage.getAppId())
							.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantApp.getTenantId())
							.and(BizDictItemMongodb.FIELD.DICT_ID).is(syncSysDictMessage.getDictId());
						Query bziQuery = Query.query(bdiCriteria);
						List<BizDictItemMongodb> bdiMongodbs = mongoTemplate.find(bziQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
						// 修改
						List<String> bdiIds = bdiMongodbs.stream().map(BizDictItemMongodb::getItemId).collect(Collectors.toList());
						List<String> sdiIds = sdiMongodbs.stream().map(SysDictItemMongodb::getItemId).collect(Collectors.toList());
						bdiMongodbs.stream().filter(bdi -> sdiIds.contains(bdi.getItemId())).forEach(bdi -> {
							SysDictItemMongodb sdi = sdiMongodbs.stream().filter(x -> x.getItemId().equals(bdi.getItemId())).findFirst().orElse(null);
							if (sdi != null) {
								//重新生成图标
								String bdiIcon = null;
								try {
									if (sdi.getIcon() != null && !sdi.getIcon().isBlank()) {
										String fileName = CoreConstants.nextIdStr();
										MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdi.getIcon(),
											fileName.concat(FilesUtil.getType(sdi.getIcon())));
										if (multipartFile != null) {
											List<String> urls = publicFileCommonService.uploadFile(tenantApp.getTenantId()
													.concat("/")
													.concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ITEM_ICON)
													.concat("/")
													.concat(fileName)
													.concat(FilesUtil.getType((FilesUtil.getType(sdi.getIcon())))),
												multipartFile);
											if (urls.size() > 2) bdiIcon = urls.get(2);
										}
									}
								} catch (Exception e) {
									log.info("[SyncBizDictBySyncSysDict bdi uploadPublicFile] error", e);
								}

								Criteria criteria = Criteria
									.where(BizDictItemMongodb.FIELD.APP_ID).is(bdi.getAppId())
									.and(BizDictItemMongodb.FIELD.TENANT_ID).is(bdi.getTenantId())
									.and(BizDictItemMongodb.FIELD.ITEM_ID).is(bdi.getItemId());
								Query query = Query.query(criteria);

//								Update update = Update.update(BizDictItemMongodb.FIELD.SYNC_VERSION, sdi.getVersion())
								Update update = new Update()
									.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME)
									.set(BizDictItemMongodb.FIELD.ITEM_NAME, sdi.getItemName())
									.set(BizDictItemMongodb.FIELD.ENABLED, sdi.getEnabled())
									.set(BizDictItemMongodb.FIELD.ICON, bdiIcon)
									.set(BizDictItemMongodb.FIELD.IS_SYNC_ICON,true)
									.set(BizDictItemMongodb.FIELD.EDITABLE, sdi.getEditable())
									.set(BizDictItemMongodb.FIELD.REMARK, sdi.getRemark());
//								if (bdi.getReductionVersion() < sdi.getVersion()) {
//									update.set(BizDictItemMongodb.FIELD.REDUCTION_REMARK, sdi.getRemark())
//										.set(BizDictItemMongodb.FIELD.REDUCTION_ICON, sdi.getIcon())
//										.set(BizDictItemMongodb.FIELD.REDUCTION_ITEM_NAME, sdi.getItemName())
//										.set(BizDictItemMongodb.FIELD.REDUCTION_VERSION, sdi.getVersion());
//								}
								transactionTemplate.executeWithoutResult(status -> mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM));
							}
						});
						// 删除
						List<BizDictItemMongodb> deleteBdi = bdiMongodbs.stream()
							.filter(bdi -> !sdiIds.contains(bdi.getItemId()) && bdi.getIsSync())
							.collect(Collectors.toList());
						deleteBdi.forEach(bdi -> {
							// 删除所有子节点
							List<BizDictItemMongodb> deleteItems = bdiMongodbs.stream().filter(x -> x.getLeftNo() > bdi.getLeftNo() && x.getRightNo() < bdi.getRightNo())
								.sorted(Comparator.comparing(BizDictItemMongodb::getRightNo))
								.collect(Collectors.toList());
							deleteItems.forEach(this::deleteItem);
						});

						// 新增
						List<SysDictItemMongodb> insertSdi = sdiMongodbs.stream()
							.filter(sdi -> !bdiIds.contains(sdi.getItemId()))
							.sorted(Comparator.comparing(SysDictItemMongodb::getRightNo).reversed())
							.collect(Collectors.toList());
						insertSdi.forEach(x -> insertItem(x, syncSysDictMessage.getAppId(), tenantApp.getTenantId()));

				}
			});
			// 消费成功！
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[SyncBizDict] message handler end");
		} catch (Exception e) {
			log.info("[SyncBizDict] error: ", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 重新投递
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}
		log.info("[SyncBizDict] end");
	}

	private void insertItem(SysDictItemMongodb sdim, String appId, String tenantId) {
		//重新生成图标
		String icon = null;
		try {
			if (sdim.getIcon() != null && !sdim.getIcon().isBlank()) {
				String fileName = CoreConstants.nextIdStr();
				MultipartFile multipartFile = FilesUtil.urlConvertCairoMultipart(sdim.getIcon(),
					fileName.concat(FilesUtil.getType(sdim.getIcon())));
				if (multipartFile != null) {
					List<String> urls = publicFileCommonService.uploadFile(tenantId
							.concat("/")
							.concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ITEM_ICON)
							.concat("/")
							.concat(fileName)
							.concat(FilesUtil.getType((FilesUtil.getType(sdim.getIcon())))),
						multipartFile);
					if (urls.size() > 2) icon = urls.get(2);
				}
			}
		} catch (Exception e) {
			log.info("[SyncBizDictBySyncSysDict bdi uploadPublicFile] error", e);
		}



		String parentItemId = Optional.ofNullable(sdim.getParentItemId()).orElse(ROOT_ID);
		Query parentQuery = Query.query(Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(sdim.getAppId())
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(sdim.getDictId())
			.and(BizDictItemMongodb.FIELD.ITEM_ID).is(parentItemId)
		);
		BizDictItemMongodb parentItem = mongoTemplate.findOne(parentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		if (parentItem == null && ROOT_ID.equals(parentItemId)) {
			Query rootNodeQuery = Query.query(Criteria
				.where(BizDictMongodb.FIELD.APP_ID).is(appId)
				.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(BizDictMongodb.FIELD.DICT_ID).is(sdim.getDictId()));
			BizDictMongodb bdm = mongoTemplate.findOne(rootNodeQuery, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
			if (bdm != null) {
				if (bdm.getLeftNo() == null) {
					Update update = new Update();
					update.set(BizDictMongodb.FIELD.LEFT_NO, 1);
					update.set(BizDictMongodb.FIELD.RIGHT_NO, 2);
					update.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);
					FindAndModifyOptions options = FindAndModifyOptions.options().returnNew(true);
					mongoTemplate.findAndModify(rootNodeQuery, update, options, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
					bdm.setLeftNo(1)
						.setRightNo(2);
				}
				parentItem = BizDictItemMongodb.builder()
					.leftNo(bdm.getLeftNo())
					.rightNo(bdm.getRightNo())
					.depth(0)
					.build();
			}
		}
		int position = parentItem.getRightNo();
		int left = parentItem.getRightNo();
		int right = parentItem.getRightNo() + 1;


		// 左值扩容
		Query leftParentQuery = Query.query(Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(sdim.getAppId())
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(sdim.getDictId())
			.and(BizDictItemMongodb.FIELD.LEFT_NO).gte(position)
		);
		leftParentQuery.with(Sort.by(Sort.Order.desc(BizDictItemMongodb.FIELD.LEFT_NO)));
		List<BizDictItemMongodb> leftNodes = mongoTemplate.find(leftParentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		leftNodes.forEach(x -> {
			Query query = Query.query(Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(sdim.getAppId())
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(sdim.getDictId())
				.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
			);
			Update update = new Update()
				.inc(BizDictItemMongodb.FIELD.LEFT_NO, 2)
				.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult updateResult = mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
			log.debug("update system dict item left values to db : [{}]", updateResult);
		});

		// 右值扩容
		Query rightParentQuery = Query.query(Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(sdim.getAppId())
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(sdim.getDictId())
			.and(BizDictItemMongodb.FIELD.RIGHT_NO).gte(position)
		);
		rightParentQuery.with(Sort.by(Sort.Order.desc(BizDictItemMongodb.FIELD.RIGHT_NO)));
		List<BizDictItemMongodb> moveRightNodes = mongoTemplate.find(rightParentQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		moveRightNodes.forEach(x -> {
			Query query = Query.query(Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(sdim.getAppId())
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(tenantId)
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(sdim.getDictId())
				.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
			);
			Update update = new Update()
				.inc(BizDictItemMongodb.FIELD.RIGHT_NO, 2)
				.currentDate(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME);
			UpdateResult updateResult = mongoTemplate.updateFirst(query, update, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
			log.debug("update system dict item right values to db : [{}]", updateResult);
			// TODO 更新字典表 left,right
		});

		// 更新root右值
		Query sysDictQuery = Query.query(Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(sdim.getAppId())
			.and(BizDictMongodb.FIELD.TENANT_ID).is(tenantId)
			.and(BizDictMongodb.FIELD.DICT_ID).is(sdim.getDictId())
		);
		Update sysDictUpdate = new Update()
			.inc(BizDictMongodb.FIELD.RIGHT_NO, 2)
			.currentDate(BizDictMongodb.FIELD.METADATA.UPDATE_TIME);
		UpdateResult updateResult = mongoTemplate.updateFirst(sysDictQuery, sysDictUpdate, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
		log.debug("update system dict right values to db : [{}]", updateResult);

		// 插入字典项
		BizDictItemMongodb itemMongodb = BizDictItemMongodb.builder()
			.appId(sdim.getAppId())
			.tenantId(tenantId)
			.dictId(sdim.getDictId())
			.parentItemId(parentItemId)
			.itemId(sdim.getItemId())
			.itemName(sdim.getItemName())
			.enabled(Optional.ofNullable(sdim.getEnabled()).orElse(true))
			.editable(sdim.getEditable())
			.remark(sdim.getRemark())
			.icon(icon)
			.leftNo(left)
			.rightNo(right)
			.depth(parentItem.getDepth() + 1)
			.isSync(true)
			.isSyncIcon(true)
//			.syncVersion(sdim.getVersion())
//			.reductionVersion(sdim.getVersion())
			.reductionRemark(sdim.getRemark())
			.reductionIcon(sdim.getIcon())
			.reductionItemName(sdim.getItemName())
			.metadata(TenantAppUserMetadataMongodb.builder()
				.createUserId(null)
				.updateUserId(null)
				.build())
			.build();
		mongoTemplate.insert(itemMongodb, MongodbConstants.Collection.BIZ_DICT_ITEM);
	}

	private void deleteItem(BizDictItemMongodb deleteItem) {
		int inc = -(deleteItem.getRightNo() - deleteItem.getLeftNo() + 1);
		Query deleteNodeQuery = Query.query(Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(deleteItem.getAppId())
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(deleteItem.getTenantId())
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(deleteItem.getDictId())
			.and(BizDictItemMongodb.FIELD.LEFT_NO).gte(deleteItem.getLeftNo())
			.and(BizDictItemMongodb.FIELD.RIGHT_NO).lte(deleteItem.getRightNo())
		);
		// 更新左值
		Query otherNodeLeftQuery = Query.query(Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(deleteItem.getAppId())
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(deleteItem.getTenantId())
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(deleteItem.getDictId())
			.and(BizDictItemMongodb.FIELD.LEFT_NO).gt(deleteItem.getLeftNo())
		);
		otherNodeLeftQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.LEFT_NO)));
		Update otherNodeLeftUpdate = new Update()
			.inc(BizDictItemMongodb.FIELD.LEFT_NO, inc)
			.set(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

		// 更新右值
		Query otherNodeRightQuery = Query.query(Criteria
			.where(BizDictItemMongodb.FIELD.APP_ID).is(deleteItem.getAppId())
			.and(BizDictItemMongodb.FIELD.TENANT_ID).is(deleteItem.getTenantId())
			.and(BizDictItemMongodb.FIELD.DICT_ID).is(deleteItem.getDictId())
			.and(BizDictItemMongodb.FIELD.RIGHT_NO).gt(deleteItem.getRightNo())
		);
		otherNodeRightQuery.with(Sort.by(Sort.Order.asc(BizDictItemMongodb.FIELD.RIGHT_NO)));

		Update otherNodeRightUpdate = new Update()
			.inc(BizDictItemMongodb.FIELD.RIGHT_NO, inc)
			.set(BizDictItemMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());

		List<BizDictItemMongodb> deleteBizDictItemMongodbList = mongoTemplate.findAllAndRemove(deleteNodeQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		if (!deleteBizDictItemMongodbList.isEmpty()) {
			// 移动到删除子表
			mongoTemplate.insert(deleteBizDictItemMongodbList, MongodbConstants.DeletedCollection.BIZ_DICT_ITEM);
		}

		// 移动其他菜单左值
		List<BizDictItemMongodb> otherLeftNodes = mongoTemplate.find(otherNodeLeftQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		otherLeftNodes.forEach(x -> {
			Query query = Query.query(Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(x.getAppId())
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(deleteItem.getTenantId())
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(x.getDictId())
				.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
			);
			UpdateResult otherNodeLeftUpdateResult = mongoTemplate.updateFirst(query, otherNodeLeftUpdate, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
			log.debug("OtherNodeLeftUpdateResult: {}", otherNodeLeftUpdateResult);
		});

		// 移动其他菜单右值
		List<BizDictItemMongodb> otherRightNodes = mongoTemplate.find(otherNodeRightQuery, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
		otherRightNodes.forEach(x -> {
			Query query = Query.query(Criteria
				.where(BizDictItemMongodb.FIELD.APP_ID).is(x.getAppId())
				.and(BizDictItemMongodb.FIELD.TENANT_ID).is(deleteItem.getTenantId())
				.and(BizDictItemMongodb.FIELD.DICT_ID).is(x.getDictId())
				.and(BizDictItemMongodb.FIELD.ITEM_ID).is(x.getItemId())
			);
			UpdateResult otherNodeRightUpdateResult = mongoTemplate.updateFirst(query, otherNodeRightUpdate, BizDictItemMongodb.class, MongodbConstants.Collection.BIZ_DICT_ITEM);
			log.debug("OtherNodeRightUpdateResult: {}", otherNodeRightUpdateResult);
		});

		// 更新ROOT节点右值
		Query rootNodeQuery = Query.query(Criteria
			.where(BizDictMongodb.FIELD.APP_ID).is(deleteItem.getAppId())
			.and(BizDictMongodb.FIELD.TENANT_ID).is(deleteItem.getTenantId())
			.and(BizDictMongodb.FIELD.DICT_ID).is(deleteItem.getDictId())
		);

		Update rootNodeRightUpdate = new Update()
			.inc(BizDictMongodb.FIELD.RIGHT_NO, inc)
			.set(BizDictMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now());
		mongoTemplate.updateFirst(rootNodeQuery, rootNodeRightUpdate, BizDictMongodb.class, MongodbConstants.Collection.BIZ_DICT);
        //删除图标
		List<String> deleteIcons = deleteBizDictItemMongodbList.stream().filter(x -> !x.getIsSyncIcon()).map(BizDictItemMongodb::getIcon).collect(Collectors.toList());
		if (!deleteIcons.isEmpty()){
			publicFileCommonService.deleteFile(deleteItem.getAppId().concat("/").concat(FileKeyPrefixConstants.Collection.BIZ_DICT_ITEM_ICON),deleteIcons);
		}
	}
}
