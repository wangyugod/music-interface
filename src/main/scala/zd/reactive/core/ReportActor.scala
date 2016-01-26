package zd.reactive.core

import akka.actor.ActorLogging
import zd.reactive.domain.ReportRequest
import akka.actor.Actor
import zd.reactive.db.DBHelper
import zd.reactive.domain.ReportResponse
import zd.reactive.domain.DropdownDirectiveRequest
import sun.org.mozilla.javascript.internal.ast.WithStatement
import DBHelper._
import zd.reactive.domain.DropdownDirectiveResponse
import zd.reactive.domain.DropdownDirectiveResponse
import zd.reactive.domain.InterBusinessReportRequest
import zd.reactive.domain.InterBusinessReportRequest
import zd.reactive.domain.InterBusinessReportRequest

class ReportActor extends Actor with ActorLogging {
  @Override
  def receive: Actor.Receive = {
    case request: ReportRequest =>
      log.debug(s"receive ring donwload request")
      context.parent ! reportDemo(request)
    case request: DropdownDirectiveRequest =>
      context.parent ! directiveQuery(request)
    case request: InterBusinessReportRequest =>
      context.parent ! interBusinessReport(request)
    case _ => log.error("invalid message")
  }

  def reportDemo(request: ReportRequest) = {
    val sql = """select c.fs_agent,c.fs_name,sum(c.sqmyejf) sqmyejf,sum(c.bqfsejf) bqfsejf,sum(c.bqfsedf) bqfsedf,sum(c.bqmyejf) bqmyejf   from (
 select y.jzrq,b.fs_agent,b.fs_name,case when to_date(y.jzrq,'yyyyMMdd') between last_day(to_date(:startDate,'yyyyMMdd')) and last_day(to_date(:endDate,'yyyyMMdd')) then  nvl(sum(y.sqmyejf),0) else 0 end  sqmyejf, nvl(sum(y.bqfsejf),0) bqfsejf,
 nvl(sum(y.bqfsedf),0) bqfsedf,case when to_date(y.jzrq,'yyyyMMdd') between last_day(to_date(:startDate,'yyyyMMdd')) and last_day(to_date(:endDate,'yyyyMMdd')) then  nvl(sum(y.bqmyejf),0) else 0 end  bqmyejf 
 from (
    select t.fs_agent,t.fs_name,a.fs_agent as p_fs_agent ,a.fs_name as p_fs_name from tagent t 
          join tagent a
            on a.fs_upagentid = t.fs_agent
           and a.fi_attr = 2
         where t.fi_attr = 1 
 ) b 
  join rjz_ywzkb_4_01@dw y on y.jgdm=b.p_fs_agent and y.kmdh='11' 
  and to_date(y.jzrq,'yyyyMMdd')>=last_day(to_date(:startDate,'yyyyMMdd')) 
  and to_date(y.jzrq,'yyyyMMdd')<=last_day(to_date(:endDate,'yyyyMMdd'))
 group by b.fs_agent,b.fs_name,y.jzrq
 order by y.jzrq desc
 ) c
 join tagent t on t.fs_agent=c.fs_agent
  where 1=1 
 group by c.fs_agent,c.fs_name 
"""
    withNamedStatement(sql) {
      namedStatement =>
        log.debug(s"before execute query")
        namedStatement.setString("startDate", request.startDate)
        namedStatement.setString("endDate", request.endDate)
        val r = namedStatement.executeQuery()

        log.debug(s"execute query done ")
        var list = List[ReportResponse]()
        while (r.next()) {
          list = ReportResponse(r.getString("fs_agent"), r.getString("fs_name"), r.getString("sqmyejf"), r.getString("bqfsejf"), r.getString("bqfsedf"), r.getString("bqmyejf")) :: list
        }
        r.close()
        if (log.isDebugEnabled)
          log.debug("result list for report result is : " + list)
        list
    }
  }

  def directiveQuery(request: DropdownDirectiveRequest) = {
    val columns = request.columnNames
    val columnSql = columns.mkString(",")
    val sql = "select " + columnSql + " from " + request.tableName + " where " + request.condition
    if (log.isDebugEnabled)
      log.debug(s"directive sql is $sql")
    withStatement(sql) {
      statement =>
        log.debug(s"sql is ${sql}")
        for ((value, idx) <- request.values.zipWithIndex) {
          log.debug(s"idx ${idx} value ${value}")
          statement.setString(idx + 1, value)
        }
        val r = statement.executeQuery()
        var list = List[Map[String, String]]()
        while (r.next()) {
          var map = Map[String, String]()
          for (cn <- columns)
            map = map + (cn -> r.getString(cn))
          list = map :: list
        }
        if (log.isDebugEnabled)
          log.debug("result list for directive result is : " + list)
        list
    }
  }

  def interBusinessReport(request: InterBusinessReportRequest):Map[String,String] = {
    val startDate = request.startDate
    val endDate = request.endDate
    val department = request.dept
    var map = Map[String, String]()
    
    //保险查询
    val insuranceIncomeSql = """select jgdm as agent, sum(dfse) as amount from rjz_autotrans@dw where jgdm= :dept and kmh like '60210301%' and  jzrq >=:startDate and jzrq< :endDate and czy<>'zzzz' group by jgdm"""
    withNamedStatement(insuranceIncomeSql) {
      namedStatement =>
        log.debug(s"before execute query")
        namedStatement.setString("startDate", request.startDate)
        namedStatement.setString("endDate", request.endDate)
        namedStatement.setString("dept", request.dept)
        val r = namedStatement.executeQuery()
        while (r.next()) {
          map += ("insuranceIncome" -> r.getBigDecimal("AMOUNT").toPlainString())
          log.debug(s"got result:${map}")
        }
        log.debug(s"step1 done")
        r.close()
    }
    
    log.debug(s"step1: ${map}")
    
    //财险查询
    val estInsuranceSql = """select jgdm,count(*) as cnt,round(sum(dfse)/10000,2) as amount from rjz_autotrans@dw where jgdm = :dept and  jzrq >= :startDate and jzrq< :endDate and kmh like '22410407%'  and zt='正常' and dfse >0 and hm like '%保险%' group by jgdm"""
    withNamedStatement(estInsuranceSql) {
      namedStatement =>
        namedStatement.setString("startDate", request.startDate)
        namedStatement.setString("endDate", request.endDate)
        namedStatement.setString("dept", request.dept)
        val r = namedStatement.executeQuery()

        while (r.next()) {
          map += ("estSumAmount" -> r.getBigDecimal("AMOUNT").toPlainString())
          map += ("estCount" -> r.getString("CNT"))
          log.debug(s"got result:${map}")
        }
        log.debug(s"step2 done")
        r.close()
    }
    
    log.debug(s"step2: ${map}")
    
    //寿险查询
    val lifeInsuranceSql = """select jgdm, count(*) as cnt, round(sum(dfse)/10000,2) as amount from rjz_autotrans@dw where jgdm= :dept and  jzrq >= :startDate and jzrq< :endDate and  kmh like '3013%' and ( zynr like '中%'or zynr like '新%'or zynr like '联%' or zynr like '泰%'or zynr like '太%' or zynr like '平%'or zynr like '人%'）and dfse>0 and zt='正常' group by jgdm"""
    withNamedStatement(lifeInsuranceSql) {
      namedStatement =>
        namedStatement.setString("startDate", request.startDate)
        namedStatement.setString("endDate", request.endDate)
        namedStatement.setString("dept", request.dept)
        val r = namedStatement.executeQuery()

        while (r.next()) {
          map += ("lifeInsSumAmount" -> r.getBigDecimal("AMOUNT").toPlainString())
          map += ("lifeInsCount" -> r.getString("CNT"))
          log.debug(s"got result:${map}")
        }
        r.close()
    }
    
    log.debug(s"step3: ${map}")
    
    //代收代付
    val ioSql = """select jgdm,count(*) as cnt,sum(jfse),sum(dfse) as amount from rjz_autotrans@dw where jgdm= :dept and  jzrq >= :startDate and jzrq< :endDate  and  kmh like '22410405%' and (zynr like  '%代%' or zynr like  '%批%'  ) and hm not like '代理非税业务分成%' and (jfse<>0 or dfse<>0  ) and zt='正常' group by jgdm"""
    withNamedStatement(ioSql) {
      namedStatement =>
        namedStatement.setString("startDate", request.startDate)
        namedStatement.setString("endDate", request.endDate)
        namedStatement.setString("dept", request.dept)
        val r = namedStatement.executeQuery()

        while (r.next()) {
          map += ("ioSumAmount" -> r.getBigDecimal("AMOUNT").toPlainString())
          map += ("ioCount" -> r.getString("CNT"))
          log.debug(s"got result:${map}")
        }
        r.close()
    }
    if(log.isDebugEnabled)
      log.debug(s"result: ${map}")
    map
  }
}