package com.example.http4s
package jetty

import javax.servlet._

import com.codahale.metrics.MetricRegistry
import org.http4s.server.jetty.JettyBuilder
import org.http4s.server.metrics._
import org.http4s.util.StreamApp

object JettyExample extends StreamApp {
  val metrics = new MetricRegistry

  def stream(args: List[String]) = JettyBuilder
    .bindHttp(8080)
    .mountService(ExampleService.service, "/http4s")
    .mountService(metricsService(metrics), "/metrics")
    .mountFilter(NoneShallPass, "/http4s/science/black-knight/*")
    .serve
}
