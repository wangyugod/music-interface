package infothunder.reactive.db

import java.sql.PreparedStatement
import com.mchange.v2.c3p0.ComboPooledDataSource
import java.sql.Connection
import infothunder.reactive.Configuration._

object AccessRushDB {
private val dataSource = new ComboPooledDataSource()
  dataSource.setJdbcUrl(ardbUrl)
  dataSource.setUser(arUserName)
  dataSource.setPassword(arPassword)
  dataSource.setDriverClass(driver)
  dataSource.setInitialPoolSize(2)
  dataSource.setMinPoolSize(1)
  dataSource.setMaxPoolSize(10)
  dataSource.setMaxStatements(50)
  dataSource.setMaxIdleTime(60);
  dataSource.setCheckoutTimeout(60000)
  dataSource.setMaxIdleTimeExcessConnections(40)
  dataSource.setIdleConnectionTestPeriod(300)
  println(s"user: $arUserName password: $arPassword")

  //initialize connection pool 
  def init = {
    dataSource.getConnection
  }

  def withStatement[T](sql: String)(f: PreparedStatement => T): T = {
    println(s"prepare to get connection")
    val conn = dataSource.getConnection
    println(s"got connection $conn")
    val statement = conn.prepareStatement(sql)
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
    println(s"got connection $conn")
    try{      
      f(conn)
    } finally {
      conn.close()
    }
  }

  def close = dataSource.close()
}