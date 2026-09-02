# API 面收敛计划(P2 详细 / P3 待定)

> 状态:**仅计划,未实施**。基线与现状分析见 [api-surface.md](api-surface.md)(2026-08-30)。
> P0 安全补口已完成(commit c25cbef5);本文档覆盖其余两阶段。

## 背景数字

- 设计意图:`api/<面>/<族>/Xxx<面>ApiService`(薄壳:上下文提取 + 权限映射)
  → `modules/<族>/XxxCommonService`(共享业务逻辑)。
- 实际:**CommonService 合计 5,240 行,ApiService 合计 52,997 行**——共享核心只有
  适配层的 1/10;163 个 ApiService 中 **59 个完全不引用 CommonService**,
  直接持有 MongoTemplate/Repository 自带全套逻辑(如 `MenuSubappApiService`
  与 `MenuCommonService` 平行实现同一查询)。
- 后果:同一资源族在 4~8 个面各自演化,行为漂移无锚点;这是 166 controller /
  70,194 行 api 目录的结构性根源。

## P2:ApiService 下沉(按族分批,每批一分支)

### 分批顺序(风险从低到高)

| 批次 | 范围 | 涉及 ApiService | 特点 |
|---|---|---|---|
| 1 | biz_log 族 | cairo_web_manage×3、endpoint×2、account×1 | 纯查询无状态,写路径少 |
| 2 | file 族 | client×6、tenant_subapp×4、subapp×3 | MinIO 签名逻辑集中,公共下沉后直传链路统一 |
| 3 | dict 族 + 零散查询类(area/link/captcha/app_release) | client×2、tenant_subapp×1、open×5 | 字典读取各面应强一致 |
| 4 | authorization 族 | client×4、(含 *_authorization) | 涉及令牌授权语义,需先补集成测试 |
| 5 | wxmp 族 | client×3 | 外部微信生态,行为以稳定为先 |
| 6 | menu / permission 族 | client×1、subapp×2、tenant_subapp×2 | **与权限模型深耦合,最后动**;改前必须有行为快照测试 |
| — | 不下沉:verify_code/client(自助类) | 各面 ×1 | 逻辑本就一面一份,无共享价值 |

### 每批标准流程

1. `git checkout -b refactor/sink-<族>` 自 main;
2. 先补 CommonService 行为锚定测试(嵌入式 Mongo,参照
   `SubappCommonServiceEmbeddedMongoTest` 的 flapdoodle transitions 写法);
3. 逻辑下沉,ApiService 收缩为上下文提取 + 委托;**controller、URL、args、
   响应结构零改动**(对外契约不变);
4. `./gradlew test` 全绿 + 管理台 FE 冒烟(涉及页面);
5. FF 合并 main,删分支,更新本文档进度列。

### 验收标准

- 每族 CommonService 行数 ≥ 该族各面 ApiService 之和的 70%;
- 该族"不引用 CommonService 的 ApiService"清零(自助类除外);
- 全仓测试绿,api-surface.md 附录重生成无防护覆盖回退。

### 风险与对策

- **各面行为已漂移**(同名方法不同结果):下沉前以测试锚定"现状"而非"理想",
  差异行为在 CommonService 用参数显式建模,不做静默统一;
- Feign 客户端(auth/sdk)与 client 面契约耦合:批次 2/5 改动后同步跑 sdk 编译;
- 单批控制在 ≤8 个 ApiService,避免大爆炸式合并。

## P3:面处置(两个前置问题已于 2026-08-30 拍板)

| 项 | 裁决 | 动作 |
|---|---|---|
| account 面 | **有仓外存量调用** → 保留 | 不裁撤;后续如需可在 sdk 补 Feign 契约 |
| 镜像面(subapp↔tenant_subapp、endpoint↔tenant_endpoint) | **已交付企业版能力** → 不合并 | 维持镜像,靠 P2 共享 CommonService 消除逻辑重复 |
| weboffice 加固(可选,未裁决) | — | `VerifyWebOfficeSignInterceptor` 增加 Date 新鲜度校验(±5 分钟),消除重放窗口(审计结论见 api-surface.md §四) |

## 进度跟踪

| 批次 | 状态 |
|---|---|
| P0 安全补口 | ✅ c25cbef5 |
| P1 基线文档 + 审计 | ✅ 本批文档 |
| P2 批次 1~6 | ⬜ 未启动 |
| P3 | ✅ 已裁决:account 面与 tenant 镜像面均保留(见上表) |
