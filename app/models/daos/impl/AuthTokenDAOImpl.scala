package models.daos.impl

import jakarta.inject.Inject
import models.AuthToken
import models.daos.AuthTokenDAO
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.GetResult

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class AuthTokenDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)
                                (implicit ec: ExecutionContext) extends AuthDAOSlick with AuthTokenDAO {
  import models.daos.impl.DUPostgresProfile.api.*

  implicit val getAuthTokenResult: GetResult[AuthToken] = GetResult(r => AuthToken(r.<<, r.<<, r.<<))

  override def find(id: UUID): Future[Option[AuthToken]] =
    db.run(slickAuthTokens.filter(_.id === id).result.headOption)
      .map(_.map(t => AuthToken(t.id, t.userID, t.expiry)))

  override def findExpired(): Future[Seq[AuthToken]] = {
    val action = sql"""SELECT * FROM auth.token WHERE expiry < current_timestamp""".as[AuthToken]

    db.run(action)
  }

  override def save(token: AuthToken): Future[Option[AuthToken]] =
    db.run((slickAuthTokens returning slickAuthTokens).insertOrUpdate(DbAuthToken(token.id, token.userID, token.expiry))
      .map(_.map(t => AuthToken(t.id, t.userID, t.expiry))))

  override def remove(id: UUID): Future[Unit] =
    db.run(slickAuthTokens.filter(_.id === id).delete).map(_ => ())
}
