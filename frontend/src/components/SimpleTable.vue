<template>
  <div class="table-wrapper">
    <table>
      <thead>
        <tr>
          <th v-for="column in columns" :key="column.key">{{ column.label }}</th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="rows.length === 0">
          <td :colspan="columns.length" class="empty">暂无数据</td>
        </tr>
        <tr v-for="(row, index) in rows" :key="row.id || row.code || row.username || index">
          <td v-for="column in columns" :key="column.key">
            {{ formatCell(row, column) }}
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
const props = defineProps({
  columns: {
    type: Array,
    default: () => []
  },
  rows: {
    type: Array,
    default: () => []
  }
})

const formatCell = (row, column) => {
  if (typeof column.formatter === 'function') {
    return column.formatter(row[column.key], row)
  }
  return row[column.key] ?? '-'
}
</script>

<style scoped>
.table-wrapper {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  text-align: left;
  padding: 12px 14px;
  border-bottom: 1px solid #e2e8f0;
  white-space: nowrap;
}

th {
  font-size: 14px;
  color: #475569;
  background: #f8fafc;
}

.empty {
  text-align: center;
  color: #94a3b8;
}
</style>
