// The Data Transfer Object (DTO) Layer
// This shape the final JSON output exactly how frontend charts expect to read them.

package com.stockservice.api.dto;
import java.util.List;
public record CandleResponse(
    String symbol,
    String timeframe,
    List<CandleData> candles,
    int count
) {}
