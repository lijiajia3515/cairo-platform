// 菜单/权限点种子注入：走 manage API（服务端计算左右值），失败可清库重跑
// 线格式为 camelCase（任务 #33 翻转后的契约）；sort 由服务端自动分配，载荷不传
const BASE = "http://127.0.0.1:10010";
const AUTH_CTX = "cairo/web/manage/v1";
const fs = require("fs");
const path = require("path");

async function login() {
  const body = new URLSearchParams({
    client_id: "cairo_web_v1", client_secret: "cairo_web_v1",
    grant_type: "app_user:password", username: "admin", password: "123456"
  });
  const r = await fetch(`${BASE}/open_api/oauth2/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body
  });
  const j = await r.json();
  if (j.code !== "Success") throw new Error("登录失败: " + j.message);
  return j.data.access_token;
}

async function main() {
  const token = await login();
  console.log("登录 OK");
  const post = async (path, data) => {
    const r = await fetch(BASE + path, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `subapp_user ${AUTH_CTX}/${token}`,
        "Accept-Language": "zh-CN,zh;q=0.9",
        "app-id": "cairo", "endpoint-id": "web", "subapp-id": "manage", "subapp-version": "v1"
      },
      body: JSON.stringify(data)
    });
    const j = await r.json();
    if (j.code !== "Success") throw new Error(`${path} -> ${j.code}: ${j.message} | body=${JSON.stringify(data)}`);
    return j.data;
  };

  // 数据源：db/data/menu.json + permission.json（camelCase 基线）
  const DATA = path.join(__dirname, "db", "data");
  const menuRows = JSON.parse(fs.readFileSync(path.join(DATA, "menu.json"), "utf8"));
  const permRows = JSON.parse(fs.readFileSync(path.join(DATA, "permission.json"), "utf8"));
  const byId = {};
  menuRows.forEach(m => byId[m.menuId] = { menuName: m.menuName, component: m.component, icon: m.icon, hiddenMenu: m.hiddenMenu, menus: [], perms: [] });
  permRows.forEach(p => { if (byId[p.menuId]) byId[p.menuId].perms.push(p); });
  const roots = [];
  menuRows.forEach(m => {
    const node = byId[m.menuId];
    node.permissions = node.perms.map(p => ({ permissionId: p.permissionId, permissionName: p.permissionName, icon: p.icon || "", authorities: p.authorities || [], defaultPermission: p.defaultPermission === true, hiddenPermission: p.hiddenPermission === true }));
    if (m.parentId === "0" || m.parentId === "-1") roots.push(node);
    else if (byId[m.parentId]) byId[m.parentId].menus.push(node);
  });
  let menuCount = 0, permCount = 0;
  const idMap = {}; // 种子 menuId -> 新 menuId

  const createPerm = async (menuId, p) => {
    await post("/cairo_web_manage_api/permission/create_permission", {
      menuId,
      permissionId: p.permissionId,
      permissionName: p.permissionName,
      icon: p.icon || "",
      authorities: p.authorities || [],
      type: null,
      defaultPermission: p.defaultPermission === true,
      hiddenPermission: p.hiddenPermission === true
    });
    permCount++;
  };

  const createMenu = async (node, parentId) => {
    await post("/cairo_web_manage_api/menu/create_menu", {
      parentId: parentId || "0",
      menuName: node.menuName,
      component: node.component || "",
      icon: node.icon || "",
      hiddenMenu: node.hiddenMenu === true
    });
    // create_menu 不回传 Id：查列表按 父Id+名称 解析
    const list = await post("/cairo_web_manage_api/menu/get_menu_list", {});
    const hit = list.filter(m => m.parentId === (parentId || "0") && m.menuName === node.menuName && !Object.values(idMap).includes(m.menuId));
    if (hit.length === 0) throw new Error("创建后未找到菜单: " + node.menuName);
    const newId = hit[hit.length - 1].menuId;
    idMap[node.menuId] = newId;
    menuCount++;
    for (const p of node.permissions || []) await createPerm(newId, p);
    for (const child of node.menus || []) await createMenu(child, newId);
  };

  for (const root of roots) await createMenu(root, "0");

  // 校验
  const menus = await post("/cairo_web_manage_api/menu/get_menu_list", {});
  const perms = await post("/cairo_web_manage_api/permission/get_permission_list", {});
  console.log(`[DONE] 菜单 ${menuCount} 个（服务端 ${Array.isArray(menus) ? menus.length : "?"}）、权限点 ${permCount} 个（服务端 ${Array.isArray(perms) ? perms.length : "?"}）`);
}

main().catch(e => { console.error("[FAIL]", e.message); process.exit(1); });
