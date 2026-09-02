package io.github.lijiajia3515.cairo.auth.modules.client;

import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.ClientMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.oauth2.server.authorization.client.CairoRegisteredClient;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import lombok.extern.slf4j.Slf4j;

import io.micrometer.tracing.annotation.NewSpan;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Client Service
 */
@Slf4j
@Component
public class ClientService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;

	public ClientService(MongoTemplate mongoTemplate, TransactionTemplate transactionTemplate) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
	}

	/**
	 * 所有客户端
	 *
	 * @return 所有客户端列表
	 */
	@NewSpan
	public List<RegisteredClient> allClient() {
		return mongoTemplate.findAll(ClientMongodb.class, MongodbConstants.Collection.CLIENT)
			.stream()
			.map(ClientConverter::convert)
			.collect(Collectors.toList());
	}

	/**
	 * 创建client
	 *
	 * @param client client
	 * @return client
	 */
	@NewSpan
	public Optional<RegisteredClient> createClient(CairoRegisteredClient client) {
		String id = CoreConstants.SNOWFLAKE.nextIdStr();
		return Optional.ofNullable(client)
			.map(ClientConverter::convert)
			.map(x -> x.setId(id).setClientId(id).setClientSecret(id))
			.map(x -> transactionTemplate.execute(status -> {
				try {
					return mongoTemplate.insert(x, MongodbConstants.Collection.CLIENT);
				} catch (Exception e) {
					log.debug("createClient", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("创建client失败");
				}
			}))
			.map(ClientConverter::convert);
	}

	/**
	 * 创建client
	 *
	 * @param client client
	 * @return client
	 */
	@NewSpan
	public Optional<RegisteredClient> createClient(ClientMongodb client) {
		return Optional.ofNullable(client)
			.map(x -> transactionTemplate.execute(status -> {
				try {
					return mongoTemplate.insert(x, MongodbConstants.Collection.CLIENT);
				} catch (Exception e) {
					log.debug("createClient", e);
					status.setRollbackOnly();
					throw new ConflictBusinessException("创建client失败");
				}
			}))
			.map(ClientConverter::convert);
	}

	/**
	 * 根据id 更新client属性
	 *
	 * @param client client
	 * @return client
	 */
	@NewSpan
	public Optional<RegisteredClient> modifyById(RegisteredClient client) {
		Query query = Query.query(Criteria.where(ClientMongodb.FIELD._ID));
		Update update = buildClientMongoUpdate(client);
		update.set(ClientMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now())
			.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
		return transactionTemplate.execute(status -> {
			try {
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
				log.info("[client][modifyById]-> Result: {}", updateResult);
				return Optional.ofNullable(mongoTemplate.findOne(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT)).map(ClientConverter::convert);
			} catch (Exception e) {
				log.debug("modifyById", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改client失败");
			}
		});
	}

	/**
	 * 根据ClientId 更新Client属性
	 *
	 * @param client client
	 * @return client
	 */
	@NewSpan
	public Optional<RegisteredClient> modifyByClientId(RegisteredClient client) {
		Query query = Query.query(Criteria.where(ClientMongodb.FIELD.CLIENT_ID));
		Update update = buildClientMongoUpdate(client);
		update.set(ClientMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now())
			.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
		return transactionTemplate.execute(status -> {
			try {
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, ClientMongodb.class, MongodbConstants.Collection.CLIENT);

				log.info("[client][modifyByClientId]-> Result: {}", updateResult);
				return Optional.ofNullable(mongoTemplate.findOne(query, ClientMongodb.class, MongodbConstants.Collection.CLIENT)).map(ClientConverter::convert);
			} catch (Exception e) {
				log.debug("modifyById", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改client失败");
			}
		});
	}

	/**
	 * 根据ClientId 更新状态
	 *
	 * @param clientId clientId
	 * @param enabled  enabled default value is false
	 */
	@NewSpan
	public void updateStatus(String clientId, boolean enabled) {
		Query query = Query.query(Criteria.where(ClientMongodb.FIELD.CLIENT_ID).is(clientId));
		Update update = Update.update(ClientMongodb.FIELD.ENABLED, enabled);
		update.set(ClientMongodb.FIELD.METADATA.UPDATE_TIME, LocalDateTime.now())
			.set(ClientMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());
		transactionTemplate.execute(status -> {
			try {
				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, ClientMongodb.class, MongodbConstants.Collection.CLIENT);
				log.info("[client][updateStatus]-> Result: {}", updateResult);
				return updateResult;
			} catch (Exception e) {
				log.debug("updateStatus", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("updateStatus fail");
			}
		});
	}

	/**
	 * 构建更新条件
	 *
	 * @param client client
	 * @return update
	 */
	private Update buildClientMongoUpdate(RegisteredClient client) {
		Update update = new Update();
		Optional.ofNullable(client.getClientSecret())
			.ifPresent(x -> update.set(ClientMongodb.FIELD.CLIENT_SECRET, x));
		Optional.ofNullable(client.getAuthorizationGrantTypes())
			.ifPresent(x ->
				update.set(ClientMongodb.FIELD.AUTHORIZATION_GRANT_TYPES,
					x.parallelStream()
						.map(AuthorizationGrantType::getValue)
						.collect(Collectors.toSet())
				)
			);
		Optional.ofNullable(client.getScopes()).ifPresent(x -> update.set(ClientMongodb.FIELD.SCOPES, x));
		Optional.ofNullable(client.getClientAuthenticationMethods())
			.ifPresent(x -> update.set(ClientMongodb.FIELD.CLIENT_AUTHENTICATION_METHODS,
				x.parallelStream().map(ClientAuthenticationMethod::getValue).collect(Collectors.toSet()))
			);
		Optional.ofNullable(client.getRedirectUris())
			.ifPresent(redirectUris -> update.set(ClientMongodb.FIELD.REDIRECT_URIS, redirectUris));
		Optional.ofNullable(client.getClientSettings())
			.ifPresent(clientSettings -> update.set(ClientMongodb.FIELD.CLIENT_SETTINGS.SELF, clientSettings));
		Optional.ofNullable(client.getTokenSettings())
			.ifPresent(tokenSettings -> update.set(ClientMongodb.FIELD.TOKEN_SETTINGS.SELF, tokenSettings));
		return update;
	}


}
