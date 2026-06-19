<template>
  <main>
    <CandleChart />
  </main>
</template>

<script setup>
// Import the component from your components folder
import CandleChart from './components/CandleChart.vue';
</script>

<style>
/* Global reset styling */
body {
  margin: 0;
  background-color: #fafafa;
  color: #333;
}
main {
  padding: 20px;
}
</style>