# Weather Forecast Server

A simple HTTP server that provides weather forecasts using the National Weather Service API.
This project is based on Gitter ScalaConsultants standard: https://github.com/ScalaConsultants/zio-scala3-quickstart.g8/tree/master
Following ZIO documentation: https://zio.dev/guides/quickstarts/restful-webservice

## Features

- **GET /weather?lat={latitude}&lon={longitude}** - Returns weather forecast for given coordinates
  - Short forecast description (e.g., "Partly Cloudy")
  - Current temperature in Fahrenheit
  - Temperature characterization: "hot" (≥80°F), "cold" (≤50°F), or "moderate" (51-79°F)

## Quick Start

### Prerequisites

- Java 21+
- sbt 1.9+

### Build and Run

```bash
# Compile the project
sbt compile

# Run the server
sbt run
```

The server will start on `http://localhost:8080`

**⚠️ IMPORTANT:** If you encounter network connectivity issues (e.g., "Failed to fetch grid data"), comment out the `-Djava.net.preferIPv4Stack=true` line in `.jvmopts`. This setting was added for WSL2 compatibility but may cause issues on some systems.

### Example Usage

```bash
# Get weather for New York City
curl "http://localhost:8080/weather?lat=40.7128&lon=-74.0060"

# Response:
# {
#   "conditions": "Partly Cloudy",
#   "temperature": 45.0,
#   "temperatureCharacterization": "cold"
# }

# Get weather for Los Angeles
curl "http://localhost:8080/weather?lat=34.0522&lon=-118.2437"

# Invalid request (missing parameters)
curl "http://localhost:8080/weather?lat=40.7"
# Response: "Missing lon parameter" (400 Bad Request)
```

## Testing

### Run All Tests

```bash
sbt test
```

**Test Results:**
```
14 tests passed. 0 tests failed. 0 tests ignored.
```

### Run Specific Test Suites

```bash
# Unit tests only (fast)
sbt "testOnly com.example.domain.* com.example.api.*"

# Integration tests only (makes real API calls, slower)
sbt "testOnly *NWSIntegrationSpec"
```

### Test Coverage

- **Domain Tests** (`WeatherSpec.scala`) - 4 tests
  - Temperature characterization logic (hot/cold/moderate)
  - Domain model validation

- **API Route Tests** (`WeatherRoutesSpec.scala`) - 7 tests
  - Valid coordinate handling
  - Missing parameter validation (lat, lon, both)
  - Invalid parameter validation (non-numeric values)
  - Error handling (external service failures)

- **Integration Tests** (`NWSIntegrationSpec.scala`) - 3 tests
  - Real National Weather Service API calls
  - End-to-end functionality verification for NYC and LA
  - Response validation

### Manual Testing

```bash
# Start the server
sbt run

# In another terminal, test endpoints:

# Valid requests
curl "http://localhost:8080/weather?lat=40.7&lon=-74.0"
curl "http://localhost:8080/weather?lat=34.05&lon=-118.24"

# Validation errors
curl "http://localhost:8080/weather"                     # Missing both params
curl "http://localhost:8080/weather?lat=40.7"           # Missing lon
curl "http://localhost:8080/weather?lat=abc&lon=123"    # Invalid lat

# Health check
curl "http://localhost:8080/ping"
```

## Architecture

```
src/main/scala/com/example/
├── Boot.scala                  # Application entry point
├── api/
│   ├── WeatherRoutes.scala     # HTTP endpoints
│   ├── Extensions.scala        # Response helpers
│   └── Utils.scala             # Error handling
├── client/
│   ├── NWSClient.scala         # National Weather Service API client
│   └── NWSModels.scala         # API response models
├── domain/
│   ├── Weather.scala           # Domain models
│   └── DomainError.scala       # Error types
├── service/
│   └── WeatherService.scala   # Business logic
└── config/
    └── Configuration.scala     # Application configuration
```

## Implementation Notes

### Shortcuts Taken (for time-boxed exercise)

1. **No caching** - Each request hits the NWS API
   - Production: Add TTL-based cache for forecast data

2. **Basic error handling** - Generic error messages
   - Production: More specific error codes and messages

3. **No rate limiting** - Direct passthrough to NWS API
   - Production: Implement rate limiting to respect NWS quotas

4. **IPv4 forcing** - `.jvmopts` forces IPv4 stack for WSL2 compatibility
   - Production: Proper dual-stack networking

5. **Minimal validation** - Only validates lat/lon are numbers
   - Production: Validate coordinate ranges (-90 to 90, -180 to 180)

6. **No authorization** - Anyone can call this service
   - Productin: Full authorization and session handling


### API Flow

For implementation details and logic flow see:

    - https://www.weather.gov/documentation/services-web-api
    - https://weather-gov.github.io/api/general-faqs


1. Client requests `/weather?lat=X&lon=Y`
2. Server validates and parses coordinates
3. Call NWS `/points/{lat},{lon}` to get grid data
4. Extract forecast URL from response
5. Call forecast URL to get weather periods
6. Return first period with temperature characterization

### Key Design Decisions

- **ZIO & ZIO Layers DI** - Chose to go that route since I've been refreshing ZIO lately
- **Separate domain models** - Clean separation between NWS API models and domain models
- **Accept header fix** - Use `application/geo+json` to avoid redirect issues with zio-http client (irrelevant but worth to note)

## Configuration

```hocon
api {
  host = "0.0.0.0"  # Server bind address
  port = 8080       # Server port
}
```

## Dependencies

- **ZIO 2.0.13** - Effect system for async/concurrent programming
- **zio-http 3.0.0-RC1** - HTTP server and client
- **zio-json 0.5.0** - JSON serialization
- **zio-config 3.0.7** - Configuration management
- **zio-test** - Testing framework

## License

This is a coding exercise project.
