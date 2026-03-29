<template>
  <div class="page-stack">
    <PageSection title="消息中心" description="集中查看系统通知、库存预警和业务提醒。">
      <template #actions>
        <el-button type="primary" @click="markAllRead">全部已读</el-button>
      </template>
      <div class="toolbar">
        <el-input v-model="keyword" placeholder="搜索标题或内容" clearable class="filter-item" />
        <el-select v-model="readStatus" placeholder="读取状态" clearable class="filter-item">
          <el-option label="未读" value="unread" />
          <el-option label="已读" value="read" />
        </el-select>
      </div>
      <AppTable :columns="columns" :rows="filteredRows" :loading="loading" empty-text="暂无消息">
        <template #isRead="{ value }">
          <el-tag :type="value ? 'success' : 'warning'">{{ value ? '已读' : '未读' }}</el-tag>
        </template>
        <template #actions="{ row }">
          <el-button link type="primary" :disabled="row.isRead" @click="markRead(row.id)">标记已读</el-button>
        </template>
      </AppTable>
    </PageSection>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchMessages, readMessage } from '../../api/message'
import { useAppStore } from '../../stores/app'

const appStore = useAppStore()
const loading = ref(false)
const keyword = ref('')
const readStatus = ref('')
const rows = ref([])
const columns = [
  { key: 'title', label: '标题', minWidth: 220 },
  { key: 'messageType', label: '类型', width: 120 },
  { key: 'content', label: '内容', minWidth: 280 },
  { key: 'isRead', label: '读取状态', width: 100 },
  { key: 'createdAt', label: '创建时间', minWidth: 180 }
]

const filteredRows = computed(() => rows.value.filter((item) => {
  const hitKeyword = !keyword.value || `${item.title || ''}${item.content || ''}`.includes(keyword.value)
  const hitStatus = !readStatus.value || (readStatus.value === 'read' ? Boolean(item.isRead) : !item.isRead)
  return hitKeyword && hitStatus
}))

const syncNotificationStore = () => {
  appStore.setNotifications(rows.value.map((item) => ({
    id: String(item.id),
    title: item.title,
    content: item.content,
    type: item.messageType === 'ALERT' ? 'danger' : 'warning',
    read: Boolean(item.isRead)
  })))
}

const loadMessages = async () => {
  loading.value = true
  try {
    const result = await fetchMessages()
    rows.value = result.data || []
    syncNotificationStore()
  } finally {
    loading.value = false
  }
}

const markRead = async (id) => {
  await readMessage(id)
  ElMessage.success('消息已标记为已读')
  await loadMessages()
}

const markAllRead = async () => {
  const targets = rows.value.filter((item) => !item.isRead)
  await Promise.all(targets.map((item) => readMessage(item.id)))
  ElMessage.success('全部消息已标记为已读')
  await loadMessages()
}

onMounted(loadMessages)
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.filter-item { width: 260px; }
</style>
