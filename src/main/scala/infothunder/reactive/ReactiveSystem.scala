package infothunder.reactive

import akka.io.IO
import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import infothunder.reactive.api.{ReactiveApi, MainActors}
import infothunder.reactive.db.DBHelper
import infothunder.reactive.routing.RestRouting
import akka.pattern.ask
import scala.concurrent.duration._
import spray.can.Http


/**
 * Created by Simon Wang on 2014/10/26.
 */
object ReactiveSystem extends App{
  implicit val system = ActorSystem("music-infterface")
  val serviceActor = system.actorOf(Props(new RestRouting), name = "rest-routing")
  system.registerOnTermination{
    //close datasource connection pool when shutting down
    println("reactive system shut down now")
    DBHelper.close
  }
  implicit val timeout = Timeout(60.seconds)

  IO(Http) ? Http.Bind(serviceActor, Configuration.host, port = Configuration.portHttp)

}


object Configuration {
  import com.typesafe.config.ConfigFactory

  private val config = ConfigFactory.load
  config.checkValid(ConfigFactory.defaultReference)

  val host = config.getString("mimi.host")
  val portHttp = config.getInt("mimi.ports.http")
  val portTcp  = config.getInt("mimi.ports.tcp")
  val portWs   = config.getInt("mimi.ports.ws")

  val dbUrl = config.getString("jdbc.url")
  val userName = config.getString("jdbc.username")
  val password = config.getString("jdbc.password")
  val driver = config.getString("jdbc.driver")
  
  val ardbUrl = config.getString("arjdbc.url")
  val arUserName = config.getString("arjdbc.username")
  val arPassword = config.getString("arjdbc.password") 
}