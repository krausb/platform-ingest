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

import java.util.Properties

/**
  * Configuration for the [[MqttBroker]]
  *
  * @param bindAddress
  * @param bindPort
  * @param defaultUser
  * @param defaultPassword
  */
case class BrokerConfig(
    bindAddress: String,
    bindPort: Integer,
    defaultUser: String,
    defaultPassword: String
)

object MqttConfig {

  /**
    * Factory for returning a default configuration
    *
    * @return [[BrokerConfig("0.0.0.0", 1883, "mqttuser", "mqttuser123")]]
    */
  def getDefaultConfig(): BrokerConfig = BrokerConfig("0.0.0.0", 1883, "mqttuser", "mqttuser123")

  /**
    * Factory for generating a MQTT Config
    *
    * @param bindAddress
    * @param bindPort
    * @param defaultUser
    * @param defaultPassword
    * @return [[BrokerConfig]]
    */
  def getConfig(bindAddress: String,
                bindPort: Integer,
                defaultUser: String,
                defaultPassword: String): BrokerConfig =
    BrokerConfig(bindAddress, bindPort, defaultUser, defaultPassword)

  /**
    * Convert [[BrokerConfig]] to [[Properties]]
    *
    * @param brokerConfig
    * @return
    */
  def getPropertiesFromConfig(brokerConfig: BrokerConfig): Properties = {
    val props = new Properties()
    props.setProperty("host", brokerConfig.bindAddress)
    props.setProperty("port", brokerConfig.bindPort.toString)
    props.setProperty("allow_anonymous", "true")
    props.setProperty("allow_zero_byte_client_id", "false")
    props
  }
}
