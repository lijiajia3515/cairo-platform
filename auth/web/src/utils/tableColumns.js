// 表格列工厂——列表页列定义的统一规范
// 时间列格式化、长字段省略+悬停复制、操作列 ≤3 平铺+更多下拉,收编此前逐页复制的 JSX 单元格
import { h } from 'vue';
import { get } from 'lodash';
import { Button as TButton, Dropdown as TDropdown, Avatar as TAvatar, Tag as TTag, Switch as TSwitch, Tooltip as TTooltip, Icon as TIcon } from 'tdesign-vue-next';
import { MessagePlugin, LoadingPlugin, DialogPlugin } from 'tdesign-vue-next';

import useCopy from '@/hooks/useCopy';
import { hasPermission } from '@/plugins/permission';

// 日期时间统一展示格式:数据源为 ISO/UTC,转本地时区。
// 表格内展示用短格式(当年 MM-DD HH:mm,跨年 YYYY-MM-DD),悬停 title 给全量秒级——
// 阿里云/腾讯云式密度:时间列窄,不横向撑表格
export const formatDateTime = (value) => {
  if (!value) return '';
  const date = new Date(typeof value === 'string' ? value.replace('Z', '+00:00') : value);
  if (Number.isNaN(date.getTime())) return String(value);
  const p = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${p(date.getMonth() + 1)}-${p(date.getDate())} ${p(date.getHours())}:${p(date.getMinutes())}:${p(date.getSeconds())}`;
};

export const formatShortTime = (value) => {
  const full = formatDateTime(value);
  if (!full) return '';
  const [, md, hm] = full.match(/^\d{4}-(\d{2}-\d{2}) (\d{2}:\d{2})/) || [];
  if (!md) return full;
  const now = new Date();
  const sameYear = full.slice(0, 4) === String(now.getFullYear());
  return sameYear ? `${md} ${hm}` : full.slice(0, 10);
};

const readRow = (row, colKey) => get(row, colKey);

// 表头单行所需最小宽度:medium 字号 14px/字 + 单元格左右 padding——低于此值表头逐字竖排换行
const titleMinWidth = (title) => String(title).length * 14 + 34;

// 列宽兜底:布局为原生 auto(见 design.scss),width 只是建议值、可被压缩,只有 minWidth 是硬下限;
// 小屏时列压到下限后整表出横向滚动,而非把表头/内容压成竖排
const floorWidth = ({ width, minWidth, title }) => Math.max(width || 0, minWidth || 0, titleMinWidth(title));

// 时间列:短格式(当年 MM-DD HH:mm)+ 悬停气泡展示完整时间(阿里云式,含年与秒)
// + 悬停浮现复制图标,一点即复制完整时间(细粒度需求不换行不撑列宽)
// opts.or 备选键:主键为空时取备选值(如注销列——注销中取申请时间,成功取完成时间,两列并一列)
export const timeColumn = (colKey, title, { width = 96, or } = {}) => ({
  colKey,
  title,
  width,
  minWidth: floorWidth({ width, title }),
  cell: (h2, { row }) => {
    const raw = readRow(row, colKey) || (or ? readRow(row, or) : null);
    if (!raw) return '';
    const full = formatDateTime(raw);
    return h('span', { class: 'copy-cell' }, [
      h(TTooltip, { content: full }, () => h('span', { class: 'sl1' }, formatShortTime(raw))),
      h(TIcon, {
        name: 'copy', size: '12px', class: 'copyIcon pick',
        title: '复制完整时间',
        onClick: () => useCopy(full),
      }),
    ]);
  },
});

// 长文本列:单行省略 + 原生 tooltip。
// 封顶必设:布局为原生 auto,nowrap 文本的列宽由内容撑开(长 UA/UUID 会顶到数百像素),
// 单元格封顶列宽后超长文本省略号截断,悬停 title 看全量
export const ellipsisColumn = (colKey, title, { width = 160 } = {}) => ({
  colKey,
  title,
  width,
  minWidth: floorWidth({ width, title }),
  cell: (h2, { row }) => {
    const value = readRow(row, colKey);
    return value == null || value === '' ? null : cappedText(String(value), width);
  },
});

// 省略号截断的封顶文本(inline-block 才能让 overflow/ellipsis 在表格单元格里生效)
export const cappedText = (text, cap) => h('span', {
  class: 'sl1',
  title: text,
  style: { display: 'inline-block', maxWidth: `${cap}px`, verticalAlign: 'bottom' },
}, text);

// 悬停复制单元格:文本省略 + hover 显示复制图标(复制 copyKey ?? 本列值);cap 封顶省略,防长值撑爆列
const copyCell = (row, textKey, copyKey, cap) => {
  const text = readRow(row, textKey);
  const copyValue = copyKey ? readRow(row, copyKey) : text;
  return copyValueCell(text, copyValue, cap);
};

// 值版悬停复制单元格(实体引用单元格的名称段)
const copyValueCell = (text, copyValue, cap) => {
  if (text == null || text === '') return null;
  return h('span', { class: 'copy-cell' }, [
    cap ? cappedText(String(text), cap) : h('span', { class: 'sl1', title: String(text) }, String(text)),
    copyValue == null || copyValue === '' ? null : h(TIcon, {
      name: 'copy', size: '12px', class: 'copyIcon pick',
      title: '复制',
      onClick: () => useCopy(String(copyValue)),
    }),
  ]);
};

// 实体引用单元格(企业/应用/终端/子应用/用户/账号通用):
// 16px 图标 + 12px 名称同行垂直居中——视觉等重(图标不再大于文字);
// 悬停复制实体 ID;onClick 整体可点(查看详情);无图标仅显名称
export const entityCell = ({ name, icon, id, onClick, cap }) => {
  if ((name == null || name === '') && (id == null || id === '')) return null;
  const label = (name == null || name === '') ? String(id) : String(name);
  const cell = h('span', { class: 'entity-cell' }, [
    icon ? h('img', { class: 'entity-icon', src: icon, loading: 'lazy', alt: '' }) : null,
    copyValueCell(label, id != null && id !== '' ? id : label, cap),
  ]);
  return onClick ? h('span', { class: 'entity-cell pick', onClick: () => onClick() }, cell.children) : cell;
};

// 复制列:展示值与复制值不同时传 copyKey(如展示终端名、复制终端ID)
export const copyColumn = (colKey, title, { width = 130, copyKey } = {}) => ({
  colKey,
  title,
  width,
  minWidth: floorWidth({ width, title }),
  cell: (h2, { row }) => copyCell(row, colKey, copyKey, Math.max(width - 24, 60)),
});

// 实体引用列(企业/应用/终端/子应用/客户端等):entityCell 规格——16px 图标+12px 名称,
// 悬停复制实体 ID(copyKey 省略时复制名称本身)。旧名 avatarCopyColumn 保留为别名
export const entityColumn = ({ colKey, title, iconKey, copyKey, onClick, minWidth = 110, cap = 140 }) => ({
  colKey,
  title,
  minWidth: floorWidth({ minWidth, title }),
  cell: (h2, { row }) => entityCell({
    name: readRow(row, colKey),
    icon: iconKey ? readRow(row, iconKey) : null,
    id: copyKey ? readRow(row, copyKey) : null,
    onClick: onClick ? () => onClick(row) : null,
    cap,
  }),
});
export const avatarCopyColumn = entityColumn;

// 用户/账号列:头像(无图回退昵称/ID 前几字)+名称,可点击查看详情;未绑定时显示 unbound 文案
// opts: { colKey='user', title='用户', recordKey, nameKey='nickname', avatarKey='accountAvatarUrl', idKey='userId', onClick, unbound, width=160 }
//   recordKey 行内嵌套对象键(如日志行的 user/account);不传则直接读行字段(扁平结构)
//   onClick (src, row) => void,src 为 recordKey 指向的对象(无 recordKey 时为行本身)
export const userColumn = ({ colKey = 'user', title = '用户', recordKey, nameKey = 'nickname', avatarKey = 'accountAvatarUrl', idKey = 'userId', onClick, unbound, width = 160 } = {}) => ({
  colKey,
  title,
  width,
  minWidth: floorWidth({ width, title }),
  cell: (h2, { row }) => {
    const src = recordKey ? (row[recordKey] || {}) : row;
    const name = get(src, nameKey);
    const id = get(src, idKey);
    if (unbound && !id && !name) return unbound;
    // entityCell 规格:16px 头像+12px 名称视觉等重,悬停复制用户 ID,可点查看详情
    return entityCell({
      name: name != null && name !== '' ? name : id,
      icon: get(src, avatarKey),
      id,
      onClick: onClick ? () => onClick(src, row) : null,
      cap: Math.max(width - 56, 60),
    });
  },
});

// 二值状态文案/颜色预设
const STATUS_PRESETS = {
  enable: { true: { label: '启用', theme: 'success' }, false: { label: '禁用', theme: 'danger' } },
  lock: { true: { label: '已锁定', theme: 'danger' }, false: { label: '未锁定', theme: 'success' } },
  yesno: { true: { label: '是', theme: 'success' }, false: { label: '否', theme: 'default' } },
};

// 只读二值列:彩色标签展示(是否隐藏/是否内部应用等不可行内直改的字段)
// pairs 传自定义映射,如 { true: { label: '内部', theme: 'primary' }, false: { label: '公开', theme: 'default' } }
export const statusTagColumn = (colKey, title, { type = 'enable', pairs, width = 90 } = {}) => ({
  colKey,
  title,
  width,
  minWidth: floorWidth({ width, title }),
  cell: (h2, { row }) => {
    const value = readRow(row, colKey);
    const preset = (pairs || STATUS_PRESETS[type])[value];
    return preset ? h(TTag, { theme: preset.theme, variant: 'light' }, () => preset.label) : null;
  },
});

// 可编辑二值列:行内开关直改(确认弹窗 → 调状态 API → 成功刷新;取消/失败不动受控值即回滚)
// opts: { colKey='enabled', title='状态', type='enable', pairs, confirmOf, api, idKeys=[], valueKey, label, perm, refresh, width }
//   api      状态修改接口 (params) => Promise
//   idKeys   从行数据取的标识字段(如 ['clientId'])
//   valueKey 提交的状态字段名,默认同 colKey(如 appRelease 的 latestVersion)
//   label    实体名,用于确认文案(如 '客户端' → 是否启用该客户端?)
//   perm     权限点,无权限时开关禁用
//   refresh  成功后的列表刷新回调
//   pairs    自定义文案/颜色映射,默认按 type 取预设(如热门:{ true:{label:'热门',theme:'warning'}, ... })
//   confirmOf 自定义确认动作文案 (value) => '设为热门' | '取消热门'
//   extra    提交时附加的静态参数(如 appRelease 的 { type: 'web' });可传 (row) => object 在确认时求值(如取当前组件状态)
export const switchColumn = ({ colKey = 'enabled', title = '状态', type = 'enable', pairs, confirmOf, api, idKeys = [], valueKey, label, perm, refresh, extra, width = 110 } = {}) => {
  const preset = pairs || STATUS_PRESETS[type];
  const confirmText = confirmOf || ((value) => ({
    enable: value ? '启用' : '禁用',
    lock: value ? '锁定' : '解锁',
    yesno: value ? '开启' : '关闭',
  }[type] || '修改'));
  const onChange = (value, row) => {
    const action = confirmText(value);
    const confirmDia = DialogPlugin({
      header: '提示',
      body: `是否${action}${label ? '该' + label : ''}?`,
      confirmBtn: '确定',
      cancelBtn: '取消',
      onConfirm: async () => {
        LoadingPlugin(true);
        try {
          const params = { ...(typeof extra === 'function' ? extra(row) : extra) };
          idKeys.forEach((k) => { params[k] = row[k]; });
          params[valueKey || colKey] = value;
          const res = await api(params);
          if (res.code === 'Success') {
            MessagePlugin.success(`${action}成功`);
            refresh && refresh();
          } else {
            MessagePlugin.error(res.message || `${action}失败`);
          }
        } finally {
          LoadingPlugin(false);
          confirmDia.destroy();
        }
      },
    });
  };
  return {
    colKey,
    title,
    width,
    minWidth: floorWidth({ width, title }),
    cell: (h2, { row }) => {
      const value = readRow(row, colKey);
      if (value !== true && value !== false) return null;
      return h(TSwitch, {
        value,
        size: 'small', // 密度:默认 large(44px)会把整行撑到 60+,small 保持行高紧凑
        label: [preset.true.label, preset.false.label],
        disabled: perm ? !hasPermission(perm) : false,
        onChange: (val) => onChange(val, row),
      });
    },
  };
};


// 操作列:visible 过滤后前 3 个平铺,其余收进「更多」下拉;fixed right
// buttons: [{ content, theme = 'primary', onClick(row, rowIndex), visible(row) }]
// 更多下拉项按平铺按钮的主题着色(danger→error 红等)——否则收进下拉后统一裸色,危险操作不可辨
const DROPDOWN_THEMES = { danger: 'error', warning: 'warning', success: 'success' };
export const opColumn = (buttons, { title = '操作', width = 160 } = {}) => ({
  colKey: 'operation',
  title,
  width,
  minWidth: floorWidth({ width, title }),
  fixed: 'right',
  cell: (h2, { row, rowIndex }) => {
    const visible = buttons.filter((b) => (b.visible ? b.visible(row) !== false : true));
    const flat = visible.slice(0, 3);
    const more = visible.slice(3);
    const children = flat.map((b) => h(TButton, {
      variant: 'text',
      theme: b.theme || 'primary',
      size: 'small',
      onClick: () => b.onClick(row, rowIndex),
    }, () => b.content));
    if (more.length) {
      children.push(h(TDropdown, {
        options: more.map((b, index) => ({ content: b.content, value: index, theme: DROPDOWN_THEMES[b.theme] || 'default' })),
        trigger: 'click',
        onClick: (value) => {
          const target = more[Number(value)];
          if (target) target.onClick(row, rowIndex);
        },
      }, () => h(TButton, { variant: 'text', theme: 'primary', size: 'small' }, () => '更多')));
    }
    return h('span', { class: 'op-cell' }, children);
  },
});
