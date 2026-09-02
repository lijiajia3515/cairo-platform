package io.github.lijiajia3515.cairo.auth.modules.notify.category.args;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetNotifyCategoryArgs implements Serializable {

	/**
	 * 类别ID
	 */
	private List<String> categoryIds;
}
