<template>
  <header class="app-header">
    <div class="header-content">
      <!-- Logo -->
      <router-link to="/" class="logo">
        <el-icon :size="28"><Platform /></el-icon>
        <span>STEAM</span>
      </router-link>
      
      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">首页</router-link>
        <router-link to="/store" class="nav-item">商店</router-link>
        <router-link to="/flash-sale" class="nav-item nav-item-flash">
          <el-icon style="margin-right: 4px;"><Lightning /></el-icon>
          秒杀
        </router-link>
        <router-link v-if="isLoggedIn" to="/points-mall" class="nav-item nav-item-points">
          <el-icon style="margin-right: 4px;"><GoldMedal /></el-icon>
          积分商城
        </router-link>
        <router-link v-if="isLoggedIn" to="/library" class="nav-item">游戏库</router-link>
      </nav>
      
      <!-- 搜索框 -->
      <div class="search-box">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索游戏"
          :prefix-icon="Search"
          clearable
          @keyup.enter="handleSearch"
        />
      </div>
      
      <!-- 用户区域 -->
      <div class="user-area">
        <template v-if="isLoggedIn">
          <!-- 通知铃铛 -->
          <el-dropdown 
            trigger="click" 
            @visible-change="handleNotificationDropdown"
            :hide-on-click="false"
            class="notification-dropdown"
          >
            <div class="icon-btn notification-bell">
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                <el-icon :size="22"><Bell /></el-icon>
              </el-badge>
            </div>
            <template #dropdown>
              <div class="notification-panel">
                <div class="notification-header">
                  <span class="notification-title">
                    <el-icon><Bell /></el-icon>
                    通知
                  </span>
                  <el-button 
                    v-if="unreadCount > 0" 
                    type="primary" 
                    link 
                    size="small"
                    @click="handleMarkAllRead"
                  >
                    全部已读
                  </el-button>
                </div>
                <div class="notification-list" v-loading="notificationsLoading">
                  <div 
                    v-for="notification in notifications" 
                    :key="notification.id" 
                    class="notification-item"
                    :class="{ unread: notification.isRead === 0 }"
                    @click="handleNotificationClick(notification)"
                  >
                    <img 
                      v-if="notification.game?.coverImage" 
                      :src="notification.game.coverImage" 
                      class="notification-avatar"
                    />
                    <div class="notification-content">
                      <div class="notification-title-text">{{ notification.title }}</div>
                      <div class="notification-desc">{{ notification.content }}</div>
                      <div class="notification-meta">
                        <span class="notification-time">{{ formatNotificationTime(notification.createdAt) }}</span>
                        <span v-if="notification.priceDropPercent" class="notification-price-drop">
                          降价 {{ notification.priceDropPercent }}%
                        </span>
                      </div>
                    </div>
                    <el-icon v-if="notification.isRead === 0" class="unread-dot"><CircleClose /></el-icon>
                  </div>
                  <el-empty v-if="!notificationsLoading && notifications.length === 0" description="暂无通知" :image-size="60" />
                </div>
                <div class="notification-footer">
                  <router-link to="/notifications" @click="closeDropdown">
                    查看全部
                  </router-link>
                </div>
              </div>
            </template>
          </el-dropdown>
          
          <!-- 购物车 -->
          <router-link to="/cart" class="icon-btn">
            <el-badge :value="cartCount" :hidden="cartCount === 0">
              <el-icon :size="22"><ShoppingCart /></el-icon>
            </el-badge>
          </router-link>
          
          <!-- 愿望单 -->
          <router-link to="/wishlist" class="icon-btn">
            <el-icon :size="22"><Star /></el-icon>
          </router-link>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="getAvatarUrl(userInfo?.avatar)">
                {{ userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo?.nickname || userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="recharge">
                  <el-icon><Wallet /></el-icon>
                  充值中心
                  <span style="margin-left: 8px; color: #f59e0b; font-weight: 600;">
                    ¥{{ userInfo?.balance?.toFixed(2) || '0.00' }}
                  </span>
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><Document /></el-icon>
                  我的订单
                </el-dropdown-item>
                <el-dropdown-item command="library">
                  <el-icon><Collection /></el-icon>
                  我的游戏
                </el-dropdown-item>
                <el-dropdown-item command="wishlist">
                  <el-icon><Star /></el-icon>
                  愿望单
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        
        <template v-else>
          <router-link to="/login">
            <el-button type="primary">登录</el-button>
          </router-link>
          <router-link to="/register">
            <el-button>注册</el-button>
          </router-link>
        </template>
      </div>
      
      <!-- 移动端菜单按钮 -->
      <el-button class="mobile-menu-btn" text @click="showMobileMenu = true">
        <el-icon :size="24"><Menu /></el-icon>
      </el-button>
    </div>
    
    <!-- 移动端抽屉菜单 -->
    <el-drawer v-model="showMobileMenu" direction="rtl" size="280px" :show-close="false">
      <template #header>
        <div class="drawer-header">
          <el-icon :size="24"><Platform /></el-icon>
          <span>STEAM</span>
        </div>
      </template>
      <div class="mobile-menu">
        <router-link to="/" class="mobile-nav-item" @click="showMobileMenu = false">
          <el-icon><HomeFilled /></el-icon>
          首页
        </router-link>
        <router-link to="/store" class="mobile-nav-item" @click="showMobileMenu = false">
          <el-icon><Shop /></el-icon>
          商店
        </router-link>
        <router-link to="/flash-sale" class="mobile-nav-item" @click="showMobileMenu = false">
          <el-icon><Lightning /></el-icon>
          限时秒杀
        </router-link>
        <template v-if="isLoggedIn">
          <router-link to="/points-mall" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><GoldMedal /></el-icon>
            积分商城
          </router-link>
          <router-link to="/recharge" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Wallet /></el-icon>
            充值中心
          </router-link>
          <router-link to="/library" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Collection /></el-icon>
            游戏库
          </router-link>
          <router-link to="/cart" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><ShoppingCart /></el-icon>
            购物车
          </router-link>
          <router-link to="/wishlist" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Star /></el-icon>
            愿望单
          </router-link>
          <router-link to="/notifications" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="mobile-notification-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
            消息通知
          </router-link>
          <router-link to="/orders" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Document /></el-icon>
            我的订单
          </router-link>
          <router-link to="/profile" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><User /></el-icon>
            个人中心
          </router-link>
          <div class="mobile-nav-item" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            退出登录
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><User /></el-icon>
            登录
          </router-link>
          <router-link to="/register" class="mobile-nav-item" @click="showMobileMenu = false">
            <el-icon><Edit /></el-icon>
            注册
          </router-link>
        </template>
      </div>
    </el-drawer>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useCartStore } from '@/store/cart'
import { notificationApi } from '@/api'
import type { Notification } from '@/types'
import { Search, Lightning, GoldMedal, Wallet, Bell, CircleClose } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

const searchKeyword = ref('')
const showMobileMenu = ref(false)

const notifications = ref<Notification[]>([])
const notificationsLoading = ref(false)
const unreadCount = ref(0)
let notificationTimer: number | null = null

const isLoggedIn = computed(() => userStore.isLoggedIn)
const userInfo = computed(() => userStore.userInfo)
const cartCount = computed(() => cartStore.count)

async function fetchNotifications() {
  if (!isLoggedIn.value) return
  
  notificationsLoading.value = true
  try {
    const [notificationsRes, countRes] = await Promise.all([
      notificationApi.getNotifications(10),
      notificationApi.getUnreadCount()
    ])
    notifications.value = notificationsRes.data.data || []
    unreadCount.value = countRes.data.data?.unreadCount || 0
  } catch (error) {
    console.error('Failed to fetch notifications')
  } finally {
    notificationsLoading.value = false
  }
}

async function handleNotificationDropdown(visible: boolean) {
  if (visible) {
    await fetchNotifications()
  }
}

async function handleNotificationClick(notification: Notification) {
  if (notification.isRead === 0) {
    try {
      await notificationApi.markAsRead(notification.id)
      notification.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('Failed to mark notification as read')
    }
  }
  
  if (notification.gameId) {
    router.push(`/game/${notification.gameId}`)
  }
}

async function handleMarkAllRead() {
  try {
    await notificationApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = 1)
    unreadCount.value = 0
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    console.error('Failed to mark all as read')
  }
}

function closeDropdown() {
  const dropdown = document.querySelector('.notification-dropdown .el-dropdown')
  if (dropdown) {
    const event = new MouseEvent('click', { bubbles: true })
    document.dispatchEvent(event)
  }
}

function formatNotificationTime(dateStr: string) {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return date.toLocaleDateString('zh-CN')
}

onMounted(() => {
  if (isLoggedIn.value) {
    fetchNotifications()
    notificationTimer = window.setInterval(fetchNotifications, 60000)
  }
})

onUnmounted(() => {
  if (notificationTimer) {
    clearInterval(notificationTimer)
    notificationTimer = null
  }
})

// 获取头像URL，处理默认头像
function getAvatarUrl(avatar: string | undefined): string {
  if (!avatar || avatar === '/avatars/default.png') {
    return '/avatars/default.svg'
  }
  return avatar
}

// 搜索
function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ path: '/store', query: { keyword: searchKeyword.value } })
    searchKeyword.value = ''
  }
}

// 下拉菜单命令
function handleCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'recharge':
      router.push('/recharge')
      break
    case 'orders':
      router.push('/orders')
      break
    case 'library':
      router.push('/library')
      break
    case 'wishlist':
      router.push('/wishlist')
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    showMobileMenu.value = false
    ElMessage.success('已退出登录')
    router.push('/')
  } catch {
    // 取消
  }
}
</script>

<style lang="scss" scoped>
.app-header {
  background: var(--steam-darker);
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: var(--shadow-md);
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--text-white);
  font-size: 24px;
  font-weight: 700;
  text-decoration: none;
  
  &:hover {
    color: var(--steam-light-blue);
  }
}

.nav-menu {
  display: flex;
  gap: 8px;
  
  .nav-item {
    padding: 8px 16px;
    color: var(--text-primary);
    text-decoration: none;
    border-radius: var(--radius-sm);
    transition: all 0.3s;
    
    &:hover {
      background: var(--bg-hover);
      color: var(--text-white);
    }
    
    &.router-link-active {
      background: var(--steam-blue);
      color: var(--text-white);
    }
  }
}

.search-box {
  flex: 1;
  max-width: 300px;
  
  :deep(.el-input__wrapper) {
    background: rgba(0, 0, 0, 0.3);
    border-radius: var(--radius-md);
  }
}

.user-area {
  display: flex;
  align-items: center;
  gap: 16px;
  
  .icon-btn {
    color: var(--text-primary);
    padding: 8px;
    border-radius: var(--radius-sm);
    transition: all 0.3s;
    
    &:hover {
      background: var(--bg-hover);
      color: var(--steam-light-blue);
    }
  }
  
  .user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
    padding: 4px 8px;
    border-radius: var(--radius-sm);
    transition: background 0.3s;
    
    &:hover {
      background: var(--bg-hover);
    }
    
    .username {
      color: var(--text-primary);
      font-size: 14px;
    }
  }
}

.mobile-menu-btn {
  display: none;
  color: var(--text-white);
}

.drawer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-white);
}

.mobile-menu {
  display: flex;
  flex-direction: column;
  
  .mobile-nav-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 16px;
    color: var(--text-primary);
    text-decoration: none;
    border-radius: var(--radius-sm);
    cursor: pointer;
    transition: all 0.3s;
    
    &:hover {
      background: var(--bg-hover);
      color: var(--text-white);
    }
  }
}

// 通知面板
.notification-dropdown {
  :deep(.el-dropdown-menu) {
    padding: 0;
    min-width: 360px;
    max-width: 400px;
  }
}

.notification-bell {
  position: relative;
}

.notification-badge {
  :deep(.el-badge__content) {
    background: var(--steam-green);
    color: var(--steam-darker);
    border: none;
  }
}

.notification-panel {
  width: 360px;
  max-height: 480px;
  display: flex;
  flex-direction: column;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  overflow: hidden;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid var(--border-color);
  
  .notification-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-white);
  }
}

.notification-list {
  flex: 1;
  overflow-y: auto;
  max-height: 360px;
}

.notification-item {
  display: flex;
  gap: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid var(--border-color);
  
  &:hover {
    background: var(--bg-hover);
  }
  
  &.unread {
    background: rgba(102, 192, 244, 0.05);
  }
  
  .notification-avatar {
    width: 48px;
    height: 48px;
    object-fit: cover;
    border-radius: var(--radius-sm);
    flex-shrink: 0;
  }
  
  .notification-content {
    flex: 1;
    min-width: 0;
    
    .notification-title-text {
      font-size: 14px;
      font-weight: 500;
      color: var(--text-white);
      margin-bottom: 4px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
    
    .notification-desc {
      font-size: 12px;
      color: var(--text-secondary);
      margin-bottom: 6px;
      line-height: 1.4;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .notification-meta {
      display: flex;
      align-items: center;
      gap: 12px;
      
      .notification-time {
        font-size: 11px;
        color: var(--text-secondary);
      }
      
      .notification-price-drop {
        font-size: 11px;
        color: var(--steam-green);
        font-weight: 600;
      }
    }
  }
  
  .unread-dot {
    color: var(--steam-light-blue);
    font-size: 8px;
    flex-shrink: 0;
    margin-top: 6px;
  }
}

.notification-footer {
  padding: 12px 16px;
  border-top: 1px solid var(--border-color);
  text-align: center;
  
  a {
    color: var(--steam-light-blue);
    font-size: 13px;
    text-decoration: none;
    
    &:hover {
      color: var(--text-white);
    }
  }
}

@media (max-width: 900px) {
  .nav-menu {
    display: none;
  }
}

@media (max-width: 768px) {
  .header-content {
    gap: 12px;
  }
  
  .search-box {
    display: none;
  }
  
  .user-area {
    display: none;
  }
  
  .mobile-menu-btn {
    display: flex;
  }
}
</style>
