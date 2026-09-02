package io.github.lijiajia3515.cairo.web.error;

import java.util.Map;

import org.springframework.boot.actuate.autoconfigure.web.servlet.ManagementErrorEndpoint;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.error.ErrorAttributeOptions.Include;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;

//@Controller
public class CairoManagementErrorEndpoint extends ManagementErrorEndpoint {

	private final ErrorAttributes errorAttributes;

	private final ErrorProperties errorProperties;


	public CairoManagementErrorEndpoint(ErrorAttributes errorAttributes, ServerProperties serverProperties) {
		super(errorAttributes, serverProperties.getError());
		Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
		Assert.notNull(serverProperties.getError(), "ErrorProperties must not be null");
		this.errorAttributes = errorAttributes;
		this.errorProperties = serverProperties.getError();
	}

	@RequestMapping("${server.error.path:${error.path:/error}}")
	@ResponseBody
	public Map<String, Object> invoke(ServletWebRequest request) {
		return this.errorAttributes.getErrorAttributes(request, getErrorAttributeOptions(request));
	}

	private ErrorAttributeOptions getErrorAttributeOptions(ServletWebRequest request) {
		ErrorAttributeOptions options = ErrorAttributeOptions.defaults();
		if (this.errorProperties.isIncludeException()) {
			options = options.including(Include.EXCEPTION);
		}
		if (includeStackTrace(request)) {
			options = options.including(Include.STACK_TRACE);
		}
		if (includeMessage(request)) {
			options = options.including(Include.MESSAGE);
		}
		if (includeBindingErrors(request)) {
			options = options.including(Include.BINDING_ERRORS);
		}
		return options;
	}

	private boolean includeStackTrace(ServletWebRequest request) {
		switch (this.errorProperties.getIncludeStacktrace()) {
			case ALWAYS:
				return true;
			case ON_PARAM:
				return getBooleanParameter(request, "trace");
			default:
				return false;
		}
	}

	private boolean includeMessage(ServletWebRequest request) {
		switch (this.errorProperties.getIncludeMessage()) {
			case ALWAYS:
				return true;
			case ON_PARAM:
				return getBooleanParameter(request, "message");
			default:
				return false;
		}
	}

	private boolean includeBindingErrors(ServletWebRequest request) {
		switch (this.errorProperties.getIncludeBindingErrors()) {
			case ALWAYS:
				return true;
			case ON_PARAM:
				return getBooleanParameter(request, "errors");
			default:
				return false;
		}
	}

	protected boolean getBooleanParameter(ServletWebRequest request, String parameterName) {
		String parameter = request.getParameter(parameterName);
		if (parameter == null) {
			return false;
		}
		return !"false".equalsIgnoreCase(parameter);
	}

}
