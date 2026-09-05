// 看板数据源:当前为静态占位,聚合接口就绪后改为拉取并保持返回结构不变
import { ref } from 'vue';

export default function useDashboardData() {
  const loading = ref(false);

  // 假数据(结构与未来聚合接口返回保持一致,替换实现不动视图层)
  const stats = ref([
    { key: 'account', label: '账号总数', value: 1286, trend: 12 },
    { key: 'app', label: '应用数', value: 9, trend: 2 },
    { key: 'session', label: '在线会话', value: 37, trend: -4 },
    { key: 'login_today', label: '今日登录', value: 214, trend: 8 },
  ]);

  // 近 7 日登录趋势(成功/失败双序列,单位:次)
  const loginTrend = ref({
    days: ['08-28', '08-29', '08-30', '08-31', '09-01', '09-02', '09-03'],
    success: [186, 202, 178, 240, 219, 231, 214],
    failed: [23, 18, 31, 15, 27, 12, 19],
  });

  // 认证方式分布(占比)
  const authTypeShare = ref([
    { name: '密码', value: 62 },
    { name: '验证码', value: 24 },
    { name: '第三方', value: 14 },
  ]);

  // 最近登录记录
  const recentLogins = ref([
    { account: 'admin', ip: '127.0.0.1', region: '本机', type: '密码', result: '成功', time: '09-03 18:24' },
    { account: 'alice', ip: '203.0.113.7', region: '广东 深圳', type: '验证码', result: '成功', time: '09-03 17:58' },
    { account: 'bob', ip: '198.51.100.2', region: '浙江 杭州', type: '密码', result: '失败', time: '09-03 17:31' },
    { account: 'carol', ip: '192.0.2.55', region: '北京', type: '第三方', result: '成功', time: '09-03 16:47' },
  ]);

  return { loading, stats, loginTrend, authTypeShare, recentLogins };
}
