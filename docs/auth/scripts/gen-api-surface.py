#!/usr/bin/env python3
# 从 auth/service 源码生成 API 面端点清单(api-surface.md 的附录部分)
# 用法:cd auth/service/src/main/java && python3 ../../../../../docs/auth/scripts/gen-api-surface.py /tmp/inventory.md
# 注:清单反映的是代码现状(含方法级防护注解),非设计意图
import os
import re
import sys

mapping_re = re.compile(r'@(Get|Post|Put|Delete|Patch)Mapping(?:\(([^)]*)\))?')
sig_re = re.compile(r'\b(public|protected|private)\s+[\w<>,\s\[\].?]+?\s+\w+\s*\(')
base_re = re.compile(r'@RequestMapping\(\s*"([^"]+)"')

FACE_META = {
    'open': ('open_api', '匿名(SecurityConfig 整体 ignore)'),
    'client': ('client_api', 'CLIENT 凭证(服务间)'),
    'cairo_web_manage': ('cairo_web_manage_api', 'CAIRO_WEB_MANAGE_USER'),
    'subapp': ('subapp_user_api', 'SUBAPP_USER'),
    'app_user': ('app_user_api', 'APP_USER'),
    'tenant_app_user': ('tenant_app_user_api', 'TENANT_APP_USER'),
    'tenant_subapp': ('tenant_subapp_user_api', 'TENANT_SUBAPP_USER'),
    'account': ('account_api', 'ACCOUNT'),
}

controllers = []
for root, dirs, files in os.walk('.'):
    for fn in files:
        if not fn.endswith('Controller.java'):
            continue
        p = os.path.join(root, fn)
        rel = p.replace('./', '')
        if '/api/' in rel:
            parts = rel.split('/api/')[1].split('/')
            face = parts[0]
            fam = '/'.join(parts[1:-1]) or '(根)'
        elif 'weboffice' in rel:
            face, fam = 'weboffice', 'weboffice'
        else:
            face, fam = 'misc', rel.split('/')[-2]
        txt = open(p, encoding='utf-8').read()
        lines = txt.splitlines()
        m = base_re.search(txt)
        base = m.group(1) if m else ''
        sec_m = re.search(r'CairoSecurityType\.(\w+)', txt)
        wps = '@VerifyWebOfficeSign' in txt
        eps = []
        for i, line in enumerate(lines):
            mm = mapping_re.search(line)
            if not mm:
                continue
            verb, arg = mm.group(1).upper(), (mm.group(2) or '')
            pm = re.search(r'"([^"]*)"', arg)
            path = pm.group(1) if pm else ''
            pre, capt = '', False
            # 注解可在映射行上方或下方:向上回溯连续注解块,再向下扫到方法签名
            lo = i
            while lo - 1 >= 0 and lines[lo - 1].strip().startswith('@'):
                lo -= 1
            for j in range(lo, min(i + 8, len(lines))):
                if j > i and sig_re.search(lines[j]):
                    break
                pq = re.search(r'@PreAuthorize\("(.*)"\)', lines[j])
                if pq:
                    pre = pq.group(1)
                if '@VerifyCaptchaToken' in lines[j]:
                    capt = True
            eps.append((verb, path, pre, capt))
        controllers.append(dict(face=face, fam=fam, name=fn[:-5], base=base,
                                sec=sec_m.group(1) if sec_m else None, wps=wps, eps=eps))

order = ['open', 'client', 'cairo_web_manage', 'subapp', 'app_user',
         'tenant_app_user', 'tenant_subapp', 'account', 'weboffice', 'misc']
out = []
for face in order:
    fcs = sorted([c for c in controllers if c['face'] == face], key=lambda c: (c['fam'], c['name']))
    if not fcs:
        continue
    url, typ = FACE_META.get(face, ('', ''))
    n_ep = sum(len(c['eps']) for c in fcs)
    hdr = f'### {face} 面' if face in FACE_META else f'### 特例:{face}'
    out.append(f'{hdr} — `{url}`' + (f' 主体类型 `{typ}`' if typ else (' WPS-2 签名' if face == 'weboffice' else '')))
    out.append('')
    out.append(f'Controller {len(fcs)} 个,端点 {n_ep} 个。')
    out.append('')
    out.append('| Controller | 端点 | 方法级防护 |')
    out.append('|---|---|---|')
    for c in fcs:
        fam = c['fam']
        for (verb, path, pre, capt) in c['eps']:
            guards = []
            if pre:
                guards.append('`' + pre + '`')
            if capt:
                guards.append('验证码')
            if not guards:
                guards = ['(类型闸/principal 限定)'] if c['sec'] else ['**无**']
            p = path if path else '(根)'
            out.append(f"| {fam}/{c['name']} | `{verb} {p}` | {'; '.join(guards)} |")
    out.append('')

dest = sys.argv[1] if len(sys.argv) > 1 else '/tmp/api-surface-inventory.md'
open(dest, 'w', encoding='utf-8').write('\n'.join(out) + '\n')
print(f'{len(controllers)} controllers, {sum(len(c["eps"]) for c in controllers)} endpoints -> {dest}')
