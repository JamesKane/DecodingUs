package utils.auth

import models.User
import play.silhouette.api.Env
import play.silhouette.impl.authenticators.CookieAuthenticator

/**
 * The default env.
 */
trait DefaultEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}
