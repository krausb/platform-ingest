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

import akka.actor.{ ActorRef, ActorSystem }
import com.typesafe.scalalogging.Logger
import sun.reflect.generics.reflectiveObjects.NotImplementedException

/**
  * Factory to construct target sinks
  */
object SinkFactory {

  private val log = Logger(getClass)

  /**
    * Factory method to create a concrete sink
    * @param activeSink
    * @return
    */
  def createSink(activeSink: String)(implicit system: ActorSystem): ActorRef =
    activeSink match {
      case "KafkaSink" =>
        log.info("Creating new KafkaSink...")
        system.actorOf(KafkaSink.props)
      case _ =>
        throw new NotImplementedException()
    }

}
