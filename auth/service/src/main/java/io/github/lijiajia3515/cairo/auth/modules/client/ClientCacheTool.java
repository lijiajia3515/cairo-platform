package io.github.lijiajia3515.cairo.auth.modules.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientCacheTool {

	@Caching(evict = {
		@CacheEvict(cacheNames = "oauth2_client_by_id", key = "#id"),
		@CacheEvict(cacheNames = "oauth2_client_by_clientId", key = "#clientId")
	})
	public void removeCache(String id, String clientId) {
		log.debug("remove client cache: id: {} clientId: {}", id, clientId);
	}
}
