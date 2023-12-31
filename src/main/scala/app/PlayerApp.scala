package app

import zio._
import zio.http._
import zio.json._

import java.util.UUID

import state.PlayersRepo
import units.Players

object PlayerApp {
  def apply(): Http[PlayersRepo, Throwable, Request, Response] =
    Http.collectZIO[Request] {
      case Method.GET -> Root / "player" / "allPlayers" =>
        PlayerFlow.getAllPlayers()
      case Method.GET -> Root / "player" / "createPlayer" / name =>
        PlayerFlow.createPlayer(name)
      case Method.GET -> Root / "player" / "getPlayer" / id =>
        PlayerFlow.getPlayer(id)
      case Method.GET -> Root / "player" / "deletePlayer" / id =>
        PlayerFlow.deletePlayer(id)
    }
}

object PlayerFlow {
  def getAllPlayers(): ZIO[PlayersRepo, Throwable, Response] =
    PlayersRepo
      .getAllPlayers()
      .map(players => Response.json(players.map(_.toJson).toJson))

  def createPlayer(name: String): ZIO[PlayersRepo, Throwable, Response] = for {
    uuid <- Random.nextUUID
    resp <- PlayersRepo
      .addPlayer(name, uuid)
      .map(playerData => Response.json(playerData.toJson))
  } yield resp

  def getPlayer(id: String): ZIO[PlayersRepo, Throwable, Response] = {
    val uuid = UUID.fromString(id)
    PlayersRepo
      .getOnePlayer(uuid)
      .map {
        case Some(player) =>
          Response.json(player.toJson)
        case None => Response.status(Status.NotFound)
      }
  }
  def deletePlayer(id: String): ZIO[PlayersRepo, Throwable, Response] = {
    val uuid = UUID.fromString(id)
    PlayersRepo.deletePlayer(uuid).map(player => Response.json(player.toJson))
  }
}
