package com.murali.datatypes.dt1_io

import cats.effect.{ContextShift, Fiber, IO, SyncIO, Timer}

import java.io._
import scala.concurrent.{CancellationException, ExecutionContext}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.control.NonFatal

object IO8_ConcurrencyAndCancellation extends App{


  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  def retryUntilRight[A, B](io: IO[Either[A, B]]): IO[B] = {
    io.flatMap {
      case Right(b) => IO.pure(b)
      case Left(_) => retryUntilRight(io)
    }
  }

  // non terminating IO that is not cancellable
  val nonCancelable: IO[Int] = retryUntilRight(IO(Left(0)))

  // non-terminating IO that is cancelable because there is an
  // async boundary created by IO.shift before `flatMap` chain

  val cancelable: IO[Int] = IO.shift *> retryUntilRight(IO(Left(0)))

  // Building cancellable IO tasks

  def unsafeFileToString(file: File) = {
    val in =  new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))
    try {
      val sb = new StringBuilder()
      var hasNext = true

      while (hasNext) {
        hasNext = false
        val line = in.readLine()
        if (line != null) {
          hasNext = true
          sb.append(line)
        }
      }
      sb.toString()
    }
    finally {
      in.close()
      }
    }

  def readFile(file: File)(implicit ec: ExecutionContext) =
    IO.async[String]{ cb =>
      ec.execute(() => {
      try {
      cb(Right(unsafeFileToString(file)))

      } catch  {
        case NonFatal(e) =>
        cb(Left(e))
      }
      }
      )

  }


  // Concurrent star + cancel


 val launchMissiles: IO[Unit] = IO.raiseError(new Exception("boom!"))

  val runToBunker = IO(println("to the bunker"))

  for {
  fiber <- launchMissiles.start
  _ <- runToBunker.handleErrorWith{error =>
  fiber.cancel *> IO.raiseError(error)
  }
  aftermath <- fiber.join

  } yield aftermath



// using unsafeRunCancelable


  implicit val timer = IO.timer(ExecutionContext.global)

  val io = IO.sleep(10.seconds) *> IO(println("Hello"))

  val cancel: IO[Unit] = io.unsafeRunCancelable(r => println(s"done $r"))

  cancel.unsafeRunSync()


  import cats.effect.SyncIO
  import cats.syntax.flatMap._
  val pureResult: SyncIO[IO[Unit]] = io.runCancelable { r =>
    IO(println(s"Done: $r"))
  }

  pureResult.toIO.flatten


  //Uncancelable  marker

  //using above io
 // val io = IO.sleep(10.seconds) *> IO(println("Hello"))
  io.uncancelable

  //Race Conditions - race and racePair

  // simple version
  def race[A, B](lh: IO[A], rh: IO[B])
                (implicit cs: ContextShift[IO]): IO[Either[A, B]] = ???

  // advanced version
  def racePair[A, B](lh: IO[A], rh: IO[B])
                    (implicit cs: ContextShift[IO]): IO[Either[(A, Fiber[IO, B]), (Fiber[IO, A], B)]] = ???



  // a race can be derived from racePair
  def race1[A, B](lh: IO[A], rh: IO[B])
                (implicit cs: ContextShift[IO]): IO[Either[A, B]] = {

    IO.racePair(lh, rh).flatMap {
      case Left((a, fiber)) => fiber.cancel.map(_ => Left(a))
      case Right((fiber, b)) => fiber.cancel.map(_ => Right(b))
    }
  }
    // using race we could implement a timeout operation


    def timeoutTo[A](fa: IO[A], after: FiniteDuration, fallback: IO[A])(implicit timer: Timer[IO], cs: ContextShift[IO]): IO[A] = {
      IO.race(fa, timer.sleep(after)).flatMap {
        case Left(a) => IO.pure(a)
        case Right(_) => fallback

      }
    }

      def timeout[A](fa: IO[A], after: FiniteDuration)(implicit timer: Timer[IO], cs: ContextShift[IO]): IO[A] = {
        val error = new CancellationException(after.toString())
        timeoutTo(fa, after, IO.raiseError(error))
      }




















}
