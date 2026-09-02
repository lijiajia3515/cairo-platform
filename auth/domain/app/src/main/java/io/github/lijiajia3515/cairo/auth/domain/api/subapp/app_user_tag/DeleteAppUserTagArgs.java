package io.github.lijiajia3515.cairo.auth.domain.api.subapp.app_user_tag;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * 删除tag参数
 */
@Data
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteAppUserTagArgs implements Serializable {

	/**
	 * 标签ID
	 */
	@NotNull
	@NotEmpty
	private List<String> tagIds;
}
