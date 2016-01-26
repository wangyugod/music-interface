package zd.reactive.db

import scalikejdbc.ConnectionPoolSettings
import zd.reactive.Configuration._
import scalikejdbc.ConnectionPool
import scalikejdbc.Log
import org.slf4j.LoggerFactory

object ScalalikeConnectionPool {
  val logger = LoggerFactory.getLogger(ScalalikeConnectionPool.getClass)
  
  Class.forName(driver)
  val settings = ConnectionPoolSettings(
    initialSize = 5,
    maxSize = 20,
    connectionTimeoutMillis = 5000L,
    validationQuery = "select 1 from dual")
    
  ConnectionPool.singleton(dbUrl, userName, password, settings)
  def init = {
    logger.info("initialize scala connection pool")
  }
  
  def close = {
    ConnectionPool.closeAll()
  }
  
}