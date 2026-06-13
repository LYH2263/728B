<template>
  <div class="compare-page">
    <div class="compare-header">
      <h1 class="page-title">游戏对比</h1>
      <el-button @click="goBack">返回</el-button>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="games.length < 2" class="empty-state">
      <el-empty description="至少需要 2 款游戏才能进行对比">
        <el-button type="primary" @click="goStore">前往商城选择游戏</el-button>
      </el-empty>
    </div>

    <div v-else class="compare-table-wrapper">
      <table class="compare-table">
        <thead>
          <tr>
            <th class="label-col">对比项</th>
            <th v-for="game in games" :key="game.id" class="game-col">
              <div class="game-header">
                <img v-lazy="game.coverImage || '/placeholder.jpg'" :alt="game.title" class="header-cover" />
                <div class="header-info">
                  <span class="header-title">{{ game.title }}</span>
                  <el-icon class="header-remove" @click="handleRemove(game.id)"><Close /></el-icon>
                </div>
              </div>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td class="label-col">封面</td>
            <td v-for="game in games" :key="game.id" class="game-col">
              <img v-lazy="game.bannerImage || game.coverImage || '/placeholder.jpg'" :alt="game.title" class="banner-img" />
            </td>
          </tr>
          <tr :class="{ 'diff-row': hasDiff(games, g => g.originalPrice) }">
            <td class="label-col">原价</td>
            <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isDiff(games, game, g => g.originalPrice) }">
              {{ game.originalPrice === 0 ? '免费' : `¥${game.originalPrice}` }}
            </td>
          </tr>
          <tr :class="{ 'diff-row': hasDiff(games, g => g.discountPrice ?? g.originalPrice) }">
            <td class="label-col">现价</td>
            <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isDiff(games, game, g => g.discountPrice ?? g.originalPrice) }">
              <template v-if="game.originalPrice === 0">
                <span class="free-price">免费游玩</span>
              </template>
              <template v-else-if="game.discountPercent && game.discountPercent > 0">
                <span class="discount-tag">-{{ game.discountPercent }}%</span>
                <span class="discount-price">¥{{ game.discountPrice }}</span>
                <span class="original-price-line">¥{{ game.originalPrice }}</span>
              </template>
              <template v-else>
                ¥{{ game.originalPrice }}
              </template>
            </td>
          </tr>
          <tr :class="{ 'diff-row': hasDiff(games, g => g.rating) }">
            <td class="label-col">评分</td>
            <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isDiff(games, game, g => g.rating) }">
              <div v-if="game.rating" class="rating-cell">
                <el-rate :model-value="game.rating" disabled :max="5" size="small" />
                <span class="rating-num">{{ game.rating }}</span>
              </div>
              <span v-else class="no-data">暂无评分</span>
            </td>
          </tr>
          <tr :class="{ 'diff-row': hasDiff(games, g => g.developer) }">
            <td class="label-col">开发商</td>
            <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isDiff(games, game, g => g.developer) }">
              {{ game.developer || '—' }}
            </td>
          </tr>
          <tr :class="{ 'diff-row': hasDiff(games, g => g.publisher) }">
            <td class="label-col">发行商</td>
            <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isDiff(games, game, g => g.publisher) }">
              {{ game.publisher || '—' }}
            </td>
          </tr>
          <tr :class="{ 'diff-row': hasDiff(games, g => g.releaseDate) }">
            <td class="label-col">发售日期</td>
            <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isDiff(games, game, g => g.releaseDate) }">
              {{ game.releaseDate || '—' }}
            </td>
          </tr>
          <tr :class="{ 'diff-row': hasDiff(games, g => g.tags) }">
            <td class="label-col">分类标签</td>
            <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isDiff(games, game, g => g.tags) }">
              <div v-if="parseTags(game.tags).length" class="tags-cell">
                <el-tag v-for="tag in parseTags(game.tags)" :key="tag" size="small" effect="plain">
                  {{ tag }}
                </el-tag>
              </div>
              <span v-else class="no-data">—</span>
            </td>
          </tr>
          <tr v-if="hasAnyRequirements()" class="section-row">
            <td :colspan="games.length + 1" class="section-label">系统配置</td>
          </tr>
          <template v-for="field in requirementFields" :key="field.key">
            <tr v-if="hasAnyReqField(field.key)" :class="{ 'diff-row': hasReqDiff(field.key) }">
              <td class="label-col">{{ field.label }}</td>
              <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isReqDiff(game, field.key) }">
                {{ getReqValue(game, 'min', field.key) || '—' }}
              </td>
            </tr>
          </template>
          <template v-for="field in requirementFields" :key="'rec-' + field.key">
            <tr v-if="hasAnyReqField(field.key, true)" :class="{ 'diff-row': hasReqDiff(field.key, true) }">
              <td class="label-col">推荐{{ field.label }}</td>
              <td v-for="game in games" :key="game.id" class="game-col" :class="{ 'diff-cell': isReqDiff(game, field.key, true) }">
                {{ getReqValue(game, 'rec', field.key) || '—' }}
              </td>
            </tr>
          </template>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCompareStore } from '@/store/compare'
import { Close } from '@element-plus/icons-vue'
import type { Game } from '@/types'

const router = useRouter()
const compareStore = useCompareStore()

const loading = ref(true)
const games = computed(() => compareStore.selectedGames)

const requirementFields = [
  { key: 'os', label: '操作系统' },
  { key: 'cpu', label: '处理器' },
  { key: 'ram', label: '内存' },
  { key: 'gpu', label: '显卡' },
  { key: 'storage', label: '存储空间' }
]

onMounted(async () => {
  await compareStore.fetchSelectedGames()
  loading.value = false
})

function goBack() {
  router.back()
}

function goStore() {
  router.push('/store')
}

function handleRemove(gameId: number) {
  compareStore.removeGame(gameId)
}

function parseTags(tags: string | undefined): string[] {
  if (!tags) return []
  try {
    return JSON.parse(tags)
  } catch {
    return []
  }
}

function parseRequirements(req: string | undefined): Record<string, string> {
  if (!req) return {}
  try {
    return JSON.parse(req)
  } catch {
    return {}
  }
}

function hasDiff(items: Game[], getter: (g: Game) => any): boolean {
  const values = items.map(getter)
  const first = values[0]
  return values.some(v => v !== first)
}

function isDiff(items: Game[], item: Game, getter: (g: Game) => any): boolean {
  if (!hasDiff(items, getter)) return false
  const values = items.map(getter)
  const current = getter(item)
  const uniqueValues = [...new Set(values)]
  return uniqueValues.length > 1 && uniqueValues.indexOf(current) !== -1 && uniqueValues.length > 1
}

function getReqValue(game: Game, prefix: 'min' | 'rec', key: string): string {
  const raw = prefix === 'min' ? game.minRequirements : game.recRequirements
  const parsed = parseRequirements(raw)
  return parsed[key] || ''
}

function hasAnyRequirements(): boolean {
  return games.value.some(g => g.minRequirements || g.recRequirements)
}

function hasAnyReqField(key: string, isRec = false): boolean {
  return games.value.some(g => {
    return !!getReqValue(g, isRec ? 'rec' : 'min', key)
  })
}

function hasReqDiff(key: string, isRec = false): boolean {
  const values = games.value.map(g => getReqValue(g, isRec ? 'rec' : 'min', key))
  const nonEmpty = values.filter(v => v)
  if (nonEmpty.length < 2) return false
  return new Set(nonEmpty).size > 1
}

function isReqDiff(game: Game, key: string, isRec = false): boolean {
  if (!hasReqDiff(key, isRec)) return false
  const values = games.value.map(g => getReqValue(g, isRec ? 'rec' : 'min', key))
  const current = getReqValue(game, isRec ? 'rec' : 'min', key)
  const nonEmpty = values.filter(v => v)
  return new Set(nonEmpty).size > 1 && nonEmpty.includes(current)
}
</script>

<style lang="scss" scoped>
.compare-page {
  padding-bottom: 80px;
}

.compare-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-white);
}

.loading-container {
  padding: 40px;
}

.empty-state {
  padding: 80px 0;
}

.compare-table-wrapper {
  overflow-x: auto;
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  background: var(--bg-card);
}

.compare-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 600px;

  th, td {
    padding: 14px 16px;
    text-align: left;
    border-bottom: 1px solid var(--border-color);
    vertical-align: top;
  }

  thead th {
    background: rgba(0, 0, 0, 0.3);
    position: sticky;
    top: 0;
    z-index: 1;
  }

  .label-col {
    width: 140px;
    min-width: 140px;
    font-weight: 600;
    color: var(--text-secondary);
    background: rgba(0, 0, 0, 0.15);
    white-space: nowrap;
  }

  .game-col {
    color: var(--text-primary);
    min-width: 200px;
  }

  .diff-row {
    .label-col {
      color: var(--steam-light-blue);
    }
  }

  .diff-cell {
    background: rgba(102, 192, 244, 0.08);
    border-left: 3px solid var(--steam-light-blue);
  }

  .section-row {
    .section-label {
      background: rgba(0, 0, 0, 0.3);
      color: var(--steam-light-blue);
      font-weight: 700;
      font-size: 15px;
      padding: 12px 16px;
      text-align: left;
    }
  }
}

.game-header {
  display: flex;
  align-items: center;
  gap: 10px;

  .header-cover {
    width: 60px;
    height: 28px;
    object-fit: cover;
    border-radius: 3px;
  }

  .header-info {
    display: flex;
    align-items: center;
    gap: 6px;
  }

  .header-title {
    color: var(--text-white);
    font-weight: 600;
    font-size: 14px;
  }

  .header-remove {
    cursor: pointer;
    color: var(--text-secondary);
    transition: color 0.2s;

    &:hover {
      color: #f56c6c;
    }
  }
}

.banner-img {
  width: 100%;
  max-width: 280px;
  border-radius: var(--radius-sm);
  object-fit: cover;
}

.free-price {
  color: var(--steam-green);
  font-weight: 600;
}

.discount-tag {
  background: var(--steam-green);
  color: var(--steam-darker);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-weight: 700;
  font-size: 12px;
  margin-right: 6px;
}

.discount-price {
  color: var(--steam-green);
  font-weight: 600;
}

.original-price-line {
  color: var(--text-secondary);
  text-decoration: line-through;
  font-size: 12px;
  margin-left: 6px;
}

.rating-cell {
  display: flex;
  align-items: center;
  gap: 6px;

  .rating-num {
    color: var(--text-secondary);
    font-size: 13px;
  }
}

.tags-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.no-data {
  color: var(--text-secondary);
  font-style: italic;
}

@media (max-width: 768px) {
  .compare-table {
    .label-col {
      width: 100px;
      min-width: 100px;
      font-size: 13px;
    }

    .game-col {
      min-width: 160px;
      font-size: 13px;
    }
  }

  .banner-img {
    max-width: 160px;
  }
}
</style>
