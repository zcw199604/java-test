<template>
  <div class="page-stack">
    <PageSection title="系统配置" description="集中维护预警阈值、审批策略和消息通知参数。">
      <el-form label-width="140px" class="single-form wide">
        <el-form-item v-for="item in rows" :key="item.configKey" :label="item.configKey">
          <div class="config-row">
            <el-input v-model="item.configValue" />
            <el-button type="primary" @click="handleSave(item)">保存</el-button>
          </div>
          <div class="config-remark">{{ item.remark || '无备注' }}</div>
        </el-form-item>
      </el-form>
    </PageSection>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchConfigs, updateConfig } from '../../api/system'
import PageSection from '../../components/PageSection.vue'

const rows = ref([])

const loadData = async () => {
  const result = await fetchConfigs().catch(() => ({ data: [] }))
  rows.value = result.data || []
}

const handleSave = async (item) => {
  await updateConfig(item.configKey, { configValue: item.configValue, remark: item.remark, configGroup: item.configGroup })
  ElMessage.success('系统配置已保存')
}

onMounted(() => {
  loadData().catch(() => undefined)
})
</script>
