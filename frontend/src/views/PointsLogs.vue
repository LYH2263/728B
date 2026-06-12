<template>
  <div class="points-logs-page">
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <el-icon :size="48" class="history-icon"><History /></el-icon>
          <div>
            <h1 class="page-title">积分明细</h1>
            <p class="page-subtitle">查看您的积分获取与消耗记录</p>
          </div>
        </div>
        <div class="header-right">
          <router-link to="/points-mall">
            <el-button type="primary" :icon="Shop">积分商城</el-button>
          </router-link>
        </div>
      </div>
    </div>

    <div class="main-content">
      <div class="stats-cards">
        <div class="stat-card">
          <div class="stat-icon balance">
            <el-icon :size="32"><Wallet /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">当前积分</span>
            <span class="stat-value">{{ userPoints?.balance || 0 }}</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon earned">
            <el-icon :size="32"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">累计获得</span>
            <span class="stat-value positive">+{{ userPoints?.totalEarned || 0 }}</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon spent">
            <el-icon :size="32"><ShoppingBag /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">累计消耗</span>
            <span class="stat-value negative">-{{ userPoints?.totalSpent || 0 }}</span>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon consecutive">
            <el-icon :size="32"><Calendar /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-label">连续签到</span>
            <span class="stat-value">{{ userPoints?.consecutiveDays || 0 }} 天</span>
          </div>
        </div>
      </div>

      <div class="tabs-section">
        <el-tabs v-model="activeTab" class="custom-tabs">
          <el-tab-pane label="积分流水" name="logs">
            <div v-loading="logsLoading" class="logs-container">
              <div v-if="pointLogs.length" class="logs-list">
                <div
                  v-for="log in pointLogs"
                  :key="log.id"
                  class="log-item"
                >
                  <div class="log-icon" :class="log.type.toLowerCase()">
                    <el-icon v-if="log.type === 'EARN'"><ArrowUp /></el-icon>
                    <el-icon v-else><ArrowDown /></el-icon>
                  </div>
                  <div class="log-content">
                    <div class="log-header">
                      <span class="log-description">{{ log.description }}</span>
                      <span class="log-amount" :class="log.type.toLowerCase()">
                        {{ log.type === 'EARN' ? '+' : '-' }}{{ log.amount }}
                      </span>
                    </div>
                    <div class="log-footer">
                      <span class="log-source">{{ getSourceText(log.source) }}</span>
                      <span class="log-time">{{ formatDate(log.createdAt) }}</span>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="empty-state">
                <el-empty description="暂无积分流水记录" />
              </div>

              <div v-if="pointLogs.length" class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="logsPage"
                  v-model:page-size="logsSize"
                  :total="logsTotal"
                  :page-sizes="[10, 20, 50]"
                  layout="prev, pager, next, total, sizes"
                  background
                  @size-change="handleLogsSizeChange"
                  @current-change="handleLogsPageChange"
                />
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="兑换记录" name="exchange">
            <div v-loading="exchangeLoading" class="exchange-container">
              <div v-if="exchangeRecords.length" class="exchange-list">
                <div
                  v-for="record in exchangeRecords"
                  :key="record.id"
                  class="exchange-item"
                >
                  <div class="exchange-image">
                    <img v-lazy="record.productImage" :alt="record.productName" />
                  </div>
                  <div class="exchange-content">
                    <div class="exchange-header">
                      <span class="exchange-name">{{ record.productName }}</span>
                      <el-tag :type="getExchangeStatusType(record.status)">
                        {{ getExchangeStatusText(record.status) }}
                      </el-tag>
                    </div>
                    <div class="exchange-details">
                      <span class="exchange-points">
                        <el-icon :size="14"><GoldMedal /></el-icon>
                        {{ record.pointsSpent }} 积分
                      </span>
                      <span class="exchange-time">{{ formatDate(record.createdAt) }}</span>
                    </div>
                    <div v-if="record.redeemCode" class="exchange-code">
                      <span>兑换码：</span>
                      <code>{{ record.redeemCode }}</code>
                      <el-button size="small" type="primary" plain @click="copyCode(record.redeemCode)">
                        复制
                      </el-button>
                    </div>
                  </div>
                </div>
              </div>

              <div v-else class="empty-state">
                <el-empty description="暂无兑换记录">
                  <router-link to="/points-mall">
                    <el-button type="primary">去兑换</el-button>
                  </router-link>
                </el-empty>
              </div>

              <div v-if="exchangeRecords.length" class="pagination-wrapper">
                <el-pagination
                  v-model:current-page="exchangePage"
                  v-model:page-size="exchangeSize"
                  :total="exchangeTotal"
                  :page-sizes="[10, 20, 50]"
                  layout="prev, pager, next, total, sizes"
                  background
                  @size-change="handleExchangeSizeChange"
                  @current-change="handleExchangePageChange"
                />
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  History,
  Shop,
  Wallet,
  TrendCharts,
  ShoppingBag,
  Calendar,
  ArrowUp,
  ArrowDown,
  GoldMedal
} from '@element-plus/icons-vue'
import { pointApi } from '@/api'
import type { UserPoints, PointLog, ExchangeRecord } from '@/types'

const router = useRouter()

const userPoints = ref<UserPoints | null>(null)
const activeTab = ref<'logs' | 'exchange'>('logs')

const logsLoading = ref(false)
const exchangeLoading = ref(false)

const pointLogs = ref<PointLog[]>([])
const logsPage = ref(1)
const logsSize = ref(20)
const logsTotal = ref(0)

const exchangeRecords = ref<ExchangeRecord[]>([])
const exchangePage = ref(1)
const exchangeSize = ref(10)
const exchangeTotal = ref(0)

const sourceTextMap: Record<string, string> = {
  SIGN_IN: '每日签到',
  ORDER_PAY: '订单消费',
  REVIEW: '发表评论',
  EXCHANGE: '积分兑换'
}

async function loadPointInfo() {
  try {
    const res = await pointApi.getPointInfo()
    userPoints.value = res.data.data
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  }
}

async function loadPointLogs() {
  logsLoading.value = true
  try {
    const res = await pointApi.getPointLogs(logsPage.value, logsSize.value)
    const data = res.data.data
    pointLogs.value = data.list || []
    logsTotal.value = data.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    logsLoading.value = false
  }
}

async function loadExchangeRecords() {
  exchangeLoading.value = true
  try {
    const res = await pointApi.getExchangeRecords(exchangePage.value, exchangeSize.value)
    const data = res.data.data
    exchangeRecords.value = data.list || []
    exchangeTotal.value = data.total || 0
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    exchangeLoading.value = false
  }
}

function handleLogsPageChange(page: number) {
  logsPage.value = page
  loadPointLogs()
}

function handleLogsSizeChange(size: number) {
  logsSize.value = size
  logsPage.value = 1
  loadPointLogs()
}

function handleExchangePageChange(page: number) {
  exchangePage.value = page
  loadExchangeRecords()
}

function handleExchangeSizeChange(size: number) {
  exchangeSize.value = size
  exchangePage.value = 1
  loadExchangeRecords()
}

function getSourceText(source: string): string {
  return sourceTextMap[source] || source
}

function getExchangeStatusType(status: string): string {
  const types: Record<string, string> = {
    PENDING: 'warning',
    COMPLETED: 'success',
    FAILED: 'danger'
  }
  return types[status] || 'info'
}

function getExchangeStatusText(status: string): string {
  const texts: Record<string, string> = {
    PENDING: '处理中',
    COMPLETED: '兑换成功',
    FAILED: '兑换失败'
  }
  return texts[status] || status
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

function copyCode(code: string) {
  navigator.clipboard.writeText(code).then(() => {
    ElMessage.success('兑换码已复制')
  }).catch(() => {
    ElMessage.error('复制失败，请手动复制')
  })
}

watch(activeTab, (tab) => {
  if (tab === 'logs' && pointLogs.value.length === 0) {
    loadPointLogs()
  } else if (tab === 'exchange' && exchangeRecords.value.length === 0) {
    loadExchangeRecords()
  }
})

onMounted(() => {
  loadPointInfo()
  loadPointLogs()
})
</script>

<style lang="scss" scoped>
.points-logs-page {
  min-height: calc(100vh - 64px);
  background: var(--bg-dark);
  padding-bottom: 60px;
}

.page-header {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  padding: 40px 20px;
  border-bottom: 2px solid #8b5cf6;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    right: -10%;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(139, 92, 246, 0.2) 0%, transparent 70%);
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

.history-icon {
  color: #8b5cf6;
  animation: historySpin 3s ease-in-out infinite;
}

@keyframes historySpin {
  0%, 100% { transform: rotate(-5deg); }
  50% { transform: rotate(5deg); }
}

.page-title {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  margin: 0;
  background: linear-gradient(90deg, #8b5cf6 0%, #a78bfa 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 4px 0 0 0;
}

.main-content {
  max-width: 1000px;
  margin: 0 auto;
  padding: 32px 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: #8b5cf6;
    box-shadow: 0 4px 12px rgba(139, 92, 246, 0.15);
  }
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;

  &.balance {
    background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
  }

  &.earned {
    background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
  }

  &.spent {
    background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
  }

  &.consecutive {
    background: linear-gradient(135deg, #3b82f6 0%, #60a5fa 100%);
  }
}

.stat-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  color: var(--text-secondary);
  font-size: 13px;
}

.stat-value {
  color: var(--text-white);
  font-size: 24px;
  font-weight: 700;

  &.positive {
    color: #10b981;
  }

  &.negative {
    color: #ef4444;
  }
}

.tabs-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.custom-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 20px;
    border-bottom-color: var(--border-color);
  }

  :deep(.el-tabs__nav-wrap::after) {
    background-color: var(--border-color);
  }

  :deep(.el-tabs__item) {
    color: var(--text-secondary);
    font-size: 16px;
    padding: 0 24px;
    height: 40px;
    line-height: 40px;

    &.is-active {
      color: #8b5cf6;
    }
  }

  :deep(.el-tabs__active-bar) {
    background-color: #8b5cf6;
    height: 3px;
  }
}

.logs-container,
.exchange-container {
  min-height: 400px;
}

.logs-list,
.exchange-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  transition: all 0.2s ease;

  &:hover {
    background: rgba(139, 92, 246, 0.08);
  }
}

.log-icon {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.earn {
    background: rgba(16, 185, 129, 0.15);
    color: #10b981;
  }

  &.spend {
    background: rgba(239, 68, 68, 0.15);
    color: #ef4444;
  }
}

.log-content {
  flex: 1;
  min-width: 0;
}

.log-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 4px;
}

.log-description {
  color: var(--text-white);
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.log-amount {
  font-size: 18px;
  font-weight: 700;
  flex-shrink: 0;

  &.earn {
    color: #10b981;
  }

  &.spend {
    color: #ef4444;
  }
}

.log-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.log-source {
  color: var(--text-secondary);
  font-size: 12px;
  background: var(--bg-dark);
  padding: 2px 8px;
  border-radius: 4px;
}

.log-time {
  color: var(--text-secondary);
  font-size: 12px;
}

.exchange-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--bg-hover);
  border-radius: var(--radius-md);
  transition: all 0.2s ease;

  &:hover {
    background: rgba(139, 92, 246, 0.08);
  }
}

.exchange-image {
  width: 80px;
  height: 60px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.exchange-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.exchange-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.exchange-name {
  color: var(--text-white);
  font-size: 15px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.exchange-details {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.exchange-points {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #f59e0b;
  font-weight: 600;
  font-size: 14px;

  .el-icon {
    color: #f59e0b;
  }
}

.exchange-time {
  color: var(--text-secondary);
  font-size: 12px;
}

.exchange-code {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-dark);
  border-radius: var(--radius-sm);
  margin-top: 4px;

  span {
    color: var(--text-secondary);
    font-size: 12px;
  }

  code {
    background: rgba(16, 185, 129, 0.1);
    color: #10b981;
    padding: 4px 10px;
    border-radius: 4px;
    font-family: 'Courier New', monospace;
    font-weight: 600;
    letter-spacing: 1px;
  }
}

.empty-state {
  padding: 60px 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 24px;

  :deep(.el-pagination) {
    --el-pagination-bg-color: transparent;
    --el-pagination-hover-color: #8b5cf6;

    .el-pager li.is-active {
      background-color: #8b5cf6;
    }

    .btn-prev:hover,
    .btn-next:hover,
    .el-pager li:hover {
      color: #8b5cf6;
    }

    .el-pagination__total,
    .el-pagination__jump {
      color: var(--text-secondary);
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
  .stats-cards {
    grid-template-columns: 1fr 1fr;
    gap: 12px;
  }
  .stat-card {
    padding: 16px;
    gap: 12px;
  }
  .stat-icon {
    width: 48px;
    height: 48px;

    .el-icon {
      font-size: 24px;
    }
  }
  .stat-value {
    font-size: 20px;
  }
  .tabs-section {
    padding: 16px;
  }
  .log-item,
  .exchange-item {
    padding: 12px;
  }
  .exchange-image {
    width: 60px;
    height: 45px;
  }
}
</style>
