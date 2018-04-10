/*
 * Copyright (C) 2018  Bastian Kraus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.streamarchitect.platform.ingest.sink

import akka.actor.SupervisorStrategy.{ Escalate, Restart }
import akka.actor.{
  Actor,
  ActorInitializationException,
  ActorLogging,
  OneForOneStrategy,
  SupervisorStrategy
}
import com.typesafe.scalalogging.Logger
import io.streamarchitect.platform.domain.codec.DomainCodec
import io.streamarchitect.platform.domain.telemetry.PositionedTelemetry

/**
  * Abstract SinkActor implementation template
  */
abstract class SinkActor extends Actor {

  protected val log = Logger(getClass)

  override final def receive: Receive = {
    case s: SinkMessage =>
      validateReceivedMessage(s) match {
        case Left(t: PositionedTelemetry) =>
          log.debug(s"Received entity: ${t}")
          handleReceivedMessage(s)
        case Right(t: Throwable) =>
          log.error(s"Entity cannot be deserialized: ${t.getMessage}", t)
      }

  }

  /**
    * Handler for received messages to override by the inheriting sink actor
    *
    * @param s
    */
  def handleReceivedMessage(s: SinkMessage): Unit = ???

  /**
    * Incoming message validator: tries to decode the received [[Array]] of [[Byte]]s into
    * an instance of [[PositionedTelemetry]]
    * @param s
    * @return
    */
  def validateReceivedMessage(s: SinkMessage): Either[PositionedTelemetry, Throwable] =
    try {
      Left(
        DomainCodec.decode(s.payload, PositionedTelemetry.SCHEMA$).asInstanceOf[PositionedTelemetry]
      )
    } catch {
      case t: Throwable => Right(t)
    }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy() {
      case _: ActorInitializationException => Escalate
      case e: Exception =>
        log.error("SinkActor was interrupted due to an error.", e)
        Restart
    }

}

/**
  *
  * @param payload
  */
sealed case class SinkMessage(inboundTopic: String, payload: Array[Byte])
