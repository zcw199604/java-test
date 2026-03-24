<template>
  <div class="page-stack">
    <PageSection title="无权访问" description="当前账号未被授予此页面权限，可返回驾驶舱或联系管理员。">
      <el-result icon="warning" title="访问受限" sub-title="请使用具有对应权限的账号，或联系管理员调整授权。">
        <template #extra>
          <el-space>
            <el-button @click="router.push('/dashboard')">返回驾驶舱</el-button>
            <el-button type="primary" @click="router.push('/profile')">查看我的权限</el-button>
          </el-space>
        </template>
      </el-result>
    </PageSection>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import PageSection from '../../components/PageSection.vue'

const router = useRouter()
</script>
