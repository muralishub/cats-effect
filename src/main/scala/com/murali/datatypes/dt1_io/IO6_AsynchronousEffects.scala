package com.murali.datatypes.dt1_io

import cats.effect.IO

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}
import scala.concurrent.duration.{FiniteDuration, TimeUnit}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object IO6_AsynchronousEffects extends App{


  def async[A](k: (Either[Throwable, A] => Unit) => Unit): IO[A] = ???


  // although we have a function to convert Scala Future conversion is very straightforward

  def convert[A](fa: => Future[A])(implicit ec: ExecutionContext): IO[A] = {
    IO.async{ cb =>
      // This triggers evaluation of the by-name param and of onComplete,
      // so it's OK to have side effects in this callback
      fa.onComplete{
        case Success(value) => cb(Right(value))
        case Failure(value) => cb(Left(value))
      }
    }
  }



  // Cancellable to scheduling and cancelling resources early

  def delayedTick(d: FiniteDuration)
                 (implicit sc: ScheduledExecutorService): IO[Unit] = {

    IO.cancelable { cb =>
      val r = new Runnable { def run() = cb(Right(())) }
      val f = sc.schedule(r, d.length, d.unit)

      // Returning the cancellation token needed to cancel
      // the scheduling and release resources early
      IO(f.cancel(false)).void
    }
  }


//IO.never












}
