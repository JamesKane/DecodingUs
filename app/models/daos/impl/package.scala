package models.daos

import slick.jdbc.{GetResult, PositionedParameters, SetParameter}

import java.sql.JDBCType
import java.time.{ZoneId, ZonedDateTime}
import java.util.UUID

package object impl {
  implicit object SetUUID extends SetParameter[UUID] {
    def apply(v: UUID, pp: PositionedParameters): Unit = {
      pp.setObject(v, JDBCType.BINARY.getVendorTypeNumber)
    }
  }

  implicit val uuidGetResult: GetResult[UUID] = GetResult(r => UUID.fromString(r.nextString()))
  implicit val getZonedDateTime: GetResult[ZonedDateTime] = GetResult(r =>
    ZonedDateTime.ofInstant(r.nextTimestamp().toInstant, ZoneId.systemDefault()))
}
