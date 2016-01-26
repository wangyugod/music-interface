package zd.reactive.core

import java.sql.{ Connection, ResultSet }
import akka.actor.{ Actor, ActorLogging }
import zd.reactive.db.DBHelper
import zd.reactive.domain._
import DBHelper._
import java.text.SimpleDateFormat

/**
 * Created by Simon Wang on 2014/12/3.
 */
class DataSyncActor extends Actor with ActorLogging {

  def receive = {
    case artistRequest: SyncArtistRequest =>
      log.debug(s"receive sync artist request")
      context.parent ! syncArtist(artistRequest)
      log.debug(s"send response to parent actor")
    case songRequest: SyncSongRequest =>
      log.debug(s"receive sync song request")
      context.parent ! syncSong(songRequest)
      log.debug(s"send song response to parent actor")
    case albumRequest: SyncAlbumRequest =>
      log.debug(s"receive sync song request")
      context.parent ! syncAlbum(albumRequest)
      log.debug(s"send song response to parent actor")
    case _ =>
      log.error("invalid message")

  }

  def syncArtist(request: SyncArtistRequest) = {
    val sql = "select * from( select a.*, rownum ru from (select * from syncmaterial_artist a where timetab >= ? order by timetab asc) a where rownum <= ?) where ru > ? "
    withStatement(sql) {
      prepareStatement =>
        prepareStatement.setInt(3, (request.pageNumber - 1) * request.recordPerPage)
        prepareStatement.setInt(2, request.pageNumber * request.recordPerPage)
        prepareStatement.setDate(1, parseDate(request.fromTime))
        log.debug(s"before execute query")
        val r = prepareStatement.executeQuery()
        log.debug(s"execute query done ")
        var list = List[Artist]()
        while (r.next()) {
          list = Artist(r.getString("ARTISTID"), r.getString("ARTISTNAME"), r.getString("GENDER"), r.getString("ENGLISHNAME"), r.getString("ARTISTNAMEPINYIN"), r.getString("NICKNAME"), r.getString("ARTISTPICS"), r.getString("ARTISTPICM"), r.getString("COMPANY"), r.getString("COUNTRY"), r.getString("BIRTHDATE"), r.getString("BIRTHPLACE"), r.getString("SCHOOL"), r.getString("REPRESENTWORKS"), r.getString("HEIGHT"), r.getString("WEIGHT"), r.getString("HOBBY"), r.getString("AWARDS"), r.getString("INTRO"), r.getString("SINGERAREA"), r.getString("SINGERSTYLE"), r.getString("TIMETAB")) :: list
        }
        r.close()
        if (log.isDebugEnabled)
          log.debug("result list for artist is : " + list)
        list
    }
  }

  def syncSong(request: SyncSongRequest) = withConnection {
    conn =>
      val songSql = "select * from( select a.* , rownum ru from (select  * from syncmaterial_song a where timetab >= ? and isplay='true' order by timetab asc) a where rownum <= ?) where ru > ?  "
      val albumSql = "select * from album_song where songid = ?"
      val songStatement = conn.prepareStatement(songSql)
      if (log.isDebugEnabled)
        log.debug(s"prepare to synchronize song")
      try {
        songStatement.setInt(3, (request.pageNumber - 1) * request.recordPerPage)
        songStatement.setInt(2, request.pageNumber * request.recordPerPage)
        songStatement.setDate(1, parseDate(request.fromTime))

        if (log.isDebugEnabled)
          log.debug("before query song")
        val r = songStatement.executeQuery()
        if (log.isDebugEnabled)
          log.debug("after query song")
        var songList = List[Song]()
        while (r.next()) {
          val songId = r.getString("SONGID")
          songList = Song(songId, r.getString("SONGNAME"), r.getString("LANGUAGE"), r.getString("LYRICURL"), r.getString("SONGNAMEPINYIN"), r.getString("LENGTH"), r.getString("PUBLISHYEAR"), r.getString("INTRO"), albumsBySongId(songId, conn), artistBySongId(songId, conn), r.getString("ISPLAY"), r.getString("TIMETAB")) :: songList
        }
        r.close()
        if (log.isDebugEnabled)
          log.debug("result list for song is : " + songList)
        songList
      } finally {
        songStatement.close
      }
  }

  def parseDate(date: String) = {
    val df = new SimpleDateFormat("yyyyMMddHHmmss")
    new java.sql.Date(df.parse(date).getTime())
  }

  def artistBySongId(songId: String, conn: Connection) = {
    val artistSql = "select * from artist_song where songid = ?"
    val artistStatement = conn.prepareStatement(artistSql)
    var result = List[String]()
    try {
      artistStatement.setString(1, songId)
      val r = artistStatement.executeQuery()
      while (r.next()) {
        result = r.getString("SINGERID") :: result
      }
      r.close()
    } finally {
      artistStatement.close()
    }
    if (log.isDebugEnabled)
      log.debug(s"query artist by songId $songId result $result")
    result
  }

  def albumsBySongId(songId: String, conn: Connection) = {
    val albumSql = "select * from album_song where songid = ?"
    val albumStatement = conn.prepareStatement(albumSql)
    var result = List[String]()
    try {
      albumStatement.setString(1, songId)
      val r = albumStatement.executeQuery()
      while (r.next()) {
        result = r.getString("ALBUMID") :: result
      }
      r.close()
    } finally {
      albumStatement.close()
    }

    if (log.isDebugEnabled)
      log.debug(s"query album by songId $songId result $result")
    result
  }

  def syncAlbum(request: SyncAlbumRequest) = withConnection {
    conn =>
      val sql = "select * from( select a.*, rownum ru from (select * from syncmaterial_album a  where timetab >= ? order by timetab asc) a where rownum <= ?) where ru > ? "
      val albumStatement = conn.prepareStatement(sql)
      albumStatement.setInt(3, (request.pageNumber - 1) * request.recordPerPage)
      albumStatement.setInt(2, request.pageNumber * request.recordPerPage)
      albumStatement.setDate(1, parseDate(request.fromTime))
      log.debug(s"before execute query")
      var list = List[Album]()
      try {
        val r = albumStatement.executeQuery()
        log.debug(s"execute query done ")
        while (r.next()) {
          val albumId: String = r.getString("ALBUMID")
          list = Album(albumId, r.getString("ALBUMNAME"), r.getString("TRACKCOUNT"), r.getString("PRODUCTIONCOMPANY"), r.getString("PUBLISHCOMPANY"), r.getString("PUBLISHDATE"), r.getString("PUBLISHAREA"), r.getString("LANGUAGE"), r.getString("ALBUMPICS"), r.getString("ALBUMPICM"), r.getString("SALESVOLUME"), r.getString("AWARDS"), r.getString("ALBUMNAMEPINYIN"), r.getString("ALBUMINTRO"), artistByAlbumId(albumId, conn), r.getString("TIMETAB"), r.getString("STATUS")) :: list
        }
        r.close()
      } finally {
        albumStatement.close()
      }
      if (log.isDebugEnabled)
        log.debug("result list for artist is : " + list)
      list
  }

  def artistByAlbumId(albumId: String, conn: Connection) = {
    val artistSql = "select * from artist_album where albumid = ?"
    val artistStatement = conn.prepareStatement(artistSql)
    var result = List[String]()
    try {
      artistStatement.setString(1, albumId)
      val r = artistStatement.executeQuery()
      while (r.next()) {
        result = r.getString("ARTISTID") :: result
      }
      r.close()
    } finally {
      artistStatement.close()
    }
    if (log.isDebugEnabled)
      log.debug(s"query artist by albumId $albumId result $result")
    result
  }
}

