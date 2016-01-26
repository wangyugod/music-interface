package zd.reactive.routing

import akka.actor.Actor
import akka.actor.Props
import zd.reactive.db.AccessRushDB
import zd.reactive.db.DBHelper
import zd.reactive.domain._
import zd.reactive.domain.DomainJsonSupport._
import spray.httpx.SprayJsonSupport._
               
import spray.http.HttpHeaders.RawHeader
import spray.routing.HttpService
import spray.routing.Route
import zd.reactive.core.ReportActor
import spray.http.MediaTypes._
import spray.http.StatusCodes
import zd.reactive.core.UserManagementActor

class RestRouting extends HttpService with Actor with PerRequestCreator {
  implicit def actorRefFactory = context

  def receive = runRoute(route)

  val route = {
    respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
      respondWithHeader(RawHeader("Access-Control-Allow-Headers", "Content-Type,X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5,  Date, X-Api-Version, X-File-Name")) {
        respondWithHeader(RawHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS")) {
          respondWithMediaType(`application/json`) {
            options {
              complete(StatusCodes.OK)
            } ~ post {
              path("rest" / "login") {
                entity(as[LoginRequest]) {
                  data =>
                    login(data)
                }
              } ~ path("rest" / "report" / "report-demo") {
                entity(as[ReportRequest]) {
                  data =>
                    reportDemo(data)
                }
              } ~ path("rest" / "report" / "directive"){
                entity(as[DropdownDirectiveRequest]) {
                  data =>
                    reportDemo(data)
                }
              } ~ path("rest" / "report" / "interbusistat"){
                entity(as[InterBusinessReportRequest]) {
                  data =>
                    reportDemo(data)
                }
              }
            }
          }
        }
      }
    } 
  }

  def reportDemo(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new ReportActor()), message)
    
  def login(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new UserManagementActor()), message)
    
  
}
