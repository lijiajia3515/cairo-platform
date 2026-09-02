package io.github.lijiajia3515.cairo.auth.modules.weboffice.v3;

import cn.hutool.core.io.file.FileNameUtil;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app.GetAppArgs;
import io.github.lijiajia3515.cairo.auth.domain.api.client.app_user.GetAppUserClientArgs;
import io.github.lijiajia3515.cairo.auth.domain.dto.app.App;
import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.api.client.app.AppClientApiService;
import io.github.lijiajia3515.cairo.auth.modules.biz_log.BizLog;
import io.github.lijiajia3515.cairo.core.CoreConstants;
import io.github.lijiajia3515.cairo.mongodb.domain.NoneMetadataMongodb;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.DocMode;
import io.github.lijiajia3515.cairo.auth.domain.dto.weboffice.WebOfficeDocToken;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.OfficeFileMongodb;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.OfficeFileVersionMongodb;
import io.github.lijiajia3515.cairo.auth.domain.dto.file.FileStat;
import io.github.lijiajia3515.cairo.auth.modules.file.FileTools;
import io.github.lijiajia3515.cairo.auth.modules.file.common_file.CommonFileCommonService;
import io.github.lijiajia3515.cairo.auth.domain.api.subapp.file.common_file.GetFileStatArgs;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.*;
import io.github.lijiajia3515.cairo.auth.modules.weboffice.v3.model.*;
import io.micrometer.tracing.annotation.NewSpan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static io.github.lijiajia3515.cairo.auth.modules.file.FileTools.PATH_NAME;
import static io.github.lijiajia3515.cairo.auth.modules.weboffice.WebOfficeTool.officeFilePath;
import static io.github.lijiajia3515.cairo.auth.modules.weboffice.WebOfficeTool.officeS3FilePath;
import io.github.lijiajia3515.cairo.auth.modules.app_user.AppUserCommonService;

@Slf4j
@Component
public class WebOfficeAppDocService extends WebOfficeService {

	private final CommonFileCommonService commonFileCommonService;

	private final TransactionTemplate transactionTemplate;
	private final MongoTemplate mongoTemplate;
	private final MongoTemplate readMongoTemplate;

	private final AppUserCommonService appUserCommonService;
	private final AppClientApiService appClientApiService;

	private final WebofficeProperties webofficeProperties;

	public WebOfficeAppDocService(TransactionTemplate transactionTemplate,
								  @Qualifier("mongoTemplate") MongoTemplate mongoTemplate,
								  @Qualifier("readMongoTemplate") MongoTemplate readMongoTemplate,
								  CommonFileCommonService commonFileCommonService,
								  AppUserCommonService appUserCommonService,
								  AppClientApiService appClientApiService,
								  WebofficeProperties webofficeProperties) {
		this.transactionTemplate = transactionTemplate;
		this.mongoTemplate = mongoTemplate;
		this.readMongoTemplate = readMongoTemplate;
		this.commonFileCommonService = commonFileCommonService;
		this.appUserCommonService = appUserCommonService;
		this.appClientApiService = appClientApiService;
		this.webofficeProperties = webofficeProperties;
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:get_app_doc_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
			@BizLog.Param(key = "sourceFilePath", value = "#sourceFilePath"),
		}
	)
	public OfficeFileMongodb getAppDocFile(String appId, String userId, String sourceFilePath) {
		Map<String, String> fileMap = FileTools.decodeS3Meta(sourceFilePath);
		Criteria criteria = Criteria
			.where(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileMongodb.FIELD.APP_ID).is(appId)
			.and(OfficeFileMongodb.FIELD.SOURCE_FILE_PATH).is(sourceFilePath);

		Query query = Query.query(criteria);
		OfficeFileMongodb fileMongodb = readMongoTemplate.findOne(query, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);
		if (fileMongodb == null) {
			String fileId = CoreConstants.SNOWFLAKE.nextIdStr();
			Integer fileVersion = 1;
			String filename = FileNameUtil.getName(fileMap.get(PATH_NAME));
			String extName = FileNameUtil.extName(filename);
			String newS3Filepath = officeS3FilePath(fileId, fileVersion, filename);

			List<FileStat> stat = commonFileCommonService.getFileStat(GetFileStatArgs.builder()
				.s3Urls(Collections.singletonList(sourceFilePath))
				.build()
			);
			int size = stat.get(0).getSize().intValue();
			newS3Filepath = commonFileCommonService.copyFile(sourceFilePath, newS3Filepath);

			final OfficeFileMongodb[] newFileMongodb = {OfficeFileMongodb.builder()
				.metadata(NoneMetadataMongodb.builder().build())
				.fileId(fileId)
				.fileVersion(fileVersion)
				.sourceFilePath(sourceFilePath)
				.fileName(filename)
				.type(getType(extName))
				.filepath(newS3Filepath)
				.size(size)
				.activeTime(LocalDateTime.now())
				.mode(DocMode.APP)
				.appId(appId)
				.createUserId(userId)
				.updateUserId(userId)
				.build()};

			final OfficeFileVersionMongodb[] newFileVersionMongodb = {OfficeFileVersionMongodb.builder()
				.metadata(NoneMetadataMongodb.builder().build())
				.recordId(CoreConstants.SNOWFLAKE.nextIdStr())
				.fileId(fileId)
				.fileVersion(fileVersion)
				.name(filename)
				.size(size)
				.filepath(newS3Filepath)
				.mode(DocMode.APP)
				.appId(appId)
				.createUserId(userId)
				.updateUserId(userId)
				.build()};
			transactionTemplate.executeWithoutResult(status -> {
				newFileMongodb[0] = mongoTemplate.insert(newFileMongodb[0], MongodbConstants.Collection.OFFICE_FILE);
				newFileVersionMongodb[0] = mongoTemplate.insert(newFileVersionMongodb[0], MongodbConstants.Collection.OFFICE_FILE_VERSION);
			});
			fileMongodb = newFileMongodb[0];

		}
		return fileMongodb;
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:generate_app_doc_token",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "type", value = "#type"),
			@BizLog.Param(key = "write", value = "#write"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeDocToken generateAppDocToken(String fileId, String type, boolean write, String appId, String userId) {
		return WebOfficeDocToken.builder()
			.appId(webofficeProperties.getAppid())
			.fileId(fileId)
			.type(type)
			.token(WebOfficeTicketToken.builder()
				.mode(DocMode.APP)
				.appId(appId)
				.userId(userId)
				.write(write ? "1" : "0")
				.build()
				.toToken()
			)
			.write(write)
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_file_info",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFileResponse appDocFileInfo(String fileId, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileMongodb.FIELD.APP_ID).is(appId);

		Query query = Query.query(criteria);
		Update update = Update.update(OfficeFileMongodb.FIELD.ACTIVE_TIME, LocalDateTime.now());
		OfficeFileMongodb file = mongoTemplate.findAndModify(query, update, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);

		if (file == null) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40004);
		}

		return WebOfficeFileResponse.builder()
			.id(fileId)
			.name(file.getFileName())
			.version(file.getFileVersion())
			.size(file.getSize())
			.createTime((int) file.getMetadata().getCreateTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.modifyTime((int) file.getMetadata().getUpdateTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.creatorId(WebOfficeTool.appUserToWebOfficeUserId(appId, file.getCreateUserId()))
			.modifierId(WebOfficeTool.appUserToWebOfficeUserId(appId, file.getUpdateUserId()))
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_file_download_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFileDownloadResponse appDocFileDownloadFile(String fileId, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileMongodb.FIELD.APP_ID).is(appId);

		Query query = Query.query(criteria);
		Update update = Update.update(OfficeFileMongodb.FIELD.ACTIVE_TIME, LocalDateTime.now());
		OfficeFileMongodb file = mongoTemplate.findAndModify(query, update, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);

		if (file == null) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40004);
		}

		return WebOfficeFileDownloadResponse.builder()
			.url(commonFileCommonService.getAccessFileUrl(file.getFilepath(), false))
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_permission_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "write", value = "#write"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFilePermissionResponse appDocPermissionFile(String fileId, String write, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileMongodb.FIELD.APP_ID).is(appId);

		Query query = Query.query(criteria);
		Update update = Update.update(OfficeFileMongodb.FIELD.ACTIVE_TIME, LocalDateTime.now());
		OfficeFileMongodb file = mongoTemplate.findAndModify(query, update, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);

		if (file == null) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40004);
		}

		int writeFlag = write.equals("0") ? 0 : 1;

		return WebOfficeFilePermissionResponse.builder()
			.userId(WebOfficeTool.appUserToWebOfficeUserId(appId, userId))
			.read(1)
			.update(writeFlag)
			.download(1)
			.rename(1)
			.history(1)
			.copy(1)
			.print(1)
			.saveas(1)
			.comment(1)
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_file_versions",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "offset", value = "#offset"),
			@BizLog.Param(key = "limit", value = "#limit"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public List<WebOfficeFileVersionResponse> appDocFileVersions(String fileId, int offset, int limit, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileVersionMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileVersionMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileVersionMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		query.skip(offset).limit(limit);
		query.with(Sort.by(Sort.Order.desc(OfficeFileVersionMongodb.FIELD.FILE_VERSION)));
		List<OfficeFileVersionMongodb> fileVersions = readMongoTemplate.find(query, OfficeFileVersionMongodb.class, MongodbConstants.Collection.OFFICE_FILE_VERSION);

		updateActiveFile(fileId, appId);

		return fileVersions.stream().map(x -> WebOfficeFileVersionResponse.builder()
			.id(fileId)
			.name(x.getName())
			.version(x.getFileVersion())
			.size(x.getSize())
			.createTime((int) x.getMetadata().getCreateTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.modifyTime((int) x.getMetadata().getUpdateTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.creatorId(WebOfficeTool.appUserToWebOfficeUserId(appId, x.getCreateUserId()))
			.modifierId(WebOfficeTool.appUserToWebOfficeUserId(appId, x.getUpdateUserId()))
			.build()
		).collect(Collectors.toList());
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_file_version",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "version", value = "#version"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFileVersionResponse appDocFileVersion(String fileId, int version, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileVersionMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileVersionMongodb.FIELD.FILE_VERSION).is(version)
			.and(OfficeFileVersionMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileVersionMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		OfficeFileVersionMongodb fileVersion = readMongoTemplate.findOne(query, OfficeFileVersionMongodb.class, MongodbConstants.Collection.OFFICE_FILE_VERSION);

		if (fileVersion == null) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40009);
		}

		updateActiveFile(fileId, appId);

		return WebOfficeFileVersionResponse.builder()
			.id(fileId)
			.name(fileVersion.getName())
			.version(fileVersion.getFileVersion())
			.size(fileVersion.getSize())
			.createTime((int) fileVersion.getMetadata().getCreateTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.modifyTime((int) fileVersion.getMetadata().getUpdateTime().toEpochSecond(ZoneOffset.ofHours(8)))
			.creatorId(WebOfficeTool.appUserToWebOfficeUserId(appId, fileVersion.getCreateUserId()))
			.modifierId(WebOfficeTool.appUserToWebOfficeUserId(appId, fileVersion.getUpdateUserId()))
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_download_file_version",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "fileVersion", value = "#fileVersion"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFileDownloadResponse appDocDownloadFileVersion(String fileId, int fileVersion, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileVersionMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileVersionMongodb.FIELD.FILE_VERSION).is(fileVersion)
			.and(OfficeFileVersionMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileVersionMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		OfficeFileVersionMongodb officeFileVersion = readMongoTemplate.findOne(query, OfficeFileVersionMongodb.class, MongodbConstants.Collection.OFFICE_FILE_VERSION);

		if (officeFileVersion == null) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40009);
		}

		updateActiveFile(fileId, appId);

		return WebOfficeFileDownloadResponse.builder()
			.url(commonFileCommonService.getAccessFileUrl(officeFileVersion.getFilepath(), true))
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_upload_address_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "request", value = "#request"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFileUploadAddressResponse appDocUploadAddressFile(String fileId, WebOfficeFileUploadAddressRequest request, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileMongodb.FIELD.APP_ID).is(appId);
		Query query = Query.query(criteria);
		OfficeFileMongodb file = readMongoTemplate.findOne(query, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);
		if (file == null) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40004);
		}
		int fileVersion = file.getFileVersion() + 1;
		String newVersionFilePath = officeFilePath(fileId, fileVersion, request.getName());
		List<List<String>> customUploadSignUrl = commonFileCommonService.getUploadFileSignUrl(DOC_BUCKET, List.of(newVersionFilePath), Collections.emptyMap());
		return WebOfficeFileUploadAddressResponse.builder()
			.method("PUT")
			.url(customUploadSignUrl.get(0).get(2))
			.sendBackParams(Collections.singletonMap("fileVersion", "" + fileVersion))
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_complete_upload_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "request", value = "#request"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFileResponse appDocCompleteUploadFile(String fileId, WebOfficeFileUploadCompleteRequest body, String appId, String userId) {
		try {
			HttpStatus httpStatus = HttpStatus.valueOf(body.getResponse().getStatusCode());
			int fileVersion = Integer.parseInt(Optional.ofNullable(body.getSendBackParams()).map(x -> x.get("fileVersion")).orElse("0"));
			if (!httpStatus.is2xxSuccessful()) {
				throw new WebOfficeRuntimeException(WebOfficeError.E41001);
			}

			Criteria criteria = Criteria
				.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
				.and(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
				.and(OfficeFileMongodb.FIELD.APP_ID).is(appId);
			Query query = Query.query(criteria);
			OfficeFileMongodb file = readMongoTemplate.findOne(query, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);
			if (file == null) {
				throw new WebOfficeRuntimeException(WebOfficeError.E40004);
			}

			String officeFilePath = officeS3FilePath(fileId, fileVersion, body.getRequest().getName());
			OfficeFileVersionMongodb fileVersionMongodb = OfficeFileVersionMongodb.builder()
				.recordId(CoreConstants.SNOWFLAKE.nextIdStr())
				.fileId(fileId)
				.name(body.getRequest().getName())
				.size(body.getRequest().getSize())
				.fileVersion(fileVersion)
				.filepath(officeFilePath)
				.digest(body.getRequest().getDigest())
				.mode(DocMode.APP)
				.appId(appId)
				.createUserId(userId)
				.updateUserId(userId)
				.metadata(NoneMetadataMongodb.builder().build())
				.build();
			Update fileUpdate = Update
				.update(OfficeFileMongodb.FIELD.UPDATE_USER_ID, userId)
				.set(OfficeFileMongodb.FIELD.FILE_VERSION, fileVersion)
				.set(OfficeFileMongodb.FIELD.FILE_NAME, body.getRequest().getName())
				.set(OfficeFileMongodb.FIELD.FILEPATH, officeFilePath)
				.set(OfficeFileMongodb.FIELD.SIZE, body.getRequest().getSize())
				.set(OfficeFileMongodb.FIELD.DIGEST, body.getRequest().getDigest())
				.set(OfficeFileMongodb.FIELD.UPDATE_USER_ID, userId);
			transactionTemplate.executeWithoutResult(status -> {
				mongoTemplate.insert(fileVersionMongodb, MongodbConstants.Collection.OFFICE_FILE_VERSION);
				Query fileQuery = Query.query(Criteria
					.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
					.and(OfficeFileMongodb.FIELD.FILE_VERSION).lte(fileVersion)
				);
				mongoTemplate.updateFirst(fileQuery, fileUpdate, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);
			});

			String newFileUrl = commonFileCommonService.copyFile(officeFilePath, file.getSourceFilePath());
			log.info("weboffice sync source file: {} newFile: {}", file.getSourceFilePath(), newFileUrl);

			return WebOfficeFileResponse.builder()
				.id(fileId)
				.name(file.getFileName())
				.version(file.getFileVersion())
				.size(file.getSize())
				.createTime((int) file.getMetadata().getCreateTime().toEpochSecond(ZoneOffset.ofHours(8)))
				.modifyTime((int) file.getMetadata().getUpdateTime().toEpochSecond(ZoneOffset.ofHours(8)))
				.creatorId(WebOfficeTool.appUserToWebOfficeUserId(appId, userId))
				.modifierId(WebOfficeTool.appUserToWebOfficeUserId(appId, userId))
				.build();
		} catch (RuntimeException e) {
			log.info("appDocCompleteUploadFile", e);
			throw new WebOfficeRuntimeException(WebOfficeError.E50001);
		}
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_watermark_file",
		scope = "read",
		params = {
			@BizLog.Param(key = "fileId", value = "#fileId"),
			@BizLog.Param(key = "appId", value = "#appId"),
			@BizLog.Param(key = "userId", value = "#userId"),
		}
	)
	public WebOfficeFileWatermarkResponse appDocWatermarkFile(String fileId, String appId, String userId) {
		Criteria criteria = Criteria
			.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileMongodb.FIELD.APP_ID).is(appId);

		Query query = Query.query(criteria);
		Update update = Update.update(OfficeFileMongodb.FIELD.ACTIVE_TIME, LocalDateTime.now());
		OfficeFileMongodb file = mongoTemplate.findAndModify(query, update, OfficeFileMongodb.class, MongodbConstants.Collection.OFFICE_FILE);

		if (file == null) {
			throw new WebOfficeRuntimeException(WebOfficeError.E40004);
		}
		String name = "Cairo-Platform";

		try {
			List<App> appList = appClientApiService.getAppList(GetAppArgs.builder()
				.appIds(Collections.singletonList(appId))
				.build());
			App app = Optional.ofNullable(appList)
				.map(x -> x.stream().collect(Collectors.toMap(App::getAppId, z -> z)))
				.orElse(Collections.emptyMap())
				.get(appId);
			if (app != null && app.getAppName() != null && !app.getAppName().isBlank()) {
				name = app.getAppName();
			}
		} catch (RuntimeException e) {
			log.info("getApp error", e);
		}

		return WebOfficeFileWatermarkResponse.builder()
			.type(1)
			.value(name)
			.fillStyle("rgba(192,192,192,0.6)")
			.font("bold 20px Serif")
			.rotate(-45d)
			.horizontal(250)
			.vertical(250)
			.build();
	}

	@NewSpan
	@BizLog(
		bizId = "web_office_app_doc:app_doc_user_list",
		scope = "read",
		params = {
			@BizLog.Param(key = "userIds", value = "#userIds"),
			@BizLog.Param(key = "appId", value = "#appId"),
		}
	)
	public List<WebOfficeUser> appDocUserList(List<String> userIds, String appId) {
		Set<String> appUserIds = userIds.stream().map(WebOfficeTool::webOfficeUserIdToAppUser).filter(Objects::nonNull).map(CairoAppUser::getUserId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());
		if (appUserIds.isEmpty()) {
			return Collections.emptyList();
		}

		Map<String, AppUser> basicuUserMap = appUserCommonService.getAppUserMapByAppUserIds(appId, appUserIds);
		Map<String, AppUser> userMap = basicuUserMap.values().stream().collect(Collectors.toMap(x -> WebOfficeTool.appUserToWebOfficeUserId(appId, x.getUserId()), x -> x));
		return userIds.stream()
			.map(x -> WebOfficeUser.builder()
				.id(x)
				.name(Optional.ofNullable(userMap.get(x)).map(AppUser::getNickname).orElse(x))
				.avatarUrl(Optional.ofNullable(userMap.get(x)).map(AppUser::getAccountAvatarUrl).orElse(DEFAULT_AVATAR_URL))
				.build()
			)
			.collect(Collectors.toList());
	}

	//	@NewSpan
	void updateActiveFile(String fileId, String appId) {
		Criteria criteria = Criteria
			.where(OfficeFileMongodb.FIELD.FILE_ID).is(fileId)
			.and(OfficeFileMongodb.FIELD.MODE).is(DocMode.APP)
			.and(OfficeFileMongodb.FIELD.APP_ID).is(appId);

		Query query = Query.query(criteria);
		Update update = Update.update(OfficeFileMongodb.FIELD.ACTIVE_TIME, LocalDateTime.now());
		mongoTemplate.updateFirst(query, update, MongodbConstants.Collection.OFFICE_FILE);
	}
}
