import csv
from datetime import datetime
from decimal import Decimal
from cassandra.cluster import Cluster

def ingest_stock_csv(csv_file_path):
    # 1. Connect to Cassandra running in Docker
    print("Connecting to Cassandra container...")
    cluster = Cluster(['127.0.0.1'], port=9042)
    session = cluster.connect()

    # 2. Set the keyspace context
    session.set_keyspace('stock_keyspace')

    # 3. Prepare the INSERT statement (8 columns to match your primary key table)
    insert_query = session.prepare("""
        INSERT INTO stock_time_series (symbol, timeframe, datetime, open, high, low, close, volume)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """)

    print(head_msg := f"Starting ingestion from: {csv_file_path}")
    print("-" * len(head_msg))

    inserted_count = 0

    # 4. Read and parse the CSV file
    with open(csv_file_path, mode='r', encoding='utf-8') as file:
        csv_reader = csv.DictReader(file)
        
        for row in csv_reader:
            try:
                # Convert string timestamps to Python datetime objects
                parsed_datetime = datetime.strptime(row['datetime'].strip(), '%d/%m/%Y %H:%M')
                formatted_datetime = parsed_datetime.replace(second=0, microsecond=0)

                # Data conversions to match Cassandra schemas
                symbol = row['symbol'].strip()
                timeframe='1m'
                open_val = Decimal(row['open'])
                high_val = Decimal(row['high'])
                low_val = Decimal(row['low'])
                close_val = Decimal(row['close'])
                volume = int(row['volume'])

                # 5. Execute the query with all 8 arguments mapping perfectly
                session.execute(insert_query, (
                    symbol, timeframe, formatted_datetime, open_val, high_val, low_val, close_val, volume
                ))
                
                inserted_count += 1
                if inserted_count % 1000 == 0:
                    print(f"Successfully inserted {inserted_count} records...")

            except Exception as e:
                print(f"Skipping bad row due to error: {e} | Row data: {row}")

    print("-" * len(head_msg))
    print(f"Finished! Total successfully ingested records: {inserted_count}")
    
    # Clean up connection
    cluster.shutdown()

if __name__ == "__main__":
    CSV_PATH = r"C:\Users\ACER\Documents\Stock_Market_Data_Aggregation_Service\data\stock_data.csv" 
    ingest_stock_csv(CSV_PATH)