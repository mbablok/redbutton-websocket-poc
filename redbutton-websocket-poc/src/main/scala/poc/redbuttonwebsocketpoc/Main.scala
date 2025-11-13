package poc.redbuttonwebsocketpoc

import cats.effect.std.Queue
import cats.effect.{IO, IOApp}
import fs2.concurrent.Topic
import fs2.Stream
import org.http4s.websocket.WebSocketFrame

object Main extends IOApp.Simple:
  def program: IO[Unit] = {
    for {
      q <- Queue.unbounded[IO, WebSocketFrame]
      t <- Topic[IO, WebSocketFrame]
      s <- Stream(
        Stream.fromQueueUnterminated(q).through(t.publish),
        Stream.eval(RedbuttonwebsocketpocServer.run[IO](q, t))
      ).parJoinUnbounded.compile.drain
    } yield s
  }
  override def run: IO[Unit] = program

