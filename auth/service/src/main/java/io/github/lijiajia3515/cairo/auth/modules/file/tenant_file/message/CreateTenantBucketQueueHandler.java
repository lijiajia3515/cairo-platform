package io.github.lijiajia3515.cairo.auth.modules.file.tenant_file.message;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import io.github.lijiajia3515.cairo.auth.domain.message.tenant.CreatedTenantMessage;
import io.minio.*;
import io.minio.messages.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 创建企业存储桶队列处理
 */
@Slf4j
@Component
public class CreateTenantBucketQueueHandler {
	private final MinioClient manageMinioClient;
	private final ObjectMapper objectMapper;

	public CreateTenantBucketQueueHandler(MinioClient manageMinioClient, ObjectMapper objectMapper) {
		this.manageMinioClient = manageMinioClient;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(
		queues = {"#{createTenantBucketQueue.getName()}"}
	)
	public void createTenantQueueHandle(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {

		try {
			CreatedTenantMessage createdTenantMessage = objectMapper.readValue(payload, CreatedTenantMessage.class);
			log.debug("[create_tenant_bucket] message handler start");
			String bucketName = createdTenantMessage.getTenantId();
			boolean bucketExists = manageMinioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
			if (!bucketExists) {
				// 创建存储桶
				manageMinioClient.makeBucket(MakeBucketArgs.builder()
					.bucket(bucketName)
					.build());
				// 设置标签
				manageMinioClient.setBucketTags(SetBucketTagsArgs.builder()
					.bucket(bucketName)
					.tags(new HashMap<>() {{
						put("cairo", "true");
						put("tenant", "true");
					}})
					.build());
				// 开启多版本机制
				manageMinioClient.setBucketVersioning(SetBucketVersioningArgs.builder()
					.bucket(bucketName)
					.config(new VersioningConfiguration(VersioningConfiguration.Status.ENABLED, null))
					.build());
				// 1. 删除超过3天的碎片文件
				// 2. 删除所有上传一天以上的非当前对象文件
				manageMinioClient.setBucketLifecycle(SetBucketLifecycleArgs.builder()
					.bucket(bucketName)
					.config(new LifecycleConfiguration(
						List.of(new LifecycleRule(
							Status.ENABLED,
							new AbortIncompleteMultipartUpload(3),
							null,
							new RuleFilter(""),
							"default_all_non_current_version_expiation",
							new NoncurrentVersionExpiration(1),
							null,
							null
						))
					))
					.build());
			}

			// 消费成功
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
			log.debug("[create_tenant_bucket] message handler end");
		} catch (Exception e) {
			log.info("[create_tenant_bucket] message handle error: ", e);
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
