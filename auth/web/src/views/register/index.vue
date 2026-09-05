<script setup>
import { useRouter } from 'vue-router';

import Register from './register.vue';

const router = useRouter();

const goRoute = (path) => {
  router.replace(path)
}
</script>

<template>
  <div class="login_page">
    <div class="loginContainer">
      <div class="leftBox"></div>
      <div class="rightBox">
        <div @click="goRoute('/login')" class="registerBox">
        </div>
        <div @click="goRoute('/login')" class="register-text">登录</div>
        <h5 class="loginTitle">注册</h5>
        <Register />
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login_page {
  width: 100%;
  height: 100%;
  position: fixed;
  top: 0;
  left: 0;
  background: url('@/assets/pic/loginbg.jpg') center center no-repeat;

  .loginContainer {
    width: 45%;
    // height: 60vh;
    position: fixed;
    left: 50%;
    top: 50%;
    transform: translate(-20%, -50%);
    display: flex;

    .leftBox {
      background-color: var(--td-bg-color-container);
      width: 50%;
      border-right: 1px solid var(--td-component-stroke);
      box-sizing: border-box;
      opacity: 0;
    }

    .rightBox {
      background-color: var(--td-bg-color-container);
      width: 50%;
      min-width: 415px;
      border-radius: 0;
      padding: 20px;
      box-sizing: border-box;
      position: relative;

      .loginTitle {
        padding: 10px 0;
        text-align: center;
      }

      .registerBox {
        position: absolute;
        right: -50px;
        top: -50px;
        width: 0;
        height: 0;
        border: 50px solid transparent;
        border-left-color: var(--td-success-color-5);
        transform: rotate(-45deg);
        cursor: pointer;
      }

      .register-text {
        position: absolute;
        right: 0;
        top: 0;
        font-size: 16px;
        color: #fff;
        box-sizing: border-box;
        padding: 5px 4px;
        cursor: pointer;

        &:hover {
          opacity: 0.8;
        }
      }
    }
  }
}

@media screen and (min-width: 0px) and (max-width: 750px) {
  .login_page {
    .loginContainer {
      width: 90%;
      transform: translate(-50%, -50%);
      flex-wrap: wrap;

      .leftBox {
        width: 100%;
      }

      .rightBox {
        width: 100%;
      }
    }
  }
}
</style>