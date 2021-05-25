package com.murali.datatypes.dt1_io

import cats.effect.{ContextShift, IO}
import com.murali.datatypes.dt1_io.IO7_IOSuspend


object IO7_IOSuspend extends App{

//IO.suspend has this equivalence
  //IO.suspend(f) <-> IO(f).flatten

  val test: IO[Int] = IO.suspend{IO.pure(2)}
  println(test.unsafeRunSync())

  def fib(n: Int, a: Long, b: Long): IO[Long] =
    IO.suspend {
      if (n > 0)
        fib(n - 1, b, a + b)
      else
        IO.pure(a)
    }

  // we can also insert asynchronous boundaries

  def fibb(n: Int, a: Long, b: Long)(implicit  cs: ContextShift[IO]): IO[Long] =
    IO.suspend{
      if(n == 0) IO.pure(a) else {
        val next = fibb(n - 1, b, a + b)

        // Every 100 cycles, introduce a logical thread fork

        if(n % 100 == 0)
          cs.shift *> next
        else
        next
      }
    }



}
