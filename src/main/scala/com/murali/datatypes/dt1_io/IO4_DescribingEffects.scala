package com.murali.datatypes.dt1_io

import cats.effect.IO

object IO4_DescribingEffects extends App{


  // this is safe because value is already evaluated
  def pure[A](a: A): IO[A] = IO.pure(a)

  // this is not safe because we are introducing a side effect println

  def pureSE[A](a: A): IO[Unit] = IO.pure(println(s"value is $a"))

 // this is ok because we are wrapping a side effect in a safe manner
  def pure2[A](a: A): IO[Unit] = IO.pure(a).flatMap(a => IO{println(s"this is safe $a")})


  //just an alias
val unit: IO[Unit] = IO.unit
val unit2: IO[Unit] = IO.pure(())






}
