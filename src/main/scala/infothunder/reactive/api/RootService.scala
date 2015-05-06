package infothunder.reactive.api

import akka.actor.{Actor, ActorLogging}
import spray.routing._

/**
 * Created by thinkpad-pc on 2014/10/27.
 */
class RootService(route : Route) extends Actor with HttpService with ActorLogging {
  implicit def actorRefFactory = context
  override def receive = runRoute(route)
}