/*
 * Copyright (C) 2018  Bastian Kraus
 *
 * This software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version)
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.streamarchitect.platform.ingest

import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import akka.pattern.{Backoff, BackoffSupervisor}
import io.streamarchitect.platform.ingest.mqtt.{MqttBroker, Start}

trait System {
  private val config = ConfigFactory.load()

  implicit val system = ActorSystem("plattform-ingest", config)

  val mqttBroker = system.actorOf(MqttBroker.props())

  mqttBroker ! Start
}
