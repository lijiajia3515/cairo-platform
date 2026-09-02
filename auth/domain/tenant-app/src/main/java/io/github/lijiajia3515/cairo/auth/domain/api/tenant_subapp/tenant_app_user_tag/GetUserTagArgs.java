package io.github.lijiajia3515.cairo.auth.domain.api.tenant_subapp.tenant_app_user_tag;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class GetUserTagArgs extends AbstractPage<GetUserTagArgs> implements Serializable {

	/**
	 * 启用状态，允许传空
	 */
	private Boolean enabled;
}
