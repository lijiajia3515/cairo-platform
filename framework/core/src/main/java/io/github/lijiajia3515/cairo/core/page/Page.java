package io.github.lijiajia3515.cairo.core.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Accessors(chain = true)


@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Page<T> implements Serializable {

	@Builder.Default
	private int page = 0;

	@Builder.Default
	private int size = 10;

	@Builder.Default
	private long total = 0L;

	@Builder.Default
	private List<? extends T> contents = Collections.emptyList();

	public Page() {

	}

	public Page(int page, int size) {
		this.page = page;
		this.size = size;
	}

	public Page(int page, int size, List<? extends T> contents, long total) {
		this.page = page;
		this.size = size;
		this.contents = contents;
		this.total = total;
	}

	public Page(PageRequest page) {
		this.page = page.getPageNumber();
		this.size = page.getPageSize();
	}

	public Page(PageRequest page, List<? extends T> contents, long total) {
		this.page = page.getPageNumber();
		this.size = page.getPageSize();
		this.contents = contents;
		this.total = total;
	}

	public Page(AbstractPage<?> page) {
		this.page = page.getPage();
		this.size = page.getSize();
	}

	public Page(AbstractPage<?> page, List<? extends T> contents, long total) {
		this.contents = contents;
		this.page = page.getPage();
		this.size = page.getSize();
		this.total = total;
	}

	public Stream<? extends T> stream() {
		return contents.stream();
	}

	/**
	 * converter
	 *
	 * @param mapper mapper
	 * @param <R>    new type
	 * @return new page
	 */
	public <R> Page<? extends R> convert(Function<? super T, ? extends R> mapper) {
		List<? extends R> x = contents.stream().map(mapper).collect(Collectors.toList());
		return new Page<>(page, size, x, total);
	}
}
