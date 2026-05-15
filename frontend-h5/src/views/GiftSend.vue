<template>
  <div class="gift-send-page">
    <van-nav-bar title="赠送卡券" left-arrow @click-left="goBack" fixed placeholder />

    <div class="gift-content">
      <!-- 卡券预览 -->
      <div class="voucher-preview card" :class="{ 'is-coupon': voucher.voucherType === 'COUPON' }">
        <div v-if="voucher.voucherType === 'COUPON'" class="coupon-value">
          <span class="coupon-symbol">¥</span>
          <span class="coupon-amount">{{ voucher.faceValue }}</span>
        </div>
        <div class="voucher-name">{{ voucher.remark || '卡券' }}</div>
        <div class="voucher-expire">有效期至 {{ formatDate(voucher.expireAt) }}</div>
      </div>

      <!-- 接收人 -->
      <div class="form-section card">
        <van-field
          v-model="toEmployeeId"
          label="接收人工号"
          placeholder="请输入对方工号"
          :rules="[{ required: true, message: '请输入工号' }]"
        />
        <van-field
          v-model="message"
          label="留言"
          placeholder="选填，给对方留言"
          maxlength="100"
          type="textarea"
          rows="2"
          autosize
        />
      </div>

      <div class="gift-action">
        <van-button type="primary" block round @click="onGift" :loading="submitting">
          确认赠送
        </van-button>
        <p class="gift-notice">赠送后对方需在 24 小时内领取，超时自动退回</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { Toast } from 'vant';
import { getVoucherDetail, giftVoucher } from '../api/voucher';

const route = useRoute();
const router = useRouter();

const voucher = ref({});
const toEmployeeId = ref('');
const message = ref('');
const submitting = ref(false);

function formatDate(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  return `${y}-${m}-${d}`;
}

async function fetchVoucher() {
  try {
    const res = await getVoucherDetail(route.params.id);
    voucher.value = res?.data || res;
  } catch {
    Toast.fail('加载卡券信息失败');
    router.back();
  }
}

async function onGift() {
  if (!toEmployeeId.value.trim()) {
    Toast.fail('请输入接收人工号');
    return;
  }
  submitting.value = true;
  try {
    await giftVoucher({
      voucherId: voucher.value.id,
      toEmployeeId: toEmployeeId.value.trim(),
      message: message.value.trim(),
    });
    Toast.success('赠送成功');
    router.replace({ name: 'VoucherList' });
  } catch {
    // error handled by interceptor
  } finally {
    submitting.value = false;
  }
}

function goBack() {
  router.back();
}

onMounted(() => {
  fetchVoucher();
});
</script>

<style scoped>
.gift-send-page {
  min-height: 100vh;
  background: var(--color-bg);
}

.gift-content {
  padding: var(--spacing-md);
}

.voucher-preview {
  text-align: center;
  padding: var(--spacing-lg);
  margin-bottom: var(--spacing-md);
}

.voucher-preview.is-coupon {
  background: #fff7e6;
  border-color: #ffd666;
}

.coupon-value {
  margin-bottom: var(--spacing-xs);
}

.coupon-symbol {
  font-size: 16px;
  font-weight: 700;
  color: #ee0a24;
}

.coupon-amount {
  font-size: 32px;
  font-weight: 700;
  color: #ee0a24;
  font-family: 'JetBrains Mono', 'SF Mono', monospace;
}

.voucher-name {
  font-size: var(--font-size-card-title);
  font-weight: 600;
  color: var(--color-text-primary);
}

.voucher-expire {
  margin-top: var(--spacing-xs);
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
}

.form-section {
  margin-bottom: var(--spacing-md);
}

.gift-action {
  padding: 0 var(--spacing-sm);
}

.gift-notice {
  margin-top: var(--spacing-md);
  text-align: center;
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  line-height: 1.5;
}
</style>
