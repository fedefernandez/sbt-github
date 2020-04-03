/*
 * Copyright 2019-2020 Alejandro Hernández <https://github.com/alejandrohdezma>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alejandrohdezma.sbt.github.http

import java.io.FileNotFoundException
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

import scala.io.Source
import scala.util.Try
import scala.util.control.NoStackTrace

import sbt.util.Logger

import com.alejandrohdezma.sbt.github.json.{Decoder, Json}
import com.alejandrohdezma.sbt.github.syntax.json._
import com.alejandrohdezma.sbt.github.syntax.scalatry._
import com.alejandrohdezma.sbt.github.syntax.throwable._

object client {

  final case class URLNotFound(url: String)
      extends Throwable(s"$url was not found")
      with NoStackTrace

  /**
   * Calls the provided URL with the provided authentication and
   * returns its contents as `String`.
   */
  @SuppressWarnings(Array("all"))
  def get[A: Decoder](uri: String)(implicit auth: Authentication, logger: Logger): Try[A] =
    Try {
      logger.verbose(s"Getting content from URL: $uri")

      if (cache.containsKey(uri)) {
        logger.verbose(s"$uri contents already stored on cache")
      }

      cache.computeIfAbsent(
        uri, { _ =>
          val url = new URL(uri)

          logger.verbose(s"Content for $uri not found on cache, downloading...")

          val connection = url.openConnection

          connection.setRequestProperty("Authorization", auth.header)

          val inputStream = connection.getInputStream

          Source.fromInputStream(inputStream, "UTF-8").mkString
        }
      )
    }.failMap {
      case _: FileNotFoundException => URLNotFound(uri)
      case throwable                => throwable
    }.flatMap(Json.parse).as[A].recoverWith {
      case t =>
        logger.trace(t)
        t.raise
    }

  private val cache: ConcurrentHashMap[String, String] = new ConcurrentHashMap[String, String]()

}
