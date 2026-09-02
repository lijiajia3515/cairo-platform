<script setup>
import {
  ref,
  watch,
  defineExpose
} from 'vue';
import {
  debounce,
} from 'lodash';


const props = defineProps({
  list: {
    type: Array,
    default: () => []
  }
});
watch(() => props.list, () => {
  arr.value = filterMenu(props.list);
})
const filterMenu = (list) => {
  for (let parentMenu of list) {
    if (parentMenu.menus) {
      for (let childMenu of parentMenu.menus) {
        let count = 0;
        if (childMenu.permissions && childMenu.permissions.length) {
          childMenu.permissions.forEach(item => {
            if (item.isSelected == true) {
              count++;
            }
          });
          if (count == childMenu.permissions.length) {
            childMenu.checkAll = true;
            childMenu.isIndeterminate = false;
          } else if (count != 0) {
            childMenu.checkAll = false;
            childMenu.isIndeterminate = true;
          } else {
            childMenu.checkAll = false;
            childMenu.isIndeterminate = false;
          }
        }
      }
    } else { // 没有子菜单
      let count = 0;
      if (parentMenu.permissions && parentMenu.permissions.length) {
        parentMenu.permissions.forEach(item => {
          if (item.isSelected == true) {
            count++;
          }
        });
        if (count == parentMenu.permissions.length) {
          parentMenu.checkAll = true;
          parentMenu.isIndeterminate = false;
        } else if (count != 0) {
          parentMenu.checkAll = false;
          parentMenu.isIndeterminate = true;
        } else {
          parentMenu.checkAll = false;
          parentMenu.isIndeterminate = false;
        }
      }
    }
  }

  return list
}

const arr = ref(filterMenu(props.list));

// 
const getCheck = () => {
  let checks = [];
  for (let parentMenu of arr.value) {
    if (parentMenu.menus) {
      for (let childMenu of parentMenu.menus) {
        if (childMenu.permissions) {
          childMenu.permissions.forEach(item => {
            if (item.isSelected == true) {
              checks.push(item.permissionId);
            }
          })
        }
      }
    } else {
      if (parentMenu.permissions) {
        parentMenu.permissions.forEach(item => {
          if (item.isSelected == true) {
            checks.push(item.permissionId);
          }
        })
      }
    }
  }

  return checks;
}

// 单选
const onCheckOne = debounce(() => {
  for (let parentMenu of arr.value) {
    if (parentMenu.menus) {
      for (let childMenu of parentMenu.menus) {
        if (childMenu.permissions) {
          let count = 0;
          childMenu.permissions.forEach(item => {
            if (item.isSelected == true) {
              count++;
            }
            if (item.isSelected == false && item.type == 'read' && checked.value.indexOf('read') != -1) {
              checked.value.splice('read', 1)
            }
            if (item.isSelected == false && item.type == 'write' && checked.value.indexOf('write') != -1) {
              checked.value.splice('write', 1)
            }
            if (item.isSelected == false && item.type == 'operator' && checked.value.indexOf('operator') != -1) {
              checked.value.splice('operator', 1)
            }
          });
          if (count == childMenu.permissions.length) {
            childMenu.checkAll = true;
            childMenu.isIndeterminate = false;
          } else if (count != 0) {
            childMenu.checkAll = false;
            childMenu.isIndeterminate = true;
          } else if (count == 0) {
            childMenu.checkAll = false;
            childMenu.isIndeterminate = false;
          }
        }
      }
    } else {
      // 无子菜单
      if (parentMenu.permissions) {
        let count = 0;
        parentMenu.permissions.forEach(item => {
          if (item.isSelected == true) {
            count++;
          }
          if (item.isSelected == false && item.type == 'read' && checked.value.indexOf('read') != -1) {
            checked.value.splice('read', 1)
          }
          if (item.isSelected == false && item.type == 'write' && checked.value.indexOf('write') != -1) {
            checked.value.splice('write', 1)
          }
          if (item.isSelected == false && item.type == 'operator' && checked.value.indexOf('operator') != -1) {
            checked.value.splice('operator', 1)
          }
        });
        if (count == parentMenu.permissions.length) {
          parentMenu.checkAll = true;
          parentMenu.isIndeterminate = false;
        } else if (count != 0) {
          parentMenu.checkAll = false;
          parentMenu.isIndeterminate = true;
        } else if (count == 0) {
          parentMenu.checkAll = false;
          parentMenu.isIndeterminate = false;
        }
      }
    }
  }
})

// 全选
const onCheckAll = debounce((flag) => {
  for (let parentMenu of arr.value) {
    if (parentMenu.menus) {
      for (let childMenu of parentMenu.menus) {
        if (childMenu.checkAll == true) {
          childMenu.isIndeterminate = false;
          if (childMenu.permissions) {
            childMenu.permissions.forEach(item => {
              item.isSelected = true;
            })
          }
        } else {
          if (childMenu.permissions) {
            if (childMenu.isIndeterminate == false) {
              checked.value = []
              childMenu.permissions.forEach(item => {
                item.isSelected = false;
              })
            } else {
              let count = 0;
              childMenu.permissions.forEach(item => {
                if (item.isSelected == false) {
                  count++;
                }
              });
              if (count == childMenu.permissions.length) {
                childMenu.checkAll = true;
                childMenu.isIndeterminate = false;
              }
            }
          }
        }
      }
    } else {
      if (parentMenu.checkAll == true) { // 是全选
        parentMenu.isIndeterminate = false; // 取消半选
        if (parentMenu.permissions) {
          parentMenu.permissions.forEach(item => {
            item.isSelected = true;
          })
        }
      } else {
        if (parentMenu.permissions) {
          if (parentMenu.isIndeterminate == false) {
            checked.value = []
            parentMenu.permissions.forEach(item => {
              item.isSelected = false;
            })
          } else {
            let count = 0;
            parentMenu.permissions.forEach(item => {
              if (item.isSelected == false) {
                count++;
              }
            });
            if (count == parentMenu.permissions.length) {
              parentMenu.checkAll = true;
              parentMenu.isIndeterminate = false;
            }
          }
        }
      }
    }
  }
});

const checked = ref([])
const onChange = () => {
  for (let parentMenu of arr.value) {
    if (parentMenu.menus) {
      for (let childMenu of parentMenu.menus) {
        if (childMenu.permissions) {
          if (checked.value.length == 3) {
            childMenu.checkAll = true
            childMenu.isIndeterminate = false;
          } else if (checked.value.length < 3 && checked.value.length > 0) {
            childMenu.checkAll = false
            childMenu.isIndeterminate = true;
          } else {
            childMenu.checkAll = false
            childMenu.isIndeterminate = false;
          }
          childMenu.permissions.forEach(item => {
            if (checked.value.indexOf(item.type) != -1) {
              item.isSelected = true
            }
            if (!checked.value.includes('read') && item.type == 'read') {
              item.isSelected = false
            }
            if (!checked.value.includes('write') && item.type == 'write') {
              item.isSelected = false
            }
            if (!checked.value.includes('operator') && item.type == 'operator') {
              item.isSelected = false
            }
          });
        }
      }
    } else {
      // 无子菜单
      if (parentMenu.permissions) {
        if (checked.value.length == 3) {
          parentMenu.checkAll = true
          parentMenu.isIndeterminate = false;
        }
        else if (checked.value.length < 3 && checked.value.length > 0) {
          parentMenu.checkAll = false
          parentMenu.isIndeterminate = true;
        } else {
          parentMenu.checkAll = false
          parentMenu.isIndeterminate = false;
        }
        parentMenu.permissions.forEach(item => {
          if (checked.value.indexOf(item.type) != -1) {
            item.isSelected = true
          }
          if (!checked.value.includes('read') && item.type == 'read') {
            item.isSelected = false
          }
          if (!checked.value.includes('write') && item.type == 'write') {
            item.isSelected = false
          }
          if (!checked.value.includes('operator') && item.type == 'operator') {
            item.isSelected = false
          }
        });
      }
    }
  }
}
const getChecked = (res) => {
  checked.value = res
}
defineExpose({
  getChecked,
  getCheck
})
</script>

<template>
  <t-checkbox-group v-model="checked" @change="onChange">
    <t-checkbox :check-all="true" label="全选" />
    <t-checkbox label="读" value="read" />
    <t-checkbox label="写" value="write" />
    <t-checkbox label="操作" value="operator" />
  </t-checkbox-group>
  <div class="permisson_Page">

    <div v-for="(parent, parentIndex) in arr" :key="parentIndex" class="row">
      <div class="title">{{ parent.menuName }}</div>
      <div class="right">
        <template v-if="parent.menus">
          <template v-for="(child, childIndex) in parent.menus" :key="childIndex">
            <div class="child">
              <div class="title"> <span v-if="!child.permissions || child.permissions.length == 0">{{
    child.menuName }}</span> <t-checkbox v-else :label="child.menuName" lazyLoad :on-change="onCheckAll"
                  :indeterminate="child.isIndeterminate" v-model="child.checkAll" /></div>
              <div class="permission">
                <t-checkbox lazyLoad :on-change="onCheckOne" v-model="permission.isSelected" style="marginRight:10px;"
                  v-for="(permission, permissionIndex) in child.permissions" :key="permissionIndex"> {{
    permission.permissionName }}</t-checkbox>
              </div>
            </div>
          </template>
        </template>
        <!-- 无子菜单  -->
        <template v-else>
          <div class="child">
            <div class="title"> <span v-if="!parent.permissions || parent.permissions.length == 0">{{
    parent.menuName }}</span> <t-checkbox v-else label="全选" lazyLoad :on-change="onCheckAll"
                :indeterminate="parent.isIndeterminate" v-model="parent.checkAll" /></div>
            <div class="permission">
              <t-checkbox lazyLoad :on-change="onCheckOne" v-model="permission.isSelected" style="marginRight:10px;"
                v-for="(permission, permissionIndex) in parent.permissions" :key="permissionIndex"> {{
    permission.permissionName }}</t-checkbox>
            </div>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.permisson_Page {
  width: 100%;
  height: 58vh;
  overflow-y: auto;
  box-sizing: border-box;
  border-top: 1px solid #ededed;
  border-left: 1px solid #ededed;
  border-bottom: 1px solid #ededed;

  .row {
    width: 100%;
    display: flex;
    flex-wrap: wrap;
    box-sizing: border-box;
    border-bottom: 1px solid #ededed;

    &:last-child {
      border-bottom: 0;
    }

    .title {
      width: 100px;
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 5px;
      box-sizing: border-box;

    }

    .right {
      width: calc(100% - 100px);
      box-sizing: border-box;
      border-left: 1px solid #ededed;

      .child {
        width: 100%;
        display: flex;
        flex-wrap: wrap;
        box-sizing: border-box;
        border-bottom: 1px solid #ededed;

        &:last-child {
          border-bottom: 0;
        }
      }

      .permission {
        width: calc(100% - 100px);
        float: left;
        box-sizing: border-box;
        padding: 5px 10px;
        border-left: 1px solid #ededed;
      }
    }
  }
}

::-webkit-scrollbar {
  width: 3px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  /*滚动条里面小方块*/
  border-radius: 10px;
  // box-shadow: inset 0 0 5px #3f78f5;
  background: #d2d2d2;
}

::-webkit-scrollbar-track {
  /*滚动条里面轨道*/
  // box-shadow: inset 0 0 5px #3f78f5;
  border-radius: 10px;
  background: #f1f1f1;
}
</style>