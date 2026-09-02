package io.github.lijiajia3515.cairo.auth.modules.app_user.message;


import lombok.extern.slf4j.Slf4j;


/**
 * 创建管理员用户 队列 处理器
 */
@Slf4j
// @Component
public class CreatePortalAppUserByCreatedTenantQueueHandler {

	// private final CairoSecurityProperties cairoSecurityProperties;
	// private final AccountCommonService accountCommonService;
	// private final UserCommonService userCommonService;
	// private final MongoTemplate mongoTemplate;
	// private final MongoTemplate readMongoTemplate;
	// private final TransactionTemplate transactionTemplate;
	// private final ObjectMapper objectMapper;
	// private final RabbitTemplate rabbitTemplate;
	// private final CairoRabbitmqTool cairoRabbitmqTool;
	//
	// public CreatePortalUserByCreatedTenantQueueHandler(CairoSecurityProperties cairoSecurityProperties, AccountCommonService accountCommonService, UserCommonService userCommonService,
	// 												   @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
	// 												   @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
	// 												   TransactionTemplate transactionTemplate, ObjectMapper objectMapper, RabbitTemplate rabbitTemplate, CairoRabbitmqTool cairoRabbitmqTool) {
	// 	this.cairoSecurityProperties = cairoSecurityProperties;
	// 	this.accountCommonService = accountCommonService;
	// 	this.userCommonService = userCommonService;
	// 	this.mongoTemplate = mongoTemplate;
	// 	this.readMongoTemplate = readMongoTemplate;
	// 	this.transactionTemplate = transactionTemplate;
	// 	this.objectMapper = objectMapper;
	// 	this.rabbitTemplate = rabbitTemplate;
	// 	this.cairoRabbitmqTool = cairoRabbitmqTool;
	// }
	//
	// /**
	//  * 业务队列
	//  *
	//  * @param headers headers
	//  * @param payload payload
	//  * @param message message
	//  * @param channel channel
	//  * @throws IOException 1
	//  */
	// @RabbitListener(
	// 	queues = {"#{createUserByCreatedPortalUserQueue.getName()}"}
	// )
	// public void createUserByCreatedPortalUserQueue(@Headers Map<String, Object> headers, @Payload String payload, Message message, Channel channel) throws IOException {
	// 	try {
	// 		CreatedTenantMessage createdTenantMessage = objectMapper.readValue(payload, CreatedTenantMessage.class);
	// 		log.debug("[create_portal_user] message handler start: {}", createdTenantMessage.getTenantId());
	//
	// 		if (createdTenantMessage.getOwnerAccountId() == null) {
	// 			// 消费成功
	// 			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	// 			log.debug("[create_portal_user] handler end: {}", createdTenantMessage.getTenantId());
	// 			return;
	// 		}
	//
	// 		// 获取账号
	// 		Account account = accountCommonService.getAccount(createdTenantMessage.getOwnerAccountId());
	//
	// 		UserMongodb newUser = transactionTemplate.execute(transactionStatus -> {
	// 			try {
	// 				Criteria userCriteria = Criteria
	// 					.where(UserMongodb.FIELD.TENANT_ID).is(cairoSecurityProperties.getPortalTenantId())
	// 					.and(UserMongodb.FIELD.APP_ID).is(cairoSecurityProperties.getPortalAppId())
	// 					.and(UserMongodb.FIELD.ACCOUNT_ID).is(createdTenantMessage.getOwnerAccountId());
	// 				Query userQuery = Query.query(userCriteria);
	// 				boolean exists = mongoTemplate.exists(userQuery, UserMongodb.class, MongodbConstants.Collection.USER);
	// 				if (!exists) {
	// 					String newUserId = userCommonService.getNewUserId();
	// 					UserMongodb user = UserMongodb.builder()
	// 						.tenantId(cairoSecurityProperties.getPortalTenantId())
	// 						.appId(cairoSecurityProperties.getPortalAppId())
	// 						.userId(newUserId)
	// 						.nickname(Optional.ofNullable(account).map(Account::getNickname).orElse(newUserId))
	// 						.admin(true)
	// 						.roleIds(Collections.emptyList())
	// 						.departmentIds(Collections.emptyList())
	// 						.tagIds(Collections.emptyList())
	// 						.enabled(true)
	// 						.joinTime(createdTenantMessage.getEventTime())
	// 						.accountId(createdTenantMessage.getOwnerAccountId())
	// 						.metadata(UserMetadataMongodb.builder().createUserId(newUserId).updateUserId(newUserId).build())
	// 						.build();
	// 					return mongoTemplate.insert(user, MongodbConstants.Collection.USER);
	// 				}
	// 			} catch (Exception e) {
	// 				log.warn("[create_portal_user] handler error", e);
	// 			}
	// 			return null;
	// 		});
	// 		if (newUser != null) {
	// 			// 发送创建用户消息
	// 			rabbitTemplate.convertAndSend(
	// 				cairoRabbitmqTool.getExchange().getName(CairoAuthRabbitmqExchange.AUTH),
	// 				cairoRabbitmqTool.getRouteKey().getTenantAppKey(CairoAuthRabbitmqRouteKey.CREATED_USER, cairoSecurityProperties.getPortalTenantId(), cairoSecurityProperties.getCairoAppId()),
	// 				objectMapper.writeValueAsString(
	// 					CreatedUserMessage.builder()
	// 						.tenantId(cairoSecurityProperties.getPortalTenantId())
	// 						.appId(cairoSecurityProperties.getPortalAppId())
	// 						.userId(newUser.getUserId())
	// 						.nickname(newUser.getNickname())
	// 						.admin(newUser.getAdmin())
	// 						.accountId(newUser.getAccountId())
	// 						.eventUserId(CairoSecurityContextHolder.getUserId())
	// 						.eventTime(LocalDateTime.now())
	// 						.build()
	// 				),
	// 				new CorrelationData(CoreConstants.SNOWFLAKE.nextIdStr())
	// 			);
	// 		}
	//
	// 		// 消费成功
	// 		channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
	// 		log.debug("[create_portal_user] handler end: {}", createdTenantMessage.getTenantId());
	// 	} catch (Exception e) {
	// 		log.info("[create_portal_user] handler error", e);
	// 		if (message.getMessageProperties().getDeliveryTag() > 1) {
	// 			// 拒绝消息
	// 			channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
	// 		} else {
	// 			// 消费错误，重新投递
	// 			channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
	// 		}
	// 	}
	// }

}
