<template>
  <div class="crypto-chart-container">
    <h2>Stock Candle Aggregator (TradingView)</h2>
    
    <form @submit.prevent="fetchChartData" class="filter-form">
      <div class="form-group">
        <label for="symbol">Symbol</label>
        <select id="symbol" v-model="filters.symbol">
          <option value="RELIANCE">RELIANCE</option>
          <option value="TCS">TCS</option>
        </select>
      </div>

      <div class="form-group">
        <label for="timeframe">Timeframe</label>
        <select id="timeframe" v-model="filters.timeframe">
          <option value="1m">1 Minute</option>
          <option value="5m">5 Minutes</option>
          <option value="15m">15 Minutes</option>
          <option value="30m">30 Minutes</option>
          <option value="1h">1 Hour</option>
          <option value="1d">1 Day</option>
        </select>
      </div>

      <div class="form-group">
        <label for="startDate">Start Date and Time</label>
        <input id="startDate" v-model="filters.startDate" type="datetime-local" required />
      </div>

      <div class="form-group">
        <label for="endDate">End Date and Time</label>
        <input id="endDate" v-model="filters.endDate" type="datetime-local" required />
      </div>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Loading...' : 'Fetch Chart' }}
      </button>
    </form>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>

    <div class="chart-wrapper">
      <div class="chart-header">
        <div class="chart-title">{{ currentTitle }}</div>

        <div class="candle-tooltip" v-if="hoveredCandle">
          <span class="tooltip-item"><strong class="lbl">TIME:</strong> {{ hoveredCandle.datetime }}</span>
          <span class="tooltip-item"><strong class="lbl-o">O:</strong> {{ hoveredCandle.open.toFixed(2) }}</span>
          <span class="tooltip-item"><strong class="lbl-h">H:</strong> {{ hoveredCandle.high.toFixed(2) }}</span>
          <span class="tooltip-item"><strong class="lbl-l">L:</strong> {{ hoveredCandle.low.toFixed(2) }}</span>
          <span class="tooltip-item"><strong class="lbl-c">C:</strong> {{ hoveredCandle.close.toFixed(2) }}</span>
          <span class="tooltip-item"><strong class="lbl-v">V:</strong> {{ hoveredCandle.volume.toLocaleString() }}</span>
        </div>
        <div class="candle-tooltip hint" v-else>
          <em>Hover over a candle to see full data details</em>
        </div>
      </div>

      <div ref="chartContainer" class="tv-chart-container"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onUnmounted } from 'vue';
import axios from 'axios';
import { createChart, CandlestickSeries } from 'lightweight-charts'; 

const filters = reactive({
  symbol: 'RELIANCE',
  timeframe: '15m',
  startDate: '2026-01-01T09:15', 
  endDate: '2026-01-05T15:30'    
});

const loading = ref(false);
const error = ref(null);
const currentTitle = ref('Select parameters and fetch data');
const hoveredCandle = ref(null);

const chartContainer = ref(null);
let chartInstance = null;
let candlestickSeries = null;

const fetchChartData = async () => {
  loading.value = true;
  error.value = null;
  hoveredCandle.value = null; 

  try {
    const formatAsDateTimeString = (dateTimeStr) => {
      if (!dateTimeStr) return '';
      const dateObj = new Date(dateTimeStr);
      if (isNaN(dateObj.getTime())) return dateTimeStr.replace('T', ' ') + ':00';
      return `${dateObj.getFullYear()}-${String(dateObj.getMonth() + 1).padStart(2, '0')}-${String(dateObj.getDate()).padStart(2, '0')} ${String(dateObj.getHours()).padStart(2, '0')}:${String(dateObj.getMinutes()).padStart(2, '0')}:00`;
    };

    const formattedStartTime = formatAsDateTimeString(filters.startDate);
    const formattedEndTime = formatAsDateTimeString(filters.endDate);

    const response = await axios.get('http://localhost:8080/api/v1/candles', {
      params: {
        symbol: filters.symbol.toUpperCase(),
        timeframe: filters.timeframe,
        start_date: formattedStartTime, 
        end_date: formattedEndTime       
      }
    });

    const dataArray = response.data.candles || [];

    if (dataArray.length === 0) {
      error.value = `No candles found for ${filters.symbol} (${filters.timeframe})`;
      if (candlestickSeries) candlestickSeries.setData([]);
      return;
    }

    const formattedData = dataArray.map(candle => {
      let rawTime = candle.datetime || candle.timestamp || candle.date || candle.openTime || candle.t;
      let unixTimestamp = Math.floor(new Date(rawTime).getTime() / 1000);

      return {
        time: unixTimestamp, 
        open: Number(candle.open ?? 0),
        high: Number(candle.high ?? 0),
        low: Number(candle.low ?? 0),
        close: Number(candle.close ?? 0),
        volume: Number(candle.volume ?? 0),
        originalISO: rawTime
      };
    });

    formattedData.sort((a, b) => a.time - b.time);

    const uniqueFormattedData = formattedData.filter((value, index, self) =>
      index === self.findIndex((t) => t.time === value.time)
    );

    // 🔥 FIX: Clean out the previous instance ONLY if it already exists
    if (chartInstance) {
      chartInstance.remove();
      chartInstance = null;
    }

    // Safely spin up the fresh canvas instance
    chartInstance = createChart(chartContainer.value, {
      width: chartContainer.value.clientWidth || 800,
      height: 400,
      layout: { background: { color: '#ffffff' }, textColor: '#333333' },
      grid: { vertLines: { color: '#f5f5f5' }, horzLines: { color: '#f5f5f5' } },
      timeScale: { timeVisible: true, secondsVisible: false, barSpacing: 12 },
    });

    candlestickSeries = chartInstance.addSeries(CandlestickSeries, {
      upColor: '#26a69a', 
      downColor: '#ef5350', 
      borderVisible: false,
      wickUpColor: '#26a69a', 
      wickDownColor: '#ef5350',
    });

    candlestickSeries.setData(uniqueFormattedData);
    chartInstance.timeScale().fitContent();

    currentTitle.value = `${filters.symbol.toUpperCase()} — ${filters.timeframe} Financial Canvas`;

    chartInstance.subscribeCrosshairMove(param => {
      if (!param || !param.time || param.point === undefined || param.point.x < 0 || param.point.y < 0) {
        hoveredCandle.value = null;
        return;
      }

      const data = param.seriesData.get(candlestickSeries);
      if (data) {
        const matchedSource = uniqueFormattedData.find(c => c.time === param.time);
        
        hoveredCandle.value = {
          datetime: matchedSource ? matchedSource.originalISO : new Date(param.time * 1000).toISOString(),
          open: data.open,
          high: data.high,
          low: data.low,
          close: data.close,
          volume: matchedSource ? matchedSource.volume : 0
        };
      } else {
        hoveredCandle.value = null;
      }
    });

  } catch (err) {
    console.error(err);
    error.value = `Connection Error: ${err.message}`;
  } finally {
    loading.value = false;
  }
};

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.remove();
    chartInstance = null;
  }
});
</script>

<style scoped>
.crypto-chart-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}
.filter-form {
  display: flex; gap: 15px; flex-wrap: wrap; align-items: flex-end;
  background: #f5f5f5; padding: 15px; border-radius: 8px; margin-bottom: 25px;
}
.form-group { display: flex; flex-direction: column; gap: 5px; }
label { font-size: 12px; font-weight: bold; color: #333; }
input, select, button { padding: 8px 12px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px; }
button { background-color: #41b883; color: white; border: none; cursor: pointer; font-weight: bold; }
button:hover { background-color: #35495e; }
.error-message { color: #d9534f; background-color: #fdf7f7; padding: 10px; border-left: 4px solid #d9534f; margin-bottom: 20px; }
.chart-wrapper { box-shadow: 0 4px 6px rgba(0,0,0,0.1); padding: 20px; border-radius: 8px; background: white; }

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
.chart-title { font-weight: bold; font-size: 16px; color: #222; }
.candle-tooltip {
  display: flex; gap: 15px; background: #1e222d; color: #d1d4dc;
  padding: 6px 12px; border-radius: 6px; font-size: 12px; font-family: monospace;
}
.candle-tooltip.hint { background: transparent; color: #888; font-style: italic; }
.tooltip-item { display: inline-block; }
strong.lbl { color: #90caf9; }
strong.lbl-o { color: #ffffff; }
strong.lbl-h { color: #26a69a; }
strong.lbl-l { color: #ef5350; }
strong.lbl-c { color: #ffca28; }
strong.lbl-v { color: #b39ddb; }
.tv-chart-container { width: 100%; height: 400px; }
</style>