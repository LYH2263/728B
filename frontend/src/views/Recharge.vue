<template>
  <div class="recharge-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-icon :size="48" class="wallet-icon"><WalletFilled /></el-icon>
          <div>
            <h1 class="page-title">充值中心</h1>
            <p class="page-subtitle">选择套餐充值，享更多优惠</p>
          </div>
        </div>
        <div class="header-right">
          <div class="balance-card">
            <div class="balance-label">账户余额</div>
            <div class="balance-value">
              <span class="currency">¥</span>
              <span class="amount">{{ userStore.userInfo?.balance?.toFixed(2) || '0.00' }}</span>
            </div>
            <router-link to="/profile" class="balance-detail">
              查看详情
              <el-icon><ArrowRight /></el-icon>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="plans-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Discount /></el-icon>
            充值套餐
          </h2>
          <span class="section-tip">多充多送，优惠多多</span>
        </div>

        <div v-loading="loading" class="plans-grid">
          <div
            v-for="plan in plans"
            :key="plan.id"
            class="plan-card"
            :class="{ selected: selectedPlan?.id === plan.id }"
            @click="selectPlan(plan)"
          >
            <div v-if="plan.tag" class="plan-tag" :style="{ background: plan.tagColor || '#f59e0b' }">
              {{ plan.tag }}
            </div>

            <div class="plan-header">
              <h3 class="plan-name">{{ plan.name }}</h3>
              <p class="plan-desc">{{ plan.description }}</p>
            </div>

            <div class="plan-price">
              <div class="original-price">
                <span class="currency">¥</span>
                <span class="amount">{{ plan.amount.toFixed(2) }}</span>
              </div>
              <div v-if="plan.bonusAmount > 0" class="bonus-amount">
                <el-icon><Plus /></el-icon>
                赠送 ¥{{ plan.bonusAmount.toFixed(2) }}
              </div>
            </div>

            <div class="plan-total">
              <span>实际到账：</span>
              <span class="total-amount">¥{{ plan.amount + plan.bonusAmount }}</span>
            </div>

            <div class="plan-footer">
              <el-button
                type="primary"
                :icon="ShoppingCart"
                class="recharge-btn"
                :loading="creatingOrder && selectedPlan?.id === plan.id"
                @click.stop="handleRecharge(plan)"
              >
                立即充值
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="!loading && plans.length === 0" class="empty-state">
          <el-empty description="暂无充值套餐" />
        </div>
      </div>

      <div class="records-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Notebook /></el-icon>
            充值记录
          </h2>
          <el-button size="small" @click="loadOrders">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>

        <div v-loading="ordersLoading" class="orders-list">
          <div v-if="orders.length === 0" class="empty-orders">
            <el-empty description="暂无充值记录" />
          </div>
          <div
            v-for="order in orders"
            :key="order.id"
            class="order-item"
          >
            <div class="order-left">
              <div class="order-icon" :class="order.status.toLowerCase()">
                <el-icon>
                  <Wallet v-if="order.status === 'PAID'" />
                  <Clock v-else-if="order.status === 'PENDING'" />
                  <CircleClose v-else />
                </el-icon>
              </div>
              <div class="order-info">
                <div class="order-plan">{{ order.planName }}</div>
                <div class="order-time">{{ formatDate(order.createdAt) }}</div>
                <div class="order-no">订单号：{{ order.orderNo }}</div>
              </div>
            </div>
            <div class="order-right">
              <div class="order-amount">
                +¥{{ order.totalAmount.toFixed(2) }}
              </div>
              <div class="order-status" :class="order.status.toLowerCase()">
                {{ getStatusText(order.status) }}
              </div>
            </div>
          </div>
        </div>

        <div v-if="orders.length > 0" class="load-more">
          <el-button text @click="loadMoreOrders" :loading="loadingMore">
            加载更多
          </el-button>
        </div>
      </div>
    </div>

    <PaymentDialog
      v-model="showPaymentDialog"
      :order="currentOrder"
      @success="handlePaySuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  WalletFilled,
  Wallet,
  ArrowRight,
  Discount,
  Plus,
  ShoppingCart,
  Notebook,
  Refresh,
  Clock,
  CircleClose
} from '@element-plus/icons-vue'
import { rechargeApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { RechargePlan, RechargeOrder } from '@/types'
import PaymentDialog from '@/components/PaymentDialog.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const creatingOrder = ref(false)
const ordersLoading = ref(false)
const loadingMore = ref(false)
const plans = ref<RechargePlan[]>([])
const selectedPlan = ref<RechargePlan | null>(null)
const orders = ref<RechargeOrder[]>([])
const currentOrder = ref<RechargeOrder | null>(null)
const showPaymentDialog = ref(false)
const ordersPage = ref(1)
const hasMoreOrders = ref(true)

const statusText: Record<string, string> = {
  PENDING: '待支付',
  PAID: '已支付',
  CANCELLED: '已取消',
  TIMEOUT: '已超时'
}

function getStatusText(status: string): string {
  return statusText[status] || status
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function loadPlans() {
  loading.value = true
  try {
    const res = await rechargeApi.getPlans()
    plans.value = res.data.data || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载套餐失败')
  } finally {
    loading.value = false
  }
}

async function loadOrders() {
  ordersPage.value = 1
  hasMoreOrders.value = true
  ordersLoading.value = true
  try {
    const res = await rechargeApi.getOrders(1, 10)
    orders.value = res.data.data?.list || []
    hasMoreOrders.value = orders.value.length < (res.data.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e.message || '加载记录失败')
  } finally {
    ordersLoading.value = false
  }
}

async function loadMoreOrders() {
  if (!hasMoreOrders.value || loadingMore.value) return

  loadingMore.value = true
  try {
    const nextPage = ordersPage.value + 1
    const res = await rechargeApi.getOrders(nextPage, 10)
    const newOrders = res.data.data?.list || []
    orders.value = [...orders.value, ...newOrders]
    ordersPage.value = nextPage
    hasMoreOrders.value = orders.value.length < (res.data.data?.total || 0)
  } catch (e: any) {
    ElMessage.error(e.message || '加载更多失败')
  } finally {
    loadingMore.value = false
  }
}

function selectPlan(plan: RechargePlan) {
  selectedPlan.value = plan
}

async function handleRecharge(plan: RechargePlan) {
  if (!userStore.isLoggedIn) {
    router.push({ name: 'Login', query: { redirect: '/recharge' } })
    return
  }

  selectedPlan.value = plan
  creatingOrder.value = true
  try {
    const res = await rechargeApi.createOrder(plan.id)
    currentOrder.value = res.data.data
    showPaymentDialog.value = true
  } catch (e: any) {
    ElMessage.error(e.message || '创建订单失败')
  } finally {
    creatingOrder.value = false
  }
}

function handlePaySuccess(order: RechargeOrder) {
  currentOrder.value = order
  loadOrders()
}

onMounted(() => {
  loadPlans()
  loadOrders()
  userStore.fetchUserInfo()
})
</script>

<style lang="scss" scoped>
.recharge-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-dark);
  padding-bottom: 60px;
}

.page-header {
  background: linear-gradient(135deg, #1e3a5f 0%, #0f3460 50%, #16213e 100%);
  padding: 40px 20px;
  border-bottom: 2px solid #3b82f6;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(59, 130, 246, 0.2) 0%, transparent 70%);
    border-radius: 50%;
  }
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  z-index: 1;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.wallet-icon {
  color: #3b82f6;
  animation: walletBounce 2s ease-in-out infinite;
}

@keyframes walletBounce {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-6px) rotate(3deg); }
}

.page-title {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  margin: 0;
  background: linear-gradient(90deg, #3b82f6 0%, #60a5fa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 0 0 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.balance-card {
  background: rgba(0, 0, 0, 0.3);
  padding: 20px 32px;
  border-radius: 16px;
  border: 1px solid rgba(59, 130, 246, 0.3);
  text-align: center;
  backdrop-filter: blur(10px);
}

.balance-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 13px;
  margin-bottom: 6px;
}

.balance-value {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;

  .currency {
    color: #3b82f6;
    font-size: 18px;
    font-weight: 600;
  }

  .amount {
    color: #fff;
    font-size: 32px;
    font-weight: 800;
  }
}

.balance-detail {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 10px;
  font-size: 12px;
  color: #60a5fa;
  text-decoration: none;

  &:hover {
    color: #93c5fd;
  }
}

.main-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 20px;
}

.section {
  margin-bottom: 40px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-white);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;

  .el-icon {
    color: #3b82f6;
  }
}

.section-tip {
  font-size: 13px;
  color: var(--text-secondary);
}

.plans-section {
  margin-bottom: 40px;
}

.plans-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.plan-card {
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-4px);
    border-color: #3b82f6;
    box-shadow: 0 8px 24px rgba(59, 130, 246, 0.2);
  }

  &.selected {
    border-color: #3b82f6;
    background: rgba(59, 130, 246, 0.05);
  }
}

.plan-tag {
  position: absolute;
  top: -1px;
  right: -1px;
  padding: 4px 12px;
  border-radius: 0 var(--radius-lg) 0 8px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.plan-header {
  text-align: center;
  margin-bottom: 16px;
}

.plan-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-white);
  margin: 0 0 6px 0;
}

.plan-desc {
  font-size: 12px;
  color: var(--text-secondary);
  margin: 0;
}

.plan-price {
  text-align: center;
  margin-bottom: 12px;
  padding: 16px 0;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
}

.original-price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;

  .currency {
    color: #f59e0b;
    font-size: 18px;
    font-weight: 600;
  }

  .amount {
    color: #f59e0b;
    font-size: 36px;
    font-weight: 800;
    line-height: 1;
  }
}

.bonus-amount {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-top: 8px;
  font-size: 13px;
  color: #10b981;
  font-weight: 600;

  .el-icon {
    font-size: 14px;
  }
}

.plan-total {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  margin-bottom: 16px;
  font-size: 13px;
  color: var(--text-secondary);

  .total-amount {
    color: var(--text-white);
    font-weight: 700;
    font-size: 15px;
  }
}

.plan-footer {
  margin-top: auto;
}

.recharge-btn {
  width: 100%;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  font-weight: 600;

  &:hover {
    background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
  }
}

.records-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 24px;
}

.orders-list {
  min-height: 200px;
}

.empty-orders {
  padding: 40px 0;
}

.order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-radius: var(--radius-md);
  transition: background 0.2s;

  &:hover {
    background: var(--bg-hover);
  }

  & + & {
    border-top: 1px solid var(--border-color);
  }
}

.order-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.order-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;

  &.paid {
    background: rgba(16, 185, 129, 0.15);
    color: #10b981;
  }

  &.pending {
    background: rgba(245, 158, 11, 0.15);
    color: #f59e0b;
  }

  &.cancelled, &.timeout {
    background: rgba(107, 114, 128, 0.15);
    color: #6b7280;
  }
}

.order-info {
  .order-plan {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-white);
    margin-bottom: 4px;
  }

  .order-time {
    font-size: 12px;
    color: var(--text-secondary);
    margin-bottom: 2px;
  }

  .order-no {
    font-size: 11px;
    color: var(--text-secondary);
    opacity: 0.7;
  }
}

.order-right {
  text-align: right;
}

.order-amount {
  font-size: 18px;
  font-weight: 700;
  color: #10b981;
  margin-bottom: 4px;
}

.order-status {
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  display: inline-block;

  &.paid {
    background: rgba(16, 185, 129, 0.15);
    color: #10b981;
  }

  &.pending {
    background: rgba(245, 158, 11, 0.15);
    color: #f59e0b;
  }

  &.cancelled, &.timeout {
    background: rgba(107, 114, 128, 0.15);
    color: #6b7280;
  }
}

.load-more {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.empty-state {
  padding: 60px 20px;
}

@media (max-width: 768px) {
  .page-title { font-size: 26px; }
  .page-subtitle { font-size: 12px; }
  .header-content {
    flex-direction: column;
    gap: 20px;
    align-items: flex-start;
  }
  .header-right {
    width: 100%;
  }
  .balance-card {
    width: 100%;
  }
  .plans-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }
  .plan-name { font-size: 14px; }
  .original-price .amount { font-size: 28px; }
  .plan-card {
    padding: 16px 12px;
  }
  .records-section {
    padding: 16px;
  }
  .section-title { font-size: 18px; }
}
</style>
