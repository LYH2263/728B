import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Game } from '@/types'
import { gameApi } from '@/api'
import { ElMessage } from 'element-plus'

const MIN_COMPARE = 2
const MAX_COMPARE = 4

export const useCompareStore = defineStore('compare', () => {
  const selectedIds = ref<number[]>([])
  const selectedGames = ref<Game[]>([])
  const loading = ref(false)

  const count = computed(() => selectedIds.value.length)
  const canCompare = computed(() => selectedIds.value.length >= MIN_COMPARE)
  const isFull = computed(() => selectedIds.value.length >= MAX_COMPARE)

  function isSelected(gameId: number): boolean {
    return selectedIds.value.includes(gameId)
  }

  async function toggleSelect(gameId: number) {
    if (isSelected(gameId)) {
      removeGame(gameId)
    } else {
      addGame(gameId)
    }
  }

  async function addGame(gameId: number) {
    if (isFull.value) {
      ElMessage.warning(`最多只能对比 ${MAX_COMPARE} 款游戏`)
      return
    }
    if (isSelected(gameId)) return
    selectedIds.value.push(gameId)
    await fetchSelectedGames()
  }

  function removeGame(gameId: number) {
    selectedIds.value = selectedIds.value.filter(id => id !== gameId)
    selectedGames.value = selectedGames.value.filter(g => g.id !== gameId)
  }

  function clearAll() {
    selectedIds.value = []
    selectedGames.value = []
  }

  async function fetchSelectedGames() {
    if (selectedIds.value.length === 0) {
      selectedGames.value = []
      return
    }
    loading.value = true
    try {
      const res = await gameApi.getGamesByIds(selectedIds.value)
      selectedGames.value = res.data.data || []
    } catch {
      selectedGames.value = []
    } finally {
      loading.value = false
    }
  }

  return {
    selectedIds,
    selectedGames,
    loading,
    count,
    canCompare,
    isFull,
    MIN_COMPARE,
    MAX_COMPARE,
    isSelected,
    toggleSelect,
    addGame,
    removeGame,
    clearAll,
    fetchSelectedGames
  }
}, {
  persist: {
    key: 'steam-compare',
    paths: ['selectedIds']
  }
})
