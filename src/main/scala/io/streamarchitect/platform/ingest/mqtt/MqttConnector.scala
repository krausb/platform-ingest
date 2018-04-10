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

package io.streamarchitect.platform.ingest.mqtt

import akka.NotUsed
import akka.actor.{ ActorRef, FSM, PoisonPill, Props }
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mqtt.scaladsl.MqttSource
import akka.stream.alpakka.mqtt.{ MqttConnectionSettings, MqttQoS, MqttSourceSettings }
import akka.stream.scaladsl.{ Keep, Sink }
import com.typesafe.scalalogging.Logger
import io.streamarchitect.platform.ingest.IngestConfig
import io.streamarchitect.platform.ingest.sink.{ SinkActor, SinkMessage }
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

import scala.concurrent.ExecutionContext

/**
  * MQTT Connector for reading data from the [[MqttBroker]] and sink it
  * into given [[SinkActor]] via [[SinkMessage]]
  */
class MqttConnector(sinkActor: ActorRef) extends FSM[MqttConnectorState, NotUsed] {

  private val logger = Logger(getClass)

  implicit val materializer: ActorMaterializer    = ActorMaterializer()
  implicit val executionContext: ExecutionContext = context.dispatcher

  private val connectorConfig = IngestConfig.config.getConfig("mqtt.connector")

  private val connectionSettings = MqttConnectionSettings(
    s"tcp://${connectorConfig.getString(s"broker-host")}:${connectorConfig.getInt("broker-port")}",
    connectorConfig.getString("client-id  "),
    new MemoryPersistence
  )

  val settings = MqttSourceSettings(
    connectionSettings,
    Map(connectorConfig.getString("sink-topic") -> MqttQoS.AtLeastOnce)
  )

  val mqttSource = MqttSource.atMostOnce(settings, bufferSize = 8)

  startWith(ConnectorDown, NotUsed)

  when(ConnectorDown) {
    case Event(ConnectorStart, _) =>
      goto(ConnectorUp)
    case _ =>
      logger.error("Only allowed event: Boot") // do nothing
      stay()
  }

  onTransition {
    case ConnectorDown -> ConnectorUp =>
      logger.info(s"Starting MQTT Connector...")
      startConnector
  }

  when(ConnectorUp) {
    case Event(ConnectorDown, _) =>
      goto(ConnectorDown)
  }

  onTransition {
    case ConnectorUp -> ConnectorDown =>
      logger.info(s"Shutting down MQTT Connector...")
  }

  onTermination {
    case StopEvent(FSM.Normal, state, data) =>
      logger.info("Stopping MQTT Connector System... Bye :-)")
    case StopEvent(FSM.Shutdown, state, data) ⇒ // ...
      logger.info("Stopping MQTT Connector System... Bye :-)")
    case StopEvent(FSM.Failure(cause), state, data) =>
      logger.error(s"Restarting MQTT Connector in state ${state} on error case: ${cause} ...")
      goto(ConnectorUp)
  }

  whenUnhandled {
    case Event(e, s) ⇒
      logger.error("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  initialize()

  /**
    * Method materializes and starts the mqtt source stream
    */
  private def startConnector: Unit = {
    logger.debug(s"Starting connector stream with settings: ${settings}")
    mqttSource
      .map(msg => {
        logger.debug(s"Received message: ${msg}")
        SinkMessage(msg.topic, msg.payload.toArray[Byte])
      })
      .toMat(Sink.actorRef(sinkActor, PoisonPill))(Keep.both)
      .run()
  }

}

object MqttConnector {

  def props(sinkActor: ActorRef): Props =
    Props(
      classOf[MqttConnector],
      sinkActor
    )
}

/*
 * Connector States
 */
sealed trait MqttConnectorState
case object ConnectorDown  extends MqttConnectorState
case object ConnectorUp    extends MqttConnectorState
case object ConnectorError extends MqttConnectorState

/*
 * Connector Commands
 */
sealed trait MqttConnectorCommand
case object ConnectorStart      extends MqttConnectorCommand
case object ConnectotReconnect  extends MqttConnectorCommand
case object ConnectorDisconnect extends MqttConnectorCommand
