package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.link;

import io.github.lijiajia3515.cairo.core.page.AbstractPage;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetLinkPageListArgs extends AbstractPage<GetLinkPageListArgs> implements Serializable {
	private String keyword;
}
