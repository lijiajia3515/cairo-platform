package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.client;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除客户端参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteClientArgs {
	/**
	 * id
	 */
	@NotNull
	private String id;
}
