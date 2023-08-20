# Requirements
- Must provide APIs to set and get cell value
- Must handle arithmetic formulas on cells (`+`,`-`,`*`,`/`)
- Must handle concurrent requests

# Assumptions
- Spreadsheet fits into memory.

# Run
- Install JDK 20 and set `JAVA_HOME`
- Navigate to the root folder of the project
- Execute `mvnw clean install` to run the tests
- Execute `mvnw spring-boot:run` to start up the server
- Sample POST: 
  - http://localhost:8080/api/spreadsheet/cell/A1?value=1
  - http://localhost:8080/api/spreadsheet/cell/A2?value=2
  - http://localhost:8080/api/spreadsheet/cell/A3?value=A1%2BA2 (Make sure to URL encode special characters)
- Sample GET: 
  - http://localhost:8080/api/spreadsheet/cell/A3