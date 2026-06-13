<template>
  <div class="game-qa-section">
    <div class="qa-header">
      <h2 class="section-title">
        <el-icon><ChatDotRound /></el-icon>
        玩家问答
      </h2>
      <el-button type="primary" :icon="Edit" @click="showAskDialog = true">
        我要提问
      </el-button>
    </div>

    <div class="filter-bar">
      <el-radio-group v-model="currentFilter" size="default" @change="fetchQuestions">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="pending">待解决</el-radio-button>
        <el-radio-button label="resolved">已解决</el-radio-button>
        <el-radio-button label="hot">热门</el-radio-button>
      </el-radio-group>
    </div>

    <div v-loading="questionsLoading" class="questions-list">
      <template v-if="questions.length">
        <div
          v-for="q in questions"
          :key="q.id"
          class="question-card"
          @click="openQuestionDetail(q)"
        >
          <div class="question-title-row">
            <h3 class="question-title">{{ q.title }}</h3>
            <el-tag
              v-if="q.isResolved === 1"
              type="success"
              effect="light"
              size="small"
              round
            >
              <el-icon><CircleCheck /></el-icon>
              已解决
            </el-tag>
            <el-tag
              v-else
              type="warning"
              effect="light"
              size="small"
              round
            >
              待解决
            </el-tag>
          </div>
          <p v-if="q.content" class="question-content">{{ q.content }}</p>
          <div class="question-meta">
            <div class="meta-left">
              <el-avatar :size="24" :src="getAvatarUrl(q.user?.avatar)">
                {{ q.user?.nickname?.charAt(0) || q.user?.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ q.user?.nickname || q.user?.username }}</span>
              <span class="meta-sep">·</span>
              <span class="meta-time">{{ formatDate(q.createdAt) }}</span>
            </div>
            <div class="meta-right">
              <span class="meta-stat">
                <el-icon><ChatDotRound /></el-icon>
                {{ q.answerCount }} 回答
              </span>
              <span class="meta-stat">
                <el-icon><View /></el-icon>
                {{ q.viewCount }} 浏览
              </span>
            </div>
          </div>
        </div>

        <div v-if="hasMore" class="load-more">
          <el-button :loading="loadingMore" @click="loadMore">加载更多</el-button>
        </div>
      </template>

      <el-empty v-else description="暂无问题，快来提第一个问题吧！">
        <el-button type="primary" @click="showAskDialog = true">我要提问</el-button>
      </el-empty>
    </div>

    <el-drawer
      v-model="detailDrawerVisible"
      :title="currentQuestion?.title || '问题详情'"
      direction="rtl"
      size="60%"
      :destroy-on-close="true"
    >
      <div v-if="currentQuestion" class="question-detail">
        <div class="detail-header">
          <div class="status-row">
            <el-tag
              v-if="currentQuestion.isResolved === 1"
              type="success"
              effect="light"
              size="default"
            >
              <el-icon><CircleCheck /></el-icon>
              已解决
            </el-tag>
            <el-tag
              v-else
              type="warning"
              effect="light"
              size="default"
            >
              待解决
            </el-tag>
            <span class="view-count">
              <el-icon><View /></el-icon>
              {{ currentQuestion.viewCount }} 浏览
            </span>
          </div>
          <h2 class="detail-title">{{ currentQuestion.title }}</h2>
          <div class="questioner-info">
            <el-avatar :size="32" :src="getAvatarUrl(currentQuestion.user?.avatar)">
              {{ currentQuestion.user?.nickname?.charAt(0) || currentQuestion.user?.username?.charAt(0) || 'U' }}
            </el-avatar>
            <span class="user-name">{{ currentQuestion.user?.nickname || currentQuestion.user?.username }}</span>
            <span class="meta-sep">·</span>
            <span class="meta-time">{{ formatDate(currentQuestion.createdAt) }}</span>
          </div>
          <p v-if="currentQuestion.content" class="detail-content">{{ currentQuestion.content }}</p>
        </div>

        <el-divider />

        <div class="answers-section">
          <div class="answers-header">
            <h3>
              <el-icon><ChatLineRound /></el-icon>
              回答 ({{ answers.length }})
            </h3>
          </div>

          <div v-loading="answersLoading" class="answers-list">
            <template v-if="answers.length">
              <div
                v-for="a in answers"
                :key="a.id"
                class="answer-card"
                :class="{ 'answer-adopted': a.isAdopted === 1 }"
              >
                <div v-if="a.isAdopted === 1" class="adopted-badge">
                  <el-icon color="#67c23a"><Medal /></el-icon>
                  最佳答案
                </div>
                <div class="answer-header">
                  <el-avatar :size="32" :src="getAvatarUrl(a.user?.avatar)">
                    {{ a.user?.nickname?.charAt(0) || a.user?.username?.charAt(0) || 'U' }}
                  </el-avatar>
                  <div class="answer-user">
                    <span class="user-name">{{ a.user?.nickname || a.user?.username }}</span>
                    <span class="answer-time">{{ formatDate(a.createdAt) }}</span>
                  </div>
                </div>
                <p class="answer-content">{{ a.content }}</p>
                <div class="answer-actions">
                  <el-button
                    v-if="canAdopt(a)"
                    type="success"
                    size="small"
                    :icon="Medal"
                    :disabled="currentQuestion.isResolved === 1"
                    @click.stop="handleAdopt(a.id)"
                  >
                    {{ a.isAdopted === 1 ? '已采纳' : '采纳为最佳答案' }}
                  </el-button>
                  <el-button
                    text
                    size="small"
                    :type="a.isLiked === 1 ? 'primary' : 'default'"
                    @click.stop="handleLike(a)"
                  >
                    <el-icon><component :is="a.isLiked === 1 ? StarFilled : Star" /></el-icon>
                    有用 ({{ a.likeCount }})
                  </el-button>
                </div>
              </div>
            </template>
            <el-empty v-else description="暂无回答，快来抢沙发吧！" />
          </div>
        </div>

        <el-divider />

        <div class="answer-form-section" v-if="currentQuestion.isResolved !== 1">
          <h4>
            <el-icon><Edit /></el-icon>
            撰写回答
          </h4>
          <el-form @submit.prevent="handleSubmitAnswer">
            <el-input
              v-model="newAnswerContent"
              type="textarea"
              :rows="4"
              placeholder="请输入你的回答..."
              maxlength="2000"
              show-word-limit
            />
            <div class="form-actions">
              <el-button
                type="primary"
                :icon="Promotion"
                :loading="submittingAnswer"
                :disabled="!newAnswerContent.trim()"
                @click="handleSubmitAnswer"
              >
                提交回答
              </el-button>
            </div>
          </el-form>
        </div>
        <el-alert
          v-else
          title="该问题已解决，无法再回答"
          type="success"
          :closable="false"
          show-icon
        />
      </div>
    </el-drawer>

    <el-dialog
      v-model="showAskDialog"
      title="发布问题"
      width="520px"
      :destroy-on-close="true"
    >
      <el-form @submit.prevent="handleSubmitQuestion" label-position="top">
        <el-form-item label="问题标题" required>
          <el-input
            v-model="newQuestionTitle"
            placeholder="例如：配置够不够玩？支持手柄吗？"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="问题描述（可选）">
          <el-input
            v-model="newQuestionContent"
            type="textarea"
            :rows="4"
            placeholder="详细描述你的问题，以便其他玩家更好地回答..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAskDialog = false">取消</el-button>
        <el-button
          type="primary"
          :loading="submittingQuestion"
          :disabled="!newQuestionTitle.trim()"
          @click="handleSubmitQuestion"
        >
          发布问题
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { gameQaApi } from '@/api'
import type { GameQuestion, GameAnswer, QAFilterType } from '@/types'
import { useUserStore } from '@/store/user'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ChatDotRound,
  Edit,
  CircleCheck,
  View,
  ChatLineRound,
  Star,
  StarFilled,
  Medal,
  Promotion
} from '@element-plus/icons-vue'

const props = defineProps<{
  gameId: number
}>()

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const questionsLoading = ref(false)
const questions = ref<GameQuestion[]>([])
const currentFilter = ref<QAFilterType>('all')
const page = ref(1)
const pageSize = 10
const total = ref(0)
const loadingMore = ref(false)

const detailDrawerVisible = ref(false)
const currentQuestion = ref<GameQuestion | null>(null)
const answersLoading = ref(false)
const answers = ref<GameAnswer[]>([])

const showAskDialog = ref(false)
const newQuestionTitle = ref('')
const newQuestionContent = ref('')
const submittingQuestion = ref(false)

const newAnswerContent = ref('')
const submittingAnswer = ref(false)

const hasMore = computed(() => questions.value.length < total.value)

function getAvatarUrl(avatar: string | undefined): string {
  if (!avatar || avatar === '/avatars/default.png') {
    return '/avatars/default.svg'
  }
  return avatar
}

function formatDate(date: string) {
  return new Date(date).toLocaleDateString('zh-CN')
}

function canAdopt(_answer: GameAnswer): boolean {
  return (
    userStore.isLoggedIn &&
    currentQuestion.value !== null &&
    currentQuestion.value.userId === userStore.userInfo?.id
  )
}

async function fetchQuestions(reset = true) {
  if (reset) {
    questionsLoading.value = true
    page.value = 1
    questions.value = []
  } else {
    loadingMore.value = true
  }

  try {
    const res = await gameQaApi.getQuestions(
      props.gameId,
      currentFilter.value,
      page.value,
      pageSize
    )
    const data = res.data.data
    if (reset) {
      questions.value = data.list
    } else {
      questions.value = [...questions.value, ...data.list]
    }
    total.value = data.total
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    questionsLoading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (loadingMore.value || !hasMore.value) return
  page.value++
  fetchQuestions(false)
}

async function openQuestionDetail(question: GameQuestion) {
  currentQuestion.value = question
  detailDrawerVisible.value = true
  answersLoading.value = true
  answers.value = []

  try {
    const [detailRes, answersRes] = await Promise.all([
      gameQaApi.getQuestionDetail(question.id),
      gameQaApi.getAnswers(question.id)
    ])
    currentQuestion.value = detailRes.data.data
    answers.value = answersRes.data.data
  } catch (error) {
    // 错误已处理
  } finally {
    answersLoading.value = false
  }
}

async function handleSubmitQuestion() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (!newQuestionTitle.value.trim()) {
    ElMessage.warning('请输入问题标题')
    return
  }

  submittingQuestion.value = true
  try {
    await gameQaApi.createQuestion({
      gameId: props.gameId,
      title: newQuestionTitle.value.trim(),
      content: newQuestionContent.value.trim() || undefined
    })
    ElMessage.success('提问成功')
    showAskDialog.value = false
    newQuestionTitle.value = ''
    newQuestionContent.value = ''
    fetchQuestions()
  } catch (error) {
    // 错误已处理
  } finally {
    submittingQuestion.value = false
  }
}

async function handleSubmitAnswer() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  if (!newAnswerContent.value.trim()) {
    ElMessage.warning('请输入回答内容')
    return
  }
  if (!currentQuestion.value) return

  submittingAnswer.value = true
  try {
    await gameQaApi.createAnswer({
      questionId: currentQuestion.value.id,
      content: newAnswerContent.value.trim()
    })
    ElMessage.success('回答成功')
    newAnswerContent.value = ''
    // 刷新问题详情和回答列表
    const [detailRes, answersRes] = await Promise.all([
      gameQaApi.getQuestionDetail(currentQuestion.value.id),
      gameQaApi.getAnswers(currentQuestion.value.id)
    ])
    currentQuestion.value = detailRes.data.data
    answers.value = answersRes.data.data
    // 同时刷新列表
    fetchQuestions()
  } catch (error) {
    // 错误已处理
  } finally {
    submittingAnswer.value = false
  }
}

async function handleLike(answer: GameAnswer) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  try {
    await gameQaApi.toggleLikeAnswer(answer.id)
    // 本地更新状态
    const idx = answers.value.findIndex(a => a.id === answer.id)
    if (idx !== -1) {
      const updated = { ...answers.value[idx] }
      if (updated.isLiked === 1) {
        updated.isLiked = 0
        updated.likeCount = Math.max(0, (updated.likeCount || 0) - 1)
      } else {
        updated.isLiked = 1
        updated.likeCount = (updated.likeCount || 0) + 1
      }
      answers.value.splice(idx, 1, updated)
    }
  } catch (error) {
    // 错误已处理
  }
}

async function handleAdopt(answerId: number) {
  try {
    await ElMessageBox.confirm(
      '确定将此回答采纳为最佳答案吗？采纳后问题将标记为已解决。',
      '采纳确认',
      { type: 'success' }
    )
    await gameQaApi.adoptAnswer(answerId)
    ElMessage.success('采纳成功')
    // 刷新
    if (currentQuestion.value) {
      const [detailRes, answersRes] = await Promise.all([
        gameQaApi.getQuestionDetail(currentQuestion.value.id),
        gameQaApi.getAnswers(currentQuestion.value.id)
      ])
      currentQuestion.value = detailRes.data.data
      answers.value = answersRes.data.data
    }
    fetchQuestions()
  } catch (error: any) {
    if (error !== 'cancel') {
      // 错误已处理
    }
  }
}

onMounted(() => {
  fetchQuestions()
})
</script>

<style lang="scss" scoped>
.game-qa-section {
  background: var(--bg-card);
  border-radius: var(--radius-lg);
  padding: 24px;
  border: 1px solid var(--border-color);
}

.qa-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  .section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    font-size: 20px;
    color: var(--text-white);
  }
}

.filter-bar {
  margin-bottom: 20px;
}

.questions-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.question-card {
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 18px 20px;
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: all 0.25s ease;

  &:hover {
    border-color: var(--steam-light-blue);
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(102, 192, 244, 0.1);
  }
}

.question-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;

  .question-title {
    flex: 1;
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-white);
  }
}

.question-content {
  color: var(--text-light);
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.question-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;

  .meta-left,
  .meta-right {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .meta-right {
    gap: 16px;
  }

  .user-name {
    color: var(--text-primary);
  }

  .meta-sep {
    color: var(--text-secondary);
  }

  .meta-time,
  .meta-stat {
    color: var(--text-secondary);
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.load-more {
  text-align: center;
  padding-top: 8px;
}

.question-detail {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.detail-header {
  .status-row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .view-count {
      display: flex;
      align-items: center;
      gap: 4px;
      color: var(--text-secondary);
      font-size: 13px;
    }
  }

  .detail-title {
    margin: 0 0 12px 0;
    font-size: 22px;
    font-weight: 700;
    color: var(--text-white);
  }

  .questioner-info {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 14px;

    .user-name {
      color: var(--text-primary);
      font-weight: 500;
    }

    .meta-sep {
      color: var(--text-secondary);
    }

    .meta-time {
      color: var(--text-secondary);
      font-size: 13px;
    }
  }

  .detail-content {
    color: var(--text-light);
    font-size: 14px;
    line-height: 1.7;
    white-space: pre-wrap;
  }
}

.answers-section {
  .answers-header {
    margin-bottom: 16px;

    h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0;
      font-size: 16px;
      color: var(--text-white);
    }
  }
}

.answers-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.answer-card {
  position: relative;
  background: var(--bg-secondary);
  border-radius: var(--radius-md);
  padding: 18px;
  border: 1px solid var(--border-color);

  &.answer-adopted {
    border-color: var(--steam-green);
    background: rgba(103, 194, 58, 0.06);

    .adopted-badge {
      display: flex;
      align-items: center;
      gap: 4px;
      position: absolute;
      top: -10px;
      right: 14px;
      background: var(--steam-green);
      color: #fff;
      padding: 3px 10px;
      border-radius: 12px;
      font-size: 12px;
      font-weight: 600;
    }
  }

  .adopted-badge {
    display: none;
  }
}

.answer-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;

  .answer-user {
    display: flex;
    flex-direction: column;

    .user-name {
      color: var(--text-white);
      font-weight: 500;
    }

    .answer-time {
      color: var(--text-secondary);
      font-size: 12px;
    }
  }
}

.answer-content {
  color: var(--text-light);
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.answer-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.answer-form-section {
  h4 {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 0 0 12px 0;
    color: var(--text-white);
  }

  .form-actions {
    margin-top: 12px;
    text-align: right;
  }
}

@media (max-width: 768px) {
  .game-qa-section {
    padding: 16px;
  }

  .question-meta {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
