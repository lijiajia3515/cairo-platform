package io.github.lijiajia3515.cairo.gateway.framework.error;

import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayDefaultError;
import io.github.lijiajia3515.cairo.gateway.framework.domain.GatewayErrorBusinessResult;
import org.springframework.boot.autoconfigure.template.TemplateAvailabilityProviders;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.HtmlUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.github.lijiajia3515.cairo.gateway.framework.error.CairoErrorAttributes.BUSINESS;

public class CairoErrorWebExceptionHandler extends DefaultErrorWebExceptionHandler {

	private static final MediaType TEXT_HTML_UTF8 = new MediaType("text", "html", StandardCharsets.UTF_8);

	private static final Map<HttpStatus.Series, String> SERIES_VIEWS;
	private final ApplicationContext applicationContext;

	private final WebProperties.Resources resources;
	private final TemplateAvailabilityProviders templateAvailabilityProviders;

	static {
		Map<HttpStatus.Series, String> views = new EnumMap<>(HttpStatus.Series.class);
		views.put(HttpStatus.Series.CLIENT_ERROR, "4xx");
		views.put(HttpStatus.Series.SERVER_ERROR, "5xx");
		SERIES_VIEWS = Collections.unmodifiableMap(views);
	}

	private final ErrorProperties errorProperties;

	public CairoErrorWebExceptionHandler(CairoErrorAttributes errorAttributes, WebProperties.Resources resources, ErrorProperties errorProperties, ApplicationContext applicationContext) {
		super(errorAttributes, resources, errorProperties, applicationContext);
		this.errorProperties = errorProperties;
		this.applicationContext = applicationContext;
		this.resources = resources;
		this.templateAvailabilityProviders = new TemplateAvailabilityProviders(applicationContext);
	}

	protected Mono<ServerResponse> renderErrorView(ServerRequest request) {
		// todo
		Map<String, Object> map = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.TEXT_HTML));
		GatewayErrorBusinessResult<GatewayDefaultError<?>> result = (GatewayErrorBusinessResult<GatewayDefaultError<?>>) map.get(BUSINESS);
		int errorStatus = result.getError().getStatus();
		ServerResponse.BodyBuilder responseBody = ServerResponse.status(errorStatus).contentType(TEXT_HTML_UTF8);
		return Flux.just(getData(errorStatus).toArray(new String[]{}))
			.flatMap((viewName) -> renderErrorView(viewName, responseBody, result))
			.switchIfEmpty(errorProperties.getWhitelabel().isEnabled()
				? renderDefaultErrorView(responseBody, result) : Mono.error(getError(request)))
			.next();
	}

	protected Mono<ServerResponse> renderErrorView(String viewName, ServerResponse.BodyBuilder responseBody,
												   GatewayErrorBusinessResult<GatewayDefaultError<?>> result) {
		if (isTemplateAvailable(viewName)) {
			return responseBody.render(viewName, result);
		}
		Resource resource = resolveResource(viewName);
		if (resource != null) {
			return responseBody.body(BodyInserters.fromResource(resource));
		}
		return Mono.empty();
	}

	private boolean isTemplateAvailable(String viewName) {
		return this.templateAvailabilityProviders.getProvider(viewName, applicationContext) != null;
	}

	private Resource resolveResource(String viewName) {
		for (String location : this.resources.getStaticLocations()) {
			try {
				Resource resource = this.applicationContext.getResource(location);
				resource = resource.createRelative(viewName + ".html");
				if (resource.exists()) {
					return resource;
				}
			} catch (Exception ex) {
				// Ignore
			}
		}
		return null;
	}

	protected Mono<ServerResponse> renderDefaultErrorView(ServerResponse.BodyBuilder responseBody,
														  GatewayErrorBusinessResult<GatewayDefaultError<?>> result) {
		StringBuilder builder = new StringBuilder();

		builder.append("<html><body><h1>Oops！</h1>")
			.append("<div id='created'>Time: [").append(result.getError().getTime()).append("]</div>")
			.append("<div id='requestId'> RequestId: [").append(result.getError().getRequestId()).append("]</div>")
			.append("<div> Business: [code=").append(htmlEscape(result.getCode())).append(", message=").append(htmlEscape(result.getMessage())).append("]</div>")
			.append("<div> HttpStatus: [type=").append(htmlEscape(result.getError().getError())).append(", status=").append(htmlEscape(result.getError().getStatus())).append("]</div>");

		if (result.getError().getMessage() != null) {
			builder.append("<div> Message: [").append(htmlEscape(result.getError().getMessage())).append("]</div>");
		}
		if (result.getError().getTrace() != null) {
			builder.append("<div style='white-space:pre-wrap;'> Trace: --->").append(htmlEscape(result.getError().getTrace())).append("</div>");
		}

		builder.append("</body></html>");
		return responseBody.bodyValue(builder.toString());
	}

	private String htmlEscape(Object input) {
		return (input != null) ? HtmlUtils.htmlEscape(input.toString()) : null;
	}

	private List<String> getData(int errorStatus) {
		List<String> data = new ArrayList<>();
		data.add("error/" + errorStatus);
		HttpStatus.Series series = HttpStatus.Series.resolve(errorStatus);
		if (series != null) {
			data.add("error/" + SERIES_VIEWS.get(series));
		}
		data.add("error/error");
		return data;
	}

	protected Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
		Map<String, Object> map = getErrorAttributes(request, getErrorAttributeOptions(request, MediaType.ALL));
		GatewayErrorBusinessResult<GatewayDefaultError<?>> result = (GatewayErrorBusinessResult<GatewayDefaultError<?>>) map.get(BUSINESS);

		return ServerResponse
			.status(result.getError().getStatus())
			.contentType(MediaType.APPLICATION_JSON)
			.body(BodyInserters.fromValue(result));
	}


}
