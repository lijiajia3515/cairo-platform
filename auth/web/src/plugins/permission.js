import { useUserStore } from '@/store/user';

// v-allow 指令的 JS 等价判定:JSX/渲染函数里指令不生效(编译期被丢弃),操作列按钮等场景用它
export const hasPermission = (key) => {
  if (key == true || key == null) return true;
  const userStore = useUserStore();
  const permissions = userStore.permissionListGetter;
  if (permissions.indexOf('app_admin') != -1) return true;
  if (permissions && permissions.length > 0) {
    return permissions.indexOf(key) != -1;
  }
  return false;
};

const setPermisePlugin = {
  install(app, options) {
    // Pinia 已在此前 app.use(store) 注册，此处可安全取用
    const userStore = useUserStore();

    app.directive('allow', {
      mounted: (el, binding) => {
        try {
          let permissions = userStore.permissionListGetter;
          // 判断是不是有管理员权限
          let isAdmin = permissions.indexOf('app_admin') != -1;
          if (isAdmin) return;

          // 直接使用权限表定义的 permissionId（如 menu.read）
          let key = binding.value;
          if (key == true) {
            return;
          }
          // 权限表尚未加载(空)时不删——等它就绪前删除视图根节点会把整页变成注释节点,
          // 卡死 transition(out-in);正常判定只在权限表就绪后做
          if (permissions && permissions.length > 0) { // 有权限列表
            if (permissions.indexOf(key) == -1) {
              el.parentNode.removeChild(el);
            }
          }
        } catch (err) {
          console.log(err)
        }
      },
    })
  }
};

export default setPermisePlugin;
