package models.daos.impl

import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import java.time.ZonedDateTime
import java.util.UUID

trait AuthDbTableDefinitions {
  protected val driver: JdbcProfile

  import DUPostgresProfile.api._

  /**
   * Represents a database authentication token.
   *
   * @param id     The unique identifier of the token.
   * @param userID The unique identifier of the user associated with the token.
   * @param expiry The expiration date and time of the token.
   */
  case class DbAuthToken(id: UUID, userID: UUID, expiry: ZonedDateTime)

  /**
   * Represents a database table for storing authentication tokens.
   *
   * @param tag the table identifier
   */
  class AuthTokens(tag: Tag) extends Table[DbAuthToken](tag, Some("auth"), "token") {
    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def userId: Rep[UUID] = column[UUID]("user_id")
    def expiry: Rep[ZonedDateTime] = column[ZonedDateTime]("expiry")

    def * : ProvenShape[DbAuthToken] = (id, userId, expiry) <> (DbAuthToken.apply, DbAuthToken.unapply)
  }

  /**
   * Case class representing a database user.
   *
   * @param userID    The unique identifier of the user.
   * @param firstName The optional first name of the user.
   * @param lastName  The optional last name of the user.
   * @param email     The optional email of the user.
   * @param avatarURL The optional URL of the user's avatar image.
   * @param activated Indicates whether the user is activated or not.
   * @param role      The role of the user.
   */
  case class DbUser(
                     userID: UUID,
                     firstName: Option[String],
                     lastName: Option[String],
                     email: Option[String],
                     avatarURL: Option[String],
                     activated: Boolean,
                     role: Int
                   )

  /**
   * Represents a table in the database that stores user information.
   *
   * @param tag The tag representing the table.
   */
  class Users(tag: Tag) extends Table[DbUser](tag, Some("auth"), "user") {
    def id: Rep[UUID] = column[UUID]("id", O.PrimaryKey)
    def firstName: Rep[Option[String]] = column[Option[String]]("first_name")
    def lastName: Rep[Option[String]] = column[Option[String]]("last_name")
    def email: Rep[Option[String]] = column[Option[String]]("email")
    def avatarURL: Rep[Option[String]] = column[Option[String]]("avatar_url")
    def activated: Rep[Boolean] = column[Boolean]("activated")
    def role: Rep[Int] = column[Int]("role_id")

    def * : ProvenShape[DbUser] = (id, firstName, lastName, email, avatarURL, activated, role) <> (DbUser.apply, DbUser.unapply)
  }

  /**
   * Represents login information for a Database user.
   *
   * @param id          The unique identifier for the login information.
   * @param providerID  The ID of the provider associated with the login information.
   * @param providerKey The key associated with the login information for the provider.
   */
  case class DbLoginInfo(
                          id: Option[Long],
                          providerID: String,
                          providerKey: String
                        )

  /**
   * Database table representation of login information.
   *
   * @param tag The tag to associate with this table.
   */
  class LoginInfos(tag: Tag) extends Table[DbLoginInfo](tag, Some("auth"), "login_info") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def providerID: Rep[String] = column[String]("provider_id")
    def providerKey: Rep[String] = column[String]("provider_key")

    def * : ProvenShape[DbLoginInfo] = (id.?, providerID, providerKey).mapTo[DbLoginInfo]
  }

  /**
   * Represents the login information of a user in the database.
   *
   * @param userID      The unique identifier of the user.
   * @param loginInfoId The unique identifier of the login information.
   * @param updated     The date and time when the login information was last updated.
   */
  case class DbUserLoginInfo(
                              userID: UUID,
                              loginInfoId: Long
                            )

  /**
   * Represents a table that holds user login information.
   *
   * @param tag the tag used for the table
   */
  class UserLoginInfos(tag: Tag) extends Table[DbUserLoginInfo](tag, Some("auth"), "user_login_info") {
    def userID = column[UUID]("user_id")

    def loginInfoId = column[Long]("login_info_id")

    def * = (userID, loginInfoId).mapTo[DbUserLoginInfo]

    def idx = primaryKey("pk_user_login", (userID, loginInfoId))
  }

  /**
   * Represents the password information for a database user.
   *
   * @param hasher      The hashing algorithm used to hash the password.
   * @param password    The hashed password.
   * @param salt        An optional salt value used for password hashing.
   * @param loginInfoId The unique identifier of the associated login info.
   */
  case class DbPasswordInfo(
                             hasher: String,
                             password: String,
                             salt: Option[String],
                             loginInfoId: Long
                           )

  /**
   * Represents a table that stores password information.
   *
   * @param tag the tag associated with this table
   */
  class PasswordInfos(tag: Tag) extends Table[DbPasswordInfo](tag, Some("auth"), "password_info") {
    def hasher = column[String]("hasher")

    def password = column[String]("password")

    def salt = column[Option[String]]("salt")

    def loginInfoId = column[Long]("login_info_id")

    def * = (hasher, password, salt, loginInfoId).mapTo[DbPasswordInfo]
  }

  /**
   * Represents the OAuth1 information for a specific user.
   *
   * @param id          The unique identifier for the OAuth1 information (optional).
   * @param token       The OAuth1 token.
   * @param secret      The OAuth1 secret.
   * @param loginInfoId The unique identifier for the associated login info.
   */
  case class DbOAuth1Info(
                           id: Option[Long],
                           token: String,
                           secret: String,
                           loginInfoId: Long
                         )

  /**
   * Represents a table in the database that stores OAuth1 info.
   *
   * @param tag the tag identifying the table in the query
   */
  class OAuth1Infos(tag: Tag) extends Table[DbOAuth1Info](tag, Some("auth"), "oauth1_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def token = column[String]("token")

    def secret = column[String]("secret")

    def loginInfoId = column[Long]("login_info_id")

    def * = (id.?, token, secret, loginInfoId).mapTo[DbOAuth1Info]
  }

  /**
   * A case class representing OAuth2 related information for a user.
   *
   * @param id           The optional database identifier for the OAuth2 info.
   * @param accessToken  The access token for the OAuth2 info.
   * @param tokenType    The optional token type for the OAuth2 info.
   * @param expiresIn    The optional duration in seconds for which the access token is valid.
   * @param refreshToken The optional refresh token for the OAuth2 info.
   * @param loginInfoId  The database identifier for the associated login info.
   */
  case class DbOAuth2Info(
                           id: Option[Long],
                           accessToken: String,
                           tokenType: Option[String],
                           expiresIn: Option[Int],
                           refreshToken: Option[String],
                           loginInfoId: Long
                         )

  /**
   * A table definition for storing OAuth2 information.
   *
   * @param tag a unique identifier for this table
   */
  class OAuth2Infos(tag: Tag) extends Table[DbOAuth2Info](tag, Some("auth"), "oauth2_info") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def accessToken = column[String]("access_token")

    def tokenType = column[Option[String]]("token_type")

    def expiresIn = column[Option[Int]]("expires_in")

    def refreshToken = column[Option[String]]("refresh_token")

    def loginInfoId = column[Long]("login_info_id")

    def * = (id.?, accessToken, tokenType, expiresIn, refreshToken, loginInfoId).mapTo[DbOAuth2Info]
  }

  /**
   * Represents information about an OpenID user in the database.
   *
   * @param id          The unique identifier of the OpenID user.
   * @param loginInfoId The ID of the associated login information in the database.
   */
  case class DbOpenIDInfo(
                           id: String,
                           loginInfoId: Long
                         )

  /**
   * A class that represents the OpenIDInfos table in the database.
   *
   * @param tag the tag to identify this table in the Slick queries
   */
  class OpenIDInfos(tag: Tag) extends Table[DbOpenIDInfo](tag, Some("auth"), "openid_info") {
    def id = column[String]("id", O.PrimaryKey)

    def loginInfoId = column[Long]("login_info_id")

    def * = (id, loginInfoId).mapTo[DbOpenIDInfo]
  }

  /**
   * A case class representing an OpenID attribute.
   *
   * @param id    The unique identifier of the attribute.
   * @param key   The key associated with the attribute.
   * @param value The value of the attribute.
   */
  case class DbOpenIDAttribute(
                                id: String,
                                key: String,
                                value: String
                              )

  /**
   * Represents a table for storing OpenID attributes.
   *
   * @param tag The tag representing the table.
   */
  class OpenIDAttributes(tag: Tag) extends Table[DbOpenIDAttribute](tag, Some("auth"), "openid_attributes") {
    def id = column[String]("id")

    def key = column[String]("key")

    def value = column[String]("value")

    def * = (id, key, value).mapTo[DbOpenIDAttribute]
  }

  /**
   * Represents Google Authenticator TOTP information stored in the database.
   *
   * @param id          The database identifier for the TOTP information.
   * @param sharedKey   The shared secret key associated with the TOTP information.
   * @param loginInfoId The database identifier of the associated login information.
   */
  case class DbGoogleTotpInfo(id: Int, sharedKey: String, loginInfoId: Long)
  
  /**
   * Represents the mapping of the GoogleTotpInfo table in the database.
   *
   * @param tag The tag representing the table in the database.
   */
  class GoogleTotpInfo(tag: Tag) extends Table[DbGoogleTotpInfo](tag, Some("auth"), "google_totp_info") {
    def id = column[Int]("id", O.PrimaryKey)
    def sharedKey = column[String]("shared_key")
    def loginInfoId = column[Long]("login_info_id")

    def * = (id, sharedKey, loginInfoId).mapTo[DbGoogleTotpInfo]
  }

  /**
   * Represents the Google TOTP credentials stored in the database.
   *
   * @param id         The unique identifier of the credentials.
   * @param totpInfoId The ID of the Google TOTP information related to these credentials.
   * @param qrUrl      The URL representing the QR code for scanning to set up the TOTP authenticator app.
   */
  case class DbGoogleTotpCredentials(id: Int, totpInfoId: Int, qrUrl: String)
  
  /**
   * Represents a table that stores Google TOTP credentials.
   *
   * @param tag the tag associated with this table
   */
  class GoogleTotpCredentials(tag: Tag) extends Table[DbGoogleTotpCredentials](tag, Some("auth"), "google_totp_credentials") {
    def id = column[Int]("id", O.PrimaryKey)
    def totpInfoID = column[Int]("totp_info_id")
    def qrUrl = column[String]("qr_url")

    def * = (id, totpInfoID, qrUrl).mapTo[DbGoogleTotpCredentials]
  }

  /**
   * Represents a class that holds the scratch codes associated with a TOTP authentication method
   *
   * @param googleTotpId   The Google TOTP ID associated with the scratch codes
   * @param passwordInfoId The password info ID associated with the scratch codes
   */
  case class DbTotpInfoScratchCodes(googleTotpId: Int, passwordInfoId: Int)
  
  /**
   * Represents the table structure and schema for the `totp_info_scratch_codes` table.
   *
   * @param tag the table tag used for database queries
   */
  class TotpInfoScratchCodes(tag: Tag) extends Table[DbTotpInfoScratchCodes](tag, Some("auth"), "totp_info_scratch_codes") {
    def googleTotpId = column[Int]("google_totp_info_id")
    def passwordInfoId = column[Int]("password_info_id")

    def * = (googleTotpId, passwordInfoId).mapTo[DbTotpInfoScratchCodes]

    def idx = primaryKey("totp_cred_scratch_codes_pkey", (googleTotpId, passwordInfoId))
  }
  
  /**
   * Represents a database entity for storing scratch codes associated with a Google
   * TOTP credential.
   *
   * @param id               The unique identifier of the scratch code entity.
   * @param googleTotpCredId The unique identifier of the associated Google TOTP credential.
   * @param scratchCode      The scratch code string.
   */
  case class DbTotpCredScratchCodes(id: Int, googleTotpCredId: Int, scratchCode: String)
  
  /**
   * Table representing the "totp_cred_scratch_codes" table in the "auth" schema.
   *
   * @param tag the table tag
   */
  class TotpCredScratchCodes(tag: Tag) extends Table[DbTotpCredScratchCodes](tag, Some("auth"), "totp_cred_scratch_codes") {
    def id = column[Int]("id", O.PrimaryKey)
    def googleTotpCredId = column[Int]("google_totp_cred_id")
    def scratchCode = column[String]("scratch_code")
    
    def * = (id, googleTotpCredId, scratchCode).mapTo[DbTotpCredScratchCodes]
  }

  // table query definitions
  val slickUsers = TableQuery[Users]
  val slickAuthTokens = TableQuery[AuthTokens]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickOAuth1Infos = TableQuery[OAuth1Infos]
  val slickOAuth2Infos = TableQuery[OAuth2Infos]
  val slickOpenIDInfos = TableQuery[OpenIDInfos]
  val slickOpenIDAttributes = TableQuery[OpenIDAttributes]
  val slickGoogleTotpInfos = TableQuery[GoogleTotpInfo]
  val slickGoogleTotpCredentials = TableQuery[GoogleTotpCredentials]
  val slickTotpInfoScratchCodes = TableQuery[TotpInfoScratchCodes]
  val slickTotpCredScratchCodes = TableQuery[TotpCredScratchCodes]
}
