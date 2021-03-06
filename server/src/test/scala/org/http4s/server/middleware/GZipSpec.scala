package org.http4s
package server
package middleware

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

import cats.implicits._
import fs2.{Chunk, Stream}
import org.http4s.server.syntax._
import org.http4s.dsl._
import org.http4s.headers._
import org.http4s.server.syntax._
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties

class GZipSpec extends Http4sSpec {
  "GZip" should {
    "fall through if the route doesn't match" in {
      val service = GZip(HttpService.empty) |+| HttpService {
        case GET -> Root =>
          Ok("pong")
      }
      val req = Request(Method.GET, Uri.uri("/"))
        .putHeaders(`Accept-Encoding`(ContentCoding.gzip))
      val resp = service.orNotFound(req).unsafeRun
      resp.status must_== (Status.Ok)
      resp.headers.get(`Content-Encoding`) must beNone
    }

    checkAll("encoding", new Properties("GZip") {
      property("middleware encoding == GZIPOutputStream encoding") = forAll { vector: Vector[Array[Byte]] =>
        val service = GZip(HttpService { case GET -> Root => Ok(Stream.emits(vector)) })
        val req = Request(Method.GET, Uri.uri("/")).putHeaders(`Accept-Encoding`(ContentCoding.gzip))
        val actual = service.orNotFound(req).as[Chunk[Byte]].map(_.toArray)

        val byteArrayStream = new ByteArrayOutputStream()
        val gzipStream = new GZIPOutputStream(byteArrayStream)
        vector.foreach(gzipStream.write)
        gzipStream.close()
        val expected = byteArrayStream.toByteArray

        actual must returnValue(expected)
      }
    })
  }
}
