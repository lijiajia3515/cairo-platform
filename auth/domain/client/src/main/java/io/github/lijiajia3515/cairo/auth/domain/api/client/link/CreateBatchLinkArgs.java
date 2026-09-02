package io.github.lijiajia3515.cairo.auth.domain.api.client.link;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.URL;

import java.io.Serializable;
import java.util.List;

/**
 * 批量创建短链
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBatchLinkArgs implements Serializable {

	/**
	 * 短链数组
	 */
	@NotNull
	@NotEmpty
	private List<@NotNull @URL String> linkUrls;


}
