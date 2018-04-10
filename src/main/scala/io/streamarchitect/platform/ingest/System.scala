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

package io.streamarchitect.platform.ingest

import scala.concurrent.duration._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import io.streamarchitect.platform.ingest.mqtt.{
  BrokerStart,
  ConnectorStart,
  MqttBroker,
  MqttConnector
}
import io.streamarchitect.platform.ingest.sink.SinkFactory

import scala.concurrent.ExecutionContext

trait System {
  private val config = ConfigFactory.load()

  implicit val system               = ActorSystem("streamarchitect-io-platform-ingest", config)
  implicit val ec: ExecutionContext = system.dispatcher

  /*
   * Bootstrap and start Kafka Producer
   */
  val sink = SinkFactory.createSink(IngestConfig.config.getString("activeSink"))

  /*
   * Bootstrap and start MQTT Broker
   */
  val mqttBroker = system.actorOf(MqttBroker.props())
  mqttBroker ! BrokerStart

  /*
   * Bootstrap MQTT Client to send data to the sink
   * Workaround with the delay should be removed:
   * Required is a kind of [[Future]] event provided by the mqttBroker
   * that notifies us when the broker finished its bootstrap phase and
   * is ready for connection
   */
  system.scheduler.scheduleOnce(5 seconds) {
    val mqttConnector = system.actorOf(MqttConnector.props(sink))
    mqttConnector ! ConnectorStart
  }
}
