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

package com.alejandrohdezma.sbt.github.github.repository

import sbt.util.Logger

import com.alejandrohdezma.sbt.github._
import com.alejandrohdezma.sbt.github.github._
import com.alejandrohdezma.sbt.github.github.error.GithubError
import com.alejandrohdezma.sbt.github.http.Authentication
import com.alejandrohdezma.sbt.github.http.Authentication.Token
import org.http4s.dsl.io._
import org.specs2.mutable.Specification

class RepositoryCollaboratorsSpec extends Specification {

  "repository.collaborators" should {

    "return list of collaborators (alphabetically ordered) with user info from Github API" >> withServer {
      case GET -> Root / "me"  => Ok("""{
        "name": "Me",
        "login": "me",
        "html_url": "http://example.com/me",
        "email": "me@example.com"
      }""")
      case GET -> Root / "you" => Ok("""{
        "login": "you",
        "html_url": "http://example.com/you",
        "avatar_url": "http://example.com/you.png"
      }""")
      case GET -> Root / "him" => Ok("""{
        "name": "Him",
        "login": "him",
        "html_url": "http://example.com/him"
      }""")
      case req @ GET -> Root / "collaborators" =>
        Ok(s"""[
          {
            "login": "me",
            "avatar_url": "http://example.com/me.png",
            "html_url": "http://example.com/me",
            "url": "${req.urlTo("me")}"
          },
          {
            "login": "you",
            "html_url": "http://example.com/you",
            "url": "${req.urlTo("you")}"
          },
          {
            "login": "him",
            "avatar_url": null,
            "html_url": "http://example.com/him",
            "url": "${req.urlTo("him")}"
          }
        ]""")
    } { uri =>
      val repository = EmptyRepository.copy(collaboratorsUrl = url"${uri}collaborators")

      val collaborators = repository.collaborators(List("me", "you", "him"))

      val expected = Collaborators(
        Collaborator(
          "you",
          url"http://example.com/you",
          Some(url"${uri}you"),
          None,
          None,
          None
        ),
        Collaborator(
          "him",
          url"http://example.com/him",
          Some(url"${uri}him"),
          Some("Him"),
          None,
          None
        ),
        Collaborator(
          "me",
          url"http://example.com/me",
          Some(url"${uri}me"),
          Some("Me"),
          Some("me@example.com"),
          Some(url"http://example.com/me.png")
        )
      )

      collaborators must beSuccessfulTry(expected)
    }

    "exclude collaborators not in provided list and don't retrieve user info for them" >> withServer {
      case GET -> Root / "me" =>
        Ok("""{"login": "me", "html_url": "http://example.com/me", "name": "Me"}""")
      case req @ GET -> Root / "collaborators" =>
        Ok(s"""[
          {
            "login": "me",
            "avatar_url": "http://example.com/me.png",
            "html_url": "http://example.com/me",
            "url": "${req.urlTo("me")}"
          },
          {
            "login": "you",
            "html_url": "http://example.com/you",
            "url": "http://api.example.com/you"
          }
        ]""")
    } { uri =>
      val repository = EmptyRepository.copy(collaboratorsUrl = url"${uri}collaborators")

      val collaborators = repository.collaborators(List("me"))

      val expected = Collaborators(
        Collaborator(
          "me",
          url"http://example.com/me",
          Some(url"${uri}me"),
          Some("Me"),
          None,
          Some(url"http://example.com/me.png")
        )
      )

      collaborators must beSuccessfulTry(expected)
    }

    "return generic error on any error" >> withServer {
      case GET -> Root / "collaborators" => Ok("""{"hello": "hi"}""")
    } { uri =>
      val repository = EmptyRepository.copy(collaboratorsUrl = url"${uri}collaborators")

      val collaborators = repository.collaborators(Nil)

      val expected = GithubError("Unable to get repository collaborators")

      collaborators must beAFailedTry(equalTo(expected))
    }

  }

  implicit val authentication: Authentication = Token("1234")
  implicit val noOpLogger: Logger             = Logger.Null

  lazy val EmptyRepository: Repository =
    Repository(
      "",
      "",
      License("", url"http://example.com"),
      url"http://example.com",
      0,
      url"http://example.com",
      url"http://example.com",
      None,
      url"http://example.com"
    )

}
