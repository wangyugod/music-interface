package zd.reactive.routing

import akka.actor.Actor
import akka.actor.Props
import zd.reactive.db.AccessRushDB
import zd.reactive.db.DBHelper
import zd.reactive.domain._
import spray.http.HttpHeaders.RawHeader
import spray.routing.HttpService
import spray.routing.Route
import zd.reactive.core.ReportActor
import spray.http.MediaTypes._
import spray.http.StatusCodes

class RestRouting extends HttpService with Actor with PerRequestCreator {
  implicit def actorRefFactory = context

  def receive = runRoute(route)

  DBHelper.init
  AccessRushDB.init

  val route = {
    respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
      respondWithHeader(RawHeader("Access-Control-Allow-Headers", "Content-Type,X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5,  Date, X-Api-Version, X-File-Name")) {
        respondWithHeader(RawHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS")) {
          respondWithMediaType(`application/json`) {
            options {
              complete(StatusCodes.OK)
            } ~ post {
              path("rest" / "report" / "report-demo") {
                import spray.httpx.SprayJsonSupport._
                import spray.util._
                import ReportRequestJsonSupport._
                entity(as[ReportRequest]) {
                  data =>
                    reportDemo(data)
                }
              } ~ path("rest" / "report" / "directive"){
                import spray.httpx.SprayJsonSupport._
                import spray.util._
                import DropdownDirectiveRequestJsonSupport._
                entity(as[DropdownDirectiveRequest]) {
                  data =>
                    reportDemo(data)
                }
              } ~ path("rest" / "report" / "interbusistat"){
                import spray.httpx.SprayJsonSupport._
                import spray.util._
                import InterBusinessReportRequestJsonSupport._
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
    /*get {
      path("pets") {
        parameters('names) { names =>
          syncData{

          }
          /*petsWithOwner {
            //GetPetsWithOwners(names.split(',').toList)
          }*/
        }
      }
    }*/
  }
  val index = <html>
                <body>
                  <h1>
                    Say hello to
                    <i>spray-routing</i>
                    on
                    <i>spray-can</i>
                    !
                  </h1>
                  <p>Defined resources:</p>
                  <form action="/rest/player/songs" method="POST">
                    <input type="text" name="songs"/>
                    <input type="text" name="password"/>
                    <input type="submit" value="submit"/>
                  </form>
                </body>
              </html>

  /*def petsWithOwner(message : RestMessage): Route =
    ctx => perRequest(ctx, Props(new GetPetsWithOwnersActor(petService, ownerService)), message)*/

  def reportDemo(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new ReportActor()), message)
}
