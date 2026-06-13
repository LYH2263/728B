<template>
  <div class="collections-page">
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><FolderOpened /></el-icon>
        我的合集
      </h1>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        新建合集
      </el-button>
    </div>

    <div v-if="loading" class="loading">
      <el-skeleton :rows="5" animated />
    </div>

    <template v-else>
      <div v-if="collections.length" class="collections-grid">
        <div
          v-for="item in collections"
          :key="item.id"
          class="collection-card"
          @click="goToDetail(item.id)"
        >
          <div class="cover-mosaic">
            <template v-if="parsedCovers(item.coverImages).length">
              <div
                v-for="(cover, idx) in parsedCovers(item.coverImages).slice(0, 4)"
                :key="idx"
                class="mosaic-item"
                :class="{ 'single': parsedCovers(item.coverImages).length === 1, 'double': parsedCovers(item.coverImages).length === 2 }"
              >
                <img :src="cover" :alt="item.name" />
              </div>
            </template>
            <div v-else class="empty-cover">
              <el-icon :size="40"><Folder /></el-icon>
            </div>
          </div>

          <div class="collection-info">
            <div class="collection-name">
              {{ item.name }}
              <el-tag v-if="item.isPublic" size="small" type="success" class="public-tag">公开</el-tag>
              <el-tag v-else size="small" type="info" class="public-tag">私密</el-tag>
            </div>
            <div class="collection-meta">
              <span><el-icon><Grid /></el-icon> {{ item.gameCount }} 款游戏</span>
              <span><el-icon><PriceTag /></el-icon> ¥{{ item.totalPrice.toFixed(2) }}</span>
            </div>
            <div v-if="item.description" class="collection-desc">{{ item.description }}</div>
          </div>

          <div class="collection-actions" @click.stop>
            <el-dropdown trigger="click" @command="(cmd: string) => handleCommand(cmd, item)">
              <el-button text size="small">
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="edit">编辑</el-dropdown-item>
                  <el-dropdown-item command="togglePublic">
                    {{ item.isPublic ? '设为私密' : '设为公开' }}
                  </el-dropdown-item>
                  <el-dropdown-item command="delete" divided style="color: #ef4444">删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </div>

      <el-empty v-else description="还没有合集，快去创建一个吧">
        <el-button type="primary" @click="showCreateDialog = true">新建合集</el-button>
      </el-empty>
    </template>

    <el-dialog v-model="showCreateDialog" title="新建合集" width="440px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="createForm.name" placeholder="如：打折再买" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="可选" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="createForm.isPublic">
            <el-radio :label="0">私密</el-radio>
            <el-radio :label="1">公开</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>

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
        <el-button type="primary" :loading="editLoading" @click="handleEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { collectionApi } from '@/api'
import type { GameCollection } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const loading = ref(true)
const collections = ref<GameCollection[]>([])

const showCreateDialog = ref(false)
const createLoading = ref(false)
const createForm = reactive({ name: '', description: '', isPublic: 0 })

const showEditDialog = ref(false)
const editLoading = ref(false)
const editForm = reactive({ id: 0, name: '', description: '', isPublic: 0 })

onMounted(async () => {
  await fetchCollections()
  loading.value = false
})

async function fetchCollections() {
  try {
    const res = await collectionApi.getMyCollections()
    collections.value = res.data.data || []
  } catch {
    collections.value = []
  }
}

function parsedCovers(coverImages: string | undefined | null): string[] {
  if (!coverImages) return []
  try { return JSON.parse(coverImages) } catch { return [] }
}

function goToDetail(id: number) {
  router.push(`/collections/${id}`)
}

async function handleCreate() {
  if (!createForm.name.trim()) {
    ElMessage.warning('请输入合集名称')
    return
  }
  createLoading.value = true
  try {
    await collectionApi.createCollection({
      name: createForm.name.trim(),
      description: createForm.description.trim() || undefined,
      isPublic: createForm.isPublic
    })
    ElMessage.success('合集创建成功')
    showCreateDialog.value = false
    createForm.name = ''
    createForm.description = ''
    createForm.isPublic = 0
    await fetchCollections()
  } catch { /* handled */ } finally {
    createLoading.value = false
  }
}

function handleCommand(cmd: string, item: GameCollection) {
  if (cmd === 'edit') {
    editForm.id = item.id
    editForm.name = item.name
    editForm.description = item.description || ''
    editForm.isPublic = item.isPublic
    showEditDialog.value = true
  } else if (cmd === 'togglePublic') {
    handleTogglePublic(item)
  } else if (cmd === 'delete') {
    handleDelete(item)
  }
}

async function handleEdit() {
  if (!editForm.name.trim()) {
    ElMessage.warning('请输入合集名称')
    return
  }
  editLoading.value = true
  try {
    await collectionApi.updateCollection(editForm.id, {
      name: editForm.name.trim(),
      description: editForm.description.trim() || undefined,
      isPublic: editForm.isPublic
    })
    ElMessage.success('合集已更新')
    showEditDialog.value = false
    await fetchCollections()
  } catch { /* handled */ } finally {
    editLoading.value = false
  }
}

async function handleTogglePublic(item: GameCollection) {
  try {
    await collectionApi.updateCollection(item.id, { isPublic: item.isPublic ? 0 : 1 })
    ElMessage.success(item.isPublic ? '已设为私密' : '已设为公开')
    await fetchCollections()
  } catch { /* handled */ }
}

async function handleDelete(item: GameCollection) {
  try {
    await ElMessageBox.confirm(
      `确定删除合集「${item.name}」吗？合集中的游戏关联将被一并删除。`,
      '删除确认',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    await collectionApi.deleteCollection(item.id)
    ElMessage.success('合集已删除')
    await fetchCollections()
  } catch { /* cancelled or handled */ }
}
</script>

<style lang="scss" scoped>
.collections-page {
  max-width: 1000px;
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
}

.collections-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.collection-card {
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;

  &:hover {
    border-color: var(--steam-light-blue);
    transform: translateY(-2px);
    box-shadow: var(--shadow-lg);
  }
}

.cover-mosaic {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-template-rows: 1fr 1fr;
  height: 160px;
  background: rgba(0, 0, 0, 0.3);
  overflow: hidden;

  .mosaic-item {
    overflow: hidden;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    &.single {
      grid-column: 1 / -1;
      grid-row: 1 / -1;
    }

    &.double {
      grid-row: 1 / -1;
    }
  }

  .empty-cover {
    grid-column: 1 / -1;
    grid-row: 1 / -1;
    display: flex;
    align-items: center;
    justify-content: center;
    color: var(--text-secondary);
    background: rgba(0, 0, 0, 0.2);
  }
}

.collection-info {
  padding: 16px;
}

.collection-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-white);
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;

  .public-tag {
    flex-shrink: 0;
  }
}

.collection-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-secondary);

  span {
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.collection-desc {
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.collection-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 2;

  .el-button {
    background: rgba(0, 0, 0, 0.6);
    border-radius: 50%;
    padding: 6px;
    color: var(--text-primary);

    &:hover {
      background: rgba(0, 0, 0, 0.8);
      color: var(--steam-light-blue);
    }
  }
}

@media (max-width: 768px) {
  .collections-grid {
    grid-template-columns: 1fr;
  }
}
</style>
