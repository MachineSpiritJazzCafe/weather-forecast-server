package com.example.domain

import zio.json._

// Domain models for weather forecast

case class Coordinates(latitude: Double, longitude: Double)

object Coordinates:
  implicit val decoder: JsonDecoder[Coordinates] = DeriveJsonDecoder.gen[Coordinates]
  implicit val encoder: JsonEncoder[Coordinates] = DeriveJsonEncoder.gen[Coordinates]

enum TemperatureCharacterization:
  case Hot, Cold, Moderate

object TemperatureCharacterization:
  implicit val decoder: JsonDecoder[TemperatureCharacterization] = JsonDecoder.string.map {
    case "hot"      => Hot
    case "cold"     => Cold
    case "moderate" => Moderate
    case _          => Moderate
  }

  implicit val encoder: JsonEncoder[TemperatureCharacterization] = JsonEncoder.string.contramap {
    case Hot      => "hot"
    case Cold     => "cold"
    case Moderate => "moderate"
  }

  // Business logic: characterize temperature in Fahrenheit
  def fromFahrenheit(temp: Double): TemperatureCharacterization =
    if temp >= 80 then Hot
    else if temp <= 50 then Cold
    else Moderate

case class WeatherForecast(
  conditions: String,
  temperature: Double,
  temperatureCharacterization: TemperatureCharacterization
)

object WeatherForecast:
  implicit val decoder: JsonDecoder[WeatherForecast] = DeriveJsonDecoder.gen[WeatherForecast]
  implicit val encoder: JsonEncoder[WeatherForecast] = DeriveJsonEncoder.gen[WeatherForecast]
