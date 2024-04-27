package models.services.impl

import jakarta.inject.Inject
import models.AuthToken
import models.daos.AuthTokenDAO
import models.services.AuthTokenService

import java.time.ZonedDateTime
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class AuthTokenServiceImpl @Inject() (authTokenDAO: AuthTokenDAO)(implicit ex: ExecutionContext) extends AuthTokenService {
  override def create(userID: UUID, expiry: FiniteDuration = 12 hours): Future[AuthToken] = {
    val token = AuthToken(UUID.randomUUID(), userID = userID, expiry = ZonedDateTime.now().plusSeconds(expiry.toSeconds))
    authTokenDAO.save(token).map(_ => token)
  }

  override def validate(id: UUID): Future[Option[AuthToken]] = ???
  
  override def clean: Future[Seq[AuthToken]] = ???
}
