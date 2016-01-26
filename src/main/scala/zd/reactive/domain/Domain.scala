package zd.reactive.domain

import spray.json.DefaultJsonProtocol
import spray.json.ParserInput._
import spray.http._
import spray.json.JsonParser._
import spray.json._
import spray.httpx.SprayJsonSupport
import spray.httpx.SprayJsonSupport._
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.http._
import HttpCharsets._
import MediaTypes._

// Messages

trait RestMessage


case class Error(message: String) extends RestMessage

case class Validation(message: String)

case class SUCCESS(code: Int, message: String) extends RestMessage

case class DropdownDirectiveRequest(columnNames: Array[String], tableName: String, condition: String, values: Array[String]) extends RestMessage

case class DropdownDirectiveResponse(result: List[(String, String)])extends RestMessage

case class InterBusinessReportRequest(startDate: String, endDate: String, dept: String)extends RestMessage

case class InterBusinessReportResponse(overallIncome: String, ioBusiIncome: String, ioSumAmount: String, ioCount: Int, insuranceIncome: String, estInsuranceIncome: String, estSumAmount: String, estCount: Int, lifeInsuranceIncome: String, lifeInsCount: Int, lifeInsSumAmount: String, stockIncome: String, stockSumAmount: String, stockCount: Int, finBusiIncome: String, personalFinIncome: String, personalFinAmt: String, personalFinCnt: Int, entFinAmt: String, entFinCount: Int, otherBusIncome: String, otherSumAmt: String)extends RestMessage

case class ReportRequest(startDate: String, endDate: String) extends RestMessage

case class ReportResponse(agent:String, name: String, sqmyejf: String, bqfsejf: String, bqfsedf: String, bqmyejf: String) extends RestMessage

case class LoginRequest(login: String, password: String) extends RestMessage

case class LoginResponse(login: String, success: Boolean, message: String) extends RestMessage



object DomainJsonSupport extends DefaultJsonProtocol{
  implicit val reportJsonFormat = jsonFormat2(ReportRequest)
  implicit val dropdownJsonFormat = jsonFormat4(DropdownDirectiveRequest)
  implicit val interBusinessReportJsonFormat = jsonFormat3(InterBusinessReportRequest)
  implicit val loginRequestJsonFormat = jsonFormat2(LoginRequest)
}
