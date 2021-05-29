package com.murali.datatypes.dt1_io

import cats.effect.{IO, Timer}

import scala.concurrent.duration.FiniteDuration

object IO11_ErrorHandling extends App{

  //using raiseError

  val boom = IO.raiseError(new Exception("boom"))
//boom.unsafeRunSync()

  //using Attempt its like catch in try/catch



  boom.attempt.unsafeRunSync()

//implementing retries with exponential backoff
  def retryWithBackoff[A](ioa: IO[A], initialDelay: FiniteDuration, maxRetries: Int)
                         (implicit timer: Timer[IO]): IO[A] = {

    ioa.handleErrorWith { error =>
      if (maxRetries > 0)
        IO.sleep(initialDelay) *> retryWithBackoff(ioa, initialDelay * 2, maxRetries - 1)
      else
        IO.raiseError(error)
    }
  }


}
