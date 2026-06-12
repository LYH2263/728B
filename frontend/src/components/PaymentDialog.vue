<template>
  <el-dialog
    v-model="visible"
    title="扫码支付"
    width="420px"
    :close-on-click-modal="false"
    :show-close="true"
    @close="handleClose"
  >
    <div class="payment-dialog">
      <div v-if="order" class="payment-info">
        <div class="order-info">
          <span class="label">充值金额：</span>
          <span class="amount">¥{{ order.amount.toFixed(2) }}</span>
        </div>
        <div v-if="order.bonusAmount > 0" class="bonus-info">
          <el-icon color="#10b981"><Present /></el-icon>
          <span>赠送 ¥{{ order.bonusAmount.toFixed(2) }}，共到账 ¥{{ order.totalAmount.toFixed(2) }}</span>
        </div>
      </div>

      <div class="qr-section">
        <div class="qr-wrapper">
          <div class="qr-code" v-if="!paid">
            <div class="qr-pattern">
              <div v-for="i in 25" :key="i" class="qr-row">
                <div
                  v-for="j in 25"
                  :key="j"
                  class="qr-cell"
                  :class="{ filled: isQrFilled(i, j) }"
                ></div>
              </div>
            </div>
            <div class="qr-logo">
              <el-icon :size="32" color="#07c160"><Wallet /></el-icon>
            </div>
          </div>
          <div v-else class="paid-success">
            <el-icon :size="64" color="#10b981"><CircleCheck /></el-icon>
            <p class="success-text">支付成功</p>
          </div>
        </div>

        <div class="pay-method">
          <el-icon :size="18" color="#07c160"><Wallet /></el-icon>
          <span>微信支付</span>
        </div>
      </div>

      <div v-if="!paid" class="countdown-section">
        <div class="countdown-text">
          <el-icon><Timer /></el-icon>
          <span>请在 </span>
          <span class="countdown-time">{{ formatCountdown }}</span>
          <span> 内完成支付</span>
        </div>
        <div class="countdown-bar">
          <div class="countdown-progress" :style="{ width: countdownPercent + '%' }"></div>
        </div>
      </div>

      <div v-if="!paid" class="mock-pay-section">
        <el-button
          type="success"
          size="large"
          :loading="paying"
          class="mock-pay-btn"
          @click="handleMockPay"
        >
          <el-icon><MagicStick /></el-icon>
          模拟支付成功
        </el-button>
        <p class="mock-tip">演示环境，点击按钮模拟支付成功</p>
      </div>

      <div v-if="paid" class="paid-detail">
        <div class="detail-item">
          <span class="label">订单号：</span>
          <span class="value">{{ order?.orderNo }}</span>
        </div>
        <div class="detail-item">
          <span class="label">充值金额：</span>
          <span class="value">¥{{ order?.amount.toFixed(2) }}</span>
        </div>
        <div class="detail-item">
          <span class="label">赠送金额：</span>
          <span class="value bonus">+¥{{ order?.bonusAmount.toFixed(2) }}</span>
        </div>
        <div class="detail-item total">
          <span class="label">实际到账：</span>
          <span class="value">¥{{ order?.totalAmount.toFixed(2) }}</span>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button v-if="!paid" @click="handleClose">取消支付</el-button>
      <el-button v-else type="primary" @click="handleClose">完成</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Wallet,
  CircleCheck,
  Timer,
  Present,
  MagicStick
} from '@element-plus/icons-vue'
import type { RechargeOrder } from '@/types'
import { rechargeApi } from '@/api'
import { useUserStore } from '@/store/user'

interface Props {
  modelValue: boolean
  order: RechargeOrder | null
}

const props = defineProps<Props>()
const emit = defineEmits(['update:modelValue', 'success'])

const userStore = useUserStore()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const paying = ref(false)
const paid = ref(false)
const countdown = ref(900)
const timer = ref<number | null>(null)

const formatCountdown = computed(() => {
  const minutes = Math.floor(countdown.value / 60)
  const seconds = countdown.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

const countdownPercent = computed(() => {
  return (countdown.value / 900) * 100
})

const qrPattern = Array.from({ length: 25 }, (_, i) =>
  Array.from({ length: 25 }, (_, j) => {
    const seed = (i * 7 + j * 13 + i * j) % 100
    return seed > 40
  })
)

function isQrFilled(i: number, j: number): boolean {
  return qrPattern[i - 1]?.[j - 1] ?? false
}

function startCountdown() {
  if (timer.value) {
    clearInterval(timer.value)
  }
  countdown.value = 900
  timer.value = window.setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (timer.value) {
        clearInterval(timer.value)
        timer.value = null
      }
      if (!paid.value) {
        ElMessage.warning('支付超时，订单已关闭')
        emit('update:modelValue', false)
      }
    }
  }, 1000)
}

async function handleMockPay() {
  if (!props.order || paid.value) return

  paying.value = true
  try {
    const res = await rechargeApi.mockPay(props.order.orderNo)
    const order = res.data.data as RechargeOrder

    paid.value = true
    await userStore.fetchUserInfo()

    emit('success', order)
    ElMessage.success('支付成功，余额已到账')
  } catch (e: any) {
    ElMessage.error(e.message || '支付失败')
  } finally {
    paying.value = false
  }
}

function handleClose() {
  if (timer.value) {
    clearInterval(timer.value)
    timer.value = null
  }
  emit('update:modelValue', false)
}

watch(
  () => props.modelValue,
  (val) => {
    if (val && props.order) {
      paid.value = props.order.status === 'PAID'
      if (!paid.value) {
        startCountdown()
      }
    }
  }
)

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value)
  }
})
</script>

<style lang="scss" scoped>
.payment-dialog {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 0;
}

.payment-info {
  width: 100%;
  text-align: center;
  margin-bottom: 20px;

  .order-info {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 8px;

    .amount {
      font-size: 28px;
      font-weight: 700;
      color: var(--text-white);
      margin-left: 8px;
    }
  }

  .bonus-info {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 6px;
    font-size: 13px;
    color: #10b981;
    background: rgba(16, 185, 129, 0.1);
    padding: 8px 16px;
    border-radius: 20px;
  }
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.qr-wrapper {
  width: 200px;
  height: 200px;
  padding: 10px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
}

.qr-code {
  width: 180px;
  height: 180px;
  position: relative;
}

.qr-pattern {
  width: 100%;
  height: 100%;
}

.qr-row {
  display: flex;
}

.qr-cell {
  flex: 1;
  aspect-ratio: 1;

  &.filled {
    background: #000;
  }
}

.qr-logo {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 48px;
  height: 48px;
  background: #fff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.paid-success {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;

  .success-text {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: #10b981;
  }
}

.pay-method {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: var(--text-secondary);
}

.countdown-section {
  width: 100%;
  margin-bottom: 20px;
}

.countdown-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 10px;

  .countdown-time {
    color: #ef4444;
    font-weight: 600;
    font-size: 16px;
  }

  .el-icon {
    color: #ef4444;
  }
}

.countdown-bar {
  width: 100%;
  height: 6px;
  background: var(--bg-hover);
  border-radius: 3px;
  overflow: hidden;
}

.countdown-progress {
  height: 100%;
  background: linear-gradient(90deg, #10b981, #34d399);
  border-radius: 3px;
  transition: width 1s linear;
}

.mock-pay-section {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.mock-pay-btn {
  width: 100%;
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  border: none;
  font-weight: 600;

  &:hover {
    background: linear-gradient(135deg, #10b981 0%, #07c160 100%);
  }
}

.mock-tip {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.paid-detail {
  width: 100%;
  background: var(--bg-hover);
  border-radius: 8px;
  padding: 16px;

  .detail-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 0;
    font-size: 13px;

    .label {
      color: var(--text-secondary);
    }

    .value {
      color: var(--text-primary);
      font-weight: 500;

      &.bonus {
        color: #10b981;
      }
    }

    &.total {
      border-top: 1px solid var(--border-color);
      margin-top: 8px;
      padding-top: 12px;

      .label {
        font-weight: 600;
        color: var(--text-primary);
      }

      .value {
        font-size: 18px;
        font-weight: 700;
        color: #f59e0b;
      }
    }
  }
}
</style>
