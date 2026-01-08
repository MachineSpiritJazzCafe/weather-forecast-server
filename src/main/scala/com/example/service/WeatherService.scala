package com.example.service

import com.example.client.NWSClient
import com.example.domain._
import zio._

// Service trait for weather operations
trait WeatherService:
  def getForecast(coords: Coordinates): IO[DomainError, WeatherForecast]

// Implementation using National Weather Service API
class WeatherServiceLive(nwsClient: NWSClient) extends WeatherService:

  override def getForecast(coords: Coordinates): IO[DomainError, WeatherForecast] =
    nwsClient.getForecast(coords)

object WeatherService:
  val live: ZLayer[NWSClient, Nothing, WeatherService] =
    ZLayer.fromFunction(new WeatherServiceLive(_))

  // Accessor methods (ZIO service pattern)
  def getForecast(coords: Coordinates): ZIO[WeatherService, DomainError, WeatherForecast] =
    ZIO.serviceWithZIO[WeatherService](_.getForecast(coords))
