package poc.redbuttonwebsocketpoc

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic
import fs2.Stream
import org.http4s.websocket.WebSocketFrame
import scala.concurrent.duration.*
import scala.util.Random

object Main extends IOApp.Simple:

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
              s"""{"reactions":{"reaction-1":$r1,"reaction-2":$r2,"reaction-3":$r3,"reaction-4":$r4,"reaction-5":$r5}}"""
            )
          )
          .through(t.publish)
      ).parJoinUnbounded.compile.drain
    } yield s
  }
  override def run: IO[Unit] = program
