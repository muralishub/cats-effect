package com.murali.datatypes.dt1_io

import cats.effect.IO

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import java.time.format.DateTimeFormatter
import scala.concurrent.duration.DurationInt

object IO1_Introduction extends App{

  val writeToDatabase  = Future{println("I am executed at" + LocalDateTime.now())}

  for {
   _ <- writeToDatabase
   _ <- writeToDatabase
   } yield ()

   for {
    _ <- Future{println("I am executed at" + LocalDateTime.now())}
    _ <- Future{println("I am executed at" + LocalDateTime.now())}
  } yield ()

  // so about value writeToDatabase is not referentially transparent

  // lets make it RT using IO

  val writeToDatabaseIO  = IO{println("IO is executed at " + LocalDateTime.now())}

  val result = for {
    _ <- writeToDatabaseIO
    _ <- writeToDatabaseIO
  } yield ()


  val result2 = for {
    _ <- IO{println("IO is executed at " + LocalDateTime.now())}
    _ <- IO{println("IO is executed at " + LocalDateTime.now())}
  } yield ()



  result.unsafeRunSync()
  result2.unsafeRunSync()



}
