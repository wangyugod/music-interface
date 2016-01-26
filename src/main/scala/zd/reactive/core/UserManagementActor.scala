package zd.reactive.core

import akka.actor.Actor
import akka.actor.ActorLogging
import zd.reactive.domain._
import zd.reactive.Configuration._
import scalikejdbc._
import com.tsxy.libs.security.md5.MD5
import com.tsxy.libs.util.PasswordUtil

class UserManagementActor extends Actor with ActorLogging {
  def receive: Actor.Receive = {
    case request: LoginRequest =>
      context.parent ! login(request)
    case _ =>

  }

  def login(request: LoginRequest) = {
    DB readOnly { implicit session =>
      case class QueryResult(password: String, status: String)
      val resultOpt: Option[QueryResult] = sql"""select ca.ext_num,ca.hjzxyh,u.fs_passwd,u.fi_status,u.fi_level , u.fs_earlydays ,t.fs_name as level_name,u.fs_name,u.fs_agent,u.fs_operno,a.Fi_Attr ,en.fs_objno,a.fs_upagentid from tusermanage u 
          join tnormalparm t on u.fi_level=t.fi_no and fi_type=2 
          join tagent a on a.fs_agent=u.fs_agent and u.fs_operno = ${request.login}  
          left join (select to_char(wm_concat( n.fs_objno)) as fs_objno,n.fs_operno from tusermanage e join tuseraddition n  on n.fs_operno = e.fs_operno and n.fi_attr = 1  group by n.fs_operno ) en on en.fs_operno=u.fs_operno 
          left join call_teloper ca on u.fs_operno=ca.user_id""".map { rs => QueryResult(rs.string("fs_passwd"), rs.string("fi_status"))}.single.apply()
      resultOpt match {
        case Some(result) if !pwdServerEnabled =>
          val localPwdMatch = result.password.equals(MD5.encryptWithSalt(request.password))
          if(log.isDebugEnabled)
            log.debug(s"local password matched $localPwdMatch")
          processLogin(localPwdMatch, request.login, result.status)
        case Some(result) if pwdServerEnabled =>
          val serverPwdMatch = result.password.equals(PasswordUtil.encodePassWd(request.login, request.password))
          if(log.isDebugEnabled)
            log.debug(s"server password matched $serverPwdMatch")
          processLogin(serverPwdMatch, request.login, result.status)
        case _ =>
          LoginResponse(request.login, false, "用户名和密码不匹配!")
      }
    }
  }

  private def processLogin(matched: Boolean, login: String, status: String) = {
    if (matched) {
      if (!"0".equals(status)) {
        LoginResponse(login, false, "该用户已被禁止登录！")
      } else {
        LoginResponse(login, true, "登陆成功")
      }
    } else {
      LoginResponse(login, false, "用户名和密码不匹配!")
    }
  }
}