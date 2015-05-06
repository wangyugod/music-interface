package infothunder.reactive.routing

/**
 * Created by Simon Wang on 2014/12/3.
 */

import akka.actor._
import akka.actor.SupervisorStrategy.Stop
import infothunder.reactive.domain.{Validation, Error, RestMessage}
import infothunder.reactive.routing.PerRequest.{WithProps, WithActorRef}
import spray.http.StatusCodes._
import spray.routing.RequestContext
import akka.actor.OneForOneStrategy
import spray.httpx.Json4sSupport
import scala.concurrent.duration._
import org.json4s.DefaultFormats
import spray.http.StatusCode

trait PerRequest extends Actor with Json4sSupport with ActorLogging {

  import context._

  val json4sFormats = DefaultFormats

  def r: RequestContext

  def target: ActorRef

  def message: RestMessage

  setReceiveTimeout(30.seconds)
  target ! message

  def receive = {
    case res: RestMessage => complete(OK, res)
    case result: List[RestMessage] =>
      log.debug(s"received result $result")
      complete(OK, result)
    case v: Validation => complete(BadRequest, v)
    case ReceiveTimeout => complete(GatewayTimeout, Error("Request timeout"))
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) = {
    r.complete(status, obj)
    stop(self)
  }

  override val supervisorStrategy =
    OneForOneStrategy() {
      case e => {
        complete(InternalServerError, Error(e.getMessage))
        Stop
      }
    }
}

object PerRequest {

  case class WithActorRef(r: RequestContext, target: ActorRef, message: RestMessage) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, message: RestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }

}

trait PerRequestCreator {
  this: Actor =>

  def perRequest(r: RequestContext, target: ActorRef, message: RestMessage) =
    context.actorOf(Props(new WithActorRef(r, target, message)))

  def perRequest(r: RequestContext, props: Props, message: RestMessage) =
    context.actorOf(Props(new WithProps(r, props, message)))
}
