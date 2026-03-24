<template>
  <div class="page-stack">
    <PageSection title="角色权限" description="通过权限树批量分配模块访问与操作能力。">
      <div class="two-column-grid compact">
        <el-card shadow="never">
          <div class="tree-title">角色列表</div>
          <el-radio-group v-model="currentRole">
            <el-radio-button label="ADMIN">平台管理员</el-radio-button>
            <el-radio-button label="PURCHASER">采购专员</el-radio-button>
            <el-radio-button label="SELLER">销售专员</el-radio-button>
            <el-radio-button label="KEEPER">库管人员</el-radio-button>
          </el-radio-group>
        </el-card>
        <el-card shadow="never">
          <div class="tree-title">权限树</div>
          <el-tree ref="treeRef" :data="treeData" show-checkbox node-key="id" default-expand-all />
          <div class="dialog-footer">
            <el-button v-permission="'admin:role:view'" type="primary" @click="handleSave">保存权限</el-button>
          </div>
        </el-card>
      </div>
    </PageSection>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageSection from '../../components/PageSection.vue'

const treeRef = ref(null)
const currentRole = ref('ADMIN')
const treeData = [
  { id: 1, label: '工作台', children: [{ id: 11, label: '驾驶舱' }, { id: 12, label: '个人中心' }] },
  { id: 2, label: '采购管理', children: [{ id: 21, label: '采购订单' }, { id: 22, label: '采购入库' }, { id: 23, label: '采购分析' }] },
  { id: 3, label: '销售管理', children: [{ id: 31, label: '销售订单' }, { id: 32, label: '销售出库' }, { id: 33, label: '销售绩效' }] },
  { id: 4, label: '库存管理', children: [{ id: 41, label: '库存总览' }, { id: 42, label: '库存流水' }, { id: 43, label: '库存盘点' }, { id: 44, label: '库存台账' }] },
  { id: 5, label: '统计追溯', children: [{ id: 51, label: '经营大屏' }, { id: 52, label: '合规追溯' }, { id: 53, label: '异常审核' }] }
]

const handleSave = () => {
  const checked = treeRef.value?.getCheckedKeys?.() || []
  ElMessage.success(`已为 ${currentRole.value} 保存 ${checked.length} 项权限`)
}
</script>
