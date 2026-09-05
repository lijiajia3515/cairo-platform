package io.github.lijiajia3515.cairo.auth.api.cairo_web_manage.menu;

import com.baomidou.lock.annotation.Lock4j;
import com.mongodb.client.result.UpdateResult;
import io.github.lijiajia3515.cairo.auth.constants.FileKeyPrefixConstants;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.PermissionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AppUserMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.MenuMongodb;
import io.github.lijiajia3515.cairo.auth.framework.security.CairoSecurityContextHolder;
import io.github.lijiajia3515.cairo.auth.framework.security.cairo_security.CairoSecurityProperties;
import io.github.lijiajia3515.cairo.auth.domain.dto.permission.Permission;
import io.github.lijiajia3515.cairo.auth.modules.permission.PermissionCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app.AppCommonService;
import io.github.lijiajia3515.cairo.auth.modules.endpoint.EndpointCommonService;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserTool;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.auth.modules.file.FileCommonService;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuCommonService;
import io.github.lijiajia3515.cairo.auth.modules.menu.MenuConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MenuNode;
import io.github.lijiajia3515.cairo.auth.domain.dto.menu.MetadataMenu;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.CreateMenuArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.DeleteMenuArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.GetMenuListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.GetMenuPageListArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.GetMenuTreeArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.ModifyMenuInfoArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.menu.MoveMenuArgs;
import io.github.lijiajia3515.cairo.auth.modules.subapp.SubappCommonService;
import io.github.lijiajia3515.cairo.auth.modules.subapp_version.SubappVersionCommonService;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.core.exception.BusinessException;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import io.github.lijiajia3515.cairo.core.page.Page;
import io.github.lijiajia3515.cairo.core.tree.Tree2Converter;
import io.micrometer.tracing.annotation.NewSpan;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.constants.CairoAuthConstants.ROOT_ID;

/**
 * [cairo_web_manage/api] menu service
 */
@Slf4j
@Validated
@Component
public class MenuCairoWebManageApiService {

	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;
	private final TransactionTemplate transactionTemplate;
	private final AppCommonService appCommonService;
	private final EndpointCommonService endpointCommonService;
	private final MenuCommonService menuCommonService;
	private final AppUserCommonService appUserCommonService;
	private final PermissionCommonService permissionCommonService;
	private final CairoSecurityProperties cairoSecurityProperties;
	private final FileCommonService fileCommonService;
	private final SubappCommonService subappCommonService;
	private final SubappVersionCommonService subappVersionCommonService;

	public MenuCairoWebManageApiService(@Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
										@Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
										TransactionTemplate transactionTemplate,
										AppCommonService appCommonService,
										EndpointCommonService endpointCommonService,
										SubappCommonService subappCommonService,
										MenuCommonService menuCommonService,
										AppUserCommonService appUserCommonService,
										PermissionCommonService permissionCommonService,
										CairoSecurityProperties cairoSecurityProperties,
										FileCommonService fileCommonService,
										SubappVersionCommonService subappVersionCommonService) {
		this.appCommonService = appCommonService;
		this.endpointCommonService = endpointCommonService;
		this.subappCommonService = subappCommonService;
		this.menuCommonService = menuCommonService;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.transactionTemplate = transactionTemplate;
		this.appUserCommonService = appUserCommonService;
		this.permissionCommonService = permissionCommonService;
		this.cairoSecurityProperties = cairoSecurityProperties;
		this.fileCommonService = fileCommonService;
		this.subappVersionCommonService = subappVersionCommonService;
	}


	/**
	 * 获取元素菜单 集合模式
	 *
	 * @param appId 应用id
	 * @param args  参数
	 * @return 元素 list
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MetadataMenu> getMenuList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuListArgs args) {
		checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		if (args.getParentId() != null && !args.getParentId().isBlank()) {
			criteria.and(MenuMongodb.FIELD.PARENT_ID).is(args.getParentId());
		}

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(MenuMongodb.FIELD.LEFT_NO)
		));

		List<MenuMongodb> list = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		return getMetadataMenuList(list);
	}

	/**
	 * 获取元素菜单 分页模式
	 *
	 * @param appId          应用id
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           参数
	 * @return 元素 page
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_page_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public Page<MetadataMenu> getMenuPageList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuPageListArgs args) {
		checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);

		Optional.ofNullable(args.getParentId()).ifPresent(menuId -> criteria.and(MenuMongodb.FIELD.PARENT_ID).is(menuId));

		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.by(MenuMongodb.FIELD.LEFT_NO)
		));

		long total = readMongoTemplate.count(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		query.with(args.pageable());
		List<MenuMongodb> ms = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);
		List<MetadataMenu> metadataMenuList = getMetadataMenuList(ms);
		return new Page<>(args, metadataMenuList, total);
	}

	/**
	 * 获取菜单树（适用于我的资源接口）
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           参数，父级id 默认全部
	 * @return 菜单数组
	 */
	@NewSpan
	@BizLog(
		bizId = "menu:get_menu_tree_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public List<MenuNode> getMenuTreeList(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated GetMenuTreeArgs args) {
		checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
		String parentId = Optional.ofNullable(args).map(GetMenuTreeArgs::getParentId).orElse(ROOT_ID);

		Query parentQuery = Query.query(Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
			.and(MenuMongodb.FIELD.MENU_ID).is(parentId)
		);
		MenuMongodb parentMenu = readMongoTemplate.findOne(parentQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Criteria criteria = Criteria
			.where(MenuMongodb.FIELD.APP_ID).is(appId)
			.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
			.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
			.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion);
		if (parentMenu != null) {
			criteria.and(MenuMongodb.FIELD.LEFT_NO).gte(parentMenu.getLeftNo())
				.and(MenuMongodb.FIELD.RIGHT_NO).lte(parentMenu.getRightNo());
		}
		Query query = Query.query(criteria);
		query.with(Sort.by(
			Sort.Order.asc(MenuMongodb.FIELD.DEPTH),
			Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)
		));
		List<MenuMongodb> menus = readMongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU);

		Set<String> menuIds = menus.stream().map(MenuMongodb::getMenuId).collect(Collectors.toSet());
		Map<String, List<PermissionMongodb>> permissionMap = Optional.of(menuIds)
			.filter(x -> !x.isEmpty())
			.map(mIds -> {
				Query actiionPermissionQuery = Query.query(
					Criteria.where(PermissionMongodb.FIELD.APP_ID).is(appId)
						.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(PermissionMongodb.FIELD.MENU_ID).in(menuIds)
				);
				actiionPermissionQuery.with(Sort.by(
					Sort.Order.asc(PermissionMongodb.FIELD.MENU_ID)
				));
				return readMongoTemplate.find(actiionPermissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION).stream()
					.collect(Collectors.groupingBy(PermissionMongodb::getMenuId));
			}).orElse(Collections.emptyMap());

		List<MenuNode> nodes = menus.stream()
			.map(x -> MenuNode.builder()
				.menuId(x.getMenuId())
				.parentId(x.getParentId())
				.menuName(x.getMenuName())
				.path(x.getPath())
				.component(x.getComponent())
				.icon(x.getIcon())
				.leftNo(x.getLeftNo())
				.rightNo(x.getRightNo())
				.depth(x.getDepth())
				.tags(x.getTags())
				.hiddenMenu(x.isHiddenMenu())
				.permissions(
					permissionMap.getOrDefault(x.getMenuId(), Collections.emptyList())
						.stream().map(e -> MenuNode.Permission.builder()
							.permissionId(e.getPermissionId())
							.permissionName(e.getPermissionName())
							.authorities(e.getAuthorities())
							.type(e.getType())
							.hiddenPermission(Optional.ofNullable(e.getHiddenPermission()).orElse(false))
							.defaultPermission(Optional.ofNullable(e.getDefaultPermission()).orElse(false))
							.sort(e.getSort())
							.icon(e.getIcon())
							.build()
						).collect(Collectors.toList())
				)
				.build()
			)
			.collect(Collectors.toList());
		return Tree2Converter.build(nodes, ROOT_ID);
	}

	/**
	 * 创建菜单
	 * 锁：同一子应用版本+只允许一个用户创建菜单
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           args
	 */
	@NewSpan
	@Lock4j(name = "create_menu", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion"})
	@BizLog(
		bizId = "menu:create_menu",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void createMenu(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated CreateMenuArgs args) {

		transactionTemplate.executeWithoutResult(status -> {
			try {
				checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
				String parentId = Optional.ofNullable(args.getParentId()).orElse(ROOT_ID);
				Query parentQuery = Query.query(
					Criteria.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(parentId)
				);
				MenuMongodb parentMenu = mongoTemplate.findOne(parentQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				if (parentMenu == null && ROOT_ID.equals(parentId)) {
					// 无根时确保根存在(已有无根菜单则包裹修复,而非盲插默认根撞唯一索引)
					parentMenu = menuCommonService.ensureRootMenu(appId, endpointId, subappId, subappVersion);
				}

				if (parentMenu == null) {
					throw new ConflictBusinessException("ParentId 错误");
				}

				// find brother
				Query brotherNodeQuery = Query.query(
					Criteria.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.PARENT_ID).is(parentId)
				);
				brotherNodeQuery.with(Sort.by(Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)));

				List<MenuMongodb> brotherNodes = mongoTemplate.find(brotherNodeQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				Optional<MenuMongodb> afterNode = brotherNodes.stream().filter(a -> a.getMenuId().equals(args.getBeforeId())).findFirst();
				int position;
				int left;
				int right;
				if (brotherNodes.isEmpty() || afterNode.isEmpty()) {
					position = parentMenu.getRightNo();
					left = parentMenu.getRightNo();
					right = parentMenu.getRightNo() + 1;
				} else {
					position = afterNode.get().getLeftNo();
					left = afterNode.get().getLeftNo();
					right = afterNode.get().getLeftNo() + 1;
				}

				// 左值扩容
				Query leftParentQuery = Query.query(Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.LEFT_NO).gte(position)
				);
				leftParentQuery.with(Sort.by(Sort.Order.desc(MenuMongodb.FIELD.LEFT_NO)));
				List<MenuMongodb> leftMenus = mongoTemplate.find(leftParentQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				leftMenus.forEach(x -> {
					Query query = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId())
					);
					Update update = new Update()
						.inc(MenuMongodb.FIELD.LEFT_NO, 2)
						.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(query, update, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.debug("leftParentMenusUpdateResult: [{}]", updateResult);
				});

				// 右值扩容
				Query rightParentQuery = Query.query(Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.RIGHT_NO).gte(position)
				);
				rightParentQuery.with(Sort.by(Sort.Order.desc(MenuMongodb.FIELD.RIGHT_NO)));

				List<MenuMongodb> moveRightMenus = mongoTemplate.find(rightParentQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				moveRightMenus.forEach(x -> {
					Query query = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId())
					);
					Update update = new Update()
						.inc(MenuMongodb.FIELD.RIGHT_NO, 2)
						.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
					UpdateResult updateResult = mongoTemplate.updateFirst(query, update, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.debug("rightParentMenusUpdateResult: [{}]", updateResult);
				});

				// 插入菜单
				MenuMongodb menu = MenuMongodb.builder()
					.appId(appId)
					.endpointId(endpointId)
					.subappId(subappId)
					.subappVersion(subappVersion)
					.menuId(CoreConstants.nextIdStr())
					.parentId(parentMenu.getMenuId())
					.menuName(args.getMenuName())
					.path(args.getPath())
					.component(args.getComponent())
					.icon(args.getIcon())
					.tags(args.getTags())
					.hiddenMenu(args.isHiddenMenu())
					.leftNo(left)
					.rightNo(right)
					.depth(parentMenu.getDepth() + 1)
					.metadata(AppUserMetadataMongodb.builder()
						.createUserId(CairoSecurityContextHolder.getSubappUserId())
						.updateUserId(CairoSecurityContextHolder.getSubappUserId())
						.build())
					.build();
				MenuMongodb insert = mongoTemplate.insert(menu, MongodbConstants.Collection.MENU);
				log.info("insert menu: {}", insert.getMenuId());
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.info("createMenu", e);
				throw new ConflictBusinessException("菜单创建失败");
			}
		});
	}

	/**
	 * 菜单修改
	 * 锁：同一个菜单只能被一个用户操作
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           args
	 */
	@NewSpan
	@Lock4j(name = "modify_menu", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion", "#args.menuId"})
	@BizLog(
		bizId = "menu:modify_menu",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void modifyMenu(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated ModifyMenuInfoArgs args) {
		UpdateResult result = transactionTemplate.execute(status -> {
			try {
				checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
				Criteria criteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.MENU_ID).is(args.getMenuId());
				Query query = Query.query(criteria);
				MenuMongodb menu = mongoTemplate.findOne(query, MenuMongodb.class, MongodbConstants.Collection.MENU);

				if (menu == null) {
					throw new ConflictBusinessException("菜单不存在，更新菜单失败");
				}

				Update update = new Update();
				if (args.getMenuName() != null) {
					update.set(MenuMongodb.FIELD.MENU_NAME, args.getMenuName());
				}

				if (args.getPath() != null) {
					update.set(MenuMongodb.FIELD.PATH, args.getPath());
				}

				if (args.getComponent() != null) {
					update.set(MenuMongodb.FIELD.COMPONENT, args.getComponent());
				}

				if (args.getIcon() != null) {
					update.set(MenuMongodb.FIELD.ICON, args.getIcon());
				}


				if (args.getTags() != null) {
					update.set(MenuMongodb.FIELD.TAGS, args.getTags());
				}

				if (args.getHiddenMenu() != null) {
					update.set(MenuMongodb.FIELD.HIDDEN_MENU, args.getHiddenMenu());
				}
				update.set(MenuMongodb.FIELD.METADATA.UPDATE_USER_ID, CairoSecurityContextHolder.getSubappUserId());

				update.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);

				return mongoTemplate.updateFirst(query, update, MenuMongodb.class, MongodbConstants.Collection.MENU);
			} catch (ConflictBusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.debug("modifyMenu", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("修改菜单失败");
			}
		});

		if (result == null || result.getModifiedCount() < 1) {
			throw new ConflictBusinessException("修改菜单失败");
		}
	}


	/**
	 * 移动菜单
	 * 锁：同一个子应用版本下只允许一个用户移动菜单
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           args
	 */
	@NewSpan
	@Lock4j(name = "move_menu", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion"})
	@BizLog(
		bizId = "menu:move_menu",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void moveMenu(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated MoveMenuArgs args) {


		// 	1. 删除移动的节点
		// 	2. 缩容
		//  3. 扩容
		//  4. 插入删移动的节点（更新leftNo,rightNo)
		transactionTemplate.executeWithoutResult(status -> {
			try {
				checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
				// 先查询三个节点信息
				Criteria criteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.MENU_ID).in(args.getMoveId(), args.getBeforeId(), args.getParentId());
				Query query = Query.query(criteria);
				Map<String, MenuMongodb> menuIdMap = mongoTemplate.find(query, MenuMongodb.class, MongodbConstants.Collection.MENU).stream().collect(Collectors.toMap(MenuMongodb::getMenuId, x -> x));
				// 移动的节点
				MenuMongodb moveMenu = menuIdMap.get(args.getMoveId());
				// 移动后的父节点
				MenuMongodb parentMenu = menuIdMap.get(args.getParentId());
				// 移动后的左边节点
				MenuMongodb beforeMenu = menuIdMap.get(args.getBeforeId());
				if (moveMenu == null || parentMenu == null) {
					throw new ConflictBusinessException("moveId is null or parentId 错误");
				}

				if (parentMenu.getLeftNo() >= moveMenu.getLeftNo() && parentMenu.getRightNo() <= moveMenu.getRightNo()) {
					throw new ConflictBusinessException("parentId 不能设置为移动节点的子节点");
				}


				// 容错beforeId错误的问题，默认移动到最后
				if (beforeMenu != null && !beforeMenu.getParentId().equals(parentMenu.getMenuId())) {
					beforeMenu = null;
				}

				// 查询后删除移动的节点
				Criteria moveNodeCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.LEFT_NO).gte(moveMenu.getLeftNo())
					.and(MenuMongodb.FIELD.RIGHT_NO).lte(moveMenu.getRightNo());
				Query moveNodeQuery = Query.query(moveNodeCriteria);
				moveNodeQuery.with(Sort.by(Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)));
				List<MenuMongodb> moveNodes = mongoTemplate.findAllAndRemove(moveNodeQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				// 移动的数值
				int moveNum = moveNodes.size() * 2;

				// 缩容左值
				Criteria subNodeLeftCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.LEFT_NO).gte(moveMenu.getRightNo() + 1);
				Query subNodeLeftQuery = Query.query(subNodeLeftCriteria);
				subNodeLeftQuery.with(Sort.by(Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)));
				List<MenuMongodb> subNodeLeftNodes = mongoTemplate.find(subNodeLeftQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);

				Update subNodeLeftUpdate = new Update()
					.inc(MenuMongodb.FIELD.LEFT_NO, -moveNum)
					.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
				subNodeLeftNodes.forEach(x -> {
					Query subNodeLeftSubQuery = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId()));
					UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateFirst(subNodeLeftSubQuery, subNodeLeftUpdate, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);
				});

				// 缩容右值
				Criteria subNodeRightCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.RIGHT_NO).gte(moveMenu.getRightNo() + 1);
				Query subNodeRightQuery = Query.query(subNodeRightCriteria);
				subNodeRightQuery.with(Sort.by(Sort.Order.asc(MenuMongodb.FIELD.RIGHT_NO)));
				List<MenuMongodb> subNodeRightNodes = mongoTemplate.find(subNodeRightQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				Update subNodeRightUpdate = new Update()
					.inc(MenuMongodb.FIELD.RIGHT_NO, -moveNum)
					.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
				subNodeRightNodes.forEach(x -> {
					Query subNodeRightSubQuery = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId()));
					UpdateResult addNodeRightUpdateResult = mongoTemplate.updateFirst(subNodeRightSubQuery, subNodeRightUpdate, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.info("addNodeRightUpdateResult: {}", addNodeRightUpdateResult);
				});

				Criteria newNodeCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.MENU_ID).in(args.getBeforeId(), args.getParentId());
				Query newNodequery = Query.query(newNodeCriteria);

				Map<String, MenuMongodb> newMenuIdMap = mongoTemplate.find(newNodequery, MenuMongodb.class, MongodbConstants.Collection.MENU).stream().collect(Collectors.toMap(MenuMongodb::getMenuId, x -> x));

				// 移动后的父节点
				MenuMongodb newParentMenu = newMenuIdMap.get(args.getParentId());
				// 移动后的左边节点
				MenuMongodb newBeforeMenu = beforeMenu == null ? null : newMenuIdMap.get(args.getBeforeId());

				// 扩容
				int startAddNum = Optional.ofNullable(newBeforeMenu).map(MenuMongodb::getLeftNo).orElse(newParentMenu.getRightNo());
				// 扩容左值
				Criteria addNodeLeftCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.LEFT_NO).gte(startAddNum);
				Query addNodeLeftQuery = Query.query(addNodeLeftCriteria);
				addNodeLeftQuery.with(Sort.by(Sort.Order.desc(MenuMongodb.FIELD.LEFT_NO)));
				List<MenuMongodb> addNodeLeftNodes = mongoTemplate.find(addNodeLeftQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				Update addNodeLeftUpdate = new Update()
					.inc(MenuMongodb.FIELD.LEFT_NO, moveNum)
					.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
				addNodeLeftNodes.forEach(x -> {
					Query addNodeLeftSubQuery = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId()));
					UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateFirst(addNodeLeftSubQuery, addNodeLeftUpdate, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);
				});


				// 扩容右值
				Criteria addNodeRightCriteria = Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.RIGHT_NO).gte(startAddNum);
				Query addNodeRightQuery = Query.query(addNodeRightCriteria);
				addNodeRightQuery.with(Sort.by(Sort.Order.desc(MenuMongodb.FIELD.RIGHT_NO)));
				List<MenuMongodb> addNodeRightNodes = mongoTemplate.find(addNodeRightQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				Update addNodeRightUpdate = new Update()
					.inc(MenuMongodb.FIELD.RIGHT_NO, moveNum)
					.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);
				addNodeRightNodes.forEach(x -> {
					Query addNodeRightSubQuery = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId()));
					UpdateResult addNodeLeftUpdateResult = mongoTemplate.updateFirst(addNodeRightSubQuery, addNodeRightUpdate, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.info("addNodeLeftUpdateResult: {}", addNodeLeftUpdateResult);
				});


				newMenuIdMap = mongoTemplate.find(newNodequery, MenuMongodb.class, MongodbConstants.Collection.MENU).stream().collect(Collectors.toMap(MenuMongodb::getMenuId, x -> x));

				// 移动后的父节点
				MenuMongodb newParentMenu2 = newMenuIdMap.get(args.getParentId());
				// 移动后的左边节点
				MenuMongodb newBeforeMenu2 = beforeMenu == null ? null : newMenuIdMap.get(args.getBeforeId());


				int leftNo = moveMenu.getLeftNo();

				moveNodes.forEach(x -> {
					if (newBeforeMenu2 == null) {
						// 使用右基点
						x.setLeftNo(x.getLeftNo() - leftNo + newParentMenu2.getRightNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newParentMenu2.getRightNo() - moveNum);
					} else {
						// 使用左基点
						x.setLeftNo(x.getLeftNo() - leftNo + newBeforeMenu2.getLeftNo() - moveNum);
						x.setRightNo(x.getRightNo() - leftNo + newBeforeMenu2.getLeftNo() - moveNum);
					}
					if (x.getMenuId().equals(args.getMoveId())) {
						x.setParentId(args.getParentId());
					}
					x.setDepth(x.getDepth() + parentMenu.getDepth() + 1 - moveMenu.getDepth());

					x.getMetadata().setUpdateTime(LocalDateTime.now());
				});


				mongoTemplate.insert(moveNodes, MongodbConstants.Collection.MENU);
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				log.info("e", e);
				status.setRollbackOnly();
				throw new ConflictBusinessException("移动失败");
			}
		});
	}

	/**
	 * 删除菜单
	 * 锁：同一个子应用版本下只允许一个用户删除菜单
	 *
	 * @param appId          appId
	 * @param endpointId  endpointId
	 * @param subappId      subappId
	 * @param subappVersion subappVersion
	 * @param args           args
	 */

	@NewSpan
	@Lock4j(name = "delete_menu", keys = {"#appId", "#endpointId", "#subappId", "#subappVersion", "#args.menuId"})

	@BizLog(
		bizId = "menu:delete_menu",
		scope = "write",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "endpointId", value = "#endpointId"),
			@BizLog.Param(key = "subappId", value = "#subappId"),
			@BizLog.Param(key = "subappVersion", value = "#subappVersion"),
			@BizLog.Param(key = "args", value = "#args")
		}
	)
	public void deleteMenu(@Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion, @Validated DeleteMenuArgs args) {
		if (args.getMenuId().equals(ROOT_ID)) {
			throw new ConflictBusinessException("根节点不能删除");
		}
		List<String> iconList = new ArrayList<>();
		transactionTemplate.executeWithoutResult(status -> {
			try {
				checkParams(readMongoTemplate, appId, endpointId, subappId, subappVersion);
				List<Permission> deletePermissionList = permissionCommonService.existsPermissionList(mongoTemplate, appId, endpointId, subappId, subappVersion, args.getMenuId());
				if (!deletePermissionList.isEmpty()) {
					String permissionNames = deletePermissionList.stream().map(x -> String.format("\"%s\"", x.getPermissionName())).collect(Collectors.joining(","));
					throw new ConflictBusinessException("菜单含有功能权限[" + permissionNames + "]，请先删除功能权限后再操作");
				}
				Query menuQuery = Query.query(
					Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(args.getMenuId())
				);
				MenuMongodb deleteMenu = mongoTemplate.findOne(menuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				if (deleteMenu == null) {
					throw new ConflictBusinessException("菜单不存在，删除失败");
				}
				Query deleteMenuQuery = Query.query(Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.LEFT_NO).gte(deleteMenu.getLeftNo())
					.and(MenuMongodb.FIELD.RIGHT_NO).lte(deleteMenu.getRightNo())
				);
				int inc = -(deleteMenu.getRightNo() - deleteMenu.getLeftNo() + 1);
				if (inc < -2) {
					throw new ConflictBusinessException("该菜单含有子菜单，请先删除子菜单后在操作");
				}

				// 更新左值
				Query otherMenuLeftQuery = Query.query(Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.LEFT_NO).gt(deleteMenu.getLeftNo())
				);
				otherMenuLeftQuery.with(Sort.by(Sort.Order.asc(MenuMongodb.FIELD.LEFT_NO)));
				Update otherMenuLeftUpdate = new Update();
				otherMenuLeftUpdate.inc(MenuMongodb.FIELD.LEFT_NO, inc);
				otherMenuLeftUpdate.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);

				// 更新右值
				Query otherMenuRightQuery = Query.query(Criteria
					.where(MenuMongodb.FIELD.APP_ID).is(appId)
					.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
					.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
					.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
					.and(MenuMongodb.FIELD.RIGHT_NO).gt(deleteMenu.getRightNo())
				);
				otherMenuRightQuery.with(Sort.by(Sort.Order.asc(MenuMongodb.FIELD.RIGHT_NO)));

				Update otherMenuRightUpdate = new Update();
				otherMenuRightUpdate.inc(MenuMongodb.FIELD.RIGHT_NO, inc);
				otherMenuRightUpdate.currentDate(MenuMongodb.FIELD.METADATA.UPDATE_TIME);

				List<MenuMongodb> deletedMenuMongodbList = mongoTemplate.findAllAndRemove(deleteMenuQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				if (!deletedMenuMongodbList.isEmpty()) {
					// 移动到删除影子表
					Collection<MenuMongodb> insert = mongoTemplate.insert(deletedMenuMongodbList, MongodbConstants.DeletedCollection.MENU);
					List<String> icons = insert.stream().map(MenuMongodb::getIcon).toList();
					iconList.addAll(icons);
				}
				List<String> menuIds = deletedMenuMongodbList.stream().map(MenuMongodb::getMenuId).collect(Collectors.toList());

				// 删除功能权限
				if (!menuIds.isEmpty()) {
					Query deletePermissionQuery = Query.query(Criteria
						.where(PermissionMongodb.FIELD.APP_ID).is(appId)
						.and(PermissionMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(PermissionMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(PermissionMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(PermissionMongodb.FIELD.MENU_ID).in(menuIds)
					);
					List<PermissionMongodb> deletedPermissionMongodbList = mongoTemplate.findAllAndRemove(deletePermissionQuery, PermissionMongodb.class, MongodbConstants.Collection.PERMISSION);
					if (!deletedPermissionMongodbList.isEmpty()) {
						mongoTemplate.insert(deletedPermissionMongodbList, MongodbConstants.DeletedCollection.PERMISSION);
					}
				}

				// 移动其他菜单左右值
				List<MenuMongodb> otherLeftMenus = mongoTemplate.find(otherMenuLeftQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				otherLeftMenus.forEach(x -> {
					Query query = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId())
					);
					UpdateResult otherMenuLeftUpdateResult = mongoTemplate.updateFirst(query, otherMenuLeftUpdate, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.debug("OtherMenuLeftUpdateResult: {}", otherMenuLeftUpdateResult);
				});

				List<MenuMongodb> otherRightMenus = mongoTemplate.find(otherMenuRightQuery, MenuMongodb.class, MongodbConstants.Collection.MENU);
				otherRightMenus.forEach(x -> {
					Query query = Query.query(Criteria
						.where(MenuMongodb.FIELD.APP_ID).is(appId)
						.and(MenuMongodb.FIELD.ENDPOINT_ID).is(endpointId)
						.and(MenuMongodb.FIELD.SUBAPP_ID).is(subappId)
						.and(MenuMongodb.FIELD.SUBAPP_VERSION).is(subappVersion)
						.and(MenuMongodb.FIELD.MENU_ID).is(x.getMenuId())
					);
					UpdateResult otherMenuLeftUpdateResult = mongoTemplate.updateFirst(query, otherMenuRightUpdate, MenuMongodb.class, MongodbConstants.Collection.MENU);
					log.debug("OtherMenuRightUpdateResult: {}", otherMenuLeftUpdateResult);
				});
			} catch (BusinessException e) {
				status.setRollbackOnly();
				throw e;
			} catch (Exception e) {
				status.setRollbackOnly();
				log.debug("removeMenu", e);
				throw new ConflictBusinessException("删除菜单失败");
			}
			// 删除图标
			fileCommonService.deletePublicFile(appId.concat(FileKeyPrefixConstants.MENU_ICON_PREFIX), iconList);

		});
	}

	public void checkParams(MongoTemplate mongoTemplate, @Valid @NotNull String appId, @Valid @NotNull String endpointId, @Valid @NotNull String subappId, @Valid @NotNull String subappVersion) {
		appCommonService.checkAppId(mongoTemplate, appId);
		endpointCommonService.checkEndpointId(mongoTemplate, appId, endpointId);
		subappCommonService.checkSubappId(mongoTemplate, appId, endpointId, subappId);
		subappVersionCommonService.checkSubappVersion(mongoTemplate, subappId, subappVersion);
	}

	/**
	 * 包装数据
	 *
	 * @param ms ms
	 * @return cairo menu list
	 */
	List<MetadataMenu> getMetadataMenuList(List<MenuMongodb> ms) {

		Set<String> metadataUserIds = CairoAppUserTool.getAppUserMetadataUserIds(ms.stream().map(MenuMongodb::getMetadata).collect(Collectors.toList()));
		Map<String, AppUser> metadataUserMap = Optional.of(metadataUserIds)
			.filter(userIds -> !userIds.isEmpty())
			.map(userIds -> appUserCommonService.getAppUserMapByAppUserIds(cairoSecurityProperties.getCairoAppId(), userIds))
			.orElse(Collections.emptyMap());

		return ms.stream().map(x -> MenuConverter.convertMetadataMenu(x, metadataUserMap)).collect(Collectors.toList());
	}


}
