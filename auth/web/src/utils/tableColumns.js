// 表格列工厂——列表页列定义的统一规范
// 时间列格式化、长字段省略+悬停复制、操作列 ≤3 平铺+更多下拉,收编此前逐页复制的 JSX 单元格
import { h } from 'vue';
import { get } from 'lodash';
import { Button as TButton, Dropdown as TDropdown, Avatar as TAvatar, Tag as TTag, Switch as TSwitch } from 'tdesign-vue-next';
import { MessagePlugin, LoadingPlugin, DialogPlugin } from 'tdesign-vue-next';

import useCopy from '@/hooks/useCopy';
import { hasPermission } from '@/plugins/permission';

// 日期时间统一展示格式:YYYY-MM-DD HH:mm:ss(数据源为 ISO/UTC,转本地时区)
export const formatDateTime = (value) => {
  if (!value) return '';
  const date = new Date(typeof value === 'string' ? value.replace('Z', '+00:00') : value);
  if (Number.isNaN(date.getTime())) return String(value);
  const p = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${p(date.getMonth() + 1)}-${p(date.getDate())} ${p(date.getHours())}:${p(date.getMinutes())}:${p(date.getSeconds())}`;
};

const readRow = (row, colKey) => get(row, colKey);

// 时间列:格式化 + 定宽
export const timeColumn = (colKey, title, { width = 170 } = {}) => ({
  colKey,
  title,
  width,
  cell: (h2, { row }) => formatDateTime(readRow(row, colKey)),
});

// 长文本列:单行省略 + 原生 tooltip
export const ellipsisColumn = (colKey, title, { width = 160 } = {}) => ({
  colKey,
  title,
  width,
  cell: (h2, { row }) => {
    const value = readRow(row, colKey);
    return value == null || value === '' ? null : h('span', { class: 'sl1', title: String(value) }, String(value));
  },
});

// 悬停复制单元格:文本省略 + hover 显示复制图标(复制 copyKey ?? 本列值)
const copyCell = (row, textKey, copyKey) => {
  const text = readRow(row, textKey);
  const copyValue = copyKey ? readRow(row, copyKey) : text;
  if (text == null || text === '') return null;
  return h('span', { class: 'copy-cell' }, [
    h('span', { class: 'sl1', title: String(text) }, String(text)),
    copyValue == null || copyValue === '' ? null : h('i', {
      class: 'iconfont icon-fuzhi pick copyIcon',
      title: '复制',
      onClick: () => useCopy(String(copyValue)),
    }),
  ]);
};

// 复制列:展示值与复制值不同时传 copyKey(如展示终端名、复制终端ID)
export const copyColumn = (colKey, title, { width = 200, copyKey } = {}) => ({
  colKey,
  title,
  width,
  cell: (h2, { row }) => copyCell(row, colKey, copyKey),
});

// 头像+名称+悬停复制列(应用/终端/客户端等图标实体的通用列)
export const avatarCopyColumn = ({ colKey, title, iconKey, copyKey, width = 220 }) => ({
  colKey,
  title,
  width,
  cell: (h2, { row }) => {
    const icon = iconKey ? readRow(row, iconKey) : null;
    const name = readRow(row, colKey);
    return h('span', { class: 'avatar-copy-cell' }, [
      icon ? h(TAvatar, { imageProps: { lazy: true }, shape: 'round', hideOnLoadFailed: true, alt: String(name || '').slice(0, 2), size: '24px', image: icon }) : null,
      copyCell(row, colKey, copyKey),
    ]);
  },
});

// 用户/账号列:头像(无图回退昵称/ID 前几字)+名称,可点击查看详情;未绑定时显示 unbound 文案
// opts: { colKey='user', title='用户', recordKey, nameKey='nickname', avatarKey='accountAvatarUrl', idKey='userId', onClick, unbound, width=160 }
//   recordKey 行内嵌套对象键(如日志行的 user/account);不传则直接读行字段(扁平结构)
//   onClick (src, row) => void,src 为 recordKey 指向的对象(无 recordKey 时为行本身)
export const userColumn = ({ colKey = 'user', title = '用户', recordKey, nameKey = 'nickname', avatarKey = 'accountAvatarUrl', idKey = 'userId', onClick, unbound, width = 160 } = {}) => ({
  colKey,
  title,
  width,
  cell: (h2, { row }) => {
    const src = recordKey ? (row[recordKey] || {}) : row;
    const name = get(src, nameKey);
    const id = get(src, idKey);
    if (unbound && !id && !name) return unbound;
    if (!name && !id) return null;
    const label = String(name || id);
    const avatar = get(src, avatarKey);
    return h('span', {
      class: onClick ? 'user-cell pick' : 'user-cell',
      ...(onClick ? { onClick: () => onClick(src, row) } : {}),
    }, [
      h(TAvatar, {
        size: 'medium',
        hideOnLoadFailed: true,
        ...(avatar ? { image: avatar, alt: label.slice(0, 2) } : {}),
      }, () => label.slice(0, 3)),
      h('span', { class: 'sl1', title: label }, label),
    ]);
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
    cell: (h2, { row }) => {
      const value = readRow(row, colKey);
      if (value !== true && value !== false) return null;
      return h(TSwitch, {
        value,
        label: [preset.true.label, preset.false.label],
        disabled: perm ? !hasPermission(perm) : false,
        onChange: (val) => onChange(val, row),
      });
    },
  };
};


// 操作列:visible 过滤后前 3 个平铺,其余收进「更多」下拉;fixed right
// buttons: [{ content, theme = 'primary', onClick(row, rowIndex), visible(row) }]
export const opColumn = (buttons, { title = '操作', width = 160 } = {}) => ({
  colKey: 'operation',
  title,
  width,
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
        options: more.map((b, index) => ({ content: b.content, value: index })),
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
