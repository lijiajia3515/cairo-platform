//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.github.lijiajia3515.cairo.core.result.error;

import java.util.List;
import java.util.Objects;

/**
 * 简单错误对象
 */
public class SimpleBusinessError {
	private List<SimpleExceptionErrorMessage> exception;
	private String trace;


	public SimpleBusinessError() {
	}

	public SimpleBusinessError(List<SimpleExceptionErrorMessage> exception, String trace) {
		this.exception = exception;
		this.trace = trace;
	}

	protected SimpleBusinessError(SimpleBusinessErrorBuilder<?, ?> b) {
		this.exception = b.exception;
		this.trace = b.trace;
	}

	public SimpleBusinessError exception(List<SimpleExceptionErrorMessage> exception) {
		this.exception = exception;
		return this;
	}

	public List<SimpleExceptionErrorMessage> getException() {
		return this.exception;
	}

	public SimpleBusinessError setException(List<SimpleExceptionErrorMessage> exception) {
		this.exception = exception;
		return this;
	}

	public String trace() {
		return this.trace;
	}

	public SimpleBusinessError trace(String trace) {
		this.trace = trace;
		return this;
	}

	public String getTrace() {
		return this.trace;
	}

	public SimpleBusinessError setTrace(String trace) {
		this.trace = trace;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		SimpleBusinessError that = (SimpleBusinessError) o;
		return Objects.equals(exception, that.exception) && Objects.equals(trace, that.trace);
	}

	@Override
	public int hashCode() {
		return Objects.hash(exception, trace);
	}

	@Override
	public String toString() {
		return "SimpleBusinessError{" + "exception=" + exception + ", trace='" + trace + '\'' + '}';
	}

	public static SimpleBusinessErrorBuilder<?, ?> builder() {
		return new SimpleBusinessErrorBuilderImpl();
	}

	public SimpleBusinessErrorBuilder<?, ?> toBuilder() {
		return (new SimpleBusinessErrorBuilderImpl()).$fillValuesFrom(this);
	}

	public List<SimpleExceptionErrorMessage> exception() {
		return this.exception;
	}


	private static final class SimpleBusinessErrorBuilderImpl extends SimpleBusinessErrorBuilder<SimpleBusinessError, SimpleBusinessErrorBuilderImpl> {
		private SimpleBusinessErrorBuilderImpl() {
		}

		protected SimpleBusinessErrorBuilderImpl self() {
			return this;
		}

		public SimpleBusinessError build() {
			return new SimpleBusinessError(this);
		}
	}

	public abstract static class SimpleBusinessErrorBuilder<C extends SimpleBusinessError, B extends SimpleBusinessErrorBuilder<C, B>> {
		private List<SimpleExceptionErrorMessage> exception;
		private String trace;

		public SimpleBusinessErrorBuilder() {
		}

		protected B $fillValuesFrom(C instance) {
			$fillValuesFromInstanceIntoBuilder(instance, this);
			return this.self();
		}

		private static void $fillValuesFromInstanceIntoBuilder(SimpleBusinessError instance, SimpleBusinessErrorBuilder<?, ?> b) {
			b.exception(instance.exception);
			b.trace(instance.trace);
		}

		protected abstract B self();

		public abstract C build();

		public B exception(List<SimpleExceptionErrorMessage> exception) {
			this.exception = exception;
			return this.self();
		}

		public B trace(String trace) {
			this.trace = trace;
			return this.self();
		}

		public B exception(Throwable e) {
			this.exception = SimpleErrorUtil.convertExceptionMessage(e);
			this.trace = SimpleErrorUtil.convertTrace(e);
			return this.self();
		}
	}

}
