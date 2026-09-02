package io.github.lijiajia3515.cairo.auth.modules.subapp;

import com.mongodb.client.MongoClients;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
import io.github.lijiajia3515.cairo.auth.constants.MongodbConstants;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.SubappMongodb;
import io.github.lijiajia3515.cairo.core.exception.ConflictBusinessException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 子应用公共服务集成测试：嵌入式 MongoDB（flapdoodle transitions API，无 Docker 依赖）
 * <p>
 * 首次运行会从 fastdl.mongodb.org 下载 mongod 二进制并缓存（~/.embedmongo），之后直接复用。
 */
class SubappCommonServiceEmbeddedMongoTest {

	private static TransitionWalker.ReachedState<RunningMongodProcess> mongod;
	private static MongoTemplate mongoTemplate;
	private static SubappCommonService service;

	@BeforeAll
	static void startEmbeddedMongo() throws IOException {
		int port = freePort();
		mongod = Mongod.builder()
			.net(Start.to(Net.class).initializedWith(Net.of("localhost", port, false)))
			.build()
			// 钉住 7.0：PRODUCTION 解析出的旧构建依赖 OpenSSL 1.1（libcrypto.so.1.1），
			// 在仅带 OpenSSL 3 的 ubuntu-latest（CI）上无法启动；7.0 的 ubuntu 构建链 OpenSSL 3
			.start(Version.Main.V7_0);

		SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(
			MongoClients.create("mongodb://localhost:" + port), "cairo_test");
		mongoTemplate = new MongoTemplate(factory);
		TransactionTemplate transactionTemplate = new TransactionTemplate(new MongoTransactionManager(factory));
		service = new SubappCommonService(mongoTemplate, transactionTemplate, mongoTemplate);
	}

	@AfterAll
	static void stopEmbeddedMongo() {
		if (mongod != null) {
			mongod.close();
		}
	}

	@BeforeEach
	void cleanCollection() {
		mongoTemplate.remove(new Query(), SubappMongodb.class, MongodbConstants.Collection.SUBAPP);
	}

	private static int freePort() throws IOException {
		try (ServerSocket socket = new ServerSocket(0)) {
			return socket.getLocalPort();
		}
	}

	private void insert(String appId, String endpointId, String subappId) {
		mongoTemplate.insert(SubappMongodb.builder()
			.appId(appId).endpointId(endpointId).subappId(subappId)
			.scope("public").enabled(true)
			.build(), MongodbConstants.Collection.SUBAPP);
	}

	@Test
	void 按ID集合查询_过滤并排序() {
		insert("cairo", "web", "manage");
		insert("cairo", "web", "demo");
		insert("cairo", "app", "other");

		// 库里 3 条，只查其中 2 个 ID：验证 in 过滤 + 排序（demo < manage）
		List<io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp> list =
			service.getSubappListBySubappIds(List.of("manage", "demo"));

		assertThat(list).hasSize(2);
		assertThat(list).extracting(io.github.lijiajia3515.cairo.auth.domain.dto.subapp.Subapp::getSubappId)
			.containsExactly("demo", "manage");
	}

	@Test
	void 空集合入参_返回空列表() {
		assertThat(service.getSubappListBySubappIds(null)).isEmpty();
		assertThat(service.getSubappListBySubappIds(List.of())).isEmpty();
	}

	@Test
	void 按ID集合转Map_以subappId为键() {
		insert("cairo", "web", "manage");
		insert("cairo", "web", "demo");

		var map = service.getSubappMapBySubappIds(List.of("manage", "demo"));

		assertThat(map).containsOnlyKeys("manage", "demo");
		assertThat(map.get("manage").getAppId()).isEqualTo("cairo");
	}

	@Test
	void 校验子应用ID_存在时放行() {
		insert("cairo", "web", "manage");

		assertThatCode(() -> service.checkSubappId(mongoTemplate, "cairo", "web", "manage"))
			.doesNotThrowAnyException();
	}

	@Test
	void 校验子应用ID_不存在时冲突() {
		insert("cairo", "web", "manage");

		assertThatThrownBy(() -> service.checkSubappId(mongoTemplate, "cairo", "web", "missing"))
			.isInstanceOf(ConflictBusinessException.class);
	}
}
