// This receives the parameters, parses the text dates into Java timestamp objects, and sends back the JSON.

package com.stockservice.api.controller;
import com.stockservice.api.dto.CandleResponse;
import com.stockservice.api.service.StockAggregationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:5173")
public class StockCandleController {
    @Autowired
    private StockAggregationService aggregationService;

    @GetMapping("/candles")
    public ResponseEntity<CandleResponse> getCandles(
            @RequestParam String symbol,
            @RequestParam String timeframe,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start_date,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end_date) {

        CandleResponse response = aggregationService.getAggregatedCandles(
                symbol.toUpperCase(), timeframe, start_date, end_date
        );

        return ResponseEntity.ok(response);
    }
}
