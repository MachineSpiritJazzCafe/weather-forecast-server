package com.example.client

import com.example.domain._
import zio._
import zio.http._
import zio.json._

// Client for National Weather Service API
class NWSClient(client: Client):

  private val baseUrl = "https://api.weather.gov"
  private val userAgent = Header.UserAgent.Product("weather-forecast-server", Some("1.0"))

  def getForecast(coords: Coordinates): IO[DomainError, WeatherForecast] =
    for {
      // Step 1: Get grid point data
      pointsUrl <- ZIO.succeed(s"$baseUrl/points/${coords.latitude},${coords.longitude}")
      _         <- ZIO.logInfo(s"Fetching grid data from: $pointsUrl")

      // Create request with proper headers
      // Use application/geo+json as Accept header (matches curl)
      request    <- ZIO.succeed(
                      Request
                        .get(URL.decode(pointsUrl).toOption.get)
                        .addHeader(userAgent)
                        .addHeader(Header.Accept(MediaType.parseCustomMediaType("application/geo+json").get))
                    )

      pointsResp <- client.request(request)
                     .tapError(err => ZIO.logError(s"HTTP request failed: ${err}"))
                     .mapError(err => ExternalApiError(s"Failed to fetch grid data: ${err.getMessage} - Check network connectivity"))

      pointsBody <- pointsResp.body.asString
                     .mapError(err => ExternalApiError(s"Failed to read grid response: ${err.getMessage}"))

      points     <- ZIO.fromEither(pointsBody.fromJson[PointsResponse])
                     .mapError(err => ExternalApiError(s"Failed to parse grid response: $err"))

      // Step 2: Get forecast from the URL returned by points API
      forecastUrl = points.properties.forecast
      _          <- ZIO.logInfo(s"Fetching forecast from: $forecastUrl")

      forecastReq  <- ZIO.succeed(
                       Request
                         .get(URL.decode(forecastUrl).toOption.get)
                         .addHeader(userAgent)
                         .addHeader(Header.Accept(MediaType.parseCustomMediaType("application/geo+json").get))
                     )

      forecastResp <- client.request(forecastReq)
                       .tapError(err => ZIO.logError(s"HTTP request failed: ${err}"))
                       .mapError(err => ExternalApiError(s"Failed to fetch forecast: ${err.getMessage} - Check network connectivity"))

      forecastBody <- forecastResp.body.asString
                       .mapError(err => ExternalApiError(s"Failed to read forecast response: ${err.getMessage}"))

      forecast     <- ZIO.fromEither(forecastBody.fromJson[ForecastResponse])
                       .mapError(err => ExternalApiError(s"Failed to parse forecast response: $err"))

      // Step 3: Extract today's forecast (first period)
      todayPeriod  <- ZIO.fromOption(forecast.properties.periods.headOption)
                       .orElseFail(ExternalApiError("No forecast periods available"))

      // Convert temperature to Fahrenheit if needed (API returns F by default)
      tempF         = todayPeriod.temperature

    } yield WeatherForecast(
      conditions = todayPeriod.shortForecast,
      temperature = tempF,
      temperatureCharacterization = TemperatureCharacterization.fromFahrenheit(tempF)
    )

object NWSClient:
  val layer: ZLayer[Client, Nothing, NWSClient] =
    ZLayer.fromFunction(new NWSClient(_))
