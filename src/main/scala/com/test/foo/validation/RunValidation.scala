package com.test.foo.validation

object RunValidation {
  def main(args: Array[String]) {
    val argString = args.mkString(",")
    println(s"Started with $argString")
    Thread.sleep(10000)
    println(s"Finished with $argString")
  }
}