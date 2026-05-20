<template>
  <div class="manual-add-page">
    <van-nav-bar title="添加卡券" left-arrow @click-left="goBack" fixed placeholder />

    <div class="form-content">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.name"
            label="卡券名称"
            placeholder="请输入卡券名称"
            :rules="[{ required: true, message: '请输入名称' }]"
          />
          <van-field
            v-model="form.voucherType"
            label="卡券类型"
            placeholder="请选择"
            is-link
            readonly
            @click="showTypePicker = true"
            :rules="[{ required: true, message: '请选择类型' }]"
          />
          <van-field
            v-if="form.voucherType === 'COUPON'"
            v-model="form.faceValue"
            label="面值"
            placeholder="请输入面值"
            type="number"
          />
          <van-field
            v-model="form.expireAt"
            label="有效期"
            placeholder="请选择"
            is-link
            readonly
            @click="showDatePicker = true"
          />
          <van-field
            v-model="form.voucherCode"
            label="核销码"
            placeholder="选填"
          />
          <van-field
            v-model="form.remark"
            label="备注"
            placeholder="选填"
            type="textarea"
            rows="2"
            autosize
          />
        </van-cell-group>

        <div class="submit-area">
          <van-button type="primary" block round native-type="submit" :loading="submitting">
            确认添加
          </van-button>
        </div>
      </van-form>
    </div>

    <!-- Type picker -->
    <van-popup v-model:show="showTypePicker" position="bottom">
      <van-picker
        :columns="typeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePicker = false"
      />
    </van-popup>

    <!-- Date picker -->
    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        :min-date="minDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { showSuccessToast } from 'vant';
import { addManualVoucher } from '../api/voucher';

const router = useRouter();
const submitting = ref(false);
const showTypePicker = ref(false);
const showDatePicker = ref(false);

const form = reactive({
  name: '',
  voucherType: '',
  faceValue: '',
  expireAt: '',
  voucherCode: '',
  remark: '',
});

const typeColumns = [
  { text: '资源使用券', value: 'RESOURCE_USAGE' },
  { text: '优惠券', value: 'COUPON' },
];

const minDate = new Date();

function onTypeConfirm({ selectedOptions }) {
  form.voucherType = selectedOptions[0]?.text || '';
  showTypePicker.value = false;
}

function onDateConfirm(date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  form.expireAt = `${y}-${m}-${d}`;
  showDatePicker.value = false;
}

async function onSubmit() {
  submitting.value = true;
  try {
    await addManualVoucher({
      name: form.name,
      voucherType: form.voucherType,
      faceValue: form.faceValue ? parseFloat(form.faceValue) : null,
      expireAt: form.expireAt,
      voucherCode: form.voucherCode,
      remark: form.remark,
    });
    showSuccessToast('添加成功');
    router.replace({ name: 'VoucherList' });
  } catch {
    // handled by interceptor
  } finally {
    submitting.value = false;
  }
}

function goBack() { router.back(); }
</script>

<style scoped>
.manual-add-page {
  min-height: 100vh;
  background: var(--color-bg);
  padding-bottom: 50px;
}

.form-content {
  padding: var(--spacing-md);
}

.submit-area {
  margin-top: var(--spacing-lg);
  padding: 0 var(--spacing-sm);
}
</style>
