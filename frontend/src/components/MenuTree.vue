<template>
  <el-menu
    :default-active="activePath"
    :collapse="collapse"
    background-color="transparent"
    text-color="rgba(255,255,255,0.86)"
    active-text-color="#f6d37b"
    router
  >
    <template v-for="group in groups" :key="group.key">
      <el-sub-menu :index="group.key">
        <template #title>
          <el-icon><component :is="group.icon" /></el-icon>
          <span>{{ group.title }}</span>
        </template>
        <el-menu-item v-for="item in group.children" :key="item.path" :index="item.path">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-sub-menu>
    </template>
  </el-menu>
</template>

<script setup>
defineProps({
  groups: { type: Array, default: () => [] },
  activePath: { type: String, default: '/dashboard' },
  collapse: { type: Boolean, default: false }
})
</script>
