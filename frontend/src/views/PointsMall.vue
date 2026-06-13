<template>
  <div class="points-mall-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-icon :size="48" class="gift-icon"><Present /></el-icon>
          <div>
            <h1 class="page-title">积分商城</h1>
            <p class="page-subtitle">每日签到、消费、写评论赚积分，兑换好礼！</p>
          </div>
        </div>
        <div class="header-right">
          <div class="points-display">
            <el-icon :size="24" class="coin-icon"><GoldMedal /></el-icon>
            <span class="points-label">当前积分</span>
            <span class="points-value">{{ userPoints?.balance || 0 }}</span>
          </div>
          <router-link to="/points-logs">
            <el-button type="primary" :icon="List">积分明细</el-button>
          </router-link>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="sign-in-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Calendar /></el-icon>
            每日签到
          </h2>
          <div class="consecutive-days">
            <el-icon><TrendCharts /></el-icon>
            已连续签到 <span class="highlight">{{ signInStatus?.consecutiveDays || 0 }}</span> 天
          </div>
        </div>

        <div class="sign-in-calendar">
          <div class="calendar-header">
            <h3>{{ currentMonth }}</h3>
            <div class="sign-in-btn-wrapper">
              <el-button
                type="warning"
                size="large"
                :icon="signInStatus?.signedToday ? Select : Present"
                :loading="signingIn"
                :disabled="signInStatus?.signedToday"
                class="sign-in-btn"
                @click="handleSignIn"
              >
                {{ signInStatus?.signedToday ? '今日已签到' : '立即签到' }}
              </el-button>
            </div>
          </div>

          <div class="calendar-weekdays">
            <span v-for="day in weekdays" :key="day" class="weekday">{{ day }}</span>
          </div>

          <div class="calendar-days">
            <div
              v-for="(day, index) in calendarDays"
              :key="index"
              class="calendar-day"
              :class="{
                'is-today': day.isToday,
                'is-signed': day.isSigned,
                'is-empty': !day.day,
                'is-future': day.isFuture
              }"
            >
              <span class="day-number" v-if="day.day">{{ day.day }}</span>
              <el-icon v-if="day.isSigned" class="check-icon"><CircleCheck /></el-icon>
            </div>
          </div>

          <div class="sign-in-rules">
            <div class="rule-item">
              <span class="rule-icon">🎁</span>
              <span>每日签到 +10 积分</span>
            </div>
            <div class="rule-item">
              <span class="rule-icon">🔥</span>
              <span>连续 7 天额外奖励 +50 积分</span>
            </div>
            <div class="rule-item">
              <span class="rule-icon">💰</span>
              <span>订单消费每 1 元 +1 积分</span>
            </div>
            <div class="rule-item">
              <span class="rule-icon">📝</span>
              <span>发表评论 +20 积分</span>
            </div>
          </div>
        </div>
      </div>

      <div class="products-section">
        <div class="section-header">
          <h2 class="section-title">
            <el-icon><Shop /></el-icon>
            积分兑换
          </h2>
          <div class="tabs">
            <el-button
              :type="activeTab === 'all' ? 'primary' : 'default'"
              size="small"
              @click="activeTab = 'all'"
            >
              全部
            </el-button>
            <el-button
              :type="activeTab === 'COUPON' ? 'primary' : 'default'"
              size="small"
              @click="activeTab = 'COUPON'"
            >
              优惠券
            </el-button>
            <el-button
              :type="activeTab === 'VIRTUAL' ? 'primary' : 'default'"
              size="small"
              @click="activeTab = 'VIRTUAL'"
            >
              虚拟物品
            </el-button>
          </div>
        </div>

        <div v-loading="loading" class="products-grid">
          <div
            v-for="product in filteredProducts"
            :key="product.id"
            class="product-card"
            :class="{ 'out-of-stock': product.stock <= 0, 'not-enough': (userPoints?.balance || 0) < product.pointsRequired }"
          >
            <div class="product-image">
              <img v-lazy="product.image" :alt="product.name" />
              <div class="product-type-tag" :class="product.type.toLowerCase()">
                {{ product.type === 'COUPON' ? '优惠券' : '虚拟物品' }}
              </div>
              <div v-if="product.stock <= 0" class="sold-out-mask">
                <span>已兑完</span>
              </div>
            </div>

            <div class="product-info">
              <h3 class="product-name">{{ product.name }}</h3>
              <p class="product-desc">{{ product.description }}</p>

              <div class="product-stats">
                <div class="points-cost">
                  <el-icon :size="18"><GoldMedal /></el-icon>
                  <span class="cost-value">{{ product.pointsRequired }}</span>
                </div>
                <div class="stock-info">
                  库存: {{ product.stock }}
                </div>
              </div>

              <div class="product-footer">
                <div class="sold-count">已兑 {{ product.soldCount }} 件</div>
                <el-button
                  type="primary"
                  size="small"
                  :loading="exchangingMap[product.id]"
                  :disabled="product.stock <= 0 || exchangingMap[product.id]"
                  class="exchange-btn"
                  @click="handleExchange(product)"
                >
                  {{ (userPoints?.balance || 0) < product.pointsRequired ? '积分不足' : '立即兑换' }}
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="!loading && filteredProducts.length === 0" class="empty-state">
          <el-empty description="暂无商品" />
        </div>
      </div>
    </div>

    <el-dialog
      v-model="showExchangeDialog"
      title="兑换成功"
      width="420px"
      :close-on-click-modal="false"
    >
      <div v-if="exchangeResult" class="exchange-success">
        <div class="success-icon">
          <el-icon :size="64" color="#67c23a"><CircleCheck /></el-icon>
        </div>
        <h3>{{ exchangeResult.productName }}</h3>
        <p class="cost-info">消耗积分：<span>{{ exchangeResult.pointsSpent }}</span></p>
        <p class="remaining-info">剩余积分：<span>{{ exchangeResult.remainingPoints }}</span></p>
        <div v-if="exchangeResult.redeemCode" class="redeem-code">
          <p>兑换码：</p>
          <div class="code-display">
            <code>{{ exchangeResult.redeemCode }}</code>
            <el-button size="small" type="primary" @click="copyCode(exchangeResult.redeemCode!)">
              复制
            </el-button>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="showExchangeDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  GoldMedal,
  List,
  Calendar,
  Present,
  Select,
  CircleCheck,
  Shop,
  TrendCharts
} from '@element-plus/icons-vue'
import { pointApi } from '@/api'
import type { UserPoints, PointProduct, SignInResult, ExchangeResult } from '@/types'

const loading = ref(false)
const signingIn = ref(false)
const userPoints = ref<UserPoints | null>(null)
const signInStatus = ref<SignInResult | null>(null)
const products = ref<PointProduct[]>([])
const activeTab = ref<'all' | 'COUPON' | 'VIRTUAL'>('all')
const exchangingMap = ref<Record<number, boolean>>({})
const showExchangeDialog = ref(false)
const exchangeResult = ref<ExchangeResult | null>(null)

const weekdays = ['日', '一', '二', '三', '四', '五', '六']

const currentMonth = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}年${now.getMonth() + 1}月`
})

const calendarDays = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth()
  const today = now.getDate()

  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const daysInMonth = lastDay.getDate()
  const firstDayOfWeek = firstDay.getDay()

  const days: Array<{ day: number | null; isToday: boolean; isSigned: boolean; isFuture: boolean }> = []

  for (let i = 0; i < firstDayOfWeek; i++) {
    days.push({ day: null, isToday: false, isSigned: false, isFuture: false })
  }

  for (let day = 1; day <= daysInMonth; day++) {
    const isToday = day === today
    const isFuture = day > today
    const isSigned = !isFuture && !isToday && day <= today - 1 && signInStatus.value?.consecutiveDays
      ? (today - day) < (signInStatus.value.consecutiveDays || 0)
      : false

    days.push({
      day,
      isToday,
      isSigned: isToday ? !!signInStatus.value?.signedToday : isSigned,
      isFuture
    })
  }

  return days
})

const filteredProducts = computed(() => {
  if (activeTab.value === 'all') {
    return products.value
  }
  return products.value.filter(p => p.type === activeTab.value)
})

async function loadData() {
  loading.value = true
  try {
    const [pointsRes, statusRes, productsRes] = await Promise.all([
      pointApi.getPointInfo(),
      pointApi.getSignInStatus(),
      pointApi.getProducts()
    ])
    userPoints.value = pointsRes.data.data
    signInStatus.value = statusRes.data.data
    products.value = productsRes.data.data || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleSignIn() {
  if (signInStatus.value?.signedToday) return

  signingIn.value = true
  try {
    const res = await pointApi.signIn()
    const result = res.data.data as SignInResult
    signInStatus.value = result
    userPoints.value!.balance = result.totalPoints
    ElMessage.success({
      message: result.message,
      duration: 3000
    })
  } catch (e: any) {
    ElMessage.error(e.message || '签到失败')
  } finally {
    signingIn.value = false
  }
}

async function handleExchange(product: PointProduct) {
  const balance = userPoints.value?.balance || 0
  if (balance < product.pointsRequired) {
    ElMessage.warning('积分不足')
    return
  }
  if (product.stock <= 0) {
    ElMessage.warning('商品库存不足')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认消耗 ${product.pointsRequired} 积分兑换「${product.name}」？`,
      '确认兑换',
      {
        confirmButtonText: '确认兑换',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    exchangingMap.value[product.id] = true
    const res = await pointApi.exchangeProduct(product.id)
    const result = res.data.data as ExchangeResult

    exchangeResult.value = result
    userPoints.value!.balance = result.remainingPoints
    product.stock -= 1
    product.soldCount += 1
    showExchangeDialog.value = true

    ElMessage.success('兑换成功')
  } catch (e: any) {
    if (e !== 'cancel') {
      const msg = e?.response?.data?.message || e.message || '兑换失败'
      ElMessage.error(msg)
    }
  } finally {
    exchangingMap.value[product.id] = false
  }
}

function copyCode(code: string) {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('兑换码已复制')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.points-mall-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-dark);
  padding-bottom: 60px;
}

.page-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 40px 20px;
  border-bottom: 2px solid #f59e0b;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(245, 158, 11, 0.2) 0%, transparent 70%);
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

.gift-icon {
  color: #f59e0b;
  animation: giftBounce 2s ease-in-out infinite;
}

@keyframes giftBounce {
  0%, 100% { transform: translateY(0) rotate(-5deg); }
  50% { transform: translateY(-8px) rotate(5deg); }
}

.page-title {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  margin: 0;
  background: linear-gradient(90deg, #f59e0b 0%, #fbbf24 100%);
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
  gap: 20px;
}

.points-display {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(0, 0, 0, 0.3);
  padding: 12px 20px;
  border-radius: 12px;
  border: 1px solid rgba(245, 158, 11, 0.3);
}

.coin-icon {
  color: #f59e0b;
}

.points-label {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.points-value {
  color: #f59e0b;
  font-size: 28px;
  font-weight: 800;
  min-width: 60px;
  text-align: right;
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
    color: #f59e0b;
  }
}

.consecutive-days {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-secondary);
  font-size: 14px;

  .el-icon {
    color: #10b981;
  }

  .highlight {
    color: #f59e0b;
    font-weight: 700;
    font-size: 18px;
    margin: 0 4px;
  }
}

.sign-in-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border-color);
  padding: 24px;
  margin-bottom: 32px;
}

.calendar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);

  h3 {
    margin: 0;
    color: var(--text-white);
    font-size: 18px;
  }
}

.sign-in-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  border: none;
  font-weight: 700;
  min-width: 140px;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.4);
  }
}

.calendar-weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 8px;

  .weekday {
    text-align: center;
    color: var(--text-secondary);
    font-size: 14px;
    font-weight: 500;
    padding: 8px 0;
  }
}

.calendar-days {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 8px;
  margin-bottom: 20px;
}

.calendar-day {
  aspect-ratio: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: var(--bg-hover);
  position: relative;
  transition: all 0.2s;

  &.is-today {
    background: rgba(245, 158, 11, 0.15);
    border: 2px solid #f59e0b;
  }

  &.is-signed {
    background: rgba(16, 185, 129, 0.15);

    .day-number {
      color: #10b981;
    }
  }

  &.is-future {
    opacity: 0.4;
  }

  &.is-empty {
    background: transparent;
  }

  &:not(.is-empty):not(.is-future):hover {
    transform: scale(1.05);
  }

  .day-number {
    color: var(--text-primary);
    font-weight: 500;
  }

  .check-icon {
    position: absolute;
    bottom: 4px;
    right: 4px;
    color: #10b981;
    font-size: 16px;
  }

  &.is-today .day-number {
    color: #f59e0b;
    font-weight: 700;
  }
}

.sign-in-rules {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 12px;
  padding: 16px;
  background: rgba(245, 158, 11, 0.05);
  border-radius: var(--radius-md);
  border: 1px solid rgba(245, 158, 11, 0.2);
}

.rule-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-primary);
  font-size: 13px;

  .rule-icon {
    font-size: 18px;
  }
}

.tabs {
  display: flex;
  gap: 8px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 20px;
}

.product-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  overflow: hidden;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;

  &:hover:not(.out-of-stock) {
    transform: translateY(-4px);
    border-color: #f59e0b;
    box-shadow: 0 8px 24px rgba(245, 158, 11, 0.15);
  }

  &.out-of-stock {
    opacity: 0.6;
  }

  &.not-enough .exchange-btn {
    background: #666;
    border-color: #666;
  }
}

.product-image {
  position: relative;
  aspect-ratio: 4 / 3;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }

  &:hover img {
    transform: scale(1.1);
  }

  .product-type-tag {
    position: absolute;
    top: 12px;
    left: 12px;
    padding: 4px 10px;
    border-radius: 4px;
    font-size: 12px;
    font-weight: 600;
    color: #fff;

    &.coupon {
      background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
    }

    &.virtual {
      background: linear-gradient(135deg, #8b5cf6 0%, #a78bfa 100%);
    }
  }

  .sold-out-mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.6);
    display: flex;
    align-items: center;
    justify-content: center;

    span {
      color: #fff;
      font-size: 24px;
      font-weight: 700;
      transform: rotate(-15deg);
    }
  }
}

.product-info {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-white);
  margin: 0 0 8px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 12px 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 39px;
}

.product-stats {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  padding: 10px;
  background: var(--bg-hover);
  border-radius: var(--radius-sm);
}

.points-cost {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e0b;
  font-weight: 700;

  .el-icon {
    color: #f59e0b;
  }

  .cost-value {
    font-size: 20px;
  }
}

.stock-info {
  font-size: 12px;
  color: var(--text-secondary);
}

.product-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
}

.sold-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.exchange-btn {
  background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  border: none;
  font-weight: 600;

  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  }
}

.empty-state {
  padding: 60px 20px;
}

.exchange-success {
  text-align: center;
  padding: 20px 0;

  .success-icon {
    margin-bottom: 16px;
  }

  h3 {
    color: var(--text-white);
    margin: 0 0 12px 0;
    font-size: 18px;
  }

  p {
    color: var(--text-secondary);
    margin: 8px 0;
    font-size: 14px;

    span {
      color: #f59e0b;
      font-weight: 700;
    }
  }
}

.redeem-code {
  margin-top: 20px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);

  p {
    margin: 0 0 8px 0;
    color: var(--text-primary);
    font-weight: 500;
  }

  .code-display {
    display: flex;
    align-items: center;
    gap: 12px;
    justify-content: center;

    code {
      background: var(--bg-dark);
      padding: 10px 20px;
      border-radius: 6px;
      font-family: 'Courier New', monospace;
      font-size: 16px;
      color: #10b981;
      font-weight: 700;
      letter-spacing: 2px;
    }
  }
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
    justify-content: space-between;
  }
  .points-value { font-size: 24px; }
  .products-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }
  .product-name { font-size: 14px; }
  .sign-in-rules {
    grid-template-columns: 1fr 1fr;
  }
}
</style>
