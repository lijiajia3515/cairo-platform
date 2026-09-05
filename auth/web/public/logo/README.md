# CAIRO Logo 资产（唯一权威源）

## 结构

```
public/logo/
├── logo.svg          ← 权威源（唯一手改处）：四级层叠（租户/应用/终端/子应用），品牌蓝 #0052D9
├── logo-{16,32,48,64,128,180,192,512}.png   ← 脚本导出，禁止手改
├── legacy/           ← 历史 logo 归档（nask-icon、旧 40px 三件），仅存档不引用
└── README.md
```

顶层兼容分发（脚本生成，历史引用路径零改动）：`public/logo.svg`、`favicon.png`(32)、
`apple-touch-icon.png`(180)、`appicon-apple.png`(180)。

## 再设计流程

1. 只改 `logo.svg`（约束：只写 viewBox 不写 width/height，浏览器才能按任意窗口尺寸精确出图）
2. 跑 `auth/web/scripts/export-logo.sh`（Chrome headless 渲染，透明底）
3. 提交本目录全部产物

## 引用位（全站统一指向本源）

- 前端 favicon：`index.html` → `/logo.svg`（SVG 优先，PNG 兜底）
- PWA manifest：`vite.config.js` icons（192/512 + maskable 180）
- 壳 header / 登录页：`<img src="/logo.svg">`
- 后端 Thymeleaf 三页：`/static/logo.svg`（fragments.html 声明，favicon.ico 兜底保留）
