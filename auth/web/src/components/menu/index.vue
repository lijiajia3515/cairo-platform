<script setup>
import { ref, reactive, watch, onMounted, onUnmounted, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useWindowSize } from '@vueuse/core';

import useState from '@/hooks/useState';
import { useUserStore } from '@/store/user';

const router = useRouter();
const route = useRoute();

const userStore = useUserStore();

import { MessagePlugin } from 'tdesign-vue-next';
const menuList = computed(() => userStore.menuListGetter);



onMounted(() => {
  console.log(menuList, 'menuList====');
  // getMenuList();

});
const getParentPath = () => {
  let parentPath = '';
  console.log(route, 'route====');
  let currentPathArr = route.path.split('/');
  const path = currentPathArr.length == 4 ? '/' + currentPathArr[1] + '/' + currentPathArr[2] : route.path;
  menuList.value.forEach(item => {
    if (item.menus) {
      item.menus.forEach(child => {
        let pathArr = child.path.split('/');
        let newPath = pathArr.length == 4 ? '/' + pathArr[1] + '/' + pathArr[2] : child.path;
        if (newPath == path) {
          parentPath = [item.path];
          return;
        }
        if (child.menus) {
          child.menus.forEach(val => {
            let pathArr1 = val.path.split('/');
            let newPath1 = pathArr1.length == 4 ? '/' + pathArr1[1] + '/' + pathArr1[2] : val.path;
            if (newPath1 == path) {
              parentPath = [item.path, child.path];
              return;
            }
          })

        } else {
          if (child.path == path) {
            parentPath = [child.path]
          }
        }

      })
    } else {
      if (item.path == path) {
        parentPath = [item.path]
      }
    }
  });
  console.log(parentPath, 'parentPath====');
  return parentPath;
}


let [expanded, setExpanded] = useState(getParentPath()); // 子菜单展开的导航集

let [collapsed, setCollapsed] = useState(false);
let [active_menu, setActiveMemu] = useState(route.path.split('/').length == 4 ? '/' + route.path.split('/')[1] + '/' + route.path.split('/')[2] : route.path); // 激活菜单项
const { width, height } = useWindowSize(); // 监听窗口大小
watch(width, () => {
  if (width.value < 1200) {
    setCollapsed(true);
  } else {
    setCollapsed(false);
  }
}, {
  immediate: true
});



watch(() => route.path, () => {
  document.title = route.name || 'Cairo运营平台';
  setActiveMemu(route.path.split('/').length == 4 ? '/' + route.path.split('/')[1] + '/' + route.path.split('/')[2] : route.path);
  setExpanded(getParentPath())
  // console.log(expanded.value, 'expanded');
}, {
  immediate: true
})
const changeHandler = (active) => {
  if (active === undefined) {
    MessagePlugin.info('菜单无法点击');
  }
}

onUnmounted(() => {

})
</script>


<template>
  <div class="menu__wrapper">

    <t-menu v-model="active_menu" :collapsed="collapsed" v-model:expanded="expanded" expand-mutex
      @change="changeHandler">
      <div v-for="(item, index) in menuList" :key="index">
        <template v-if="item.hiddenMenu == false">
          <t-menu-item :to="{ path: item.component }" v-if="item.component && (!item.menus || !item.menus.length)"
            :value="item.path">
            <template #icon>
              <img v-show="item.icon" style="width:16px;height:16px;marginRight:5px" :src="item.icon" alt="">
            </template>
            <div>
              <span class="sl1">{{ item.menuName }}</span>
              <t-space class="tag-demo" v-if="item.tags" size="small">
                <t-tag v-if="item.tags.includes('hot')" theme="danger">hot</t-tag>
                <t-tag v-if="item.tags.includes('new')" theme="danger">new</t-tag>
                <t-tag v-if="item.tags.includes('old')">old</t-tag>
                <t-tag v-if="item.tags.includes('deprecated')">deprecated</t-tag>
              </t-space>
            </div>

          </t-menu-item>

          <t-menu-item v-else-if="!item.component && item.path && (!item.menus || !item.menus.length)"
            :to="{ path: item.path.indexOf('http') == -1 ? item.path : (item.path.indexOf('http') != -1 ? '/iframe' : ''), query: { url: item.path, title: item.menuName } }"
            :value="'/iframe'">
            <template #icon>
              <img v-show="item.icon" style="width:16px;height:16px;marginRight:5px" :src="item.icon" alt="">
            </template>
            <div>
              <span class="sl1">{{ item.menuName }}</span>
              <t-space class="tag-demo" v-if="item.tags" size="small">
                <t-tag v-if="item.tags.includes('hot')" theme="danger">hot</t-tag>
                <t-tag v-if="item.tags.includes('new')" theme="danger">new</t-tag>
                <t-tag v-if="item.tags.includes('old')">old</t-tag>
                <t-tag v-if="item.tags.includes('deprecated')">deprecated</t-tag>
              </t-space>
            </div>
          </t-menu-item>

          <t-menu-item v-else-if="!item.path && !item.component && (!item.menus || !item.menus.length)">
            <template #icon>
              <img v-show="item.icon" style="width:16px;height:16px;marginRight:5px" :src="item.icon" alt="">
            </template>
            <div>
              <span class="sl1">{{ item.menuName }}</span>
              <t-space class="tag-demo" v-if="item.tags" size="small">
                <t-tag v-if="item.tags.includes('hot')" theme="danger">hot</t-tag>
                <t-tag v-if="item.tags.includes('new')" theme="danger">new</t-tag>
                <t-tag v-if="item.tags.includes('old')">old</t-tag>
                <t-tag v-if="item.tags.includes('deprecated')">deprecated</t-tag>
              </t-space>
            </div>
          </t-menu-item>

          <t-submenu v-else :value="item.path" :title="item.menuName">
            <template #icon>
              <img v-show="item.icon" style="width:16px;height:16px;marginRight:5px" :src="item.icon" alt="">
            </template>
            <div v-for="(child, i) in item.menus" :key="i">
              <template v-if="child.hiddenMenu == false">
                <!-- <t-menu-item v-if="child.component" :to="{ path: child.component }" :value="child.path">
                  <span class="sl1">{{ child.menuName }}</span>
                  <template #icon>
                    <img v-show="child.icon" style="width:16px;height:16px;marginRight:5px" :src="child.icon" alt="">
                  </template>
    </t-menu-item>

    <t-menu-item v-else="!child.component && child.path"
      :to="{ path: child.path.indexOf('http') == -1 ? child.path : (child.path.indexOf('http') != -1 ? '/iframe' : ''), query: { url: child.path, title: child.menuName } }"
      :value="'/iframe'">
      <span class="sl1">{{ child.menuName }}</span>
      <template #icon>
                    <img v-show="child.icon" style="width:16px;height:16px;marginRight:5px" :src="child.icon" alt="">
                  </template>
    </t-menu-item> -->

                <t-menu-item :to="{ path: child.component }"
                  v-if="child.component && (!child.menus || !child.menus.length)" :value="child.path">
                  <template #icon>
                    <img v-show="child.icon" style="width:16px;height:16px;marginRight:5px" :src="child.icon" alt="">
                  </template>
                  <div>
                    <span class="sl1">{{ child.menuName }}</span>
                    <t-space class="tag-demo" v-if="child.tags" size="small">
                      <t-tag v-if="child.tags.includes('hot')" theme="danger">hot</t-tag>
                      <t-tag v-if="child.tags.includes('new')" theme="danger">new</t-tag>
                      <t-tag v-if="child.tags.includes('old')">old</t-tag>
                      <t-tag v-if="child.tags.includes('deprecated')">deprecated</t-tag>
                    </t-space>
                  </div>
                  <!-- <span class="sl1">{{ child.menuName }}</span> -->
                </t-menu-item>

                <t-menu-item v-else-if="!child.component && child.path && (!child.menus || !child.menus.length)"
                  :to="{ path: child.path.indexOf('http') == -1 ? child.path : (child.path.indexOf('http') != -1 ? '/iframe' : ''), query: { url: child.path, title: child.menuName } }"
                  :value="'/iframe'">
                  <template #icon>
                    <img v-show="child.icon" style="width:16px;height:16px;marginRight:5px" :src="child.icon" alt="">
                  </template>
                  <div>
                    <span class="sl1">{{ child.menuName }}</span>
                    <t-space class="tag-demo" v-if="child.tags" size="small">
                      <t-tag v-if="child.tags.includes('hot')" theme="danger">hot</t-tag>
                      <t-tag v-if="child.tags.includes('new')" theme="danger">new</t-tag>
                      <t-tag v-if="child.tags.includes('old')">old</t-tag>
                      <t-tag v-if="child.tags.includes('deprecated')">deprecated</t-tag>
                    </t-space>
                  </div>
                </t-menu-item>

                <t-menu-item v-else-if="!child.path && !child.component && (!child.menus || !child.menus.length)">
                  <template #icon>
                    <img v-show="child.icon" style="width:16px;height:16px;marginRight:5px" :src="child.icon" alt="">
                  </template>
                  <div>
                    <span class="sl1">{{ child.menuName }}</span>
                    <t-space class="tag-demo" v-if="child.tags" size="small">
                      <t-tag v-if="child.tags.includes('hot')" theme="danger">hot</t-tag>
                      <t-tag v-if="child.tags.includes('new')" theme="danger">new</t-tag>
                      <t-tag v-if="child.tags.includes('old')">old</t-tag>
                      <t-tag v-if="child.tags.includes('deprecated')">deprecated</t-tag>
                    </t-space>
                  </div>
                </t-menu-item>

                <t-submenu v-else :value="child.path" :title="child.menuName">
                  <template #icon>
                    <img v-show="child.icon" style="width:16px;height:16px;marginRight:5px" :src="child.icon" alt="">
                  </template>
                  <div v-for="(val, i) in child.menus" :key="i">
                    <template v-if="val.hiddenMenu == false">
                      <t-menu-item v-if="val.component" :to="{ path: val.component }" :value="val.path">
                        <!-- <span class="sl1">{{ val.menuName }}</span> -->
                        <template #icon>
                          <img v-show="val.icon" style="width:16px;height:16px;marginRight:5px" :src="val.icon" alt="">
                        </template>
                        <div>
                          <span class="sl1">{{ val.menuName }}</span>
                          <t-space class="tag-demo" v-if="val.tags" size="small">
                            <t-tag v-if="val.tags.includes('hot')" theme="danger">hot</t-tag>
                            <t-tag v-if="val.tags.includes('new')" theme="danger">new</t-tag>
                            <t-tag v-if="val.tags.includes('old')">old</t-tag>
                            <t-tag v-if="val.tags.includes('deprecated')">deprecated</t-tag>
                          </t-space>
                        </div>
                      </t-menu-item>

                      <t-menu-item v-else
                        :to="{ path: val.path.indexOf('http') == -1 ? val.path : (val.path.indexOf('http') != -1 ? '/iframe' : ''), query: { url: val.path, title: val.menuName } }"
                        :value="'/iframe'">
                        <!-- <span class="sl1">{{ val.menuName }}</span> -->
                        <template #icon>
                          <img v-show="val.icon" style="width:16px;height:16px;marginRight:5px" :src="val.icon" alt="">
                        </template>
                        <div>
                          <span class="sl1">{{ val.menuName }}</span>
                          <t-space class="tag-demo" v-if="val.tags" size="small">
                            <t-tag v-if="val.tags.includes('hot')" theme="danger">hot</t-tag>
                            <t-tag v-if="val.tags.includes('new')" theme="danger">new</t-tag>
                            <t-tag v-if="val.tags.includes('old')">old</t-tag>
                            <t-tag v-if="val.tags.includes('deprecated')">deprecated</t-tag>
                          </t-space>
                        </div>
                      </t-menu-item>
                    </template>
                  </div>

                </t-submenu>
              </template>
            </div>

          </t-submenu>
        </template>
      </div>
    </t-menu>
  </div>
</template>

<style lang="scss" scoped>
.menu__wrapper {
  .iconfont {
    font-size: 16px;
    margin-right: 5px;
    vertical-align: middle;
  }
}

.t-tag {
  // width: 25px;
  height: 12px;
  font-size: 10px;
  padding: 0 2px;
  line-height: 12px;
}

.t-space {
  margin-left: 5px;
}
</style>
