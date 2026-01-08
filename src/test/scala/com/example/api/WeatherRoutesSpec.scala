package com.example.api

import com.example.domain._
import com.example.service.WeatherService
import zio._
import zio.http._
import zio.test._
import zio.test.Assertion._

object WeatherRoutesSpec extends ZIOSpecDefault:

  // Mock WeatherService for testing
  class MockWeatherService extends WeatherService:
    override def getForecast(coords: Coordinates): IO[DomainError, WeatherForecast] =
      // Return different responses based on coordinates for testing
      if coords.latitude == 40.7 && coords.longitude == -74.0 then
        ZIO.succeed(
          WeatherForecast(
            conditions = "Partly Cloudy",
            temperature = 65.0,
            temperatureCharacterization = TemperatureCharacterization.Moderate
          )
        )
      else if coords.latitude == 999.0 then
        ZIO.fail(ExternalApiError("Service unavailable"))
      else
        ZIO.succeed(
          WeatherForecast(
            conditions = "Sunny",
            temperature = 75.0,
            temperatureCharacterization = TemperatureCharacterization.Moderate
          )
        )

  val mockServiceLayer: ULayer[WeatherService] = ZLayer.succeed(new MockWeatherService)

  def spec = suite("WeatherRoutes")(
    test("GET /weather with valid coordinates returns 200 and forecast") {
      val request = Request.get(URL.decode("/weather?lat=40.7&lon=-74.0").toOption.get)
      for {
        response <- WeatherRoutes.app.runZIO(request)
        body     <- response.body.asString
      } yield assertTrue(
        response.status == Status.Ok,
        body.contains("Partly Cloudy"),
        body.contains("65.0"),
        body.contains("moderate")
      )
    }.provide(mockServiceLayer),

    test("GET /weather without lat parameter returns 400") {
      val request = Request.get(URL.decode("/weather?lon=-74.0").toOption.get)
      for {
        response <- WeatherRoutes.app.runZIO(request)
        body     <- response.body.asString
      } yield assertTrue(
        response.status == Status.BadRequest,
        body.contains("Missing lat parameter")
      )
    }.provide(mockServiceLayer),

    test("GET /weather without lon parameter returns 400") {
      val request = Request.get(URL.decode("/weather?lat=40.7").toOption.get)
      for {
        response <- WeatherRoutes.app.runZIO(request)
        body     <- response.body.asString
      } yield assertTrue(
        response.status == Status.BadRequest,
        body.contains("Missing lon parameter")
      )
    }.provide(mockServiceLayer),

    test("GET /weather without any parameters returns 400") {
      val request = Request.get(URL.decode("/weather").toOption.get)
      for {
        response <- WeatherRoutes.app.runZIO(request)
        body     <- response.body.asString
      } yield assertTrue(
        response.status == Status.BadRequest,
        body.contains("Missing lat parameter")
      )
    }.provide(mockServiceLayer),

    test("GET /weather with invalid lat returns 400") {
      val request = Request.get(URL.decode("/weather?lat=invalid&lon=-74.0").toOption.get)
      for {
        response <- WeatherRoutes.app.runZIO(request)
        body     <- response.body.asString
      } yield assertTrue(
        response.status == Status.BadRequest,
        body.contains("Invalid lat")
      )
    }.provide(mockServiceLayer),

    test("GET /weather with invalid lon returns 400") {
      val request = Request.get(URL.decode("/weather?lat=40.7&lon=invalid").toOption.get)
      for {
        response <- WeatherRoutes.app.runZIO(request)
        body     <- response.body.asString
      } yield assertTrue(
        response.status == Status.BadRequest,
        body.contains("Invalid lon")
      )
    }.provide(mockServiceLayer),

    test("GET /weather when service fails returns 503") {
      val request = Request.get(URL.decode("/weather?lat=999.0&lon=0.0").toOption.get)
      for {
        response <- WeatherRoutes.app.runZIO(request)
        body     <- response.body.asString
      } yield assertTrue(
        response.status == Status.ServiceUnavailable,
        body.contains("External service unavailable")
      )
    }.provide(mockServiceLayer)
  )
