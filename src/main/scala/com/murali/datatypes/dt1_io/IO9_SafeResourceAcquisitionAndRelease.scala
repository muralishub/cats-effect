package com.murali.datatypes.dt1_io

import cats.effect.ExitCase.{Canceled, Completed, Error}
import cats.effect.{ContextShift, IO}

import java.io.{BufferedReader, File, FileReader}
import scala.concurrent.ExecutionContext

object IO9_SafeResourceAcquisitionAndRelease extends App{

  //lets look at imprerative version of resource handling

  def readFirstLineInJava(file: File): String =  {
   val in = new BufferedReader(new FileReader(file))
    try {
      in.readLine()
    }
    finally {
      in.close()
    }
  }

// this side-effectful computation is not suitable FP abstractions


  // we can use bracket for this

  def readFirstLine(file: File): IO[String] = {
    IO(new BufferedReader(new FileReader(file))).bracket { in =>
      IO(in.readLine())
    }{ in =>
      IO(in.close()).void
    }
  }


  // consider this
  implicit val contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  def readFile2(file: File): IO[String] = {
    // Opens file with an asynchronous boundary before it,
    // ensuring that processing doesn't block the "current thread"
    val acquire = IO.shift *> IO(new BufferedReader(new FileReader(file)))

    acquire.bracket { in =>
      // Usage (the try block)
      IO {
        // Ugly, low-level Java code warning!
        val content = new StringBuilder()
        var line: String = null
        do {
          line = in.readLine()
          if (line != null) content.append(line)
        } while (line != null)
        content.toString()
      }
    } { in =>
      // Releasing the reader (the finally block)
      // This is problematic if the resulting `IO` can get
      // canceled, because it can lead to data corruption
      IO(in.close()).void
    }
  }


  // this can be slow for a large file , because JVM is capable of multithreading , IO.close can cause data corruption issues
// depending on use case we could use synchronization to prevent it

  def readFileSync(file: File): IO[String] = {
    // Opens file with an asynchronous boundary before it,
    // ensuring that processing doesn't block the "current thread"
    val acquire = IO.shift *> IO(new BufferedReader(new FileReader(file)))

    // Suspended execution because we are going to mutate
    // a shared variable
    IO.suspend {
      // Shared state meant to signal cancellation
      var isCanceled = false

      acquire.bracket { in =>
        IO {
          val content = new StringBuilder()
          var line: String = null
          do {
            // Synchronized access to isCanceled and to the reader
            line = in.synchronized {
              if (!isCanceled)
                in.readLine()
              else
                null
            }
            if (line != null) content.append(line)
          } while (line != null)
          content.toString()
        }
      } { in =>
        IO {
          // Synchronized access to isCanceled and to the reader
          in.synchronized {
            isCanceled = true
            in.close()
          }
        }.void
      }
    }
  }


// using Bracket case example to distinguish between
  // successfull completion
  //completion in error
    //cancellation


  def readLine(in: BufferedReader): IO[String] =
    IO.pure(in).bracketCase { in =>
      IO(in.readLine())
    } {
      case (_, Completed | Error(_)) =>
        // Do nothing
        IO.unit
      case (in, Canceled) =>
        IO(in.close())
    }









}
