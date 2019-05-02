package modules

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.services.{AuthenticatorService, IdentityService}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import javax.inject.Inject
import models.User
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.inject.{Binding, Module}

import scala.concurrent.ExecutionContext.Implicits.global

class SilhouetteModule extends Module {//@Inject()(auth:CookieAuthenticatorService) extends AbstractModule with ScalaModule {

  override def bindings(environment: play.api.Environment, configuration: Configuration): Seq[Binding[_]] = Seq(
    bind[Silhouette[PspsEnv]].to[SilhouetteProvider[PspsEnv]],
    bind[IdentityService[User]].to[UserService]
  )

//  override def configure(): Unit = {
//    bind[Silhouette[PspsEnv]].to[SilhouetteProvider[PspsEnv]]
//    bind[IdentityService[User]].to[UserService]
//    bind[AuthenticatorService[CookieAuthenticator]].toInstance(auth)
//  }

  @Provides
  def provideEnvironment(
        userService: UserService,
        authenticatorService: AuthenticatorService[CookieAuthenticator],
        requestProvider: RequestProvider,
        eventBus: EventBus): Environment[PspsEnv] = {
    Environment[PspsEnv](
      userService, authenticatorService, Seq(requestProvider), eventBus
    )
  }
//
//  /**
//    * Provides the signer for the authenticator.
//    *
//    * @param configuration The Play configuration.
//    * @return The signer for the authenticator.
//    */
//  @Provides @Named("authenticator-signer")
//  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
//    val config = configuration.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")
//
//    new JcaSigner(config)
//  }
//
//  /**
//    * Provides the crypter for the authenticator.
//    *
//    * @param configuration The Play configuration.
//    * @return The crypter for the authenticator.
//    */
//  @Provides @Named("authenticator-crypter")
//  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
//    val config = configuration.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")
//
//    new JcaCrypter(config)
//  }

//  /**
//    * Provides the auth info repository.
//    *
//    * @param passwordInfoDAO The implementation of the delegable password auth info DAO.
//    * @param oauth1InfoDAO The implementation of the delegable OAuth1 auth info DAO.
//    * @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
//    * @param openIDInfoDAO The implementation of the delegable OpenID auth info DAO.
//    * @return The auth info repository instance.
//    */
//  @Provides
//  def provideAuthInfoRepository(
//                                 passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo],
//                                 oauth1InfoDAO: DelegableAuthInfoDAO[OAuth1Info],
//                                 oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info],
//                                 openIDInfoDAO: DelegableAuthInfoDAO[OpenIDInfo]): AuthInfoRepository = {
//
//    new DelegableAuthInfoRepository(passwordInfoDAO, oauth1InfoDAO, oauth2InfoDAO, openIDInfoDAO)
//  }
//
//  /**
//    * Provides the authenticator service.
//    *
//    * @param signer The signer implementation.
//    * @param crypter The crypter implementation.
//    * @param cookieHeaderEncoding Logic for encoding and decoding `Cookie` and `Set-Cookie` headers.
//    * @param fingerprintGenerator The fingerprint generator implementation.
//    * @param idGenerator The ID generator implementation.
//    * @param configuration The Play configuration.
//    * @param clock The clock instance.
//    * @return The authenticator service.
//    */
//  @Provides
//  def provideAuthenticatorService(
//                                   @Named("authenticator-signer") signer: Signer,
//                                   @Named("authenticator-crypter") crypter: Crypter,
//                                   cookieHeaderEncoding: CookieHeaderEncoding,
//                                   fingerprintGenerator: FingerprintGenerator,
//                                   idGenerator: IDGenerator,
//                                   configuration: Configuration,
//                                   clock: Clock): AuthenticatorService[CookieAuthenticator] = {
//
//    val config = configuration.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
//    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)
//
//    new CookieAuthenticatorService(config, None, signer, cookieHeaderEncoding, authenticatorEncoder, fingerprintGenerator, idGenerator, clock)
//  }

}

