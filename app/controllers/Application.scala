package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.libs.json._

import play.api.libs.{ Comet }
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import akka.util.duration._

object Application extends Controller {

	def comet(query1: String, query2: String) = Action {

	  lazy val results1 = getStream(query1)

	  lazy val results2 = getStream(query2)

	  //pipe result 1 and result 2 and push to comet socket	
	  Ok.stream( results1 >- results2 &> upperCase &> Comet(callback = "parent.messageChanged"))
  
	}

	private def getStream(query: String) = {
		Enumerator.fromCallback[String](() => 
			Promise.timeout(WS.url("http://search.twitter.com/search.json?q="+query+"&rpp=1").get(), 1000 milliseconds).flatMap(_.map { response =>
				(response.json \\ "text").headOption.map(query + " : " + _.as[String])
			})
		)
	}
	
	val upperCase = Enumeratee.map[String] {
	    tweet => tweet.map(_.toUpperCase)
	}
	

	def liveTweets(query1: String, query2: String) = Action {
	  Ok(views.html.index(query1, query2))
	}

	def index() = Action {
	  //default search	
	  Redirect(routes.Application.liveTweets("java", "ruby"))
	}
  
}