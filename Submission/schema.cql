-- 1. Create the Keyspace
CREATE KEYSPACE IF NOT EXISTS stock_keyspace 
WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

-- 2. Switch to it
USE stock_keyspace;

-- 3. Create the Time-Series Table
CREATE TABLE IF NOT EXISTS stock_time_series (
    symbol text,
    timeframe text,
    datetime timestamp,
    open decimal,
    high decimal,
    low decimal,
    close decimal,
    volume bigint,
    PRIMARY KEY ((symbol, timeframe), datetime)
) WITH CLUSTERING ORDER BY (datetime DESC);