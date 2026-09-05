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
	 * 链路追踪号（对应 X-Trace-Id / 日志 traceId），成功与失败均携带
	 */
	private String requestId;

	/**
	 * 是否可重试（机器判断：408 / 429 / 5xx 为 true），失败响应携带
	 */
	private Boolean retryable;

	/**
	 * 建议重试等待秒数（仅限流 / 服务端瞬时），未提供时为 null
	 */
	private Long retryAfter;

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
		this.requestId = b.requestId;
		this.retryable = b.retryable;
		this.retryAfter = b.retryAfter;
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

	public String requestId() {
		return this.requestId;
	}

	public BusinessResult<T> requestId(String requestId) {
		this.requestId = requestId;
		return this;
	}

	public Boolean retryable() {
		return this.retryable;
	}

	public BusinessResult<T> retryable(Boolean retryable) {
		this.retryable = retryable;
		return this;
	}

	public Long retryAfter() {
		return this.retryAfter;
	}

	public BusinessResult<T> retryAfter(Long retryAfter) {
		this.retryAfter = retryAfter;
		return this;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Boolean getRetryable() {
		return retryable;
	}

	public void setRetryable(Boolean retryable) {
		this.retryable = retryable;
	}

	public Long getRetryAfter() {
		return retryAfter;
	}

	public void setRetryAfter(Long retryAfter) {
		this.retryAfter = retryAfter;
	}

	public String toString() {
		return "BusinessResult(code=" + this.getCode() + ", message=" + this.getMessage() + ", data=" + this.getData()
			+ ", requestId=" + this.getRequestId() + ", retryable=" + this.getRetryable() + ", retryAfter=" + this.getRetryAfter() + ")";
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
		private String requestId;
		private Boolean retryable;
		private Long retryAfter;

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
			b.requestId(instance.requestId);
			b.retryable(instance.retryable);
			b.retryAfter(instance.retryAfter);
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

		public B requestId(String requestId) {
			this.requestId = requestId;
			return this.self();
		}

		public B retryable(Boolean retryable) {
			this.retryable = retryable;
			return this.self();
		}

		public B retryAfter(Long retryAfter) {
			this.retryAfter = retryAfter;
			return this.self();
		}

		public String toString() {
			return "BusinessResult.BusinessResultBuilder(code=" + this.code + ", message=" + this.message + ", data=" + this.data
				+ ", requestId=" + this.requestId + ", retryable=" + this.retryable + ", retryAfter=" + this.retryAfter + ")";
		}
	}

}
