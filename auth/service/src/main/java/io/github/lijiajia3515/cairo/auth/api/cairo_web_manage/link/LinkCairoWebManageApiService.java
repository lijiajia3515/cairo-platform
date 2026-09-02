package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.link;


import cn.hutool.core.util.RandomUtil;
import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;

import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.CreateLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.CreateLinkResponse;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.DeleteLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.GetLinkPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link.ModifyLinkStatusArgs;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.LinkMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.modules.link.LinkProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.link.MetadataLink;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

@Slf4j
@Validated
@Component
public class LinkCairoWebManageApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final AppUserCommonService appUserCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final LinkProperties linkProperties;

	LinkCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
									 TransactionTemplate transactionTemplate,
									 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
									 CairoSecurityProperties cairoSecurityProperties, AppUserCommonService appUserCommonService, LinkProperties linkProperties
	) {
		this.appUserCommonService = appUserCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.linkProperties = linkProperties;
	}

	/**
	 * 获取短链分页列表
	 *
	 * @param args 参数
	 * @return 短链分页列表
	 */
	@NewSpan
	@BizLog(
		bizId = "link:get_link_page_list",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public Page<MetadataLink> getLinkPageList(GetLinkPageListArgs args) {
		Criteria criteria = new Criteria();
		Query query = Query.query(criteria);
		long count = readMongoTemplate.count(query, LinkMongodb.class, MongodbConstants.Collection.LINK);

		// 排序
		query.with(Sort.by(Sort.Order.desc(LinkMongodb.FIELD.METADATA.UPDATE_TIME))).with(args.pageable());

		List<LinkMongodb> list = readMongoTemplate.find(query, LinkMongodb.class, MongodbConstants.Collection.LINK);
		Set<String> userIds = list.stream().map(LinkMongodb::getMetadata).toList().stream().flatMap(x -> Stream.of(x.getCreateUserId(), x.getUpdateUserId())).filter(Objects::nonNull).collect(Collectors.toSet());


		Map<String, AppUser> metadataUserMap = appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds);
		List<MetadataLink> metadataLinkList = list.stream().map(x -> convertLink(x, metadataUserMap)).collect(Collectors.toList());
		return new Page<>(args, metadataLinkList, count);
	}

	/**
	 * 创建短链
	 *
	 * @param args 参数
	 * @return 新创建的短链信息
	 */
	@NewSpan
	@BizLog(
		bizId = "link:create_link",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public CreateLinkResponse createLink(@Validated CreateLinkArgs args) {
		LinkMongodb insertedMongodb = transactionTemplate.execute(status -> {
			LinkMongodb inserted = null;
			for (int i = 0; i < 2; i++) {
				try {
					if (inserted != null) break;
					String linkId = RandomUtil.randomString(7);
					LinkMongodb insertLink = LinkMongodb.builder()
						.linkId(linkId)
						.shortUrl(linkProperties.getShortUrlPrefix() + linkId)
						.linkUrl(args.getLinkUrl())
						.accessCount(0)
						.enabled(true)
						.metadata(AppUserMetadataMongodb.builder()
							.createUserId(CairoSecurityContextHolder.getAppUserId())
							.updateUserId(CairoSecurityContextHolder.getAppUserId())
							.build())
						.build();
					inserted = mongoTemplate.insert(insertLink, MongodbConstants.Collection.LINK);
				} catch (Exception e) {
					log.debug("create link error", e);
					//status.setRollbackOnly();
					// throw new ConflictBusinessException("创建短链失败");
				}
			}
			if (inserted == null) {
				throw new ConflictBusinessException("创建短链失败");
			}
			return inserted;
		});
		if (insertedMongodb != null) {
			return CreateLinkResponse.builder()
				.linkId(insertedMongodb.getLinkId())
				.linkUrl(insertedMongodb.getLinkUrl())
				.shortUrl(insertedMongodb.getShortUrl())
				.build();
		}
		throw new ConflictBusinessException("创建短链失败");
	}

	/**
	 * 修改短链状态
	 *
	 * @param args 参数
	 */
	@NewSpan
	@Lock4j(name = "modify_link_status", keys = {"#args.linkId"})
	@BizLog(
		bizId = "link:modify_link_status",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void modifyLinkStatus(ModifyLinkStatusArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria.where(LinkMongodb.FIELD.LINK_ID).is(args.getLinkId());

				Query query = Query.query(criteria);
				Update update = new Update();
				update.set(LinkMongodb.FIELD.ENABLED, args.isEnabled());
				update.set(LinkMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(LinkMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, LinkMongodb.class, MongodbConstants.Collection.LINK);
				if (updateResult.getModifiedCount() < 1) {
					throw new ConflictBusinessException("修改短链状态失败");
				}
			} catch (BusinessException e) {
				//status.setRollbackOnly(); // 这里无需回滚，减少通信开销
				throw e;
			} catch (Exception e) {
				log.debug("modify link status error ", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改短链状态失败");
			}
		});
	}

	/**
	 * 删除短链
	 *
	 * @param args
	 */
	@NewSpan
	@Lock4j(name = "delete_link", keys = {"#args.linkId"})
	@BizLog(
		bizId = "link:delete_link",
		scope = "write",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public void deleteLink(DeleteLinkArgs args) {
		transactionTemplate.executeWithoutResult(status -> {
			try {
				Criteria criteria = Criteria.where(LinkMongodb.FIELD.LINK_ID).is(args.getLinkId());

				Query query = Query.query(criteria);
				Update update = new Update();
				update.set(LinkMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getAppUserId());
				update.currentDate(LinkMongodb.FIELD.METADATA.UPDATE_TIME);

				UpdateResult updateResult = mongoTemplate.updateFirst(query, update, LinkMongodb.class, MongodbConstants.Collection.LINK);
				if (updateResult.getModifiedCount() > 0) {
					LinkMongodb link = mongoTemplate.findAndRemove(query, LinkMongodb.class, MongodbConstants.Collection.LINK);
					if (link != null) {
						mongoTemplate.insert(link, MongodbConstants.DeletedCollection.LINK);
					}
				} else {
					throw new ConflictBusinessException("删除短链失败");
				}
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("delete link error ", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("删除短链失败");
			}
		});
	}


	public MetadataLink convertLink(LinkMongodb data, Map<String, AppUser> metadataUserMap) {
		return MetadataLink.builder()
			.linkId(data.getLinkId())
			.linkUrl(data.getLinkUrl())
			.shortUrl(data.getShortUrl())
			.accessCount(data.getAccessCount())
			.lastAccessTime(data.getLastAccessTime())
			.enabled(data.isEnabled())
			.metadata(CairoAppUserConverter.convertAppUser(data.getMetadata(), metadataUserMap))
			.build();
	}

}
