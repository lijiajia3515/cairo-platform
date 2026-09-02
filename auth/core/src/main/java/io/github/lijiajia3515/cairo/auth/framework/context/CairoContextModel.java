package io.github.lijiajia3515.cairo.auth.framework.context;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CairoContextModel {
    /**
     * 请求头参数
     */
    private String headerName;
    /**
     * 路径参数
     */
    private String parameterName;
    /**
     * 默认值
     */
    private String defaultValue;
}
