::#!
@echo off

for %%a in (*.xml, *.ini, *.txt) do (
  call scala -nocompdaemon -savecompiled "%~f0" "%%~fa" > "%%~fa.sorted"
)

goto :EOF
::!#

val filename = args.head
val lines = scala.io.Source.fromFile(filename).getLines.toIndexedSeq

val sorted =
  if (filename endsWith ".xml") {
    val marker = (_: String) startsWith "<setting";
    val marker_! = (ev: String) => !marker(ev)

    val head = lines takeWhile marker_!
    val (body, tail) = (lines dropWhile marker_!) partition marker

    head ++ body.sortBy(identity) ++ tail
  }
  else if (filename endsWith ".ini") {
    val marker = (_: String) startsWith "#"

    val (head, body) = lines partition marker

    head ++ body.sortBy(identity)
  }
  else if (filename endsWith ".txt") {
    lines.sortBy(identity)
  }
  else {
    IndexedSeq.empty
  }

print(sorted mkString("", "\n", "\n"))
