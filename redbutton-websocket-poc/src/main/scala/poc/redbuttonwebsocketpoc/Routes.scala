package poc.redbuttonwebsocketpoc

import cats.effect.{Concurrent, Ref}
import cats.effect.std.Queue
import fs2.concurrent.Topic
import fs2.io.file.Files
import fs2.Pipe
import fs2.Stream
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket.WebSocketBuilder2
import org.http4s.websocket.WebSocketFrame

class Routes[F[_]: Files: Concurrent] extends Http4sDsl[F] {
  def service(
      wsb: WebSocketBuilder2[F],
      q: Queue[F, WebSocketFrame],
      t: Topic[F, WebSocketFrame],
      clientMessageReceived: Ref[F, Boolean]
  ): HttpApp[F] = {
    HttpRoutes.of[F] { case GET -> Root / "ws" =>
      val send: Stream[F, WebSocketFrame] = {
        t.subscribe(maxQueued = 1000)
      }

      val receive: Pipe[F, WebSocketFrame, Unit] = {
        _.evalMap { _ => clientMessageReceived.set(true) }
      }

      wsb.build(send, receive)
    }
  }.orNotFound
}
