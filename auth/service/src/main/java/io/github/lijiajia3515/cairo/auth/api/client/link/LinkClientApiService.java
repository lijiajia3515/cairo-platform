package io.github.lijiajia3515.cairo.auth.api.client.link;


import cn.hutool.core.util.RandomUtil;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.CreateBatchLinkArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByLinkIdArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.GetLinkListByShortUrlArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.link.LinkInfo;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.LinkMongodb;
import io.github.lijiajia3515.cairo.auth.modules.link.LinkProperties;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 短链客户端服务
 */
@Slf4j
@Validated
@Component
public class LinkClientApiService {
	private final MongoTemplate mongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate readMongoTemplate;
	private final LinkProperties linkProperties;

	LinkClientApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
						 TransactionTemplate transactionTemplate,
						 @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate, LinkProperties linkProperties) {
		this.mongoTemplate = mongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.linkProperties = linkProperties;
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
	public List<LinkInfo> createBatchLink(@Validated CreateBatchLinkArgs args) {
		List<LinkInfo> list = new ArrayList<>(args.getLinkUrls().size());
		for (String linkUrl : args.getLinkUrls()) {
			LinkMongodb insertedMongodb = transactionTemplate.execute(status -> {
				LinkMongodb inserted = null;
				for (int i = 0; i < 2; i++) {
					try {
						if (inserted != null) break;
						String linkId = RandomUtil.randomString(7);
						LinkMongodb insertLink = LinkMongodb.builder()
							.linkId(linkId)
							.shortUrl(linkProperties.getShortUrlPrefix() + linkId)
							.linkUrl(linkUrl)
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
				return inserted;
			});

			if (insertedMongodb != null) {
				list.add(LinkInfo.builder()
					.linkId(insertedMongodb.getLinkId())
					.shortUrl(insertedMongodb.getShortUrl())
					.linkUrl(insertedMongodb.getLinkUrl())
					.build());
			}
		}
		return list;
	}

	/**
	 * 获取短链链接
	 *
	 * @param args 参数
	 * @return 短链数组
	 */
	@NewSpan
	@BizLog(
		bizId = "link:get_link_list_by_short_url",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<LinkInfo> getLinkListByShortUrl(GetLinkListByShortUrlArgs args) {
		List<String> shortUrls = args.getShortUrls();
		Criteria criteria = Criteria.where(LinkMongodb.FIELD.SHORT_URL).in(shortUrls);
		Query query = Query.query(criteria);
		List<LinkMongodb> list = readMongoTemplate.find(query, LinkMongodb.class, MongodbConstants.Collection.LINK);
		return list.stream().map(x -> LinkInfo.builder()
			.linkId(x.getLinkId())
			.shortUrl(x.getShortUrl())
			.linkUrl(x.getLinkUrl())
			.build()).collect(Collectors.toList());
	}

	/**
	 * 获取短链链接
	 *
	 * @param args 参数
	 * @return 短链数组
	 */
	@NewSpan
	@BizLog(
		bizId = "link:get_link_list_by_link_id",
		scope = "read",
		params = {
			@BizLog.Param(key = "args", value = "#args"),
		}
	)
	public List<LinkInfo> getLinkListByLinkId(GetLinkListByLinkIdArgs args) {
		List<String> linkIds = args.getLinkIds();
		Criteria criteria = Criteria.where(LinkMongodb.FIELD.LINK_ID).in(linkIds);
		Query query = Query.query(criteria);
		List<LinkMongodb> list = readMongoTemplate.find(query, LinkMongodb.class, MongodbConstants.Collection.LINK);
		return list.stream().map(x -> LinkInfo.builder()
			.linkId(x.getLinkId())
			.shortUrl(x.getShortUrl())
			.linkUrl(x.getLinkUrl())
			.build()).collect(Collectors.toList());
	}

}
