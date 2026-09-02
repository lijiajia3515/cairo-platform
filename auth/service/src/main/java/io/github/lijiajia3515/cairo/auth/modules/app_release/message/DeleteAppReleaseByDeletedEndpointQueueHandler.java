package io.github.lijiajia3515.cairo.auth.modules.app_release.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.endpoint.DeletedEndpointMessage;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.auth.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppReleaseMongodb;
import io.github.lijiajia3515.cairo.auth.modules.file.public_file.PublicFileCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 删除应用发行版本根据已删除终端
 */
@Slf4j
@Component
public class DeleteAppReleaseByDeletedEndpointQueueHandler {
	private final ObjectMapper objectMapper;
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final PublicFileCommonService publicFileCommonService;

	public DeleteAppReleaseByDeletedEndpointQueueHandler(ObjectMapper objectMapper,
															MongoTemplate mongoTemplate,
															TransactionTemplate transactionTemplate,
															PublicFileCommonService publicFileCommonService) {
		this.objectMapper = objectMapper;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.publicFileCommonService = publicFileCommonService;
	}

	@RabbitListener(
		queues = {"#{deleteAppReleaseByDeletedEndpointQueue.getName()}"}
	)
	public void deleteAppReleaseByDeletedEndpointHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
		try {
			log.debug("[deleteAppReleaseByDeletedEndpoint] message handler start");
			DeletedEndpointMessage deletedEndpointMessage = objectMapper.readValue(payload, DeletedEndpointMessage.class);
			log.info("[deleteAppReleaseByDeletedEndpoint] ===> 已删除终端： AppId: {} : EndpointId{}: EventCairoUserId: {} EventTime: {} ",
				deletedEndpointMessage.getAppId(),
				deletedEndpointMessage.getEndpointId(),
				deletedEndpointMessage.getEventCairoUserId(),
				deletedEndpointMessage.getEventTime()
			);
			List<String> deleteUrls = new ArrayList<>();
			transactionTemplate.executeWithoutResult(status -> {
				try {
					Criteria criteria = Criteria
						.where(AppReleaseMongodb.FIELD.APP_ID).is(deletedEndpointMessage.getAppId())
						.and(AppReleaseMongodb.FIELD.ENDPOINT_ID).is(deletedEndpointMessage.getEndpointId());

					Query query = Query.query(criteria);
					Update update = new Update();
					update.set(AppReleaseMongodb.FIELD.METADATA.UPDATE_USER_ID, deletedEndpointMessage.getEventCairoUserId());
					update.currentDate(AppReleaseMongodb.FIELD.METADATA.UPDATE_TIME);

					 mongoTemplate.updateMulti(query, update, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
					List<AppReleaseMongodb> deleteApps = mongoTemplate.findAllAndRemove(query, AppReleaseMongodb.class, MongodbConstants.Collection.APP_RELEASE);
					if (!deleteApps.isEmpty()) {
						mongoTemplate.insert(deleteApps, MongodbConstants.DeletedCollection.APP_RELEASE);
						deleteUrls.addAll(deleteApps.stream().map(AppReleaseMongodb::getAndroidApkUrl).collect(Collectors.toList()));
						deleteUrls.addAll(deleteApps.stream().map(AppReleaseMongodb::getIosAppStoreUrl).collect(Collectors.toList()));
					}
				} catch (BusinessException e) {
					status.setRollbackOnly();
					throw new ConflictBusinessException("删除应用发行失败");
				} catch (Exception e) {
					log.debug("deleteAppRelease", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("删除应用发行失败");
				}
			});
			if (!deleteUrls.isEmpty()) {
				publicFileCommonService.deleteFile(deletedEndpointMessage.getAppId().concat("/").concat(FileKeyPrefixConstants.Collection.APP_RELEASE),deleteUrls);
			}
			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[deleteAppReleaseByDeletedEndpoint] message handler end");
		} catch (RuntimeException e) {
			log.info("[deleteAppReleaseByDeletedEndpoint] message handler error", e);
			if (message.getMessageProperties().getDeliveryTag() > 1) {
				// 拒绝消息
				channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
			} else {
				// 消费错误
				channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
			}
		}

	}

}
