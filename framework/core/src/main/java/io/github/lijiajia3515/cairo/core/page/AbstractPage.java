package io.github.lijiajia3515.cairo.core.page;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


/**
 * 抽象 分页请求参数
 *
 * @param <T> 真实参数对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class AbstractPage<T> {

	@Builder.Default
	@Min(0)
	private int page = 0;

	@Builder.Default
	@Max(10000)
	private int size = 15;

	private String sort;

	public int getPage() {
		return page;
	}

	@SuppressWarnings("unchecked")
	public T setPage(int page) {
		this.page = page;
		return (T) this;
	}

	public int getSize() {
		return size;
	}

	@SuppressWarnings("unchecked")
	public T setSize(int size) {
		this.size = size;
		return (T) this;
	}


	public Pageable pageable() {
		return PageRequest.of(page, size);
	}
}
