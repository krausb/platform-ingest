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

import akka.actor.SupervisorStrategy.{Escalate, Restart}
import akka.actor.{ActorInitializationException, OneForOneStrategy, Props, SupervisorStrategy}
import akka.kafka.ProducerSettings
import io.streamarchitect.platform.ingest.IngestConfig
import org.apache.kafka.clients.producer.{Callback, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.{ByteArraySerializer, Serializer, StringSerializer}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Kafka Sink to forward ingested messages to Kafka
  */
class KafkaSink extends SinkActor {

  implicit val ec: ExecutionContext = context.dispatcher

  private val kafkaSettings = KafkaSink.kafkaProducerSettings()
  log.info(s"Initializiing KafkaSink with settings: ${kafkaSettings}")

  private val kafkaProducer = KafkaSink.kafkaProducerSettings().createKafkaProducer()

  val outboundTopic = IngestConfig.config.getString("kafkaSink.topic")

  override def handleReceivedMessage(s: SinkMessage): Unit = {
    Future {
      val res = kafkaProducer.send(
        new ProducerRecord(
          outboundTopic,
          s.payload
        ),
        new Callback {
          override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
            log.debug(s"Successfully sent to kafka: ${metadata}")
          }
        }
      )
    }
  }

  override def supervisorStrategy: SupervisorStrategy =
    OneForOneStrategy() {
      case _: ActorInitializationException => Escalate
      case e: Exception =>
        log.error("SinkActor was interrupted due to an error.", e)
        Restart
    }

}

object KafkaSink {

  def props: Props = Props(classOf[KafkaSink])

  def kafkaProducerSettings(): ProducerSettings[String, Array[Byte]] =
    ProducerSettings(
      IngestConfig.config.getConfig("akka.kafka.producer"),
      new StringSerializer(),
      new ByteArraySerializer()
    )

}
