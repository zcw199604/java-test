<template>
  <div class="intro">
    <h2>{{ title }}</h2>
    <p>{{ description }}</p>
    <ul>
      <li v-for="item in bullets" :key="item">{{ item }}</li>
    </ul>
  </div>
</template>

<script setup>
defineProps({
  title: String,
  description: String,
  bullets: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
.intro {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 30px rgba(15, 23, 42, 0.06);
}

h2 {
  margin-top: 0;
}

p {
  color: #64748b;
}

ul {
  margin: 0;
  padding-left: 18px;
  color: #334155;
}
</style>
