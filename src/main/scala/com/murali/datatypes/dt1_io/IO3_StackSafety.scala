package com.murali.datatypes.dt1_io

import cats.effect.IO

import scala.+:
import scala.annotation.tailrec
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

object IO3_StackSafety extends App {


  val test: String = "type111/123"
  val test2: Array[String] = test.split("/")
  val r = test2.toList match {
    case (x :: y :: xs) => (x, y)
    case _ => ("", "")

  }

  println(r._2)


  def febAsync(n: Int, a: Long = 0, b: Long = 1): Future[Long] = {
    Future(a + b).flatMap(b2 => if (n > 0) {

      println(b.toString)
      Thread.sleep(2000)

      febAsync(n - 1, b, b2)
    } else Future(a)
    )
  }


  febAsync(10000)


//  def feb(n: Int, a:Long = 0, b: Long = 1): IO[Long] = {
//    IO(a +  b).flatMap{b2 =>
//    println(b2)
//      if(n > 0)
//        feb(n - 1, b, b2)
//      else IO.pure(a)
//    }
//  }
//
//
//
//feb(10).unsafeRunSync()









}
