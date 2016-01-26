package zd.reactive

import scala.concurrent.duration._
import akka.actor.ActorSystem
import akka.actor.Props
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import db.DBHelper
import routing.RestRouting
import spray.can.Http
import zd.reactive.db.DBHelper
import zd.reactive.routing.RestRouting
import zd.reactive.db.ScalalikeConnectionPool


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
    ScalalikeConnectionPool.close
  }
  implicit val timeout = Timeout(60.seconds)
  
  //initialize db setting
  DBHelper.init
  ScalalikeConnectionPool.init

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
  
  val pwdServerEnabled = config.getBoolean("business.pwd.server.enabled")
}