package com.example.domain

sealed trait DomainError(message: String)

final case class ValidationError(message: String)     extends DomainError(message)
final case class ExternalApiError(message: String)    extends DomainError(message)
case object NotFoundError                             extends DomainError("NotFoundError")
