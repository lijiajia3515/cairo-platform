package io.github.lijiajia3515.cairo.auth.framework.context;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.ENDPOINT_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.ENDPOINT_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.APP_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.APP_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CAIRO_TAG;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CAIRO_TAG_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CLIENT_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.CLIENT_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_ID_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_VERSION;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.SUBAPP_VERSION_CONTEXT;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.TENANT_ID;
import static io.github.lijiajia3515.cairo.auth.framework.context.CairoContextConstants.TENANT_ID_CONTEXT;

/**
 * cairo上下文拦截器
 */
@Slf4j
public class CairoContextInterceptor  implements HandlerInterceptor {
    private final Map<String, CairoContextModel> CONTEXTS = new ConcurrentHashMap<>() {{
        put(CAIRO_TAG, CAIRO_TAG_CONTEXT);
        put(CLIENT_ID, CLIENT_ID_CONTEXT);
        put(ENDPOINT_ID, ENDPOINT_ID_CONTEXT);
        put(APP_ID, APP_ID_CONTEXT);
        put(TENANT_ID, TENANT_ID_CONTEXT);
		put(SUBAPP_ID, SUBAPP_ID_CONTEXT);
		put(SUBAPP_VERSION, SUBAPP_VERSION_CONTEXT);
    }};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 1.获取目标类上的目标注解（可判断目标类是否存在该注解）
            CairoContext classToken = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), CairoContext.class);
            // 2.获取目标方法上的目标注解（可判断目标方法是否存在该注解）
            CairoContext methodToken = AnnotationUtils.findAnnotation(handlerMethod.getMethod(), CairoContext.class);
            CairoContext token = methodToken != null ? methodToken : classToken;
            if (token == null) {
                return true;
            }

            CONTEXTS.forEach((key, model) -> {
                String headerValue = request.getHeader(model.getHeaderName());
                String parameterValue = request.getParameter(model.getParameterName());
                if (StringUtils.hasText(headerValue)) {
                    CairoContextHolder.setValue(key, headerValue);
                } else if (StringUtils.hasText(parameterValue)) {
                    CairoContextHolder.setValue(key, parameterValue);
                } else {
                    CairoContextHolder.setValue(key, model.getDefaultValue());
                }
            });

        }
        return true;
    }
}
