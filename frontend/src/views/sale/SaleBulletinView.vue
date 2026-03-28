<template>
  <div class="page-stack">
    <PageSection title="销售信息发布" description="发布销售公告和促销信息，所有用户可查看。">
      <template #actions>
        <el-button v-permission="'bulletin:create'" type="primary" @click="showForm = true">发布公告</el-button>
      </template>
      <AppTable :columns="columns" :rows="rows">
        <template #category="{ value }"><el-tag :type="value === 'PROMOTION' ? 'warning' : 'info'">{{ value === 'PROMOTION' ? '促销' : '公告' }}</el-tag></template>
        <template #status="{ value }"><el-tag :type="value === 'PUBLISHED' ? 'success' : 'info'">{{ value === 'PUBLISHED' ? '已发布' : value }}</el-tag></template>
      </AppTable>
    </PageSection>

    <el-dialog v-model="showForm" title="发布公告" width="560px">
      <el-form label-position="top">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="请输入公告标题" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="公告" value="NOTICE" />
            <el-option label="促销" value="PROMOTION" />
          </el-select>
        </el-form-item>
        <el-form-item label="有效期"><el-date-picker v-model="form.expiredAt" type="datetime" placeholder="可选，留空则永久有效" value-format="YYYY-MM-DD HH:mm:ss" style="width: 100%" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入公告内容" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showForm = false">取消</el-button>
        <el-button type="primary" @click="handleCreate">发布</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'
import AppTable from '../../components/AppTable.vue'
import { fetchBulletins, createBulletin } from '../../api/bulletin'

const rows = ref([])
const showForm = ref(false)
const form = reactive({ title: '', content: '', category: 'NOTICE', expiredAt: '' })
const columns = [
  { key: 'title', label: '标题', minWidth: 200 },
  { key: 'category', label: '分类' },
  { key: 'status', label: '状态' },
  { key: 'createdBy', label: '发布人' },
  { key: 'createdAt', label: '发布时间', minWidth: 180 },
  { key: 'expiredAt', label: '有效期至', minWidth: 180 }
]

const loadData = async () => {
  const result = await fetchBulletins().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleCreate = async () => {
  if (!form.title) { ElMessage.warning('请输入公告标题'); return }
  await createBulletin({ title: form.title, content: form.content, category: form.category, expiredAt: form.expiredAt || null })
  ElMessage.success('发布成功')
  showForm.value = false
  form.title = ''
  form.content = ''
  form.category = 'NOTICE'
  form.expiredAt = ''
  loadData()
}

onMounted(loadData)
</script>
