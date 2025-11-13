package poc.redbuttonwebsocketpoc

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic
import fs2.Stream
import org.http4s.websocket.WebSocketFrame
import scala.concurrent.duration.*
import scala.util.Random
import io.circe.{Encoder, Decoder, Json}
import io.circe.syntax.*

object Main extends IOApp.Simple:

  case class Reactions(
                        reaction1: Int,
                        reaction2: Int,
                        reaction3: Int,
                        reaction4: Int,
                        reaction5: Int
                      )

  given Encoder[Reactions] = Encoder.instance { r =>
    Json.obj(
      "reaction-1" -> Json.fromInt(r.reaction1),
      "reaction-2" -> Json.fromInt(r.reaction2),
      "reaction-3" -> Json.fromInt(r.reaction3),
      "reaction-4" -> Json.fromInt(r.reaction4),
      "reaction-5" -> Json.fromInt(r.reaction5)
    )
  }
  given Decoder[Reactions] = Decoder.instance { c =>
    for {
      reaction1 <- c.downField("reaction-1").as[Int]
      reaction2 <- c.downField("reaction-2").as[Int]
      reaction3 <- c.downField("reaction-3").as[Int]
      reaction4 <- c.downField("reaction-4").as[Int]
      reaction5 <- c.downField("reaction-5").as[Int]
    } yield Reactions(reaction1, reaction2, reaction3, reaction4, reaction5)
  }

  def program: IO[Unit] = {
    for {
      q <- Queue.unbounded[IO, WebSocketFrame]
      t <- Topic[IO, WebSocketFrame]
      s <- Stream(
        Stream.fromQueueUnterminated(q).through(t.publish),
        Stream.eval(RedbuttonwebsocketpocServer.run[IO](q, t)),
        Stream
          .awakeEvery[IO](30.seconds)
          .map(_ => WebSocketFrame.Ping())
          .through(t.publish),
        Stream
          .repeatEval(
            for {
              delay <- IO(Random.between(5000, 10001))
              reactionsCount <- IO(
                (
                  Random.between(1, 10),
                  Random.between(1, 10),
                  Random.between(1, 10),
                  Random.between(1, 10),
                  Random.between(1, 10)
                )
              )
              (r1, r2, r3, r4, r5) = reactionsCount
              _ <- IO.sleep(delay.millis)
            } yield WebSocketFrame.Text(
              Reactions(r1, r2, r3, r4, r5).asJson.noSpaces
            )
          )
          .through(t.publish)
      ).parJoinUnbounded.compile.drain
    } yield s
  }
  override def run: IO[Unit] = program
