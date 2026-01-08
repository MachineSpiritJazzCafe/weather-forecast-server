package com.example.api

import com.example.api.Extensions._
import com.example.domain._
import zio._
import zio.http._

private[api] object Utils:

  def handleError(err: DomainError): UIO[Response] = err match {
    case NotFoundError           => ZIO.succeed(Response.status(Status.NotFound))
    case ValidationError(msg)    => msg.toResponseZIO(Status.BadRequest)
    case ExternalApiError(msg)   =>
      ZIO.logError(s"External API error: $msg") *>
        "External service unavailable".toResponseZIO(Status.ServiceUnavailable)
  }
