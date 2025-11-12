package poc.redbuttonwebsocketpoc

import cats.effect.{IO, IOApp}

object Main extends IOApp.Simple:
  val run = RedbuttonwebsocketpocServer.run[IO]
