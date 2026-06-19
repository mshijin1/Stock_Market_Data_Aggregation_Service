// This engine executes the dynamic mathematical transformation on your 1-minute data streams.

package com.stockservice.api.service;

import com.stockservice.api.dto.CandleData;
import com.stockservice.api.dto.CandleResponse;
import com.stockservice.api.model.StockTimeSeries;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockAggregationService {
    @Autowired
    private CassandraTemplate cassandraTemplate;

    public CandleResponse getAggregatedCandles(String symbol, String timeframe, LocalDateTime start, LocalDateTime end) {
        // 1. Fetch raw 1m candles within the timeframe boundaries
        Query query = Query.query(
            Criteria.where("symbol").is(symbol),
            Criteria.where("timeframe").is("1m"),
            Criteria.where("datetime").gte(start),
            Criteria.where("datetime").lte(end)
        );

        List<StockTimeSeries> rawCandles = cassandraTemplate.select(query, StockTimeSeries.class);
        
        // Always enforce ascending chronological order
        rawCandles.sort(Comparator.comparing(StockTimeSeries::getDatetime));

        // If the client requested 1m, return it immediately without heavy processing
        if (timeframe.equalsIgnoreCase("1m")) {
            List<CandleData> simpleList = rawCandles.stream()
                .map(c -> new CandleData(c.getDatetime(), c.getOpen(), c.getHigh(), c.getLow(), c.getClose(), c.getVolume()))
                .collect(Collectors.toList());
            return new CandleResponse(symbol, timeframe, simpleList, simpleList.size());
        }

        // 2. Put raw candles into their calculated time buckets
        Map<LocalDateTime, List<StockTimeSeries>> groupedBuckets = new TreeMap<>();
        int minutesParam = parseTimeframeToMinutes(timeframe);

        for (StockTimeSeries candle : rawCandles) {
            LocalDateTime bucketKey = calculateBucketWindow(candle.getDatetime(), minutesParam);
            groupedBuckets.computeIfAbsent(bucketKey, k -> new ArrayList<>()).add(candle);
        }

        // 3. Compute OHLCV logic rules across each bucket window
        List<CandleData> computedCandles = new ArrayList<>();
        for (Map.Entry<LocalDateTime, List<StockTimeSeries>> bucket : groupedBuckets.entrySet()) {
            List<StockTimeSeries> items = bucket.getValue();

            BigDecimal open = items.get(0).getOpen();
            BigDecimal close = items.get(items.size() - 1).getClose();
            BigDecimal high = items.stream().map(StockTimeSeries::getHigh).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            BigDecimal low = items.stream().map(StockTimeSeries::getLow).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
            long totalVolume = items.stream().mapToLong(StockTimeSeries::getVolume).sum();

            computedCandles.add(new CandleData(bucket.getKey(), open, high, low, close, totalVolume));
        }

        return new CandleResponse(symbol, timeframe, computedCandles, computedCandles.size());
    }

    private int parseTimeframeToMinutes(String tf) {
        String cleaned = tf.toLowerCase();
        if (cleaned.endsWith("m")) return Integer.parseInt(cleaned.replace("m", ""));
        if (cleaned.endsWith("h")) return Integer.parseInt(cleaned.replace("h", "")) * 60;
        if (cleaned.endsWith("d")) return 1440; 
        throw new IllegalArgumentException("Unsupported timeframe metric: " + tf);
    }

    private LocalDateTime calculateBucketWindow(LocalDateTime time, int minutesInterval) {
        if (minutesInterval == 1440) {
            return time.truncatedTo(ChronoUnit.DAYS); // Snap straight to midnight boundary
        }
        int elapsedMinutes = time.getHour() * 60 + time.getMinute();
        int bucketOffset = (elapsedMinutes / minutesInterval) * minutesInterval;
        return time.truncatedTo(ChronoUnit.DAYS).plusMinutes(bucketOffset);
    }
}
