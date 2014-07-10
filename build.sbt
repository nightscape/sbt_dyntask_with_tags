import sbt._
import Keys._
import Def.Initialize

organization := "com.test.foo"

name := "validation"

version := "1.0.0"

scalaVersion := "2.11.1"

def dummyTaskGen(name: String): Initialize[Task[Unit]] = Def.task {
   System.err.println(s"Started  ${name}")
   Thread.sleep(1000L*6)
   System.err.println(s"Finished ${name}")
}


lazy val validations = taskKey[Unit]("Run everything, one at a time.")

lazy val names = settingKey[Seq[String]]("All standard validations")

def validationTaskFor(arguments: String): Initialize[Task[Unit]] =
  (runMain in Compile).toTask(s" com.test.foo.validation.RunValidation $arguments")


names := {
  val fromFile = IO.read(file("validation_configs.txt"))
  fromFile.split("\n").map(_.trim).toList
}

lazy val allRuns: Def.Initialize[Task[Unit]] = Def.settingDyn {
  val zero: Def.Initialize[Seq[Task[Unit]]] = Def.setting {  Seq(task(())) }
  names.value.map(dummyTaskGen).foldLeft(zero) { (acc, current) =>
  // Does not work: names.value.map(validationTaskFor).foldLeft(zero) { (acc, current) =>
     acc.zipWith(current) {  case (taskSeq, task) =>
       taskSeq :+ task.tag(Tags.CPU)
     }
  } apply { tasks: Seq[Task[Unit]] =>
    tasks.join map { seq => () /* Ignore the sequence of unit returned */ }
  }
}

validations := allRuns.value

concurrentRestrictions in Global ++= Seq(
  Tags.limit(Tags.CPU, 2)
)
