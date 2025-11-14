package poc.redbuttonwebsocketpoc

import cats.effect.std.Queue
import cats.effect.{Async, Ref}
import cats.implicits.*
import com.comcast.ip4s.*
import fs2.concurrent.Topic
import fs2.io.file.Files
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.websocket.WebSocketFrame

object Server:

  def run[F[_]: Async: Network: Files](
      queue: Queue[F, WebSocketFrame],
      topic: Topic[F, WebSocketFrame],
      clientMessageReceived: Ref[F, Int]
  ): F[Unit] =
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpWebSocketApp(wsb =>
        new Routes().service(
          wsb,
          queue,
          topic,
          clientMessageReceived
        )
      )
      .build
      .useForever
      .void
