package modules

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.{Config, ConfigFactory}
import controllers.{DefaultRememberMeConfig, DefaultSilhouetteControllerComponents, RememberMeConfig, SilhouetteControllerComponents}
import models.daos.UserDAO
import models.daos.impl.{OAuth1InfoDAO, OAuth2InfoDAO, OpenIDInfoDAO, PasswordInfoDAO, UserDAOImpl}
import models.services.UserService
import models.services.impl.UserServiceImpl
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.openid.OpenIdClient
import play.api.libs.ws.WSClient
import play.api.mvc.{Cookie, CookieHeaderEncoding}
import play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import play.silhouette.api.crypto.*
import play.silhouette.api.repositories.AuthInfoRepository
import play.silhouette.api.services.*
import play.silhouette.api.util.*
import play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import play.silhouette.impl.authenticators.*
import play.silhouette.impl.providers.*
import play.silhouette.impl.providers.oauth1.*
import play.silhouette.impl.providers.oauth1.secrets.{CookieSecretProvider, CookieSecretSettings}
import play.silhouette.impl.providers.oauth2.*
import play.silhouette.impl.providers.openid.YahooProvider
import play.silhouette.impl.providers.openid.services.PlayOpenIDService
import play.silhouette.impl.providers.state.{CsrfStateItemHandler, CsrfStateSettings}
import play.silhouette.impl.services.*
import play.silhouette.impl.util.*
import play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import utils.auth.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler, DefaultEnv}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {
  
  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[Silhouette[DefaultEnv]].to[SilhouetteProvider[DefaultEnv]]
    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]
    bind[UserService].to[UserServiceImpl]
    bind[UserDAO].to[UserDAOImpl]
    bind[CacheLayer].to[PlayCacheLayer]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
    bind[Clock].toInstance(Clock())

    // Replace this with the bindings to your concrete DAOs
    bind[DelegableAuthInfoDAO[GoogleTotpInfo]].toInstance(new InMemoryAuthInfoDAO[GoogleTotpInfo])
  }

  /**
   * Provides an instance of DelegableAuthInfoDAO[PasswordInfo] by taking in a DatabaseConfigProvider
   *
   * @param dbConfig The DatabaseConfigProvider used for creating the PasswordInfoDAO instance
   * @return An instance of DelegableAuthInfoDAO[PasswordInfo]
   */
  @Provides
  def providePasswordInfo(dbConfig: DatabaseConfigProvider): DelegableAuthInfoDAO[PasswordInfo] = {
    new PasswordInfoDAO(dbConfig)
  }

  /**
   * Provides an instance of DelegableAuthInfoDAO[OAuth1Info] using the given DatabaseConfigProvider.
   *
   * @param dbConfig The DatabaseConfigProvider to be used for creating the OAuth1InfoDAO.
   * @return An instance of DelegableAuthInfoDAO[OAuth1Info] created using the provided DatabaseConfigProvider.
   */
  @Provides
  def provideOAuth1InfoDAO(dbConfig: DatabaseConfigProvider): DelegableAuthInfoDAO[OAuth1Info] = {
    new OAuth1InfoDAO(dbConfig)
  }

  /**
   * Provides an instance of `DelegableAuthInfoDAO[OAuth2Info]` using the given `dbConfig` as the database configuration provider.
   *
   * @param dbConfig The database configuration provider.
   * @return An instance of `DelegableAuthInfoDAO[OAuth2Info]`.
   */
  @Provides
  def provideOAuth2InfoDAO(dbConfig: DatabaseConfigProvider): DelegableAuthInfoDAO[OAuth2Info] = {
    new OAuth2InfoDAO(dbConfig)
  }

  /**
   * Provides an instance of DelegableAuthInfoDAO[OpenIDInfo].
   *
   * @param dbConfig The DatabaseConfigProvider used to initialize the OpenIDInfoDAO.
   * @return An instance of DelegableAuthInfoDAO[OpenIDInfo].
   */
  @Provides
  def provideOpenIDInfoDAO(dbConfig: DatabaseConfigProvider): DelegableAuthInfoDAO[OpenIDInfo] = {
    new OpenIDInfoDAO(dbConfig)
  }

  /**
   * Provides the HTTP layer implementation.
   *
   * @param client Play's WS client.
   * @return The HTTP layer implementation.
   */
  @Provides
  def provideHTTPLayer(client: WSClient): HTTPLayer = new PlayHTTPLayer(client)

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[CookieAuthenticator],
                          eventBus: EventBus): Environment[DefaultEnv] = {

    Environment[DefaultEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  /**
   * Provides the social provider registry.
   *
   * @param facebookProvider The Facebook provider implementation.
   * @param googleProvider The Google provider implementation.
   * @param vkProvider The VK provider implementation.
   * @param yahooProvider The Yahoo provider implementation.
   * @return The Silhouette environment.
   */
  @Provides
  def provideSocialProviderRegistry(
                                     facebookProvider: FacebookProvider,
                                     googleProvider: GoogleProvider,
                                     vkProvider: VKProvider,
                                     yahooProvider: YahooProvider): SocialProviderRegistry = {

    SocialProviderRegistry(Seq(
      googleProvider,
      facebookProvider,
      vkProvider,
      yahooProvider
    ))
  }

  /**
   * Provides the signer for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The signer for the OAuth1 token secret provider.
   */
  @Provides @Named("oauth1-token-secret-signer")
  def provideOAuth1TokenSecretSigner(configuration: Configuration): Signer = {
    val underlyingConfig = ConfigFactory.load()
    val settingPath = "silhouette.oauth1TokenSecretProvider.signer"
    if(underlyingConfig.hasPath(settingPath)) {
      val settings = underlyingConfig.getConfig(settingPath)
      new JcaSigner(JcaSignerSettings(settings.getString("key")))
    } else {
      throw new RuntimeException(s"Configuration for $settingPath not found")
    }

  }

  /**
   * Provides the crypter for the OAuth1 token secret provider.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the OAuth1 token secret provider.
   */
  @Provides @Named("oauth1-token-secret-crypter")
  def provideOAuth1TokenSecretCrypter(configuration: Configuration): Crypter = {
    val config = getConfig("silhouette.oauth1TokenSecretProvider.crypter")
    new JcaCrypter(JcaCrypterSettings(config.getString("key")))
  }

  /**
   * Provides the signer for the CSRF state item handler.
   *
   * @param configuration The Play configuration.
   * @return The signer for the CSRF state item handler.
   */
  @Provides @Named("csrf-state-item-signer")
  def provideCSRFStateItemSigner(configuration: Configuration): Signer = {
    val config = getConfig("silhouette.csrfStateItemHandler.signer")
    new JcaSigner(JcaSignerSettings(config.getString("key")))
  }
  
  /**
   * Provides the signer for the social state handler.
   *
   * @param configuration The Play configuration.
   * @return The signer for the social state handler.
   */
  @Provides @Named("social-state-signer")
  def provideSocialStateSigner(configuration: Configuration): Signer = {
    val config = getConfig("silhouette.socialStateHandler.signer")
    new JcaSigner(JcaSignerSettings(config.getString("key")))
  }

  /**
   * Provides the signer for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The signer for the authenticator.
   */
  @Provides @Named("authenticator-signer")
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = getConfig("silhouette.authenticator.signer")
    new JcaSigner(JcaSignerSettings(config.getString("key")))
  }

  /**
   * Provides the crypter for the authenticator.
   *
   * @param configuration The Play configuration.
   * @return The crypter for the authenticator.
   */
  @Provides @Named("authenticator-crypter")
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = getConfig("silhouette.authenticator.crypter")
    new JcaCrypter(JcaCrypterSettings(config.getString("key")))
  }

  /**
   * Provides the auth info repository.
   *
   * @param totpInfoDAO The implementation of the delegable totp auth info DAO.
   * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
   * @param oauth1InfoDAO The implementation of the delegable OAuth1 auth info DAO.
   * @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
   * @param openIDInfoDAO The implementation of the delegable OpenID auth info DAO.
   * @return The auth info repository instance.
   */
  @Provides
  def provideAuthInfoRepository(
                                 totpInfoDAO: DelegableAuthInfoDAO[GoogleTotpInfo],
                                 passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
                                 oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
                                 oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
                                 openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]): AuthInfoRepository = {

    new DelegableAuthInfoRepository(totpInfoDAO, passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO)
  }

  /**
   * Provides the authenticator service.
   *
   * @param signer The signer implementation.
   * @param crypter The crypter implementation.
   * @param cookieHeaderEncoding Logic for encoding and decoding `Cookie` and `Set-Cookie` headers.
   * @param fingerprintGenerator The fingerprint generator implementation.
   * @param idGenerator The ID generator implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
                                   @Named("authenticator-signer") signer: Signer,
                                   @Named("authenticator-crypter") crypter: Crypter,
                                   cookieHeaderEncoding: CookieHeaderEncoding,
                                   fingerprintGenerator: FingerprintGenerator,
                                   idGenerator: IDGenerator,
                                   configuration: Configuration,
                                   clock: Clock): AuthenticatorService[CookieAuthenticator] = {

    val config = getConfig("silhouette.authenticator")
    val settings = CookieAuthenticatorSettings(
      cookieName = config.getString("cookieName"),
      cookiePath = config.getString("cookiePath"),
      cookieDomain = if (config.hasPath("cookieDomain")) Some(config.getString("cookieDomain")) else None,
      secureCookie = config.getBoolean("secureCookie"),
      httpOnlyCookie = config.getBoolean("httpOnlyCookie"),
      sameSite = if (config.hasPath("sameSite")) Some(config.getString("sameSite")).flatMap(Cookie.SameSite.parse) else None,
      authenticatorExpiry = Duration.fromNanos(config.getDuration("authenticatorExpiry").toNanos)
    )
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(settings, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
  }

  /**
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)

  /**
   * Provides the OAuth1 token secret provider.
   *
   * @param signer The signer implementation.
   * @param crypter The crypter implementation.
   * @param configuration The Play configuration.
   * @param clock The clock instance.
   * @return The OAuth1 token secret provider implementation.
   */
  @Provides
  def provideOAuth1TokenSecretProvider(
                                        @Named("oauth1-token-secret-signer") signer: Signer,
                                        @Named("oauth1-token-secret-crypter") crypter: Crypter,
                                        configuration: Configuration,
                                        clock: Clock): OAuth1TokenSecretProvider = {
    val value = "silhouette.oauth1TokenSecretProvider"
    val settings: CookieSecretSettings = getCookieSecretSettings(value)
    new CookieSecretProvider(settings, signer, crypter, clock)
  }



  /**
   * Provides the CSRF state item handler.
   *
   * @param idGenerator The ID generator implementation.
   * @param signer The signer implementation.
   * @param configuration The Play configuration.
   * @return The CSRF state item implementation.
   */
  @Provides
  def provideCsrfStateItemHandler(
                                   idGenerator: IDGenerator,
                                   @Named("csrf-state-item-signer") signer: Signer,
                                   configuration: Configuration): CsrfStateItemHandler = {
    val config = getConfig("silhouette.csrfStateItemHandler")
    val settings = CsrfStateSettings(
      cookieName = config.getString("cookieName"),
      cookiePath = config.getString("cookiePath"),
      cookieDomain = if (config.hasPath("cookieDomain")) Some(config.getString("cookieDomain")) else None,
      secureCookie = config.getBoolean("secureCookie"),
      httpOnlyCookie = config.getBoolean("httpOnlyCookie"),
      sameSite = if (config.hasPath("sameSite")) Some(config.getString("sameSite")).flatMap(Cookie.SameSite.parse) else None,
      expirationTime = Duration.fromNanos(config.getDuration("expirationTime").toNanos)
    )
    new CsrfStateItemHandler(settings, idGenerator, signer)
  }

  /**
   * Provides the social state handler.
   *
   * @param signer The signer implementation.
   * @return The social state handler implementation.
   */
  @Provides
  def provideSocialStateHandler(
                                 @Named("social-state-signer") signer: Signer,
                                 csrfStateItemHandler: CsrfStateItemHandler): SocialStateHandler = {

    new DefaultSocialStateHandler(Set(csrfStateItemHandler), signer)
  }

  /**
   * Provides the password hasher registry.
   *
   * @return The password hasher registry.
   */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  /**
   * Provides the credentials provider.
   *
   * @param authInfoRepository The auth info repository implementation.
   * @param passwordHasherRegistry The password hasher registry.
   * @return The credentials provider.
   */
  @Provides
  def provideCredentialsProvider(
                                  authInfoRepository: AuthInfoRepository,
                                  passwordHasherRegistry: PasswordHasherRegistry): CredentialsProvider = {

    new CredentialsProvider(authInfoRepository, passwordHasherRegistry)
  }

  /**
   * Provides the TOTP provider.
   *
   * @return The credentials provider.
   */
  @Provides
  def provideTotpProvider(passwordHasherRegistry: PasswordHasherRegistry): GoogleTotpProvider = {
    new GoogleTotpProvider(passwordHasherRegistry)
  }

  /**
   * Provides the Facebook provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param socialStateHandler The social state handler implementation.
   * @param configuration The Play configuration.
   * @return The Facebook provider.
   */
  @Provides
  def provideFacebookProvider(
                               httpLayer: HTTPLayer,
                               socialStateHandler: SocialStateHandler,
                               configuration: Configuration): FacebookProvider = {
    val settings: OAuth2Settings = getOauth2Settings("silhouette.facebook")
    new FacebookProvider(httpLayer, socialStateHandler, settings)
  }

  /**
   * Provides the Google provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param socialStateHandler The social state handler implementation.
   * @param configuration The Play configuration.
   * @return The Google provider.
   */
  @Provides
  def provideGoogleProvider(
                             httpLayer: HTTPLayer,
                             socialStateHandler: SocialStateHandler,
                             configuration: Configuration): GoogleProvider = {

    new GoogleProvider(httpLayer, socialStateHandler, getOauth2Settings("silhouette.google"))
  }

  /**
   * Provides the VK provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param socialStateHandler The social state handler implementation.
   * @param configuration The Play configuration.
   * @return The VK provider.
   */
  @Provides
  def provideVKProvider(
                         httpLayer: HTTPLayer,
                         socialStateHandler: SocialStateHandler,
                         configuration: Configuration): VKProvider = {

    new VKProvider(httpLayer, socialStateHandler, getOauth2Settings("silhouette.vk"))
  }

  /**
   * Provides the Yahoo provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param client The OpenID client implementation.
   * @param configuration The Play configuration.
   * @return The Yahoo provider.
   */
  @Provides
  def provideYahooProvider(
                            httpLayer: HTTPLayer,
                            client: OpenIdClient,
                            configuration: Configuration): YahooProvider = {

    val config = getConfig("silhouette.yahoo")
    val settings = OpenIDSettings(
      providerURL = config.getString("providerURL"), 
      callbackURL = config.getString("callbackURL"), 
      axRequired = Map(),
      realm = Option(config.getString("realm"))
    )
    new YahooProvider(httpLayer, new PlayOpenIDService(client, settings), settings)
  }

  /**
   * Provides the remember me configuration.
   *
   * @param configuration The Play configuration.
   * @return The remember me config.
   */
  @Provides
  def providesRememberMeConfig(configuration: Configuration): RememberMeConfig = {
    val c = configuration.underlying
    DefaultRememberMeConfig(
      expiry = getConfigDuration(c, "silhouette.authenticator.rememberMe.authenticatorExpiry").get,
      idleTimeout = getConfigDuration(c, "silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
      cookieMaxAge = getConfigDuration(c, "silhouette.authenticator.rememberMe.cookieMaxAge")
    )
  }

  private def getCookieSecretSettings(value: String) = {
    val config = getConfig(value)
    val duration = "expriationTime"
    val settings = CookieSecretSettings(
      cookieName = config.getString("cookieName"),
      cookiePath = config.getString("cookiePath"),
      cookieDomain = Option(config.getString("cookieDomain")),
      secureCookie = config.getBoolean("secureCookie"),
      httpOnlyCookie = config.getBoolean("httpOnlyCookie"),
      sameSite = Option(config.getString("sameSite")).flatMap(v => Cookie.SameSite.parse(v)),
      expirationTime = getConfigDuration(config, duration).get
    )
    settings
  }

  private def getConfigDuration(config: Config, duration: String) = {
    val javaDuration = config.getDuration(duration)
    if(javaDuration == null) None
    else Option(Duration.fromNanos(javaDuration.toNanos))
  }

  private def getOauth2Settings(value: String) = {
    val config = getConfig(value)
    val settings = OAuth2Settings(
      authorizationURL = Option(config.getString("authorizationURL")),
      accessTokenURL = config.getString("accessTokenURL"),
      redirectURL = Option(config.getString("redirectURL")),
      apiURL = if(config.hasPath("apiURL")) Option(config.getString("apiURL")) else None,
      clientID = config.getString("clientID"),
      clientSecret = config.getString("clientSecret"),
      scope = Option(config.getString("scope"))
    )
    settings
  }

  private def getConfig(settingPath: String) = {
    val underlyingConfig = ConfigFactory.load()
    val settings = if (underlyingConfig.hasPath(settingPath)) {
      underlyingConfig.getConfig(settingPath)
    } else {
      throw new RuntimeException(s"Configuration for $settingPath not found")
    }
    settings
  }

  @Provides
  def providesSilhouetteComponents(components: DefaultSilhouetteControllerComponents): SilhouetteControllerComponents = {
    components
  }
}