 <!-- Setup and installation instructions -->

<!-- Setting up cassandra using docker-->
<!-- pull cassandra latest image in docker -->
1. docker pull cassandra:latest

<!-- create a custom network -->
2. docker network create cassandra

<!-- creating a container named cassandra with connected via custom network and exposed to localhost via post 9042 -->
3. docker run -d --name cassandra --hostname cassandra --network cassandra -p 9042:9042 -v C:\cassandra_data:/var/lib/cassandra cassandra:latest

<!-- To check if cassendra is fully booted -->
4. docker exec -it cassandra nodetool status

<!-- Into the container in interactive mode -->
5. docker exec -it cassandra cqlsh localhost 9042

<!-- To create keyspace and table refer schema.cql -->
path_to_schema.cql


 <!-- How to run the data ingestion script -->
1. move to : C:\Users\ACER\Documents\Stock_Market_Data_Aggregation_Service>
2. create a virtual environment: python -m venv venv
3. activate the environment: venv/Scripts/activate
4. install the requirements: pip install -r requirements.txt
5. run data_ingesion.py: python data_ingesion.py



 <!-- How to start the service -->
 open another terminal
 1. move to : C:\Users\ACER\Documents\Stock_Market_Data_Aggregation_Service\Submission\server\api>
 2. Build and run command to start the server: .\mvnw.cmd clean spring-boot:run

 <!-- How to run the client application -->
 open two terminals one for python client and another web client

<!-- for the python client -->
1. create a virtual environment: python -m venv venv
2. activate the environment: venv/Scripts/activate
3. install the requirements: pip install -r requirements.txt
4. run data_ingesion.py: python .\client.py --symbol RELIANCE --timeframe 15m --start_date "2026-01-01 09:15:00" --end_date "2026-01-05 15:30:00"

change symbol, timeframe, start_date and end_date according to the choice

<!-- for web client (vue.js) -->
1. move to : C:\Users\ACER\Documents\Stock_Market_Data_Aggregation_Service\Submission\client\vue_client>
2. run command to enable web service: npm run dev 
   The web service will start at port: 5173
3. paste teh url on your local browser:  http://localhost:5173/ and use it


 <!-- Sample cURL/HTTP requests and responses -->
 sample HTTP request: http://localhost:8080/api/v1/candles?symbol=RELIANCE&timeframe=15m&start_date=2026-05-01 00:00:00&end_date=2026-05-01 23:59:59

 sample response: {"symbol":"TCS","timeframe":"1h","candles":[{"datetime":"2026-05-01T14:00:00Z","open":3240.5,"high":3247,"low":3217,"close":3219,"volume":147114},{"datetime":"2026-05-01T15:00:00Z","open":3219.8,"high":3221.5,"low":3191,"close":3217.1,"volume":473419},{"datetime":"2026-05-01T16:00:00Z","open":3218.3,"high":3226,"low":3211.1,"close":3223.5,"volume":243290},{"datetime":"2026-05-01T17:00:00Z","open":3223.3,"high":3233.8,"low":3220.4,"close":3227.5,"volume":241931},{"datetime":"2026-05-01T18:00:00Z","open":3227.8,"high":3231.8,"low":3223.1,"close":3226.1,"volume":155728},{"datetime":"2026-05-01T19:00:00Z","open":3226.2,"high":3230,"low":3220.6,"close":3221,"volume":503747},{"datetime":"2026-05-01T20:00:00Z","open":3221,"high":3225.9,"low":3206.1,"close":3213,"volume":576308}],"count":7}


 <!-- Any assumptions or design decisions made -->
for the backend servive use java version 21
