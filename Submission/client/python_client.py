import argparse
import sys
import requests

def fetch_and_display_candles(symbol, timeframe, start_date, end_date):
    # 1. Base URL pointing straight to your running Spring Boot server
    base_url = "http://localhost:8080/api/v1/candles"
    
    # 2. Package parameters matching the Spring Boot query mapping
    params = {
        "symbol": symbol,
        "timeframe": timeframe,
        "start_date": start_date,
        "end_date": end_date
    }
    
    print(f"Connecting to backend service for {symbol.upper()} ({timeframe})...")
    
    try:
        # 3. Fire the HTTP GET request
        response = requests.get(base_url, params=params)
        
        # Throw an error message if the HTTP status code is bad (e.g., 400 or 500)
        response.raise_for_status()
        
        # 4. Parse the raw JSON payload response
        data = response.json()
        
        # 5. Extract fields and print in the exact requested clean terminal log format
        candles = data.get("candles", [])
        
        print("\n=== Fetched Candle Data ===")
        print(f"Symbol: {data.get('symbol')} | Timeframe: {data.get('timeframe')} | Total Candles: {data.get('count')}")
        
        for index, candle in enumerate(candles, start=1):
            print(
                f"{index} {candle.get('datetime')} | "
                f"O: {float(candle.get('open')):.2f} | "
                f"H: {float(candle.get('high')):.2f} | "
                f"L: {float(candle.get('low')):.2f} | "
                f"C: {float(candle.get('close')):.2f} | "
                f"V: {candle.get('volume')}"
            )
        print("===========================\n")
        
    except requests.exceptions.ConnectionError:
        print("\n[ERROR] Could not connect to Spring Boot. Is your server running on port 8080?", file=sys.stderr)
    except requests.exceptions.HTTPError as http_err:
        print(f"\n[ERROR] Backend returned an HTTP error: {http_err}", file=sys.stderr)
    except Exception as e:
        print(f"\n[ERROR] An unexpected error occurred: {e}", file=sys.stderr)

if __name__ == "__main__":
    # 6. Configure Argparse to handle quick inputs straight from the command line
    parser = argparse.ArgumentParser(description="Consume the Spring Boot Candle Aggregation Engine API.")
    
    parser.add_argument("--symbol", required=True, help="Stock ticker symbol (e.g., RELIANCE)")
    parser.add_argument("--timeframe", required=True, help="Target aggregation timeframe (e.g., 5m, 15m, 1h, 1d)")
    parser.add_argument("--start_date", required=True, help="Start datetime (yyyy-MM-dd HH:mm:ss)")
    parser.add_argument("--end_date", required=True, help="End datetime (yyyy-MM-dd HH:mm:ss)")
    
    args = parser.parse_args()
    
    # Fire the execution query loop
    fetch_and_display_candles(args.symbol, args.timeframe, args.start_date, args.end_date)