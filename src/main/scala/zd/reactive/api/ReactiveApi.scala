package zd.reactive.api

import akka.actor.{Props, ActorSystem}
import akka.event.Logging._
import zd.reactive.profile.ProfileService
import spray.http.HttpRequest
import spray.http.StatusCodes._
import spray.routing.directives.LogEntry
import spray.routing.{RouteConcatenation, Directives}

/**
 * Created by thinkpad-pc on 2014/10/27.
 */
trait AbstractSystem {
  implicit def system: ActorSystem
}

trait ReactiveApi extends RouteConcatenation with StaticRoute with AbstractSystem {
  this: MainActors =>
  val rootService = system.actorOf(Props(classOf[RootService], routes))

  lazy val routes =  {
    new ProfileService(profile).route ~
      staticRoute
  }

  private def showReq(req: HttpRequest) = LogEntry(req.uri, InfoLevel)
}

trait StaticRoute extends Directives {
  this: AbstractSystem =>

  lazy val staticRoute =
    path("favicon.ico") {
      getFromResource("favicon.ico")
    } ~
    pathPrefix("views"){
      getFromResourceDirectory("views/")
    } ~
      pathPrefix("markers") {
        getFromResourceDirectory("markers/")
      } ~
      pathPrefix("css") {
        getFromResourceDirectory("css/")
      } ~
      pathPrefix("scripts") {
        getFromResourceDirectory("scripts/")
      } ~
      pathEndOrSingleSlash {
        getFromResource("views/index.html")
      } ~ complete(NotFound)
}
