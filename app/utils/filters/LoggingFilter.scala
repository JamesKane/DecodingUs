package utils.filters

import org.apache.pekko.stream._
import play.api.Logging
import play.api.mvc._
import jakarta.inject._
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class LoggingFilter @Inject() (implicit val mat: Materializer, ec: ExecutionContext) extends Filter with Logging {
  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    val startTime = System.currentTimeMillis
    nextFilter(requestHeader).map { result =>
      val xRealIp = requestHeader.headers.get("X-Real-IP")
      val xForwardedFor = requestHeader.headers.get("X-Forwarded-For")
      val endTime = System.currentTimeMillis
      val requestTime = endTime - startTime
      logger.info(s"${requestHeader.method} ${requestHeader.uri} took ${requestTime}ms, returned ${result.header.status} and originated from ${requestHeader.remoteAddress} X-Real-IP: $xRealIp, X-Forwarded-For: $xForwardedFor")
      result
    }
  }
}
