<template>
  <div id="app-root">
    <router-view />
    <van-tabbar
      v-if="showTabbar"
      v-model="activeTab"
      route
      :placeholder="false"
      @change="onTabChange"
    >
      <van-tabbar-item icon="coupon-o" name="vouchers" to="/voucher-list">
        卡券
      </van-tabbar-item>
      <van-tabbar-item icon="manager-o" name="profile" to="/profile">
        我的
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();

const activeTab = ref('vouchers');

const voucherTabRoutes = ['VoucherList', 'VoucherDetail', 'GiftSend', 'ManualAdd'];
const profileTabRoutes = ['Profile', 'EditProfile', 'ChangePassword', 'UsageRecords', 'GiftInbox', 'GiftOutbox', 'GiftRecords'];

const showTabbar = computed(() => route.name !== 'Login');

function resolveTab(name) {
  if (voucherTabRoutes.includes(name)) return 'vouchers';
  if (profileTabRoutes.includes(name)) return 'profile';
  return 'vouchers';
}

watch(() => route.name, (name) => {
  activeTab.value = resolveTab(name);
}, { immediate: true });

function onTabChange(name) {
  activeTab.value = name;
}
</script>

<style scoped>
#app-root {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.van-tabbar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 100;
}
</style>
