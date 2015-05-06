package infothunder.reactive.core

import akka.actor.Actor
import akka.actor.ActorLogging
import infothunder.reactive.domain.WechatTokenRequest
import infothunder.reactive.domain.WechatTokenRequest
import infothunder.reactive.db.AccessRushDB._
import infothunder.reactive.domain.WechatTokenSuccessResponse
import infothunder.reactive.domain.WechatToeknFailResponse

class WeChatTokenActor extends Actor with ActorLogging {
  def receive: Actor.Receive = {
    case request: WechatTokenRequest =>
      log.debug(s"receive request")
      context.parent ! retrieveToken(request)
      log.debug(s"send response to parent actor")
    case _ =>
      log.error("invalid request message")
  }
  
  def retrieveToken(request: WechatTokenRequest) = {
    val sql = "SELECT app_id, access_token,jsapi_token FROM wechat_account WHERE ID = ?"
    withStatement(sql) {
      prepareStatement =>
        prepareStatement.setString(1, request.accountId)
        log.debug(s"before execute query accountId is ${request.accountId}")
        val r = prepareStatement.executeQuery()
        log.debug(s"execute query done ")
        val response =  if(r.next()) {
          WechatTokenSuccessResponse("", r.getString("JSAPI_TOKEN"), r.getString("APP_ID"))
        } else WechatToeknFailResponse(1, "公众号原始id错误")
        r.close()
        if (log.isDebugEnabled)
          log.debug("result is : " + response)
        response
    }
  }
}