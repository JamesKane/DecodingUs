package models.daos.impl

import models.AuthToken
import models.daos.AuthTokenDAO

import java.util.UUID
import scala.concurrent.Future

class AuthTokenDAOImpl extends AuthTokenDAO {
  override def find(id: UUID): Future[Option[AuthToken]] = ???

  override def findExpired(): Future[Seq[AuthToken]] = ???

  override def save(token: AuthToken): Future[Option[AuthToken]] = ???

  override def remove(id: UUID): Future[Unit] = ???
}
