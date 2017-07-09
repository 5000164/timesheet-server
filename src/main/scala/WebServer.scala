import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import spray.json.DefaultJsonProtocol._
import scala.io.StdIn

object WebServer {

  final case class Item(name: String, id: Long)

  def main(args: Array[String]) {

    implicit val itemFormat = jsonFormat2(Item)

    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val route =
      get {
        pathSingleSlash {
          complete(Item("test", 999))
        } ~
          path("ping") {
            complete("PONG!")
          } ~
          path("crash") {
            sys.error("BOOM!")
          }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
