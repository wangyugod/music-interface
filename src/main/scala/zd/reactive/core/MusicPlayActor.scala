package zd.reactive.core

import akka.actor.{ Actor, ActorLogging }
import zd.reactive.domain.{ SongPlayResponse, PlayListRequest }
import java.net.URLEncoder
import zd.reactive.domain.SongPlayResponse
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.commons.io.IOUtils

/**
 * Created by Simon Wang on 2014/12/16.
 */
class MusicPlayActor extends Actor with ActorLogging {

  val LRC_SERVER = "http://218.200.230.40:18089/"
  val MUSIC_SERVER = "http://tyst.migu.cn/"

  def receive = {
    case request: PlayListRequest =>
      context.parent ! songInfoById(request.songIds)
    case _ =>
  }

  def songInfoById(ids: Array[String]) = {
    import zd.reactive.db.DBHelper._
    val id = "(" + ids.map(str => "'" + str + "'").mkString(",") + ")"
    if (log.isDebugEnabled)
      log.debug(s"result id is $id, original data is ${ids.mkString(",")}");
    val sql = s"select substr(metadata.resourceid, length(metadata.resourceid) - 5, length(metadata.resourceid)) as resourceid,metadata.cmpurl as cmpUrl,metadata.ssize as filesize,metadata.filetype,song.copyrightid as copyrightid, song.songid as songId,song.songname as songName,t.singername as singerName,t.singerid as singerId,t.contentid as contentid, lyricurl,song.length from SYNCWIRELESSPRODUCT_METADATA metadata, syncwirelessproduct t,SYNCMATERIAL_SONG  song where t.songid in $id and metadata.zqsid = t.zqsid  and song.songid = t.songid order by ssize desc"
    withStatement(sql) {
      statement =>
        val rs = statement.executeQuery()
        var map = Map[String, SongPlayResponse]()
        while (rs.next()) {
          val lrcUrl: String =
            rs.getString("LYRICURL") match {
              case null =>
                ""
              case str if str.startsWith("http") =>
                str
              case s: String =>
                LRC_SERVER + s
            }
          if (log.isDebugEnabled)
            log.debug(s"songid ${rs.getString("SONGID")} lrcUrl is $lrcUrl")

          val lyric = if (lrcUrl != "") {
            downloadLyric(lrcUrl)
          } else ""

          val musicPlayUrl = new java.net.URL(rs.getString("CMPURL"))
          val urlStr = MUSIC_SERVER + URLEncoder.encode(musicPlayUrl.getPath(), "UTF-8")
          val song = SongPlayResponse(rs.getString("SONGID"), rs.getString("SONGNAME"), rs.getString("SINGERID"), rs.getString("SINGERNAME"), urlStr, rs.getString("RESOURCEID"), lyric, if (rs.getString("COPYRIGHTID") == null) "" else rs.getString("COPYRIGHTID"), None)
          song.resourceId match {
            case "000019" =>
              val tempSong = map.get(song.songId)
              val finalSong = if (tempSong.isDefined && tempSong.get.cmp3.isDefined) SongPlayResponse(song.songId, song.songName, song.singerId, song.singerName, song.mp3, song.resourceId, lyric, song.copyId, tempSong.get.cmp3) else song
              map = map + ((song.songId, finalSong))
            case str if (str == "020007" || str == "000009") =>
              val tempSong = map.get(song.songId)
              if (tempSong.isDefined && tempSong.get.resourceId != "000019") {
                val finalSong = if (tempSong.get.cmp3.isDefined) SongPlayResponse(song.songId, song.songName, song.singerId, song.singerName, song.mp3, song.resourceId, lyric, song.copyId, tempSong.get.cmp3) else song
                map = map + ((song.songId, finalSong))
              }
            case "000018" =>
              val tempSong = map.get(song.songId)
              if (tempSong.isDefined) {
                map = map + ((song.songId, SongPlayResponse(song.songId, song.songName, song.singerId, song.singerName, tempSong.get.mp3, tempSong.get.resourceId, lyric, rs.getString("COPYRIGHTID"), Some(urlStr))))
              } else {
                map = map + ((song.songId, SongPlayResponse(song.songId, song.songName, song.singerId, song.singerName, urlStr, song.resourceId, lyric, song.copyId, Some(urlStr))))
              }
            case _ =>
              val tempSong = map.get(song.songId)
              if (tempSong.isEmpty)
                map = map + ((song.songId, song))
          }
        }
        rs.close()
        if (log.isDebugEnabled)
          log.debug("result map is : " + map)
        map.values.toList
    }
  }

  def downloadLyric(url: String) = {
    val client = HttpClients.createDefault()
    val get = new HttpGet(url);
    val response = client.execute(get);
    val is = response.getEntity().getContent()
    val result = new String(IOUtils.toByteArray(is), "GBK")
    is.close()
    result.replace("\r\n", "||").replace("\n", "||").replace("\r", "||").replace("\"", " ").replace("\'", " ").replace("\\", "").replace("/", "")
  }
}

/*object Main {
  def main(args: Array[String]) = {
    val s = "How about \\\\\"Livin' with my ladies hash tag LIVE\\\\\""
    println(s)
    val n = s.replace("\r\n", "||").replace("\n", "||").replace("\r", "||").replace("\"", " ").replace("\'", " ").replace("\\", "").replace("/", "")
    println(n)

  }
}*/

