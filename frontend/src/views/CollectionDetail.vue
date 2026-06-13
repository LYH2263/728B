<template>
  <div class="collection-detail-page">
    <div v-if="loading" class="loading">
      <el-skeleton :rows="8" animated />
    </div>

    <template v-else>
      <div class="collection-header">
        <div class="header-left">
          <el-button text @click="goBack" class="back-btn">
            <el-icon><ArrowLeft /></el-icon>
            返回
          </el-button>
          <div class="header-info">
            <h1>{{ collection?.name }}</h1>
            <div class="header-meta">
              <span v-if="collection?.username" class="owner">
                <el-avatar :size="20" :src="getAvatarUrl(collection.userAvatar)">
                  {{ collection.username?.charAt(0) }}
                </el-avatar>
                {{ collection.username }}
              </span>
              <el-tag v-if="collection?.isPublic" size="small" type="success">公开</el-tag>
              <el-tag v-else size="small" type="info">私密</el-tag>
              <span class="meta-item"><el-icon><Grid /></el-icon> {{ collection?.gameCount }} 款游戏</span>
              <span class="meta-item"><el-icon><PriceTag /></el-icon> 总价 ¥{{ collection?.totalPrice?.toFixed(2) }}</span>
            </div>
            <p v-if="collection?.description" class="header-desc">{{ collection.description }}</p>
          </div>
        </div>
        <div v-if="isOwner" class="header-actions">
          <el-button size="small" @click="showEditDialog = true">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button size="small" type="danger" @click="handleDeleteCollection">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </div>
      </div>

      <div v-if="games.length" class="games-section">
        <div class="section-bar">
          <h2>合集中的游戏</h2>
          <el-button v-if="isOwner" size="small" type="primary" plain @click="handleSaveOrder" :disabled="!orderChanged">
            <el-icon><Check /></el-icon>
            保存排序
          </el-button>
        </div>
        <div class="games-grid">
          <div
            v-for="(item, index) in games"
            :key="item.gameId"
            class="game-item"
            :draggable="isOwner"
            @dragstart="onDragStart(index, $event)"
            @dragover.prevent="onDragOver(index)"
            @drop="onDrop(index)"
            @dragend="onDragEnd"
            :class="{ 'drag-over': dragOverIndex === index, 'dragging': dragIndex === index }"
          >
            <div class="drag-handle" v-if="isOwner">
              <el-icon><Rank /></el-icon>
            </div>
            <img
              :src="item.game.coverImage || '/placeholder.jpg'"
              :alt="item.game.title"
              class="game-cover"
              @click="goToGame(item.gameId)"
            />
            <div class="game-info" @click="goToGame(item.gameId)">
              <h3>{{ item.game.title }}</h3>
              <div class="game-price">
                <template v-if="item.game.discountPercent && item.game.discountPercent > 0">
                  <span class="discount-tag">-{{ item.game.discountPercent }}%</span>
                  <span class="original">¥{{ item.game.originalPrice }}</span>
                  <span class="current">¥{{ item.game.discountPrice }}</span>
                </template>
                <template v-else-if="item.game.originalPrice === 0">
                  <span class="free">免费</span>
                </template>
                <template v-else>
                  <span class="current">¥{{ item.game.originalPrice }}</span>
                </template>
              </div>
            </div>
            <div v-if="isOwner" class="game-actions" @click.stop>
              <el-button size="small" type="danger" text @click="handleRemoveGame(item.gameId)">
                <el-icon><Close /></el-icon>
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <el-empty v-else description="合集中暂无游戏">
        <router-link v-if="isOwner" to="/store">
          <el-button type="primary">去逛逛</el-button>
        </router-link>
      </el-empty>

      <el-dialog v-model="showEditDialog" title="编辑合集" width="440px">
        <el-form :model="editForm" label-width="80px">
          <el-form-item label="名称">
            <el-input v-model="editForm.name" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="描述">
            <el-input v-model="editForm.description" type="textarea" :rows="3" maxlength="200" show-word-limit />
          </el-form-item>
          <el-form-item label="可见性">
            <el-radio-group v-model="editForm.isPublic">
              <el-radio :label="0">私密</el-radio>
              <el-radio :label="1">公开</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="showEditDialog = false">取消</el-button>
          <el-button type="primary" :loading="editLoading" @click="handleEditCollection">保存</el-button>
        </template>
      </el-dialog>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { collectionApi } from '@/api'
import { useUserStore } from '@/store/user'
import type { GameCollection, CollectionGameItem } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const collection = ref<GameCollection | null>(null)
const games = ref<CollectionGameItem[]>([])
const originalOrder = ref<number[]>([])

const showEditDialog = ref(false)
const editLoading = ref(false)
const editForm = reactive({ name: '', description: '', isPublic: 0 })

const dragIndex = ref(-1)
const dragOverIndex = ref(-1)

const collectionId = computed(() => Number(route.params.id))
const isOwner = computed(() => collection.value?.userId === userStore.userInfo?.id)

function getAvatarUrl(avatar: string | undefined): string {
  if (!avatar || avatar === '/avatars/default.png') return '/avatars/default.svg'
  return avatar
}

onMounted(async () => {
  await fetchDetail()
  loading.value = false
})

async function fetchDetail() {
  try {
    if (userStore.isLoggedIn) {
      const res = await collectionApi.getCollectionDetail(collectionId.value)
      collection.value = res.data.data
    } else {
      const res = await collectionApi.getCollectionDetailPublic(collectionId.value)
      collection.value = res.data.data
    }
    await fetchGames()
  } catch {
    ElMessage.error('合集不存在或无权查看')
    router.push('/collections')
  }
}

async function fetchGames() {
  try {
    let res
    if (userStore.isLoggedIn && isOwner.value) {
      res = await collectionApi.getCollectionGames(collectionId.value)
    } else {
      res = await collectionApi.getCollectionGamesPublic(collectionId.value)
    }
    games.value = res.data.data || []
    originalOrder.value = games.value.map(g => g.gameId)
  } catch {
    games.value = []
  }
}

const orderChanged = computed(() => {
  if (games.value.length !== originalOrder.value.length) return false
  return games.value.some((g, i) => g.gameId !== originalOrder.value[i])
})

function goBack() {
  router.push('/collections')
}

function goToGame(gameId: number) {
  router.push(`/game/${gameId}`)
}

function onDragStart(index: number, event: DragEvent) {
  dragIndex.value = index
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'move'
  }
}

function onDragOver(index: number) {
  dragOverIndex.value = index
}

function onDrop(index: number) {
  if (dragIndex.value === -1 || dragIndex.value === index) return
  const item = games.value.splice(dragIndex.value, 1)[0]
  games.value.splice(index, 0, item)
  dragOverIndex.value = -1
  dragIndex.value = -1
}

function onDragEnd() {
  dragIndex.value = -1
  dragOverIndex.value = -1
}

async function handleSaveOrder() {
  try {
    const gameIds = games.value.map(g => g.gameId)
    await collectionApi.reorderGames(collectionId.value, gameIds)
    originalOrder.value = [...gameIds]
    ElMessage.success('排序已保存')
  } catch { /* handled */ }
}

async function handleRemoveGame(gameId: number) {
  try {
    await collectionApi.removeGameFromCollection(collectionId.value, gameId)
    ElMessage.success('已从合集移除')
    await fetchGames()
    await fetchDetail()
  } catch { /* handled */ }
}

async function handleDeleteCollection() {
  try {
    await ElMessageBox.confirm(
      '确定删除此合集吗？合集中的游戏关联将被一并删除。',
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await collectionApi.deleteCollection(collectionId.value)
    ElMessage.success('合集已删除')
    router.push('/collections')
  } catch { /* cancelled */ }
}

async function handleEditCollection() {
  if (!editForm.name.trim()) {
    ElMessage.warning('请输入合集名称')
    return
  }
  editLoading.value = true
  try {
    await collectionApi.updateCollection(collectionId.value, {
      name: editForm.name.trim(),
      description: editForm.description.trim() || undefined,
      isPublic: editForm.isPublic
    })
    ElMessage.success('合集已更新')
    showEditDialog.value = false
    await fetchDetail()
  } catch { /* handled */ } finally {
    editLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.collection-detail-page {
  max-width: 1000px;
  margin: 0 auto;
}

.collection-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  gap: 16px;
  flex-wrap: wrap;
}

.header-left {
  flex: 1;
}

.back-btn {
  color: var(--text-secondary);
  margin-bottom: 12px;
  padding-left: 0;

  &:hover {
    color: var(--steam-light-blue);
  }
}

.header-info {
  h1 {
    font-size: 28px;
    color: var(--text-white);
    margin-bottom: 12px;
  }
}

.header-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  color: var(--text-secondary);
  font-size: 14px;
  margin-bottom: 8px;

  .owner {
    display: flex;
    align-items: center;
    gap: 6px;
    color: var(--steam-light-blue);
  }

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.header-desc {
  color: var(--text-secondary);
  font-size: 14px;
  margin-top: 4px;
}

.header-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.games-section {
  .section-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h2 {
      font-size: 18px;
      color: var(--text-white);
    }
  }
}

.games-grid {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.game-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: all 0.3s;
  cursor: pointer;

  &:hover {
    border-color: var(--steam-light-blue);
  }

  &.drag-over {
    border-color: var(--steam-green);
    background: rgba(164, 208, 7, 0.05);
  }

  &.dragging {
    opacity: 0.5;
  }
}

.drag-handle {
  color: var(--text-secondary);
  cursor: grab;
  font-size: 18px;

  &:active {
    cursor: grabbing;
  }
}

.game-cover {
  width: 120px;
  height: 56px;
  object-fit: cover;
  border-radius: var(--radius-sm);
  flex-shrink: 0;
}

.game-info {
  flex: 1;
  min-width: 0;

  h3 {
    font-size: 15px;
    color: var(--text-white);
    margin-bottom: 6px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;

    &:hover {
      color: var(--steam-light-blue);
    }
  }
}

.game-price {
  display: flex;
  align-items: center;
  gap: 8px;

  .discount-tag {
    background: var(--steam-green);
    color: var(--steam-darker);
    padding: 2px 6px;
    border-radius: var(--radius-sm);
    font-weight: 600;
    font-size: 12px;
  }

  .original {
    color: var(--text-secondary);
    text-decoration: line-through;
    font-size: 12px;
  }

  .current {
    color: var(--text-primary);
    font-weight: 600;
  }

  .free {
    color: var(--steam-green);
    font-weight: 600;
  }
}

.game-actions {
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .game-item {
    flex-wrap: wrap;
  }

  .game-cover {
    width: 80px;
    height: 38px;
  }

  .drag-handle {
    display: none;
  }
}
</style>
