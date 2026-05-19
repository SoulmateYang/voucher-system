<template>
  <div class="voucher-detail-page">
    <!-- Navigation bar -->
    <van-nav-bar
      title="卡券详情"
      left-arrow
      @click-left="goBack"
      fixed
      placeholder
    />

    <!-- Loading skeleton -->
    <template v-if="loading">
      <div class="skeleton-content">
        <div class="skeleton-section card">
          <van-skeleton title round row="3" />
        </div>
        <div class="skeleton-section card">
          <van-skeleton title round row="4" />
        </div>
      </div>
    </template>

    <!-- Error state -->
    <template v-else-if="error">
      <div class="error-state">
        <van-icon name="warn-o" size="48" color="#c8c9cc" />
        <p class="error-text">{{ notFound ? '券不存在' : '加载失败' }}</p>
        <van-button
          v-if="!notFound"
          type="primary"
          size="small"
          @click="fetchDetail"
          class="retry-btn"
        >
          重新加载
        </van-button>
      </div>
    </template>

    <!-- Voucher detail -->
    <template v-else-if="voucher">
      <div class="detail-content">
          <div v-if="voucher.isOffline" class="offline-banner">
            <van-icon name="warn-o" size="16" />
            <span>离线模式 — 显示缓存数据</span>
          </div>
        <!-- Status + resource name -->
        <div class="voucher-header card" :class="{ 'is-coupon': voucher.voucherType === 'COUPON', 'is-stored': voucher.voucherType === 'STORED_VALUE' }">
          <!-- Coupon face value display -->
          <div v-if="voucher.voucherType === 'COUPON'" class="coupon-value-block">
            <span class="coupon-symbol">¥</span>
            <span class="coupon-amount">{{ voucher.faceValue || 0 }}</span>
          </div>
          <!-- Stored value balance display -->
          <div v-else-if="voucher.voucherType === 'STORED_VALUE'" class="coupon-value-block stored-value-block">
            <span class="coupon-symbol">¥</span>
            <span class="coupon-amount">{{ voucher.remainingBalance || 0 }}</span>
            <span class="stored-label">剩余余额</span>
          </div>
          <van-tag
            :class="statusTagClass(voucher.status)"
            size="small"
            class="status-badge"
          >
            {{ statusLabel(voucher.status) }}
          </van-tag>
          <h2 class="resource-name">{{ voucher.remark || (voucher.voucherType === 'COUPON' ? '优惠券' : '卡券') }}</h2>
        </div>

        <!-- QR code section -->
        <div class="qr-section card">
          <div class="qr-wrapper" ref="qrRef">
            <div id="qrCodeContainer"></div>
          </div>
          <p class="qr-hint">出示二维码给管理员核销</p>
          <p
            class="voucher-code-text"
            @click="copyVoucherCode"
          >
            {{ voucher.voucherCode }}
            <van-icon name="copy-o" size="14" class="copy-icon" />
          </p>
        </div>

        <!-- Coupon detail card -->
        <div v-if="voucher.voucherType === 'COUPON'" class="info-card card coupon-info-card">
          <div class="info-row">
            <span class="info-label">券类型</span>
            <span class="info-value">{{ voucher.discountType === 'FIXED_AMOUNT' ? '满减券' : '折扣券' }}</span>
          </div>
          <div class="info-divider" />
          <div v-if="voucher.discountType === 'PERCENTAGE'" class="info-row">
            <span class="info-label">折扣率</span>
            <span class="info-value coupon-discount">{{ 100 - (voucher.discountValue || 0) }}折</span>
          </div>
          <div v-else class="info-row">
            <span class="info-label">优惠金额</span>
            <span class="info-value">直减 ¥{{ voucher.faceValue || voucher.discountValue || 0 }}</span>
          </div>
          <div class="info-divider" />
          <div v-if="voucher.minOrderAmount > 0" class="info-row">
            <span class="info-label">使用条件</span>
            <span class="info-value">满 ¥{{ voucher.minOrderAmount }} 可用</span>
          </div>
          <div v-else class="info-row">
            <span class="info-label">使用条件</span>
            <span class="info-value">无门槛</span>
          </div>
        </div>

        <!-- Stored value card info -->
        <div v-if="voucher.voucherType === 'STORED_VALUE'" class="info-card card stored-info-card">
          <div class="info-row">
            <span class="info-label">初始余额</span>
            <span class="info-value">¥{{ voucher.initialBalance || 0 }}</span>
          </div>
          <div class="info-divider" />
          <div class="info-row">
            <span class="info-label">赠送金额</span>
            <span class="info-value">¥{{ voucher.bonusValue || 0 }}</span>
          </div>
          <div class="info-divider" />
          <div class="info-row">
            <span class="info-label">剩余余额</span>
            <span class="info-value stored-balance">¥{{ voucher.remainingBalance || 0 }}</span>
          </div>
        </div>

        <!-- Gift action -->
        <div v-if="canGift" class="gift-section card">
          <van-button type="warning" block round @click="onGift">
            赠送给好友
          </van-button>
        </div>

        <!-- Detail info card -->
        <div class="info-card card">
          <div class="info-row">
            <span class="info-label">有效期</span>
            <span class="info-value">{{ formatDateTime(voucher.expireAt) }}</span>
          </div>
          <div class="info-divider" />
          <div v-if="voucher.voucherType !== 'COUPON'" class="info-row">
            <span class="info-label">资源描述</span>
            <span class="info-value info-desc">{{ voucher.remark || '--' }}</span>
          </div>
          <div v-if="voucher.voucherType !== 'COUPON'" class="info-divider" />
          <div class="info-row">
            <span class="info-label">发放时间</span>
            <span class="info-value">{{ formatDateTime(voucher.issuedAt) }}</span>
          </div>
          <div v-if="voucher.approveRef" class="info-divider" />
          <div v-if="voucher.approveRef" class="info-row">
            <span class="info-label">审批单号</span>
            <span class="info-value mono">{{ voucher.approveRef }}</span>
          </div>
          <div v-if="voucher.status === 'USED' && voucher.usedAt" class="info-divider" />
          <div v-if="voucher.status === 'USED' && voucher.usedAt" class="info-row">
            <span class="info-label">核销时间</span>
            <span class="info-value">{{ formatDateTime(voucher.usedAt) }}</span>
          </div>
        </div>
      </div>
    </template>

    <!-- Bottom tab bar -->
    <van-tabbar fixed route>
      <van-tabbar-item
        icon="coupon-o"
        :to="{ name: 'VoucherList' }"
      >
        我的卡券
      </van-tabbar-item>
      <van-tabbar-item
        icon="records"
        @click="onUsageRecords"
      >
        使用记录
      </van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { showToast, showSuccessToast, showFailToast } from 'vant';
import QRCode from 'qrcode';
import { getVoucherDetail } from '../api/voucher';

const route = useRoute();
const router = useRouter();

const voucher = ref(null);
const loading = ref(false);
const error = ref(false);
const notFound = ref(false);
const qrRef = ref(null);

const STATUS_MAP = {
  ISSUED: { label: '有效', class: 'tag-valid' },
  USED: { label: '已使用', class: 'tag-used' },
  EXHAUSTED: { label: '已用完', class: 'tag-used' },
  EXPIRED: { label: '已过期', class: 'tag-expired' },
  CANCELLED: { label: '已作废', class: 'tag-revoked' },
};

const canGift = computed(() => {
  console.log('canGift check:', voucher.value?.status, voucher.value?.transferable);
  return voucher.value?.status === 'ISSUED'
    && voucher.value?.transferable !== 0;
});

function onGift() {
  router.push({ name: 'GiftSend', params: { id: voucher.value.id } });
}

function statusLabel(status) {
  return STATUS_MAP[status]?.label || status;
}

function statusTagClass(status) {
  return STATUS_MAP[status]?.class || '';
}

function formatDateTime(dateStr) {
  if (!dateStr) return '--';
  const date = new Date(dateStr);
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  return `${y}-${m}-${d} ${h}:${min}`;
}

function generateQRCode(voucherCode) {
  const container = document.getElementById('qrCodeContainer');
  if (!container) return;

  QRCode.toString(voucherCode, {
    type: 'svg',
    width: 200,
    margin: 1,
    color: { dark: '#323233', light: '#ffffff' },
    errorCorrectionLevel: 'M',
  }).then(svg => {
    container.innerHTML = svg;
    const svgEl = container.querySelector('svg');
    if (svgEl) {
      svgEl.style.width = '200px';
      svgEl.style.height = '200px';
    }
  }).catch(e => {
    console.error('QR生成失败:', e);
  });
}

async function copyVoucherCode() {
  if (!voucher.value?.voucherCode) return;
  try {
    await navigator.clipboard.writeText(voucher.value.voucherCode);
    showSuccessToast('已复制券码');
  } catch {
    showFailToast('复制失败');
  }
}

async function fetchDetail() {
  const id = route.params.id;
  if (!id) {
    error.value = true;
    notFound.value = true;
    return;
  }

  loading.value = true;
  error.value = false;
  notFound.value = false;

  try {
    const res = await getVoucherDetail(id);
    const data = res?.data || res;
    if (!data) {
      error.value = true;
      notFound.value = true;
      return;
    }
    voucher.value = data;
    loading.value = false;

    // 缓存到 localStorage 用于离线查看
    try {
      const cache = {
        voucherCode: data.voucherCode,
        remark: data.remark,
        voucherType: data.voucherType,
        faceValue: data.faceValue,
        status: data.status,
        expireAt: data.expireAt,
        cachedAt: new Date().toISOString(),
      };
      localStorage.setItem(`voucher_cache_${id}`, JSON.stringify(cache));
    } catch { /* ignore quota errors */ }

    // Generate QR code after DOM update
    await nextTick();
    try {
      generateQRCode(data.voucherCode);
    } catch (e) {
      console.error('QR生成失败:', e);
    }
  } catch (err) {
    // 尝试从缓存读取
    try {
      const cached = localStorage.getItem(`voucher_cache_${id}`);
      if (cached) {
        const data = JSON.parse(cached);
        voucher.value = {
          ...data,
          voucherCode: data.voucherCode,
          remark: data.remark + '（离线）',
          voucherType: data.voucherType,
          faceValue: data.faceValue,
          status: data.status,
          expireAt: data.expireAt,
          isOffline: true,
        };
        loading.value = false;
      }
    } catch { /* ignore */ }

    error.value = true;
    if (err?.response?.status === 404) {
      notFound.value = true;
    }
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.back();
}

function onUsageRecords() {
  router.push({ name: 'GiftRecords', params: { id: voucher.value.id } });
}

// Regenerate QR when route param changes (same component, different voucher)
watch(
  () => route.params.id,
  () => {
    fetchDetail();
  },
);

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped>
.voucher-detail-page {
  min-height: 100vh;
  padding-bottom: 60px; /* space for tabbar */
  background: var(--color-bg);
}

.skeleton-content {
  padding: var(--spacing-md);
}

.skeleton-section {
  margin-bottom: var(--spacing-sm);
}

.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px var(--spacing-md);
}

.error-text {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.retry-btn {
  margin-top: var(--spacing-md);
  min-width: 120px;
}

.detail-content {
  padding: var(--spacing-sm) var(--spacing-md);
}

/* Voucher header */
.voucher-header {
  margin-bottom: var(--spacing-sm);
  text-align: center;
  padding: var(--spacing-lg) var(--spacing-md);
}

.status-badge {
  display: inline-block;
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-size-small);
  padding: 2px 8px;
  line-height: 20px;
  border-radius: var(--radius-sm);
}

/* Coupon header */
.voucher-header.is-coupon {
  background: #fff7e6;
  border-color: #ffd666;
}

.coupon-value-block {
  text-align: center;
  margin-bottom: var(--spacing-sm);
}

.coupon-value-block .coupon-symbol {
  font-size: 18px;
  font-weight: 700;
  color: #ee0a24;
}

.coupon-value-block .coupon-amount {
  font-size: 36px;
  font-weight: 700;
  color: #ee0a24;
  font-family: 'JetBrains Mono', 'SF Mono', monospace;
}

.coupon-info-card {
  background: #fff7e6;
  border-color: #ffd666;
}

.coupon-discount {
  font-size: 16px;
  font-weight: 600;
  color: #fa8c16;
}

/* Resource name */
.resource-name {
  font-size: var(--font-size-heading);
  font-weight: 700;
  color: var(--color-text-primary);
  word-break: break-word;
}

/* QR section */
.qr-section {
  margin-bottom: var(--spacing-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: var(--spacing-lg) var(--spacing-md);
}

.qr-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-width: 200px;
  min-height: 200px;
}

.qr-hint {
  margin-top: var(--spacing-md);
  font-size: var(--font-size-body);
  color: var(--color-text-secondary);
}

.voucher-code-text {
  margin-top: var(--spacing-sm);
  font-family: var(--font-family-mono);
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  letter-spacing: 2px;
  cursor: pointer;
  user-select: all;
  display: inline-flex;
  align-items: center;
  gap: var(--spacing-2xs);
  padding: var(--spacing-2xs) var(--spacing-xs);
  border-radius: var(--radius-sm);
  transition: background 0.2s ease;
}

.voucher-code-text:active {
  background: var(--color-border-light);
}

.copy-icon {
  vertical-align: middle;
}

/* Info card */
.info-card {
  margin-bottom: var(--spacing-sm);
}

.info-row {
  display: flex;
  flex-direction: column;
  padding: var(--spacing-sm) 0;
}

.info-label {
  font-size: var(--font-size-small);
  color: var(--color-text-secondary);
  margin-bottom: var(--spacing-2xs);
}

.info-value {
  font-size: var(--font-size-body);
  color: var(--color-text-primary);
  word-break: break-word;
}

.info-value.mono {
  font-family: var(--font-family-mono);
  letter-spacing: 1px;
  font-size: var(--font-size-small);
}

.info-desc {
  line-height: 1.6;
}

.info-divider {
  height: 1px;
  background: var(--color-border-light);
}

.gift-section {
  margin-bottom: var(--spacing-sm);
  padding: var(--spacing-md);
}

.offline-banner {
  display: flex;
  align-items: center;
  gap: var(--spacing-xs);
  padding: var(--spacing-sm) var(--spacing-md);
  margin-bottom: var(--spacing-sm);
  background: #fff7e6;
  border-radius: var(--radius-md);
  font-size: var(--font-size-small);
  color: #fa8c16;
}
</style>
