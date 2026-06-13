<template>
  <div class="developer-page">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="10" animated />
    </div>

    <template v-else-if="developer">
      <div class="developer-header">
        <div class="header-main">
          <div class="developer-avatar">
            <el-avatar :size="100" :src="developer.avatar">
              {{ developer.name?.charAt(0) }}
            </el-avatar>
          </div>
          <div class="developer-info">
            <h1 class="developer-name">{{ developer.name }}</h1>
            <div class="developer-meta">
              <span v-if="developer.country" class="meta-item">
                <el-icon><Location /></el-icon>
                {{ developer.country }}
              </span>
              <span v-if="developer.foundedYear" class="meta-item">
                <el-icon><Calendar /></el-icon>
                成立于 {{ developer.foundedYear }}
              </span>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                {{ developer.followerCount }} 粉丝
              </span>
              <span class="meta-item">
                <el-icon><Film /></el-icon>
                {{ developer.gameCount }} 款游戏
              </span>
            </div>
            <p v-if="developer.description" class="developer-desc">{{ developer.description }}</p>
            <a v-if="developer.website" :href="developer.website" target="_blank" class="developer-website">
              <el-icon><Link /></el-icon>
              {{ developer.website }}
            </a>
          </div>
          <div class="follow-section">
            <el-button
              v-if="isLoggedIn"
              :type="isFollowing ? 'default' : 'primary'"
              size="large"
              @click="handleFollow"
              :loading="followLoading"
            >
              <el-icon><Plus /></el-icon>
              {{ isFollowing ? '已关注' : '关注' }}
            </el-button>
          </div>
        </div>
      </div>

      <div class="games-section">
        <h2 class="section-title">
          <el-icon><Film /></el-icon>
          游戏作品 ({{ games.length }})
        </h2>
        <div v-if="games.length" class="games-grid">
          <GameCard v-for="game in games" :key="game.id" :game="game" />
        </div>
        <el-empty v-else description="暂无游戏作品" />
      </div>
    </template>

    <el-empty v-else description="开发商不存在">
      <router-link to="/store">
        <el-button type="primary">返回商店</el-button>
      </router-link>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { developerApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { Developer, Game } from '@/types'
import GameCard from '@/components/GameCard.vue'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const developer = ref<Developer | null>(null)
const games = ref<Game[]>([])
const isFollowing = ref(false)
const followLoading = ref(false)

const isLoggedIn = computed(() => userStore.isLoggedIn)
const developerId = computed(() => Number(route.params.id))

onMounted(async () => {
  await fetchDeveloperDetail()
  loading.value = false
})

async function fetchDeveloperDetail() {
  try {
    const res = await developerApi.getDetail(developerId.value)
    const data = res.data.data
    developer.value = data.developer
    games.value = data.games || []
    isFollowing.value = data.isFollowing || false
  } catch (error) {
    ElMessage.error('开发商不存在')
    router.push('/store')
  }
}

async function handleFollow() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }

  followLoading.value = true
  try {
    if (isFollowing.value) {
      await developerApi.unfollow(developerId.value)
      isFollowing.value = false
      if (developer.value) {
        developer.value.followerCount = Math.max(0, developer.value.followerCount - 1)
      }
      ElMessage.success('已取消关注')
    } else {
      await developerApi.follow(developerId.value)
      isFollowing.value = true
      if (developer.value) {
        developer.value.followerCount += 1
      }
      ElMessage.success('关注成功')
    }
  } catch (error) {
    // 错误已处理
  } finally {
    followLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.developer-page {
  max-width: 1200px;
  margin: 0 auto;
}

.loading-container {
  padding: 40px;
}

.developer-header {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 32px;
  border: 1px solid var(--border-color);
  margin-bottom: 32px;

  .header-main {
    display: flex;
    gap: 24px;
    align-items: flex-start;
  }

  .developer-avatar {
    flex-shrink: 0;

    :deep(.el-avatar) {
      background: var(--steam-blue);
      font-size: 36px;
      font-weight: 700;
    }
  }

  .developer-info {
    flex: 1;
    min-width: 0;
  }

  .developer-name {
    font-size: 32px;
    font-weight: 700;
    color: var(--text-white);
    margin-bottom: 12px;
  }

  .developer-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    margin-bottom: 16px;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--text-secondary);
      font-size: 14px;
    }
  }

  .developer-desc {
    color: var(--text-light);
    font-size: 15px;
    line-height: 1.8;
    margin-bottom: 12px;
  }

  .developer-website {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: var(--steam-light-blue);
    font-size: 14px;
    text-decoration: none;
    transition: color 0.3s;

    &:hover {
      color: var(--text-white);
    }
  }

  .follow-section {
    flex-shrink: 0;
    padding-top: 8px;
  }
}

.games-section {
  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 20px;
    font-weight: 600;
    color: var(--text-white);
    margin-bottom: 20px;
  }
}

.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

@media (max-width: 768px) {
  .developer-header {
    .header-main {
      flex-direction: column;
      align-items: center;
      text-align: center;
    }

    .developer-meta {
      justify-content: center;
    }

    .follow-section {
      width: 100%;

      .el-button {
        width: 100%;
      }
    }
  }

  .games-grid {
    grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
    gap: 12px;
  }
}
</style>
