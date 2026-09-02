package io.github.lijiajia3515.cairo.auth.modules.area;


import io.github.lijiajia3515.cairo.auth.domain.dto.app_user.AppUser;
import io.github.lijiajia3515.cairo.auth.modules.app_user.CairoAppUserConverter;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.Area;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaDetail;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.AreaTree;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.MetadataArea;
import io.github.lijiajia3515.cairo.auth.domain.dto.area.MetadataAreaDetail;
import io.github.lijiajia3515.cairo.auth.domain.mongodb.AreaMongodb;

import java.util.Map;

public class AreaConverter {
	public static Area convertArea(AreaMongodb data, boolean shortName) {
		return Area.builder()
			.areaId(data.getAreaId())
			.areaName(shortName ? data.getShortAreaName() : data.getAreaName())
			.pinYinPrefix(data.getPinYinPrefix())
			.hot(data.isHot())
			.enabled(data.isEnabled())
			.depth(data.getDepth())
			.sort(data.getSort())
			.build();
	}

	public static Area convertCityArea(AreaMongodb data, boolean shortName) {
		return Area.builder()
			.areaId(data.getAreaId())
			.areaName(shortName ? data.getShortAreaName() : data.getAreaName())
			.pinYinPrefix(data.getPinYinPrefix())
			.hot(data.isHot())
			.enabled(data.isEnabled())
			.sort(data.getSort())
			.build();
	}

	public static AreaTree convertAreaTree(AreaMongodb data, boolean shortName) {
		return AreaTree.builder()
			.parentAreaId(data.getParentAreaId())
			.areaId(data.getAreaId())
			.areaName(shortName ? data.getShortAreaName() : data.getAreaName())
			.pinYinPrefix(data.getPinYinPrefix())
			.hot(data.isHot())
			.enabled(data.isEnabled())
			.depth(data.getDepth())
			.sort(data.getSort())
			.build();
	}

	public static AreaDetail convertAreaDetail(AreaMongodb data, boolean shortName) {
		AreaDetail.AreaDetailBuilder<?, ?> builder = AreaDetail.builder()
			.depth(data.getDepth())
			.areaId(data.getAreaId())
			.hot(data.isHot())
			.enabled(data.isEnabled());

		if (data.getAreaId() != null) {
			// 省
			if (!data.getAreaIds().isEmpty()) {
				builder.provinceId(data.getAreaIds().get(0));
				builder.provinceName(shortName ? data.getShortAreaNames().get(0) : data.getAreaNames().get(0));
			}

			// 市
			if (data.getAreaIds().size() >= 2) {
				builder.cityId(data.getAreaIds().get(1));
				builder.cityName(shortName ? data.getShortAreaNames().get(1) : data.getAreaNames().get(1));
			}

			// 区
			if (data.getAreaIds().size() >= 3) {
				builder.districtId(data.getAreaIds().get(2));
				builder.districtName(shortName ? data.getShortAreaNames().get(2) : data.getAreaNames().get(2));
			}

			// 街道
			if (data.getAreaIds().size() >= 4) {
				builder.streetId(data.getAreaIds().get(3));
				builder.streetName(shortName ? data.getShortAreaNames().get(3) : data.getAreaNames().get(3));
			}
		}

		return builder.build();
	}

	public static MetadataArea convertMetadataArea(AreaMongodb data, Map<String, AppUser> metadataUserMap) {
		return MetadataArea.builder()
			.areaId(data.getAreaId())
			.areaName(data.getAreaName())
			.shortAreaName(data.getShortAreaName())
			.pinYin(data.getPinYin())
			.pinYinPrefix(data.getPinYinPrefix())
			.hot(data.isHot())
			.enabled(data.isEnabled())
			.depth(data.getDepth())
			.sort(data.getSort())
			.metadata(CairoAppUserConverter.convertAppUser(data.getMetadata(), metadataUserMap))
			.build();
	}

	public static MetadataAreaDetail convertMetadataAreaDetail(AreaMongodb data, Map<String, AppUser> metadataUserMap) {
		MetadataAreaDetail.MetadataAreaDetailBuilder<?, ?> builder = MetadataAreaDetail.builder()
			.depth(data.getDepth())
			.areaId(data.getAreaId())
			.pinYin(data.getPinYin())
			.pinYinPrefix(data.getPinYinPrefix())
			.hot(data.isHot())
			.enabled(data.isEnabled())
			.sort(data.getSort())
			.metadata(CairoAppUserConverter.convertAppUser(data.getMetadata(), metadataUserMap))
			;

		if (data.getAreaId() != null) {
			// 省
			if (!data.getAreaIds().isEmpty()) {
				builder.provinceId(data.getAreaIds().get(0));
				builder.provinceName(data.getAreaNames().get(0));
				builder.provinceShortName(data.getShortAreaNames().get(0));
			}

			// 市
			if (data.getAreaIds().size() >= 2) {
				builder.cityId(data.getAreaIds().get(1));
				builder.cityName(data.getAreaNames().get(1));
				builder.cityShortName(data.getShortAreaNames().get(1));
			}

			// 区
			if (data.getAreaIds().size() >= 3) {
				builder.districtId(data.getAreaIds().get(2));
				builder.districtName(data.getAreaNames().get(2));
				builder.districtShortName(data.getShortAreaNames().get(2));
			}

			// 街道
			if (data.getAreaIds().size() >= 4) {
				builder.streetId(data.getAreaIds().get(3));
				builder.streetName(data.getAreaNames().get(3));
				builder.streetShortName(data.getShortAreaNames().get(3));
			}
		}

		return builder.build();
	}
}
