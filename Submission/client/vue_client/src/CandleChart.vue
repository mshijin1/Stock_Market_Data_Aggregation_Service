<template>
  <div class="crypto-chart-container">
    <h2>Crypto Candle Aggregator (ApexCharts)</h2>
    
    <form @submit.prevent="fetchChartData" class="filter-form">
      <div class="form-group">
        <label for="symbol">Symbol</label>
        <input id="symbol" v-model="filters.symbol" type="text" placeholder="e.g., BTCUSDT" required />
      </div>

      <div class="form-group">
        <label for="timeframe">Timeframe</label>
        <select id="timeframe" v-model="filters.timeframe">
          <option value="1m">1 Minute</option>
          <option value="5m">5 Minutes</option>
          <option value="1h">1 Hour</option>
          <option value="1d">1 Day</option>
        </select>
      </div>

      <div class="form-group">
        <label for="startDate">Start Date</label>
        <input id="startDate" v-model="filters.startDate" type="date" required />
      </div>

      <div class="form-group">
        <label for="endDate">End Date</label>
        <input id="endDate" v-model="filters.endDate" type="date" required />
      </div>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Loading...' : 'Fetch Chart' }}
      </button>
    </form>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div v-if="series[0].data.length" class="chart-wrapper">
      <apexchart 
        type="candlestick" 
        height="400" 
        :options="chartOptions" 
        :series="series"
      ></apexchart>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import axios from 'axios';

// Form State
const filters = reactive({
  symbol: 'BTCUSDT',
  timeframe: '1h',
  startDate: '',
  endDate: ''
});

const loading = ref(false);
const error = ref(null);

// ApexCharts Reactive Data Structure
const series = ref([
  {
    name: 'candle',
    data: [] // Format required: [{ x: timestamp, y: [O, H, L, C] }]
  }
]);

// ApexCharts Configuration Options
const chartOptions = reactive({
  chart: {
    type: 'candlestick',
    height: 400,
    background: '#ffffff',
    toolbar: {
      show: true
    }
  },
  title: {
    text: 'Asset Price Data',
    align: 'left'
  },
  xaxis: {
    type: 'datetime',
    labels: {
      style: {
        colors: '#333'
      }
    }
  },
  yaxis: {
    tooltip: {
      enabled: true
    },
    labels: {
      formatter: (val) => val.toFixed(2) // Formats price decimals nicely
    }
  },
  plotOptions: {
    candlestick: {
      colors: {
        upward: '#26a69a',   // Traditional green for bullish candles
        downward: '#ef5350' // Traditional red for bearish candles
      }
    }
  }
});

// API Call Handler
const fetchChartData = async () => {
  loading.value = true;
  error.value = null;

  try {
    const response = await axios.get('http://localhost:3000/api/candles', {
      params: {
        symbol: filters.symbol.toUpperCase(),
        timeframe: filters.timeframe,
        startTime: new Date(filters.startDate).getTime(),
        endTime: new Date(filters.endDate).getTime()
      }
    });

    // Map your API response to the specific object format ApexCharts expects
    const formattedData = response.data.map(candle => ({
      x: new Date(Number(candle.timestamp)), // Can be timestamp or Date instance
      y: [
        Number(candle.open),
        Number(candle.high),
        Number(candle.low),
        Number(candle.close)
      ]
    }));

    // Sort chronologically to avoid display issues
    formattedData.sort((a, b) => a.x - b.x);

    // Update state to trigger reactivity re-render
    chartOptions.title.text = `${filters.symbol.toUpperCase()} — ${filters.timeframe} Chart`;
    series.value = [{
      name: filters.symbol.toUpperCase(),
      data: formattedData
    }];

  } catch (err) {
    console.error(err);
    error.value = err.response?.data?.message || 'Failed to fetch candle data. Check backend connection.';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* Keeping your neat form styling from before */
.crypto-chart-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.filter-form {
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  align-items: flex-end;
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 25px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

label {
  font-size: 12px;
  font-weight: bold;
  color: #333;
}

input, select, button {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 4px;
  font-size: 14px;
}

button {
  background-color: #41b883;
  color: white;
  border: none;
  cursor: pointer;
  font-weight: bold;
  transition: background 0.2s;
}

button:hover {
  background-color: #35495e;
}

button:disabled {
  background-color: #aaa;
  cursor: not-allowed;
}

.error-message {
  color: #d9534f;
  background-color: #fdf7f7;
  padding: 10px;
  border-left: 4px solid #d9534f;
  margin-bottom: 20px;
}

.chart-wrapper {
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  padding: 15px;
  border-radius: 8px;
  background: white;
}
</style>