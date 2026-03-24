<template>
  <div class="page-stack">
    <PageSection title="系统配置" description="集中维护预警阈值、审批策略和消息通知参数。">
      <el-form :model="form" label-width="140px" class="single-form wide">
        <el-form-item label="低库存预警阈值"><el-input-number v-model="form.inventoryWarning" :min="1" /></el-form-item>
        <el-form-item label="超期订单预警"><el-input-number v-model="form.orderTimeout" :min="1" /></el-form-item>
        <el-form-item label="异常审批级别"><el-select v-model="form.auditLevel"><el-option label="高" value="HIGH" /><el-option label="中" value="MEDIUM" /><el-option label="低" value="LOW" /></el-select></el-form-item>
        <el-form-item label="消息提醒方式"><el-checkbox-group v-model="form.notifyChannels"><el-checkbox label="站内消息" /><el-checkbox label="短信" /><el-checkbox label="邮件" /></el-checkbox-group></el-form-item>
        <el-form-item>
          <el-button v-permission="'admin:config:view'" type="primary" @click="handleSave">实时保存</el-button>
        </el-form-item>
      </el-form>
    </PageSection>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'

const form = reactive({ inventoryWarning: 15, orderTimeout: 3, auditLevel: 'HIGH', notifyChannels: ['站内消息', '邮件'] })
const handleSave = () => ElMessage.success('系统配置已保存')
</script>
