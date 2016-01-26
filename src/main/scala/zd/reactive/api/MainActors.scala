package zd.reactive.api

import akka.actor.Props
import zd.reactive.profile.ProfileActor

/**
 * Created by thinkpad-pc on 2014/10/27.
 */
trait MainActors {
  this : AbstractSystem =>

  lazy val profile = system.actorOf(Props[ProfileActor], "profile")
}
