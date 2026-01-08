package com.example.integration

import com.example.client.NWSClient
import com.example.domain._
import zio._
import zio.http._
import zio.test._
import zio.test.Assertion._

object NWSIntegrationSpec extends ZIOSpecDefault:

  def spec = suite("NWS API Integration")(
    test("fetches real forecast for New York City") {
      for {
        nwsClient <- ZIO.service[NWSClient]
        coords     = Coordinates(40.7128, -74.0060) // NYC
        forecast  <- nwsClient.getForecast(coords)
      } yield assertTrue(
        forecast.conditions.nonEmpty,
        forecast.temperature > -100.0 && forecast.temperature < 150.0, // Reasonable temp range
        forecast.temperatureCharacterization != null
      )
    },

    test("fetches real forecast for Los Angeles") {
      for {
        nwsClient <- ZIO.service[NWSClient]
        coords     = Coordinates(34.0522, -118.2437) // LA
        forecast  <- nwsClient.getForecast(coords)
      } yield assertTrue(
        forecast.conditions.nonEmpty,
        forecast.temperature > -100.0 && forecast.temperature < 150.0,
        forecast.temperatureCharacterization != null
      )
    }
  ).provide(
    NWSClient.layer,
    Client.default
  ) @@ TestAspect.timeout(30.seconds) @@ TestAspect.withLiveClock
  // Note: These tests make real HTTP calls and may be slow or flaky
  // Consider running separately: sbt "testOnly *NWSIntegrationSpec"
