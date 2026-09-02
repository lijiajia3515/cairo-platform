package io.github.lijiajia3515.cairo.web.bind.annotation;

import java.lang.annotation.*;

/**
 * 带统一返回结果的响应信息
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessResultBody {
}
