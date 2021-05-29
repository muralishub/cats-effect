package com.murali.datatypes.dt1_io

import cats.effect.{ContextShift, IO}
import cats.effect.IO.shift

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global

object IO12_ThreadShifting extends App{


  implicit val contextShift: ContextShift[IO] = IO.contextShift(global)

  val task = IO(println("task"))

  shift(contextShift).flatMap(_ => task)

  shift.flatMap(_ => task)

  // using cats syntax
  shift *> task

  // or we can specify an asynchronous boundary after evaluation if a certain task

  task.flatMap(a => IO.shift.map(_ => a))

  //using cats syntax
  task <* IO.shift

  // example of where this can be usefull


  val cachedThreadPool = Executors.newCachedThreadPool()

  val BlockingFileIO = ExecutionContext.fromExecutor(cachedThreadPool)

  implicit val Main = ExecutionContext.global


val ioa =
  for {
  _ <- IO(println("enter your name"))
  _ <- IO.shift(BlockingFileIO)
  name <- IO(scala.io.StdIn.readLine())
  _ <- IO.shift(Main)
  _ <- IO(println(s"welcome $name"))
  _ <- IO(cachedThreadPool.shutdown())
  } yield ()



  println(ioa.unsafeRunSync())


  //following example shows using shift to reset the thread stack and yield control back to the underlying pool.


  lazy val doStuff = IO(println("stuff"))

  lazy val repeat:IO[Unit] =
    for {
    _ <- doStuff
    _ <- IO.shift
    _ <- repeat
     } yield ()

//  IO is trampolined for all synchronous and asynchronous joins. This means that you can safely call flatMap in a recursive function of arbitrary depth, without fear of blowing the stack

def signal[A](a: A):IO[A] = IO.async(_(Right(a)))

  def loop(n:Int):IO[Int] =
    signal(n).flatMap{x =>
      if(x > 0) loop(n - 1) else IO.pure(0)
    }



}
