package poc.redbuttonwebsocketpoc

import io.circe.{Encoder, Decoder, Json}

object Model:
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
