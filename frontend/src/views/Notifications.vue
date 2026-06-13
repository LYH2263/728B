<template>
  <div class="notifications-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Bell /></el-icon>
        我的通知
      </h1>
      <el-button 
        v-if="unreadCount > 0" 
        type="primary" 
        @click="handleMarkAllRead"
      >
        <el-icon><Check /></el-icon>
        全部已读
      </el-button>
    </div>
    
    <div v-if="loading" class="loading">
      <el-skeleton :rows="8" animated />
    </div>
    
    <template v-else>
      <div v-if="notifications.length" class="notification-list">
        <div 
          v-for="notification in notifications" 
          :key="notification.id" 
          class="notification-card"
          :class="{ unread: notification.isRead === 0 }"
          @click="handleNotificationClick(notification)"
        >
          <img 
            v-if="notification.game?.coverImage" 
            :src="notification.game.coverImage" 
            class="notification-cover"
            @click.stop="goToGame(notification.gameId!)"
          />
          <div class="notification-info">
            <div class="notification-header">
              <span class="notification-title">{{ notification.title }}</span>
              <el-tag 
                v-if="notification.type === 'PRICE_DROP'" 
                type="success" 
                size="small"
              >
                降价通知
              </el-tag>
              <el-tag 
                v-if="notification.type === 'NEW_GAME'" 
                type="primary" 
                size="small"
              >
                新游通知
              </el-tag>
              <el-tag 
                v-if="notification.isRead === 0" 
                type="primary" 
                size="small"
              >
                未读
              </el-tag>
            </div>
            <p class="notification-content">{{ notification.content }}</p>
            <div class="notification-details" v-if="notification.type === 'PRICE_DROP'">
              <div class="price-info">
                <span class="old-price">¥{{ notification.oldPrice?.toFixed(2) }}</span>
                <el-icon><ArrowRight /></el-icon>
                <span class="new-price">¥{{ notification.newPrice?.toFixed(2) }}</span>
                <span class="drop-amount" v-if="notification.priceDrop">
                  降 ¥{{ notification.priceDrop.toFixed(2) }}
                </span>
                <span class="drop-percent" v-if="notification.priceDropPercent">
                  -{{ notification.priceDropPercent }}%
                </span>
              </div>
            </div>
            <div class="notification-details new-game-details" v-if="notification.type === 'NEW_GAME'">
              <div class="new-game-info">
                <el-icon><Promotion /></el-icon>
                <span>{{ notification.content }}</span>
              </div>
            </div>
            <div class="notification-footer">
              <span class="notification-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(notification.createdAt) }}
              </span>
              <div class="notification-actions">
                <el-button 
                  v-if="notification.isRead === 0" 
                  type="primary" 
                  link 
                  size="small"
                  @click.stop="handleMarkAsRead(notification.id)"
                >
                  标记已读
                </el-button>
                <el-button 
                  type="danger" 
                  link 
                  size="small"
                  @click.stop="handleDelete(notification.id)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无通知">
        <router-link to="/store">
          <el-button type="primary">去逛逛</el-button>
        </router-link>
      </el-empty>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { notificationApi } from '@/api'
import type { Notification } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Bell, Check, ArrowRight, Clock, Promotion } from '@element-plus/icons-vue'

const router = useRouter()

const loading = ref(true)
const notifications = ref<Notification[]>([])
const unreadCount = ref(0)

onMounted(async () => {
  await fetchNotifications()
  loading.value = false
})

async function fetchNotifications() {
  try {
    const [notificationsRes, countRes] = await Promise.all([
      notificationApi.getNotifications(100),
      notificationApi.getUnreadCount()
    ])
    notifications.value = notificationsRes.data.data || []
    unreadCount.value = countRes.data.data?.unreadCount || 0
  } catch (error) {
    notifications.value = []
  }
}

function formatTime(dateStr: string) {
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function handleNotificationClick(notification: Notification) {
  if (notification.isRead === 0) {
    try {
      await notificationApi.markAsRead(notification.id)
      notification.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('Failed to mark as read')
    }
  }
  
  if (notification.gameId) {
    router.push(`/game/${notification.gameId}`)
  }
}

function goToGame(gameId: number) {
  router.push(`/game/${gameId}`)
}

async function handleMarkAsRead(id: number) {
  try {
    await notificationApi.markAsRead(id)
    const notification = notifications.value.find(n => n.id === id)
    if (notification) {
      notification.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
    ElMessage.success('已标记为已读')
  } catch (error) {
    console.error('Failed to mark as read')
  }
}

async function handleMarkAllRead() {
  try {
    await ElMessageBox.confirm('确定要将所有通知标记为已读吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to mark all as read')
    }
  }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await notificationApi.deleteNotification(id)
    const index = notifications.value.findIndex(n => n.id === id)
    if (index > -1) {
      if (notifications.value[index].isRead === 0) {
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
      notifications.value.splice(index, 1)
    }
    ElMessage.success('已删除')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to delete notification')
    }
  }
}
</script>

<style lang="scss" scoped>
.notifications-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 24px;
  color: var(--text-white);
  margin: 0;
}

.loading {
  padding: 20px;
}

.notification-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notification-card {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    border-color: var(--steam-light-blue);
  }
  
  &.unread {
    border-left: 3px solid var(--steam-light-blue);
    background: rgba(102, 192, 244, 0.05);
  }
  
  .notification-cover {
    width: 160px;
    height: 75px;
    object-fit: cover;
    border-radius: var(--radius-sm);
    flex-shrink: 0;
    cursor: pointer;
    transition: transform 0.3s;
    
    &:hover {
      transform: scale(1.05);
    }
  }
  
  .notification-info {
    flex: 1;
    min-width: 0;
  }
  
  .notification-header {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
    flex-wrap: wrap;
    
    .notification-title {
      font-size: 16px;
      font-weight: 600;
      color: var(--text-white);
    }
  }
  
  .notification-content {
    font-size: 14px;
    color: var(--text-secondary);
    margin-bottom: 12px;
    line-height: 1.6;
  }
  
  .notification-details {
    margin-bottom: 12px;
    
    .price-info {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .old-price {
        color: var(--text-secondary);
        text-decoration: line-through;
        font-size: 14px;
      }
      
      .new-price {
        color: var(--steam-green);
        font-weight: 600;
        font-size: 16px;
      }
      
      .drop-amount {
        color: var(--text-secondary);
        font-size: 13px;
      }
      
      .drop-percent {
        background: var(--steam-green);
        color: var(--steam-darker);
        padding: 2px 6px;
        border-radius: var(--radius-sm);
        font-size: 12px;
        font-weight: 600;
      }
    }
  }
  
  .new-game-details {
    .new-game-info {
      display: flex;
      align-items: center;
      gap: 8px;
      color: var(--steam-light-blue);
      font-size: 14px;
    }
  }
  
  .notification-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .notification-time {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--text-secondary);
      font-size: 12px;
    }
    
    .notification-actions {
      display: flex;
      gap: 12px;
    }
  }
}

@media (max-width: 768px) {
  .notification-card {
    flex-direction: column;
    
    .notification-cover {
      width: 100%;
      height: auto;
      aspect-ratio: 460 / 215;
    }
  }
}
</style>
