<template>
  <Transition name="slide-up">
    <div v-if="compareStore.count > 0" class="compare-bar">
      <div class="compare-bar-inner">
        <div class="selected-games">
          <div
            v-for="game in compareStore.selectedGames"
            :key="game.id"
            class="selected-game-chip"
          >
            <img v-lazy="game.coverImage" :alt="game.title" class="chip-cover" />
            <span class="chip-title">{{ game.title }}</span>
            <el-icon class="chip-remove" @click="compareStore.removeGame(game.id)">
              <Close />
            </el-icon>
          </div>
          <div v-if="compareStore.count < compareStore.MAX_COMPARE" class="add-hint">
            还可添加 {{ compareStore.MAX_COMPARE - compareStore.count }} 款
          </div>
        </div>
        <div class="compare-actions">
          <el-button size="small" @click="compareStore.clearAll">清空</el-button>
          <el-button
            type="primary"
            size="small"
            :disabled="!compareStore.canCompare"
            @click="goCompare"
          >
            开始对比 ({{ compareStore.count }}/{{ compareStore.MAX_COMPARE }})
          </el-button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useCompareStore } from '@/store/compare'
import { Close } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const compareStore = useCompareStore()

function goCompare() {
  if (!compareStore.canCompare) {
    ElMessage.warning(`至少选择 ${compareStore.MIN_COMPARE} 款游戏才能对比`)
    return
  }
  router.push('/compare')
}
</script>

<style lang="scss" scoped>
.compare-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background: linear-gradient(135deg, rgba(27, 40, 56, 0.98), rgba(42, 71, 94, 0.98));
  backdrop-filter: blur(12px);
  border-top: 2px solid var(--steam-light-blue);
  box-shadow: 0 -4px 24px rgba(0, 0, 0, 0.5);
}

.compare-bar-inner {
  max-width: 1400px;
  margin: 0 auto;
  padding: 12px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.selected-games {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 1;
  overflow-x: auto;
}

.selected-game-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 6px 10px;
  white-space: nowrap;
  flex-shrink: 0;

  .chip-cover {
    width: 40px;
    height: 19px;
    object-fit: cover;
    border-radius: 2px;
  }

  .chip-title {
    color: var(--text-primary);
    font-size: 13px;
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .chip-remove {
    cursor: pointer;
    color: var(--text-secondary);
    transition: color 0.2s;

    &:hover {
      color: var(--steam-light-blue);
    }
  }
}

.add-hint {
  color: var(--text-secondary);
  font-size: 13px;
  flex-shrink: 0;
}

.compare-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  transform: translateY(100%);
}

@media (max-width: 768px) {
  .compare-bar-inner {
    padding: 10px 12px;
  }

  .selected-game-chip .chip-title {
    max-width: 80px;
  }
}
</style>
