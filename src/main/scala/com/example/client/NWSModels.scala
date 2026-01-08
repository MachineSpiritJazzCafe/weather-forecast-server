package com.example.client

import zio.json._

// Models for parsing National Weather Service API responses

// Response from /points/{lat},{lon}
case class PointsResponse(properties: PointsProperties)

case class PointsProperties(forecast: String)

object PointsResponse:
  implicit val propertiesDecoder: JsonDecoder[PointsProperties] = DeriveJsonDecoder.gen[PointsProperties]
  implicit val decoder: JsonDecoder[PointsResponse] = DeriveJsonDecoder.gen[PointsResponse]

// Response from /gridpoints/.../forecast
case class ForecastResponse(properties: ForecastProperties)

case class ForecastProperties(periods: List[Period])

case class Period(
  temperature: Double,
  temperatureUnit: String,
  shortForecast: String
)

object ForecastResponse:
  implicit val periodDecoder: JsonDecoder[Period] = DeriveJsonDecoder.gen[Period]
  implicit val propertiesDecoder: JsonDecoder[ForecastProperties] = DeriveJsonDecoder.gen[ForecastProperties]
  implicit val decoder: JsonDecoder[ForecastResponse] = DeriveJsonDecoder.gen[ForecastResponse]
