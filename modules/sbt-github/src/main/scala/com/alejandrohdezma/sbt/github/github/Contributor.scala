/*
 * Copyright 2019-2022 Alejandro Hernández <https://github.com/alejandrohdezma>
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

package com.alejandrohdezma.sbt.github.github

import com.alejandrohdezma.sbt.github.json.Decoder
import com.alejandrohdezma.sbt.github.syntax.json._

/** Represents a repository contributor */
final case class Contributor(
    login: String,
    contributions: Int,
    url: String,
    avatar: Option[String]
)

object Contributor {

  implicit val ContributorDecoder: Decoder[Contributor] = json =>
    for {
      login         <- json.get[String]("login")
      contributions <- json.get[Int]("contributions")
      url           <- json.get[String]("html_url")
      avatar        <- json.get[Option[String]]("avatar_url")
    } yield Contributor(login, contributions, url, avatar.filter(_.nonEmpty))

}
