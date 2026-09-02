#!/usr/bin/env node
// 前端 API 调用 vs 后端 Controller 路由 对齐检查
// 用法：node scripts/check-api-align.cjs
const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');
const root = path.resolve(__dirname, '../../..');

// ---- 后端：类级 @RequestMapping 前缀 + 方法级映射 ----
// 注意：类级前缀只能取 class 声明之前的注解，方法级数组形式
// @RequestMapping({"/access_file_url"}) 会被误当成类前缀。
const files = execSync('find auth/service/src/main/java -name "*Controller.java"', {
  cwd: root,
  encoding: 'utf8',
}).trim().split('\n');
const routes = new Set();
for (const f of files) {
  const src = fs.readFileSync(path.join(root, f), 'utf8');
  const classIdx = src.search(/\b(?:public\s+)?class\s+\w+/);
  if (classIdx < 0) continue;
  const head = src.slice(0, classIdx);
  const body = src.slice(classIdx);

  const prefixes = [];
  for (const m of head.matchAll(/@RequestMapping\(([^)]*)\)/g)) {
    for (const p of [...m[1].matchAll(/["'](\/[^"']*)["']/g)]) prefixes.push(p[1]);
  }
  if (prefixes.length === 0) continue;

  const methodPaths = [];
  for (const m of body.matchAll(/@(?:Post|Get|Put|Delete|Request)Mapping\(([^)]*)\)/g)) {
    for (const p of [...m[1].matchAll(/["'](\/[^"']*)["']/g)]) methodPaths.push(p[1]);
  }
  for (const p of prefixes) {
    for (const m of methodPaths) routes.add(p.replace(/\/$/, '') + m);
  }
}

// ---- 前端：解析 api 层全部调用 ----
// URL 前缀常量从 src/api/urls.js 解析(openApi/appUserApi/subappUserApi/manageApi)
const urlConsts = {};
const urlsSrc = fs.readFileSync(path.join(root, 'auth/web/src/api/urls.js'), 'utf8');
for (const m of urlsSrc.matchAll(/const (\w+) = _this\.api\.\w+ \+ ["']([^"']+)["']/g)) urlConsts[m[1]] = m[2];

// 领域文件(open/personal/.../area),排除桶入口与传输层
const apiDir = path.join(root, 'auth/web/src/api');
const skip = new Set(['index.js', 'urls.js', 'fetch.js', 'axios.js', 'status.js']);
const webApi = {};
for (const name of fs.readdirSync(apiDir)) {
  if (!name.endsWith('.js') || skip.has(name)) continue;
  const src = fs.readFileSync(path.join(apiDir, name), 'utf8');
  const label = name.replace('.js', '');
  for (const m of src.matchAll(/(\w+) \+ ["'](\/[^"']+)["']/g)) {
    if (urlConsts[m[1]]) {
      const url = urlConsts[m[1]] + m[2];
      (webApi[url] = webApi[url] || []).push(label);
    }
  }
}

// ---- 对比 ----
let ok = 0;
const missing = [];
for (const [url, who] of Object.entries(webApi)) {
  if (routes.has(url)) ok++;
  else missing.push([url, who.join('+')]);
}
console.log('后端路由总数:', routes.size);
console.log('前端调用总数:', Object.keys(webApi).length);
console.log('匹配:', ok);
console.log('');
console.log('=== 前端调用在后端不存在的路由 ===');
for (const [url, who] of missing) console.log('[' + who + ']', url);
