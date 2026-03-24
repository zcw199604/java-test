<template>
  <div>
    <el-table v-loading="loading" :data="pagedRows" stripe border>
      <template v-for="column in columns" :key="column.key">
        <el-table-column
          :prop="column.key"
          :label="column.label"
          :min-width="column.minWidth || 120"
          :width="column.width"
          :fixed="column.fixed"
        >
          <template #default="scope">
            <slot :name="column.key" :row="scope.row" :value="scope.row[column.key]">
              <el-tag v-if="['status', 'bizType'].includes(column.key) && scope.row[column.key]" :type="statusTypeMap[String(scope.row[column.key])] || 'info'">
                {{ statusLabelMap[String(scope.row[column.key])] || scope.row[column.key] }}
              </el-tag>
              <span v-else>{{ scope.row[column.key] ?? '--' }}</span>
            </slot>
          </template>
        </el-table-column>
      </template>
      <el-table-column v-if="$slots.actions" label="操作" fixed="right" width="220">
        <template #default="scope">
          <slot name="actions" :row="scope.row" />
        </template>
      </el-table-column>
      <template #empty>
        <el-empty :description="emptyText" />
      </template>
    </el-table>
    <div v-if="pagination" class="table-pagination">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="rows.length"
        :page-size="pageSize"
        v-model:current-page="page"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { statusLabelMap, statusTypeMap } from '../utils/format'

export interface TableColumnConfig {
  key: string
  label: string
  minWidth?: number
  width?: number
  fixed?: 'left' | 'right'
}

const props = withDefaults(defineProps<{
  columns: TableColumnConfig[]
  rows: Array<Record<string, unknown>>
  pagination?: boolean
  pageSize?: number
  loading?: boolean
  emptyText?: string
}>(), {
  pagination: true,
  pageSize: 8,
  loading: false,
  emptyText: '暂无数据'
})

const page = ref(1)
watch(() => props.rows.length, () => {
  page.value = 1
})

const pagedRows = computed(() => {
  if (!props.pagination) return props.rows
  const start = (page.value - 1) * props.pageSize
  return props.rows.slice(start, start + props.pageSize)
})
</script>
