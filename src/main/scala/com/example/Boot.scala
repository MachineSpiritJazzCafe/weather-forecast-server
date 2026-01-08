package com.example

import com.example.api._
import com.example.client.NWSClient
import com.example.config.Configuration.ApiConfig
import com.example.service.WeatherService
import zio._
import zio.config._
import zio.http._
import zio.logging.backend.SLF4J

object Boot extends ZIOAppDefault:

  override val bootstrap: ULayer[Unit] = Runtime.removeDefaultLoggers >>> SLF4J.slf4j

  private val serverLayer =
    ZLayer
      .service[ApiConfig]
      .flatMap { cfg =>
        Server.defaultWith(_.binding(cfg.get.host, cfg.get.port))
      }
      .orDie

  val routes = Http.collect[Request] {
    case Method.GET -> !! / "ping" => Response.text("pong")
  }

  private val program = Server.serve(routes ++ WeatherRoutes.app)

  override val run =
    program.provide(
      serverLayer,
      ApiConfig.layer,
      WeatherService.live,
      NWSClient.layer,
      Client.default
    )
