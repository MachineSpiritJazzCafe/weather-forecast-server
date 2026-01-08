package com.example.api

import com.example.api.Extensions._
import com.example.domain._
import com.example.service.WeatherService
import zio._
import zio.http._

object WeatherRoutes:

  val app: HttpApp[WeatherService, Nothing] = Http.collectZIO[Request] {

    // GET /weather?lat=X&lon=Y
    case req @ Method.GET -> !! / "weather" =>
      val effect: ZIO[WeatherService, DomainError, WeatherForecast] =
        for {
          latStr   <- ZIO.fromOption(req.url.queryParams.get("lat").flatMap(_.headOption))
                        .orElseFail(ValidationError("Missing lat parameter"))
          lonStr   <- ZIO.fromOption(req.url.queryParams.get("lon").flatMap(_.headOption))
                        .orElseFail(ValidationError("Missing lon parameter"))
          latitude <- ZIO.attempt(latStr.toDouble).mapError(_ => ValidationError(s"Invalid lat: $latStr"))
          longitude<- ZIO.attempt(lonStr.toDouble).mapError(_ => ValidationError(s"Invalid lon: $lonStr"))
          forecast <- WeatherService.getForecast(Coordinates(latitude, longitude))
        } yield forecast

      effect.foldZIO(Utils.handleError, _.toResponseZIO)
  }
