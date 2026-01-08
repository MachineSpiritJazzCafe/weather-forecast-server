package com.example.domain

import zio.test._
import zio.test.Assertion._

object WeatherSpec extends ZIOSpecDefault:

  def spec = suite("Weather Domain")(
    suite("TemperatureCharacterization")(
      test("classifies temperatures >= 80°F as Hot") {
        assertTrue(
          TemperatureCharacterization.fromFahrenheit(80.0) == TemperatureCharacterization.Hot,
          TemperatureCharacterization.fromFahrenheit(85.0) == TemperatureCharacterization.Hot,
          TemperatureCharacterization.fromFahrenheit(100.0) == TemperatureCharacterization.Hot
        )
      },
      test("classifies temperatures <= 50°F as Cold") {
        assertTrue(
          TemperatureCharacterization.fromFahrenheit(50.0) == TemperatureCharacterization.Cold,
          TemperatureCharacterization.fromFahrenheit(45.0) == TemperatureCharacterization.Cold,
          TemperatureCharacterization.fromFahrenheit(0.0) == TemperatureCharacterization.Cold,
          TemperatureCharacterization.fromFahrenheit(-10.0) == TemperatureCharacterization.Cold
        )
      },
      test("classifies temperatures between 51-79°F as Moderate") {
        assertTrue(
          TemperatureCharacterization.fromFahrenheit(51.0) == TemperatureCharacterization.Moderate,
          TemperatureCharacterization.fromFahrenheit(65.0) == TemperatureCharacterization.Moderate,
          TemperatureCharacterization.fromFahrenheit(79.0) == TemperatureCharacterization.Moderate
        )
      }
    ),
    suite("Coordinates")(
      test("creates valid coordinates") {
        val coords = Coordinates(40.7128, -74.0060)
        assertTrue(
          coords.latitude == 40.7128,
          coords.longitude == -74.0060
        )
      }
    ),
    suite("WeatherForecast")(
      test("creates forecast with correct characterization") {
        val forecast = WeatherForecast(
          conditions = "Sunny",
          temperature = 75.0,
          temperatureCharacterization = TemperatureCharacterization.fromFahrenheit(75.0)
        )
        assertTrue(
          forecast.conditions == "Sunny",
          forecast.temperature == 75.0,
          forecast.temperatureCharacterization == TemperatureCharacterization.Moderate
        )
      }
    )
  )
