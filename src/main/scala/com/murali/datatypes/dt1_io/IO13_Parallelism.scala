package com.murali.datatypes.dt1_io

import cats.data.NonEmptyList
import cats.effect.{ContextShift, ExitCase, IO}
import cats.syntax.all._

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object IO13_Parallelism extends App {

  val  ioA = IO(println("running A"))
  val  ioB = IO(println("running B"))
  val  ioC = IO(println("running C"))

  implicit val io: ContextShift[IO] = IO.contextShift(ExecutionContext.global)

  val program = (ioA, ioB, ioC).parMapN((_, _, _) => ())
  program.unsafeRunSync()


  ()


  // if any of the cases fail the whole computation will be failed. while the unfinished tasks get cancelled

  implicit val timer = IO.timer(ExecutionContext.global)

  val a = IO.raiseError[Unit](new Exception("boom")) <* IO(println("this is a"))

  val b = (IO.sleep(1.second) *> IO(println("running ioB")))
    .guaranteeCase {
      case ExitCase.Canceled => IO(println("ioB was cancelleed"))
      case _ => IO.unit
    }

  val parFailure = (a, b).parMapN { (_, _) => () }

  parFailure.attempt.unsafeRunSync()

  ()

  // if one of the task fails immediately , it will not wait for the other tasks canceled and computation is completed immediately

  val ioAA = IO.sleep(10.seconds) *> IO(println("Delayed"))
  val ioBB = IO.raiseError[Unit](new Exception("boom"))
  (ioAA, ioBB).parMapN((_, _) => ())


//parSequence

  val iA = IO(1)

  val lotOfIos: IO[NonEmptyList[Int]] = NonEmptyList.of(iA, iA, iA).parSequence
  val lotOfIosequencially: IO[NonEmptyList[Int]] = NonEmptyList.of(iA, iA, iA).sequence

  lotOfIos.map(println).unsafeRunSync()


// parTraverse


  val r: IO[NonEmptyList[Int]] = NonEmptyList.of(1, 2, 3).parTraverse(i => IO(i))

}
