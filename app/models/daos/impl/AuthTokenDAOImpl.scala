package models.daos.impl

import jakarta.inject.Inject
import models.AuthToken
import models.daos.AuthTokenDAO
import play.api.db.slick.DatabaseConfigProvider

import java.sql.Timestamp
import java.time.{Instant, ZoneId}
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AuthTokenDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext) extends AuthDAOSlick with AuthTokenDAO {
  import models.daos.impl.DUPostgresProfile.api.*

  override def find(id: UUID): Future[Option[AuthToken]] =
    db.run(slickAuthTokens.filter(_.id === id).result.headOption)
      .map(_.map(t => fromDb(t)))

  override def findExpired(): Future[Seq[AuthToken]] = {
    db.run(slickAuthTokens.filter(_.expiry <= Timestamp.from(Instant.now())).result)
      .map(_.map(t => fromDb(t)))
  }

  override def save(token: AuthToken): Future[Option[AuthToken]] =
    db.run((slickAuthTokens returning slickAuthTokens).insertOrUpdate(toDb(token))
      .map(_.map(t => fromDb(t))))

  override def remove(id: UUID): Future[Unit] =
    db.run(slickAuthTokens.filter(_.id === id).delete).map(_ => ())

  // convert ZonedDateTime to Timestamp
  private def toDb(authToken: AuthToken): DbAuthToken = {
    val timestamp = Timestamp.from(authToken.expiry.toInstant);
    DbAuthToken(authToken.id, authToken.userID, timestamp)
  }

  // convert Timestamp to ZonedDateTime
  private def fromDb(dbAuthToken: DbAuthToken): AuthToken = {
    val zonedDateTime = dbAuthToken.expiry.toInstant.atZone(ZoneId.systemDefault())
    AuthToken(dbAuthToken.id, dbAuthToken.userID, zonedDateTime)
  }
}
