package io.github.lijiajia3515.cairo.web.error;

import io.github.lijiajia3515.cairo.core.result.BusinessResult;
import io.github.lijiajia3515.cairo.web.DefaultWebError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.View;
import org.springframework.web.util.HtmlUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@SuppressWarnings(" unchecked ")
public class CairoErrorView implements View {

	private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);


	@Override
	@SuppressWarnings(" unchecked ")
	public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
		if (response.isCommitted()) {
			String message = getMessage(model);
			log.error(message);
			return;
		}
		response.setContentType(TEXT_HTML_UTF8.toString());

		Object business = model.getOrDefault(CairoErrorAttributes.BUSINESS, null);
		StringBuilder builder = null;
		if (business != null && business instanceof BusinessResult) {
			builder = businessError((BusinessResult<DefaultWebError>) business);
		} else {
			builder = new StringBuilder().append("<html><body><h1>Oops！</h1></body></html>");
		}

		if (response.getContentType() == null) {
			response.setContentType(getContentType());
		}


		response.getWriter().append(builder.toString());
	}


	StringBuilder businessError(BusinessResult<DefaultWebError> result) {
		StringBuilder builder = new StringBuilder();
		builder.append("<html><body><h1>Oops！</h1>")
			.append("<div id='created'>Time: [").append(result.getData().getTime()).append("]</div>")
			.append("<div id='requestId'> RequestId: [").append(result.getData().getRequestId()).append("]</div>")
			.append("<div> Business: [code=").append(htmlEscape(result.getCode())).append(", message=").append(htmlEscape(result.getMessage())).append("]</div>")
			.append("<div> HttpStatus: [type=").append(htmlEscape(result.getData().getError())).append(", status=").append(htmlEscape(result.getData().getStatus())).append("]</div>");

		if (result.getData().getMessage() != null) {
			builder.append("<div> Message: [").append(htmlEscape(result.getData().getMessage())).append("]</div>");
		}
		if (result.getData().getTrace() != null) {
			builder.append("<div style='white-space:pre-wrap;'> Trace: --->").append(htmlEscape(result.getData().getTrace())).append("</div>");
		}

		builder.append("</body></html>");
		return builder;
	}

	private String htmlEscape(Object input) {
		return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
	}

	private String getMessage(Map<String, ?> model) {
		Object path = model.get("path");
		String message = "Cannot render error page for request [" + path + "]";
		if (model.get("message") != null) {
			message += " and exception [" + model.get("message") + "]";
		}
		message += " as the response has already been committed.";
		message += " As a result, the response may have the wrong status code.";
		return message;
	}

	@Override
	public String getContentType() {
		return "text/html";
	}
}
