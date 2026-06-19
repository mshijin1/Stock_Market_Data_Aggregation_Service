import { createApp } from 'vue';
import App from './App.vue';
import VueApexCharts from "vue3-apexcharts";

const app = createApp(App);

// Register ApexCharts component globally
app.use(VueApexCharts);

app.mount('#app');