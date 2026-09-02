package io.github.lijiajia3515.cairo.build

/**
 * 模块发布名推导：artifactId / archivesName 统一由模块路径生成并补 cairo- 前缀
 *   :framework:core    -> cairo-framework-core
 *   :auth:domain:core  -> cairo-auth-domain-core
 *   :starter:web       -> cairo-starter-web
 */
final class CairoArtifacts {

    static String artifactId(String projectPath) {
        def name = projectPath.substring(1).replace(':', '-')
        name.startsWith('cairo') ? name : "cairo-$name"
    }

    private CairoArtifacts() {
    }
}
