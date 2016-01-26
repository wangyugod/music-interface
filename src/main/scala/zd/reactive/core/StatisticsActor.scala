package zd.reactive.core

import akka.actor.ActorLogging
import akka.actor.Actor
import zd.reactive.domain.ToneDownloadInfoRequest
import zd.reactive.domain.SongPlayStatisticInfoRequest
import zd.reactive.db.DBHelper._
import zd.reactive.domain.SongPlayStatisticInfoRequest
import zd.reactive.domain.SUCCESS

/**
 * @author Administrator
 */
class StatisticsActor extends Actor with ActorLogging {
  def receive: Actor.Receive = {
    case request: ToneDownloadInfoRequest =>
      log.debug(s"receive ring donwload request")
      context.parent ! insertToneDownload(request)
    case request: SongPlayStatisticInfoRequest =>
      log.debug(s"receive song play request")
      context.parent ! insertSongPlayStatistic(request)
    case _ => log.error("invalid message")
  }
  
  def insertToneDownload(request: ToneDownloadInfoRequest) = {
    val sql = s"insert into DOWNLOADRINGTONES(ID, PHONENUM, BELLRINGID, SONGNAME, SINGERNAME, DOWNLOADTIME) values (DOWNLOADRINGTONES_ID.nextval,?,?,?,?,?)"
    withStatement(sql){
      prepareStatement =>
        prepareStatement.setString(1, request.phoneNumber)
        prepareStatement.setString(2, request.toneId)
        prepareStatement.setString(3, request.songName)
        prepareStatement.setString(4, request.singerName)
        prepareStatement.setDate(5, new java.sql.Date(new java.util.Date().getTime))
        val cnt = prepareStatement.executeUpdate();
        prepareStatement.getConnection().commit()
        if (log.isInfoEnabled)
          log.info(s"$cnt ring download records inserted")
        SUCCESS(cnt, s"$cnt ring download records inserted successfully")
    }
  }
  
  def insertSongPlayStatistic(request: SongPlayStatisticInfoRequest) = {
    val sql = s"insert into LISTENPLAYSONG(ID, SONGNAME, SINGERNAME, SONGID, COPYRIGHTID, PLAYTIME) values (LISTENPLAYSONG_ID.nextval,?,?,?,?,?)"
    withStatement(sql){
      prepareStatement =>
        prepareStatement.setString(1, request.songName)
        prepareStatement.setString(2, request.artistName)
        prepareStatement.setString(3, request.songId)
        prepareStatement.setString(4, request.copyrightId)
        prepareStatement.setDate(5, new java.sql.Date(new java.util.Date().getTime))
        val cnt = prepareStatement.executeUpdate();
        prepareStatement.getConnection().commit()
        if (log.isInfoEnabled)
          log.info(s"$cnt song play records inserted")
        SUCCESS(cnt, s"$cnt song play records inserted successfully")
    }
  }
  
  
}