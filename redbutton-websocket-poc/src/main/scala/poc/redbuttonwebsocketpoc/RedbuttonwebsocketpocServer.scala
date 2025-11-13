package poc.redbuttonwebsocketpoc

import cats.effect.Async
import com.comcast.ip4s.*
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import fs2.concurrent.Topic
import org.http4s.websocket.WebSocketFrame
import cats.effect.std.Queue
import cats.implicits.*
import fs2.io.file.Files

object RedbuttonwebsocketpocServer:

  def run[F[_]: Async: Network : Files](
      q: Queue[F, WebSocketFrame],
      t: Topic[F, WebSocketFrame]
  ): F[Unit] = {
    for {

      _ <-
        EmberServerBuilder
          .default[F]
          .withHost(ipv4"0.0.0.0")
          .withPort(port"8080")
          .withHttpWebSocketApp(wsb => new Routes().service(wsb, q, t))
          .build
    } yield ()

  }.useForever.void
