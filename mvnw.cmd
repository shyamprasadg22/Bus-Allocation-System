@echo off
set DIR=%~dp0
if exist "%DIR%.tools\apache-maven-3.9.6\bin\mvn.cmd" (
    "%DIR%.tools\apache-maven-3.9.6\bin\mvn.cmd" %*
) else (
    mvn %*
)
