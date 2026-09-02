package io.github.lijiajia3515.cairo.core.result;

import io.github.lijiajia3515.cairo.core.business.Business;

/**
 * REST API 结果实现类
 */
public class BusinessResult<T> implements Result {
	/**
	 * 业务状态码
	 */
	private String code;

	/**
	 * 描述
	 */
	private String message;

	/**
	 * 结果
	 */
	private T data;

	/**
	 * 保留默认构造函数
	 */
	public BusinessResult() {

	}

	public BusinessResult(String code, String message) {
		this.code = code;
		this.message = message;
	}


	public BusinessResult(String code, String message, T data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}

	public BusinessResult(Business business) {
		this.code = business.code();
		this.message = business.message();
	}

	public BusinessResult(Business business, T data) {
		this.code = business.code();
		this.message = business.message();
		this.data = data;
	}

	protected BusinessResult(BusinessResult.BusinessResultBuilder<T, ?, ?> b) {
		this.code = b.code;
		this.message = b.message;
		this.data = b.data;
	}

	public String code() {
		return this.code;
	}

	public BusinessResult<T> code(String code) {
		this.code = code;
		return this;
	}

	public String message() {
		return this.message;
	}

	public BusinessResult<T> message(String message) {
		this.message = message;
		return this;
	}

	public Business business() {
		return new Business() {

			@Override
			public String code() {
				return code;
			}

			@Override
			public String message() {
				return message;
			}
		};
	}

	public BusinessResult<T> business(Business business) {
		this.code = business.code();
		this.message = business.message();
		return this;
	}

	public T data() {
		return this.data;
	}


	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public BusinessResult<T> data(T data) {
		this.data = data;
		return this;
	}

	public String toString() {
		String var10000 = this.getCode();
		return "BusinessResult(code=" + var10000 + ", message=" + this.getMessage() + ", data=" + this.getData() + ")";
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T> BusinessResult.BusinessResultBuilder<T, ?, ?> builder() {
		return new BusinessResult.BusinessResultBuilderImpl();
	}

	private static final class BusinessResultBuilderImpl<T> extends BusinessResult.BusinessResultBuilder<T, BusinessResult<T>, BusinessResult.BusinessResultBuilderImpl<T>> {
		private BusinessResultBuilderImpl() {
		}

		protected BusinessResult.BusinessResultBuilderImpl<T> self() {
			return this;
		}

		@SuppressWarnings({"rawtypes", "unchecked"})
		public BusinessResult<T> build() {
			return new BusinessResult(this);
		}
	}

	public abstract static class BusinessResultBuilder<T, C extends BusinessResult<T>, B extends BusinessResult.BusinessResultBuilder<T, C, B>> {
		private String code;
		private String message;
		private T data;

		public BusinessResultBuilder() {
		}

		protected B $fillValuesFrom(C instance) {
			$fillValuesFromInstanceIntoBuilder(instance, this);
			return this.self();
		}

		private static <T> void $fillValuesFromInstanceIntoBuilder(BusinessResult<T> instance, BusinessResult.BusinessResultBuilder<T, ?, ?> b) {
			b.code(instance.code);
			b.message(instance.message);
			b.data(instance.data);
		}

		protected abstract B self();

		public abstract C build();

		public B code(String code) {
			this.code = code;
			return this.self();
		}

		public B message(String message) {
			this.message = message;
			return this.self();
		}

		public B business(Business business) {
			this.code = business.code();
			this.message = business.message();
			return this.self();
		}

		public B data(T data) {
			this.data = data;
			return this.self();
		}

		public String toString() {
			return "BusinessResult.BusinessResultBuilder(code=" + this.code + ", message=" + this.message + ", data=" + this.data + ")";
		}
	}

}
