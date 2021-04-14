package com.murali.datatypes.dt1_io

import cats.effect.IO

object IO2_RT_AndLazyEvaluation extends App{

  for {
    _ <-  addToBasket(1)
    _ <-  addToBasket(1)
  } yield ()

 // because both statements in a for compression are the same cant assign to a val

  val addToBasket  = (i: Int) =>  Some(10 + i)

  for {
    _ <-  addToBasket(1)
    _ <-  addToBasket(1)
  } yield ()


  // lazyness goes hand in hand with RT



  for {
    _ <- IO()
    _ <- IO()
  } yield ()

// because its RT we can rewrite the following
  val task = IO()

  for {
    _ <- task
    _ <- task
  } yield ()







}
