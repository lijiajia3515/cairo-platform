package io.github.lijiajia3515.cairo.auth.modules.ip2region;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;

import jakarta.annotation.PostConstruct;
import java.util.Optional;

@Slf4j
public class Ip2RegionService {
	public static final RegionModel ERROR_REGION = RegionModel.builder()
		.country("未知")
		.id("未知")
		.province("未知")
		.city("未知")
		.isp("未知")
		.build();

	private Searcher searcher;
	private final String dbPath;

	public Ip2RegionService(String dbPath) {
		this.dbPath = dbPath;
	}

	@PostConstruct
	public void init() {
		// 1、从 dbPath 加载整个 xdb 到内存。
		byte[] cBuff;
		try {
			cBuff = Searcher.loadContentFromFile(dbPath);
		} catch (Exception e) {
			log.warn("failed to load content from '{}', error: {}", dbPath, e.getMessage());
			return;
		}

		// 2、使用上述的 cBuff 创建一个完全基于内存的查询对象。
		try {
			searcher = Searcher.newWithBuffer(cBuff);
		} catch (Exception e) {
			log.warn("failed to create content cached searcher: {}", e.getMessage());
		}

	}

	public RegionModel getRegion(String ip) {
		RegionModel model = null;
		try {
			if (searcher != null) {
				String region = searcher.search(ip);
				String[] regionArray = region.split("\\|");
				if (regionArray.length == 5) {
					model = RegionModel.builder()
						.country(Optional.ofNullable(regionArray[0]).filter(x -> !"0".equals(x)).orElse(ERROR_REGION.getCountry()))
						.id(Optional.ofNullable(regionArray[1]).filter(x -> !"0".equals(x)).orElse(ERROR_REGION.getId()))
						.province(Optional.ofNullable(regionArray[2]).filter(x -> !"0".equals(x)).orElse(ERROR_REGION.getProvince()))
						.city(Optional.ofNullable(regionArray[3]).filter(x -> !"0".equals(x)).orElse(ERROR_REGION.getCity()))
						.isp(Optional.ofNullable(regionArray[4]).filter(x -> !"0".equals(x)).orElse(ERROR_REGION.getIsp()))
						.build();
				}
			}

		} catch (Exception e) {
			log.info("ip2region error: {}", e.getMessage());
		}
		return Optional.ofNullable(model).orElse(ERROR_REGION);
	}

	public String getRegionStr(String ip) {
		RegionModel region = getRegion(ip);
		if (region.getCountry().equals(ERROR_REGION.getCountry())) {
			return ERROR_REGION.getCountry();
		}
		if (region.getProvince().equals(ERROR_REGION.getProvince())) {
			return region.getCountry();
		}
		if (region.getCity().equals(ERROR_REGION.getCity())) {
			return region.getProvince();
		}
		return region.getCity();
	}
}
