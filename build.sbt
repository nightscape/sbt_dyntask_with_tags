import sbt._
import Keys._
import Def.Initialize

organization := "com.test.foo"

name := "validation"

version := "1.0.0"

scalaVersion := "2.11.1"

lazy val runAllValidations = taskKey[Seq[Unit]]("Runs all standard validations")

lazy val validations = settingKey[Seq[String]]("All standard validations")

def validationTaskFor(arguments: String): Initialize[Task[Unit]] =
  (runMain in Compile).toTask(s" com.test.foo.validation.RunValidation $arguments") tag(Tags.CPU)

def validationTasksFor(arguments: Seq[String]): Initialize[Task[Seq[Unit]]] = Def.taskDyn {
  arguments.map(validationTaskFor).joinWith(_.join)
}

validations := {
  val fromFile = IO.read(file("validation_configs.txt"))
  fromFile.split("\n").map(_.trim).toList
}

runAllValidations := Def.taskDyn { validationTasksFor(validations.value) }.value

concurrentRestrictions in Global := Seq(
  Tags.limit(Tags.CPU, 2)
)
