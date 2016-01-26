package zd.reactive.core

import akka.actor.ActorLogging
import akka.actor.Actor
import zd.reactive.domain.AccessRushRequest
import zd.reactive.db.AccessRushDB
import zd.reactive.domain.Artist
import zd.reactive.domain.AccessRush
import zd.reactive.domain.DeleteRushRequest
import zd.reactive.domain.BatchInsertRushRequest
import java.security.MessageDigest
import javax.xml.bind.DatatypeConverter
import zd.reactive.domain.SUCCESS

class AccessRushActor extends Actor with ActorLogging {

  def receive = {
    case request: AccessRushRequest =>
      log.debug(s"receive access rush request")
      context.parent ! processAccessRush(request.prefix, request.numberPerPage, request.pageNumber)
      log.debug(s"send response to parent actor")
    case request: DeleteRushRequest =>
      context.parent ! deleteRushNumbers(request.prefix)
    case request: BatchInsertRushRequest =>
      context.parent ! batchInsertRushNumbers(request.numbers)
    case _ => context.parent ! "Invalid Request"
  }

  def processAccessRush(prefix: String, numberPerPage: Int, pageNumber: Int) = {
    val sql = s"select * from( select a.*, rownum ru from (select * from wechat_users a where a.openid like '$prefix%' order by a.openid asc) a where rownum <= ?) where ru > ?"
    AccessRushDB.withStatement(sql) {
      statement =>
        statement.setInt(2, (pageNumber - 1) * numberPerPage)
        statement.setInt(1, pageNumber * numberPerPage)
        val r = statement.executeQuery()
        log.debug(s"execute query done ")
        var list = List[AccessRush]()
        while (r.next()) {
          list = AccessRush(r.getString("OPENID"), r.getString("TEL")) :: list
        }
        r.close()
        if (log.isDebugEnabled)
          log.debug("result list for ar is : " + list)
        list
    }
  }

  def deleteRushNumbers(prefix: String) = {
    val user = "\"USER\""
    val sql = s"delete from TBL_USERS a where a.$user like '$prefix%'"
    AccessRushDB.withStatement(sql) {
      statement =>
        val cnt = statement.executeUpdate();
        if (log.isInfoEnabled)
          log.info(s"$cnt records deleted")
        SUCCESS(cnt, s"$cnt records delted successfully")
    }
  }
  
  def batchInsertRushNumbers(numbers: String) = {
    val valueArray = numbers.split(":")
    val prefix = valueArray(0)
    val numberArray = valueArray(1).split(",")
    if(log.isInfoEnabled)
      log.info("phone size : " + numberArray.length)
    for(phone <- numberArray){
      insertOpenId(phone, prefix)
    }
    SUCCESS(numberArray.length, s" ${numberArray.length} records inserted successfully")
  }
  
  private def insertOpenId(phone: String, prefix: String){
    val user = "\"USER\""
    val sql = s"insert into TBL_USERS(ID,$user,TEL) values (S_TBL_USERS.nextval,?,?)"
    AccessRushDB.withStatement(sql) {
      statement =>
        statement.setString(1, prefix + md5Hash(phone))
        statement.setString(2, phone)
        val cnt = statement.executeUpdate();
        statement.getConnection().commit()
        if (log.isInfoEnabled)
          log.info(s"$cnt records inserted")
        cnt
    }
  }
  
  private def md5Hash(number: String) = {
    val md = MessageDigest.getInstance("MD5")
    val bytes = md.digest(number.getBytes)
    DatatypeConverter.printHexBinary(bytes)    
  }
}
