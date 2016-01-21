package infothunder.reactive.routing

import akka.actor.Actor
import akka.actor.Props
import infothunder.reactive.core.AccessRushActor
import infothunder.reactive.core.DataSyncActor
import infothunder.reactive.core.MusicPlayActor
import infothunder.reactive.core.StatisticsActor
import infothunder.reactive.core.WeChatTokenActor
import infothunder.reactive.db.AccessRushDB
import infothunder.reactive.db.DBHelper
import infothunder.reactive.domain._
import spray.http.HttpHeaders.RawHeader
import spray.routing.HttpService
import spray.routing.Route
import infothunder.reactive.core.ReportActor
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

  def syncData(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new DataSyncActor()), message)

  def play(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new MusicPlayActor()), message)

  def accessRush(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new AccessRushActor()), message)

  def wechatToken(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new WeChatTokenActor), message)

  def statistics(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new StatisticsActor()), message)

  def reportDemo(message: RestMessage): Route =
    ctx => perRequest(ctx, Props(new ReportActor()), message)
}
