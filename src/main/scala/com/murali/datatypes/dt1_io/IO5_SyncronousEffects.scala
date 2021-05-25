package com.murali.datatypes.dt1_io

import cats.effect.IO

object IO5_SyncronousEffects extends App{


  //IO.apply

  def apply[A](a: => A): IO[A] = ???

  // passed by value and its execution is being suspended in IO context
  // consider reading and writing form the console

  def putStrLn(value: String) = IO(println(value))
  val readLn = IO(scala.io.StdIn.readLine())

  // so we can model in a purely functional way

 val result =  for {
  _ <- putStrLn(s"Whats your name?")
  n <- readLn
  _ <- putStrLn(s"Hello, $n!")

  } yield ()




}
