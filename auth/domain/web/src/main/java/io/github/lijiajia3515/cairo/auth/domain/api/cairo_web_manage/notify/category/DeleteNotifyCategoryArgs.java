package io.github.lijiajia3515.cairo.auth.domain.api.cairo_web_manage.notify.category;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class DeleteNotifyCategoryArgs {


	@NotEmpty
	private List<String> categoryIds;
}
