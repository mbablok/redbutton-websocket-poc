package poc.redbuttonwebsocketpoc

import poc.redbuttonwebsocketpoc.Model.*

import cats.Applicative
import cats.effect.std.Queue
import cats.effect.{IO, IOApp, Ref}
import fs2.Stream
import fs2.concurrent.Topic
import io.circe.syntax.*
import org.http4s.websocket.WebSocketFrame
import scala.concurrent.duration.*
import scala.util.Random

object Main extends IOApp.Simple:

  def program: IO[Unit] = {
    for {
      queue <- Queue.unbounded[IO, WebSocketFrame]
      topic <- Topic[IO, WebSocketFrame]
      clientMessageReceived <- Ref.of[IO, Int](0)
      _ <- Stream(
        Stream.fromQueueUnterminated(queue).through(topic.publish),
        Stream
          .awakeEvery[IO](30.seconds)
          .map(_ => WebSocketFrame.Ping())
          .through(topic.publish),
        conditionalStream(
          clientMessageReceived.get.map(_ > 0),
          Stream
            .repeatEval(
              clientMessageReceived.get.flatMap { count =>
                if (count == 1) {
                  clientMessageReceived.set(2) *> createServerMessage
                } else {
                  for {
                    delay <- IO(Random.between(5000, 10001))
                    _ <- IO.sleep(delay.millis)
                    msg <- createServerMessage
                  } yield msg
                }
              }
            )
            .through(topic.publish)
        ).repeat,
        Stream.eval(
          Server.run[IO](queue, topic, clientMessageReceived)
        )
      ).parJoinUnbounded.compile.drain
    } yield ()
  }

  override def run: IO[Unit] = program

  def conditionalStream[F[_]: Applicative, A](
      condition: F[Boolean],
      stream: Stream[F, A]
  ): Stream[F, A] =
    Stream.eval(condition).flatMap { result =>
      if (result) stream else Stream.empty
    }

  val createServerMessage = IO(
    (
      Random.between(1, 10),
      Random.between(1, 10),
      Random.between(1, 10),
      Random.between(1, 10),
      Random.between(1, 10)
    )
  ).map { case (r1, r2, r3, r4, r5) =>
    WebSocketFrame.Text(
      Reactions(r1, r2, r3, r4, r5).asJson.noSpaces
    )
  }
