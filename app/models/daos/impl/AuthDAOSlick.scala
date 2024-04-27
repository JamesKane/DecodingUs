package models.daos.impl

import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

/**
 * Trait that contains generic slick db handling code to be mixed in with DAOs
 */
trait AuthDAOSlick extends AuthDbTableDefinitions with HasDatabaseConfigProvider[JdbcProfile]
