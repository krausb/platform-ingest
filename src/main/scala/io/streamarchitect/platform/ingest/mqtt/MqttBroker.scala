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
import akka.actor.{ ActorLogging, FSM, Props }
import com.typesafe.scalalogging.Logger
import io.moquette.server.Server

class MqttBroker(brokerConfig: BrokerConfig) extends FSM[MqttBrokerState, NotUsed] {

  private val logger = Logger(getClass)

  private val broker = new Server()

  startWith(BrokerDown, NotUsed)

  when(BrokerDown) {
    case Event(BrokerStart, _) =>
      goto(BrokerUp)
    case _ =>
      logger.error("Only allowed event: Boot") // do nothing
      stay()
  }

  onTransition {
    case BrokerDown -> BrokerUp =>
      logger.info(s"Starting MQTT Broker...")
      startBroker
  }

  when(BrokerUp) {
    case Event(BrokerDown, _) =>
      goto(BrokerDown)
  }

  onTransition {
    case BrokerUp -> BrokerDown =>
      logger.info(s"Shutting down MQTT Broker...")
      stopBroker
  }

  onTermination {
    case StopEvent(FSM.Normal, state, data) =>
      logger.info("Stopping MQTT Broker System... Bye :-)")
    case StopEvent(FSM.Shutdown, state, data) ⇒ // ...
      logger.info("Stopping MQTT Broker System... Bye :-)")
    case StopEvent(FSM.Failure(cause), state, data) =>
      logger.error(s"Restarting MQTT Broker in state ${state} on error case: ${cause} ...")
      goto(BrokerUp)
  }

  whenUnhandled {
    case Event(e, s) ⇒
      logger.error("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  initialize()

  private def startBroker: Unit =
    broker.startServer(MqttConfig.getPropertiesFromConfig(brokerConfig))

  private def stopBroker: Unit =
    broker.stopServer()
}

object MqttBroker {

  def props(): Props =
    Props(
      classOf[MqttBroker],
      MqttConfig.getDefaultConfig()
    )

  /**
    * Factory to create a [[MqttBroker]] with default parameters
    *
    * @return
    */
  def apply: MqttBroker = new MqttBroker(MqttConfig.getDefaultConfig())

  /**
    * Factory to create a [[MqttBroker]] with a given set of parameters
    *
    * @param bindAddress
    * @param bindPort
    * @param defaultUser
    * @param defaultPassword
    * @return
    */
  def apply(bindAddress: String,
            bindPort: Integer,
            defaultUser: String,
            defaultPassword: String): MqttBroker =
    new MqttBroker(MqttConfig.getConfig(bindAddress, bindPort, defaultUser, defaultPassword))

}

/*
 * Broker States
 */
sealed trait MqttBrokerState
case object BrokerDown  extends MqttBrokerState
case object BrokerUp    extends MqttBrokerState
case object BrokerError extends MqttBrokerState

/*
 * Broker Commands
 */
sealed trait MqttBrokerCommand
case object BrokerStart      extends MqttBrokerCommand
case object BrokerReconnect  extends MqttBrokerCommand
case object BrokerDisconnect extends MqttBrokerCommand
