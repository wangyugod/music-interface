package infothunder.reactive.routing

import akka.actor.{ Actor, Props }
import infothunder.reactive.db.DBHelper
import infothunder.reactive.domain._
import infothunder.reactive.core.{ MusicPlayActor, DataSyncActor }
import spray.http.HttpHeaders.RawHeader
import spray.routing.{ HttpService, Route }
import scala.concurrent.duration._
import infothunder.reactive.core.AccessRushActor
import infothunder.reactive.db.AccessRushDB
import infothunder.reactive.core.WeChatTokenActor

class RestRouting extends HttpService with Actor with PerRequestCreator {
  implicit def actorRefFactory = context

  def receive = runRoute(route)

  DBHelper.init
  AccessRushDB.init

  val route = {
    get {
      pathPrefix("rest" / "sync") {
        path("artist") {
          parameters('fromTime, 'pageNumber.as[Int], 'numberPerPage.as[Int]) { (fromTime, pageNumber, numberPerPage) =>
            syncData {
              SyncArtistRequest(fromTime, pageNumber, numberPerPage)
            }
          }
        } ~ path("song") {
          parameters('fromTime, 'pageNumber.as[Int], 'numberPerPage.as[Int]) { (fromTime, pageNumber, numberPerPage) =>
            syncData {
              SyncSongRequest(fromTime, pageNumber, numberPerPage)
            }
          }
        } ~ path("album") {
          parameters('fromTime, 'pageNumber.as[Int], 'numberPerPage.as[Int]) { (fromTime, pageNumber, numberPerPage) =>
            syncData {
              SyncAlbumRequest(fromTime, pageNumber, numberPerPage)
            }
          }
        }
      } ~ path("test") {
        complete("hello")
      } ~ pathEndOrSingleSlash {
        complete(index)
      } ~ path("ar" / "user") {
        parameters('prefix.as[String], 'numberPerPage.as[Int], 'pageNumber.as[Int]) {
          (prefix, numberPerPage, pageNumber) =>
            accessRush(AccessRushRequest(prefix, numberPerPage, pageNumber))
        }
      }
    } ~ post {
      path("rest" / "player" / "songs") {
        entity(as[String]) {
          data =>
            respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
              play(PlayListRequest(data.substring(1, data.length() - 1).split(",")))
            }
        }
      }
    } ~ post {
      path("rest" / "wechat" / "token") {
        entity(as[String]) {
          data =>
            respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
              wechatToken(WechatTokenRequest(data))
            }
        }
      }
    } ~ post{
      path("rest" / "ar" / "delete") {
        entity(as[String]) {
          data =>
            respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
              accessRush(DeleteRushRequest(data))
            }
        }
      }
    } ~ post{
      path("rest" / "ar" / "insert") {
        entity(as[String]) {
          data =>
            respondWithHeader(RawHeader("Access-Control-Allow-Origin", "*")) {
              accessRush(BatchInsertRushRequest(data))
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
}
