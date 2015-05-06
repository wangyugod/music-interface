package infothunder.reactive.profile

import akka.actor.{ActorRef, ActorSystem}
import spray.routing.Directives
import spray.http._
import MediaTypes._
import spray.httpx.marshalling.Marshaller
/**
 * Created by Simon Wang on 2014/11/27.
 */

case class Profile(username: String, password: String)

class ProfileService(profile: ActorRef)(implicit system: ActorSystem) extends Directives {

  lazy val route =
    pathPrefix("profile") {
      val dir = "profile"
      pathEndOrSingleSlash {
        respondWithMediaType(`text/html`) {
          complete {
            index
          }
        }
      } ~ path("register") {
        get {
          parameters('username, 'password) {
            (username, password) => {
              respondWithMediaType(`application/json`) {
                complete {
                  s"""{"name": "$username", "password": "$password"}"""
                }
              }
            }
          }
        }
        } ~ path("register" / ".*".r) {
          id => {
            complete {
              s"register id is $id"
            }
          }
        } ~ post {
          respondWithMediaType(`application/json`) {
            formFields('name.as[String], 'password.as[String]) {
              (name, password) => {
                val profile = Profile(name, password)
                complete(s"name: ${profile.username}, password: ${profile.password}")
              }
            }
          }
        } ~
        getFromResourceDirectory(dir)
    }

  val index = <html>
    <body>
      <h1>Say hello to
        <i>spray-routing</i>
        on
        <i>spray-can</i>
        !</h1>
      <p>Defined resources:</p>
      <form action="/profile/register" method="POST">
        <input type="text" name="name"/>
        <input type="text" name="password"/>
        <input type="submit" value="submit"/>
      </form>
    </body>
  </html>

}
