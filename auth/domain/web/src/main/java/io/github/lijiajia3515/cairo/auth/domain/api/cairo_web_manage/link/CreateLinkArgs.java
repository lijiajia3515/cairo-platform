package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateLinkArgs extends AbstractPage<CreateLinkArgs> implements Serializable {

	/**
	 * 短链
	 */
	@NotNull
	@URL
	private String linkUrl;

}
