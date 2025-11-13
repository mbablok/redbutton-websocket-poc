package poc.redbuttonwebsocketpoc

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic
import fs2.Stream
import org.http4s.websocket.WebSocketFrame
import scala.concurrent.duration.*

object Main extends IOApp.Simple:
  def program: IO[Unit] = {
    for {
      q <- Queue.unbounded[IO, WebSocketFrame]
      t <- Topic[IO, WebSocketFrame]
      s <- Stream(
        Stream.fromQueueUnterminated(q).through(t.publish),
        Stream.eval(RedbuttonwebsocketpocServer.run[IO](q, t)),
        Stream
          .awakeEvery[IO](1.seconds)
          .map(_ =>
            WebSocketFrame.Text(
              """{"reactions":{"reaction-1":22,"reaction-2":6,"reaction-3":5,"reaction-4":15,"reaction-5":7}}
            """
            )
          )
          .through(t.publish)
      ).parJoinUnbounded.compile.drain
    } yield s
  }
  override def run: IO[Unit] = program
