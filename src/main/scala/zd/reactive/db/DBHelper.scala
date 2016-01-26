package zd.reactive.db

import java.sql.Connection
import java.sql.PreparedStatement

import com.mchange.v2.c3p0.ComboPooledDataSource

import akka.actor.ActorLogging
import zd.reactive.Configuration._

/**
 * Created by Simon Wang on 2014/12/5.
 */
object DBHelper {
  private val dataSource = new ComboPooledDataSource()
  dataSource.setJdbcUrl(dbUrl)
  dataSource.setUser(userName)
  dataSource.setPassword(password)
  dataSource.setDriverClass(driver)
  dataSource.setInitialPoolSize(2)
  dataSource.setMinPoolSize(1)
  dataSource.setMaxPoolSize(10)
  dataSource.setMaxStatements(50)
  dataSource.setMaxIdleTime(60);
  dataSource.setCheckoutTimeout(60000)
  dataSource.setMaxIdleTimeExcessConnections(40)
  dataSource.setIdleConnectionTestPeriod(300)
  
  

  //initialize connection pool 
  def init = {
    dataSource.getConnection
  }

  def withStatement[T](sql: String)(f: PreparedStatement => T): T = {
    println(s"prepare to get connection")
    val conn = dataSource.getConnection
    println(s"got connection")
    val statement = conn.prepareStatement(sql)
    try {
      f(statement)
    } finally {
      statement.close()
      conn.close()
    }
  }
  
  def withNamedStatement[T](sql: String)(f: NamedParameterStatement => T): T = {
    println(s"prepare to get connection")
    val conn = dataSource.getConnection
    println(s"got connection")
    val statement = new NamedParameterStatement(conn, sql)
//    val statement = conn.prepareStatement(sql)
    try {
      f(statement)
    } finally {
      statement.close()
      conn.close()
    }
  }

  def withConnection[T](f: Connection => T): T = {
    println(s"prepare to get connection")
    val conn = dataSource.getConnection
    println(s"got connection")
    try{      
    	f(conn)
    } finally {
      conn.close()
    }
  }

  def close = dataSource.close()
}
