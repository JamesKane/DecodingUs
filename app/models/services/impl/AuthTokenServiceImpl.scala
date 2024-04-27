package models.services.impl

import models.AuthToken
import models.services.AuthTokenService

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

class AuthTokenServiceImpl extends AuthTokenService {
  override def create(userID: UUID, expiry: FiniteDuration): Future[AuthToken] = ???

  override def validate(id: UUID): Future[Option[AuthToken]] = ???
  
  override def clean: Future[Seq[AuthToken]] = ???
}
