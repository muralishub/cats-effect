package com.murali.datatypes.dt1_io

import cats.effect.{ContextShift, IO}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class IO10_Conversions {

  // converting scala Future and an Either into IO

  def fromFuture[A](iof: IO[Future[A]]): IO[A] = ???

// Future is not lazy and it memoizes. this function takes its parameters as an IO
  // which could be made lazy
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  IO.fromFuture(IO{Future(println("i am future"))})


  // eager evaluation

  val f = Future.successful("I come from the Future!")

  IO.fromFuture(IO.pure(f))



// fromEither
def fromEither[A](e: Either[Throwable, A]): IO[A] = e.fold(IO.raiseError, IO.pure)






}
