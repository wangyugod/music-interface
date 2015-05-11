package infothunder.reactive.domain

// Messages

trait RestMessage

case class SyncArtistRequest(fromTime: String, pageNumber: Int, recordPerPage: Int) extends RestMessage

case class SyncSongRequest(fromTime: String, pageNumber: Int, recordPerPage: Int) extends RestMessage

case class SyncAlbumRequest(fromTime: String, pageNumber: Int, recordPerPage: Int) extends RestMessage

case class Artist(artistid: String, artistname: String, gender: String, englishname: String, artistnamepinyin: String, nickname: String, artistpics: String, artistpicm: String, company: String, country: String, birthdate: String, birthplace: String, school: String, representworks: String, height: String, weight: String, hobby: String, awards: String, intro: String, singerarea: String, singerstyle: String, lastUpdatedTime: String) extends RestMessage

case class Song(songid: String, songname: String, language: String, lyricurl: String, songnamepinyin: String, length: String, publishyear: String, intro: String, albums: List[String], artists: List[String], playable: String, lastUpdatedTime: String) extends RestMessage

case class Album(albumid: String, albumname: String, trackcount: String, productioncompany: String, publishcompany: String, publishdate: String, publisharea: String, language: String, albumpics: String, albumpicm: String, salesvolume: String, awards: String, albumnamepinyin: String, albumintro: String, artists: List[String], lastUpdatedTime: String) extends RestMessage

case class PlayListRequest(songIds: Array[String]) extends RestMessage

case class SongPlayResponse(songId: String, songName: String, singerId: String, singerName: String, mp3: String, resourceId: String, lrc: String, copyId: String, cmp3: Option[String]) extends RestMessage

case class Error(message: String)

case class Validation(message: String)

case class AccessRushRequest(prefix: String, numberPerPage: Int, pageNumber: Int) extends RestMessage

case class DeleteRushRequest(prefix: String) extends RestMessage

case class BatchInsertRushRequest(numbers: String) extends RestMessage

case class AccessRush(openId: String, phoneNumber: String) extends RestMessage

case class WechatTokenRequest(accountId: String) extends RestMessage

case class WechatTokenSuccessResponse(access_token: String, jsapi_token: String, appid: String) extends RestMessage

case class WechatToeknFailResponse(errcode: Int, errmsg: String) extends RestMessage

case class SUCCESS(code: Int, message: String) extends RestMessage


// Exceptions

case object PetOverflowException extends Exception("PetOverflowException: OMG. Pets. Everywhere.")