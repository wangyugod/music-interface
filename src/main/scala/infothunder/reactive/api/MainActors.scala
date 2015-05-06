package infothunder.reactive.api

import akka.actor.Props
import infothunder.reactive.profile.ProfileActor

/**
 * Created by thinkpad-pc on 2014/10/27.
 */
trait MainActors {
  this : AbstractSystem =>

  lazy val profile = system.actorOf(Props[ProfileActor], "profile")
}
