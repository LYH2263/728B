<template>
  <div class="flash-sale-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-icon :size="48" class="flash-icon"><Lightning /></el-icon>
          <div>
            <h1 class="page-title">限时秒杀</h1>
            <p class="page-subtitle">超值低价，限时抢购，手慢无！</p>
          </div>
        </div>
        <div class="header-right">
          <el-button type="warning" :icon="RefreshRight" @click="loadData" :loading="loading">
            刷新
          </el-button>
        </div>
      </div>
    </div>

    <div class="section" v-if="ongoingSales.length">
      <div class="section-header">
        <h2 class="section-title">
          <span class="dot ongoing"></span>
          正在疯抢
          <span class="live-badge">LIVE</span>
        </h2>
      </div>
      <div class="sale-grid">
        <div
          v-for="item in ongoingSales"
          :key="item.id"
          class="sale-card"
          :class="{ disabled: isSoldOut(item) }"
        >
          <div class="card-top" @click="goToDetail(item.gameId)">
            <img
              v-lazy="item.game?.coverImage"
              :alt="item.game?.title"
              class="game-cover"
            />
            <div class="flash-badge">
              <el-icon><Lightning /></el-icon>
              秒杀
            </div>
            <div class="end-countdown" v-if="getEndCountdown(item)">
              <span class="label">距结束</span>
              <span class="time">{{ getEndCountdown(item) }}</span>
            </div>
          </div>

          <div class="card-body">
            <h3 class="game-title" @click="goToDetail(item.gameId)">
              {{ item.game?.title }}
            </h3>

            <div class="price-section">
              <div class="flash-price">
                <span class="currency">¥</span>
                <span class="amount">{{ item.flashPrice }}</span>
              </div>
              <div class="original-price">¥{{ item.game?.originalPrice }}</div>
              <div class="discount-tag">
                -{{ getDiscountPercent(item) }}%
              </div>
            </div>

            <div class="stock-section">
              <div class="stock-header">
                <span>剩余库存</span>
                <span class="stock-text">
                  {{ getRemainingStock(item) }} / {{ item.stockCount }}
                </span>
              </div>
              <el-progress
                :percentage="getSoldPercent(item)"
                :stroke-width="8"
                :show-text="false"
                color="#ff4757"
              />
              <div class="sold-hint">
                已抢 {{ item.soldCount }} 件
              </div>
            </div>

            <div class="limit-hint" v-if="item.perUserLimit > 1">
              <el-icon><InfoFilled /></el-icon>
              每人限购 {{ item.perUserLimit }} 件
            </div>
            <div class="limit-hint" v-else>
              <el-icon><InfoFilled /></el-icon>
              每人限购 1 件
            </div>
          </div>

          <div class="card-footer">
            <el-button
              type="danger"
              size="large"
              class="buy-btn"
              :icon="Lightning"
              :loading="buyingMap[item.id]"
              :disabled="isSoldOut(item) || buyingMap[item.id]"
              @click="handleBuy(item)"
            >
              {{ isSoldOut(item) ? '已售罄' : '立即抢购' }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="section" v-if="upcomingSales.length">
      <div class="section-header">
        <h2 class="section-title">
          <span class="dot upcoming"></span>
          即将开始
        </h2>
      </div>
      <div class="sale-grid">
        <div
          v-for="item in upcomingSales"
          :key="item.id"
          class="sale-card upcoming-card"
        >
          <div class="card-top" @click="goToDetail(item.gameId)">
            <img
              v-lazy="item.game?.coverImage"
              :alt="item.game?.title"
              class="game-cover"
            />
            <div class="flash-badge upcoming">
              <el-icon><Clock /></el-icon>
              即将开始
            </div>
            <div class="mask" />
          </div>

          <div class="card-body">
            <h3 class="game-title" @click="goToDetail(item.gameId)">
              {{ item.game?.title }}
            </h3>

            <div class="price-section">
              <div class="flash-price">
                <span class="currency">¥</span>
                <span class="amount">{{ item.flashPrice }}</span>
              </div>
              <div class="original-price">¥{{ item.game?.originalPrice }}</div>
              <div class="discount-tag">
                -{{ getDiscountPercent(item) }}%
              </div>
            </div>

            <div class="countdown-section">
              <div class="countdown-label">距离开抢</div>
              <div class="countdown-timer">
                <div class="time-block">
                  <span class="num">{{ getStartCountdown(item).d }}</span>
                  <span class="unit">天</span>
                </div>
                <div class="time-block">
                  <span class="num">{{ getStartCountdown(item).h }}</span>
                  <span class="unit">时</span>
                </div>
                <div class="time-block">
                  <span class="num">{{ getStartCountdown(item).m }}</span>
                  <span class="unit">分</span>
                </div>
                <div class="time-block">
                  <span class="num">{{ getStartCountdown(item).s }}</span>
                  <span class="unit">秒</span>
                </div>
              </div>
            </div>

            <div class="limit-hint">
              <el-icon><InfoFilled /></el-icon>
              每人限购 {{ item.perUserLimit }} 件
            </div>
          </div>

          <div class="card-footer">
            <el-button
              type="warning"
              size="large"
              class="buy-btn"
              disabled
            >
              <el-icon><Clock /></el-icon>
              等待开抢
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="section" v-if="endedSales.length">
      <div class="section-header">
        <h2 class="section-title">
          <span class="dot ended"></span>
          已结束
        </h2>
      </div>
      <div class="sale-grid ended-grid">
        <div
          v-for="item in endedSales"
          :key="item.id"
          class="sale-card ended-card"
        >
          <div class="card-top" @click="goToDetail(item.gameId)">
            <img
              v-lazy="item.game?.coverImage"
              :alt="item.game?.title"
              class="game-cover"
            />
            <div class="flash-badge ended">
              已结束
            </div>
            <div class="mask" />
          </div>

          <div class="card-body">
            <h3 class="game-title" @click="goToDetail(item.gameId)">
              {{ item.game?.title }}
            </h3>

            <div class="price-section">
              <div class="flash-price ended-price">
                <span class="currency">¥</span>
                <span class="amount">{{ item.flashPrice }}</span>
              </div>
              <div class="original-price">¥{{ item.game?.originalPrice }}</div>
            </div>

            <div class="stock-section ended-stock">
              <span>本次共 {{ item.stockCount }} 件，已抢 {{ item.soldCount }} 件</span>
            </div>
          </div>

          <div class="card-footer">
            <el-button
              size="large"
              class="buy-btn"
              disabled
            >
              活动已结束
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-if="!loading && allSales.length === 0">
      <el-empty description="暂无秒杀活动">
        <el-button type="primary" @click="goToStore">去商店逛逛</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Lightning,
  RefreshRight,
  InfoFilled,
  Clock
} from '@element-plus/icons-vue'
import { flashSaleApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { FlashSale, FlashSaleStatus, Order } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const allSales = ref<FlashSale[]>([])
const buyingMap = ref<Record<number, boolean>>({})
const tick = ref(0)
let timer: number | null = null

const ongoingSales = computed(() => {
  tick.value
  const now = Date.now()
  return allSales.value.filter(item => {
    const status = getLiveStatus(item, now)
    return status === 'ONGOING' && item.soldCount < item.stockCount
  })
})

const upcomingSales = computed(() => {
  tick.value
  const now = Date.now()
  return allSales.value.filter(item => {
    const status = getLiveStatus(item, now)
    return status === 'NOT_STARTED'
  })
})

const endedSales = computed(() => {
  tick.value
  const now = Date.now()
  return allSales.value.filter(item => {
    const status = getLiveStatus(item, now)
    return status === 'ENDED' || (status === 'ONGOING' && item.soldCount >= item.stockCount)
  })
})

function getLiveStatus(item: FlashSale, nowTs: number): FlashSaleStatus {
  const start = new Date(item.startTime).getTime()
  const end = new Date(item.endTime).getTime()
  if (item.soldCount >= item.stockCount) return 'SOLD_OUT'
  if (nowTs < start) return 'NOT_STARTED'
  if (nowTs > end) return 'ENDED'
  return 'ONGOING'
}

function isSoldOut(item: FlashSale): boolean {
  tick.value
  const now = Date.now()
  const end = new Date(item.endTime).getTime()
  return item.soldCount >= item.stockCount || now > end
}

function getRemainingStock(item: FlashSale): number {
  return Math.max(0, item.stockCount - item.soldCount)
}

function getSoldPercent(item: FlashSale): number {
  if (!item.stockCount) return 0
  return Math.min(100, Math.round((item.soldCount / item.stockCount) * 100))
}

function getDiscountPercent(item: FlashSale): number {
  const original = item.game?.originalPrice ?? 0
  if (!original) return 0
  return Math.round((1 - item.flashPrice / original) * 100)
}

function getStartCountdown(item: FlashSale) {
  tick.value
  const now = Date.now()
  const start = new Date(item.startTime).getTime()
  let diff = Math.max(0, start - now)
  const d = Math.floor(diff / 86400000); diff -= d * 86400000
  const h = Math.floor(diff / 3600000); diff -= h * 3600000
  const m = Math.floor(diff / 60000); diff -= m * 60000
  const s = Math.floor(diff / 1000)
  return {
    d: String(d).padStart(2, '0'),
    h: String(h).padStart(2, '0'),
    m: String(m).padStart(2, '0'),
    s: String(s).padStart(2, '0')
  }
}

function getEndCountdown(item: FlashSale): string {
  tick.value
  const now = Date.now()
  const end = new Date(item.endTime).getTime()
  let diff = Math.max(0, end - now)
  if (diff <= 0) return ''
  const d = Math.floor(diff / 86400000); diff -= d * 86400000
  const h = Math.floor(diff / 3600000); diff -= h * 3600000
  const m = Math.floor(diff / 60000); diff -= m * 60000
  const s = Math.floor(diff / 1000)
  const parts: string[] = []
  if (d > 0) parts.push(`${d}天`)
  parts.push(`${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`)
  return parts.join(' ')
}

async function loadData() {
  loading.value = true
  try {
    const res = await flashSaleApi.getFlashSales()
    allSales.value = (res.data.data as FlashSale[]) || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function handleBuy(item: FlashSale) {
  if (!userStore.isLoggedIn) {
    ElMessageBox.confirm('请先登录后参与秒杀', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      router.push({ path: '/login', query: { redirect: '/flash-sale' } })
    }).catch(() => {})
    return
  }

  if (buyingMap.value[item.id]) return
  buyingMap.value[item.id] = true

  try {
    const res = await flashSaleApi.createFlashSaleOrder(item.id)
    const order = res.data.data as Order
    ElMessage.success({
      message: `抢购成功！订单号：${order.orderNo}`,
      duration: 3000
    })
    item.soldCount += 1
    setTimeout(() => {
      router.push(`/orders`)
    }, 1500)
  } catch (e: any) {
    const msg = e?.response?.data?.message || e.message || '抢购失败'
    ElMessage.error(msg)
    if (msg.includes('已抢光') || msg.includes('售罄') || msg.includes('库存')) {
      loadData()
    }
  } finally {
    buyingMap.value[item.id] = false
  }
}

function goToDetail(gameId: number) {
  router.push(`/game/${gameId}`)
}

function goToStore() {
  router.push('/store')
}

onMounted(() => {
  loadData()
  timer = window.setInterval(() => {
    tick.value++
  }, 1000)
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<style lang="scss" scoped>
.flash-sale-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-dark);
  padding-bottom: 60px;
}

.page-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 40px 20px;
  border-bottom: 2px solid var(--steam-blue);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(255, 71, 87, 0.2) 0%, transparent 70%);
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

.flash-icon {
  color: #ffd32a;
  animation: flashPulse 1.5s ease-in-out infinite;
}

@keyframes flashPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.7; transform: scale(1.1); }
}

.page-title {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  margin: 0;
  background: linear-gradient(90deg, #ff4757 0%, #ffd32a 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 0 0 0;
}

.section {
  max-width: 1400px;
  margin: 0 auto;
  padding: 32px 20px 0;
}

.section-header {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-white);
  margin: 0;
  display: flex;
  align-items: center;
  gap: 10px;

  .dot {
    width: 10px;
    height: 10px;
    border-radius: 50%;
    display: inline-block;

    &.ongoing {
      background: #ff4757;
      box-shadow: 0 0 12px #ff4757;
      animation: dotBlink 1.2s ease-in-out infinite;
    }
    &.upcoming { background: #ffd32a; }
    &.ended { background: #888; }
  }

  .live-badge {
    font-size: 11px;
    background: #ff4757;
    color: #fff;
    padding: 2px 8px;
    border-radius: 4px;
    font-weight: 700;
    letter-spacing: 1px;
    animation: dotBlink 1.2s ease-in-out infinite;
  }
}

@keyframes dotBlink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.4; }
}

.sale-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.ended-grid {
  opacity: 0.7;
}

.sale-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  transition: all 0.3s ease;

  &:hover:not(.disabled):not(.upcoming-card):not(.ended-card) {
    transform: translateY(-4px);
    border-color: #ff4757;
    box-shadow: 0 8px 24px rgba(255, 71, 87, 0.2);
  }

  &.disabled {
    opacity: 0.7;
  }
}

.card-top {
  position: relative;
  aspect-ratio: 460 / 215;
  overflow: hidden;
  cursor: pointer;

  .game-cover {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }

  &:hover .game-cover {
    transform: scale(1.08);
  }

  .mask {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.55);
  }
}

.flash-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  padding: 4px 10px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(255, 71, 87, 0.4);

  &.upcoming {
    background: linear-gradient(135deg, #ffd32a 0%, #ffa502 100%);
    color: #2f3542;
  }

  &.ended {
    background: #666;
    box-shadow: none;
  }
}

.end-countdown {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(0, 0, 0, 0.75);
  padding: 6px 10px;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;

  .label {
    font-size: 10px;
    color: rgba(255, 255, 255, 0.6);
  }

  .time {
    font-size: 13px;
    font-weight: 700;
    color: #ff4757;
    font-family: 'Courier New', monospace;
  }
}

.card-body {
  padding: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.game-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-white);
  margin: 0;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.2s;

  &:hover {
    color: var(--steam-light-blue);
  }
}

.price-section {
  display: flex;
  align-items: baseline;
  gap: 10px;
  flex-wrap: wrap;
}

.flash-price {
  color: #ff4757;
  font-weight: 800;
  display: flex;
  align-items: baseline;

  .currency {
    font-size: 16px;
  }

  .amount {
    font-size: 30px;
    line-height: 1;
  }

  &.ended-price {
    color: #888;
  }
}

.original-price {
  color: var(--text-secondary);
  text-decoration: line-through;
  font-size: 14px;
}

.discount-tag {
  background: #ff4757;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  padding: 2px 8px;
  border-radius: 4px;
}

.stock-section {
  display: flex;
  flex-direction: column;
  gap: 6px;

  .stock-header {
    display: flex;
    justify-content: space-between;
    font-size: 12px;
    color: var(--text-secondary);
  }

  .stock-text {
    color: var(--text-primary);
    font-weight: 500;
  }

  .sold-hint {
    font-size: 11px;
    color: #ff4757;
    text-align: right;
  }

  &.ended-stock {
    font-size: 12px;
    color: var(--text-secondary);
    padding: 8px;
    background: var(--bg-hover);
    border-radius: var(--radius-sm);
    text-align: center;
  }
}

.countdown-section {
  background: var(--bg-hover);
  border-radius: var(--radius-sm);
  padding: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.countdown-label {
  font-size: 12px;
  color: var(--text-secondary);
}

.countdown-timer {
  display: flex;
  gap: 8px;
}

.time-block {
  display: flex;
  align-items: baseline;
  gap: 2px;
  background: var(--bg-dark);
  padding: 6px 8px;
  border-radius: 6px;

  .num {
    font-size: 22px;
    font-weight: 800;
    color: #ffd32a;
    font-family: 'Courier New', monospace;
    min-width: 28px;
    text-align: center;
  }

  .unit {
    font-size: 11px;
    color: var(--text-secondary);
  }
}

.limit-hint {
  font-size: 12px;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  background: var(--bg-hover);
  border-radius: var(--radius-sm);
}

.card-footer {
  padding: 0 16px 16px;
}

.buy-btn {
  width: 100%;
  font-weight: 700;
  font-size: 16px;

  &.el-button--danger {
    background: linear-gradient(135deg, #ff4757 0%, #ff6b81 100%);
    border: none;

    &:hover {
      background: linear-gradient(135deg, #ff6b81 0%, #ff4757 100%);
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(255, 71, 87, 0.4);
    }
  }
}

.empty-state {
  max-width: 1400px;
  margin: 0 auto;
  padding: 80px 20px;
}

@media (max-width: 768px) {
  .page-title { font-size: 26px; }
  .page-subtitle { font-size: 12px; }
  .sale-grid { grid-template-columns: 1fr; }
  .flash-price .amount { font-size: 24px; }
  .time-block .num { font-size: 18px; }
  .section-title { font-size: 18px; }
}
</style>
