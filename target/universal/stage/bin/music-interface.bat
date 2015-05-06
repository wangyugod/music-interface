@REM music-interface launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM MUSIC_INTERFACE_config.txt found in the MUSIC_INTERFACE_HOME.
@setlocal enabledelayedexpansion

@echo off

if "%MUSIC_INTERFACE_HOME%"=="" set "MUSIC_INTERFACE_HOME=%~dp0\\.."

set "APP_LIB_DIR=%MUSIC_INTERFACE_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%MUSIC_INTERFACE_HOME%\MUSIC_INTERFACE_config.txt"
set CFG_OPTS=
if exist %CFG_FILE% (
  FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%CFG_FILE%") DO (
    set DO_NOT_REUSE_ME=%%i
    rem ZOMG (Part #2) WE use !! here to delay the expansion of
    rem CFG_OPTS, otherwise it remains "" for this loop.
    set CFG_OPTS=!CFG_OPTS! !DO_NOT_REUSE_ME!
  )
)

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running music-interface.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)


rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "!_JAVA_OPTS!"=="" set _JAVA_OPTS=!CFG_OPTS!

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=
set _APP_ARGS=

:param_loop
call set _PARAM1=%%1
set "_TEST_PARAM=%~1"

if ["!_PARAM1!"]==[""] goto param_afterloop


rem ignore arguments that do not start with '-'
if "%_TEST_PARAM:~0,1%"=="-" goto param_java_check
set _APP_ARGS=!_APP_ARGS! !_PARAM1!
shift
goto param_loop

:param_java_check
if "!_TEST_PARAM:~0,2!"=="-J" (
  rem strip -J prefix
  set _JAVA_PARAMS=!_JAVA_PARAMS! !_TEST_PARAM:~2!
  shift
  goto param_loop
)

if "!_TEST_PARAM:~0,2!"=="-D" (
  rem test if this was double-quoted property "-Dprop=42"
  for /F "delims== tokens=1,*" %%G in ("!_TEST_PARAM!") DO (
    if not ["%%H"] == [""] (
      set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
    ) else if [%2] neq [] (
      rem it was a normal property: -Dprop=42 or -Drop="42"
      call set _PARAM1=%%1=%%2
      set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
      shift
    )
  )
) else (
  if "%1"=="-main" (
    set CUSTOM_MAIN_CLASS=%2
    shift
  ) else (
    set _APP_ARGS=!_APP_ARGS! !_PARAM1!
  )
)
shift
goto param_loop
:param_afterloop

set _JAVA_OPTS=!_JAVA_OPTS! !_JAVA_PARAMS!
:run
 
set "APP_CLASSPATH=%APP_LIB_DIR%\infothunder.reactive.music-interface-0.2.jar;%APP_LIB_DIR%\commons-httpclient-3.0.jar;%APP_LIB_DIR%\commons-io-2.4.jar;%APP_LIB_DIR%\commons-logging.jar;%APP_LIB_DIR%\httpclient-4.3.5.jar;%APP_LIB_DIR%\httpcore-4.3.2.jar;%APP_LIB_DIR%\ojdbc14.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.10.4.jar;%APP_LIB_DIR%\io.spray.spray-json_2.10-1.3.0.jar;%APP_LIB_DIR%\io.spray.spray-can-1.3.1.jar;%APP_LIB_DIR%\io.spray.spray-io-1.3.1.jar;%APP_LIB_DIR%\io.spray.spray-util-1.3.1.jar;%APP_LIB_DIR%\io.spray.spray-http-1.3.1.jar;%APP_LIB_DIR%\org.parboiled.parboiled-scala_2.10-1.1.6.jar;%APP_LIB_DIR%\org.parboiled.parboiled-core-1.1.6.jar;%APP_LIB_DIR%\io.spray.spray-routing-1.3.1.jar;%APP_LIB_DIR%\io.spray.spray-httpx-1.3.1.jar;%APP_LIB_DIR%\org.jvnet.mimepull.mimepull-1.9.4.jar;%APP_LIB_DIR%\com.chuusai.shapeless_2.10-1.2.4.jar;%APP_LIB_DIR%\com.typesafe.akka.akka-actor_2.10-2.3.5.jar;%APP_LIB_DIR%\com.typesafe.config-1.2.1.jar;%APP_LIB_DIR%\org.json4s.json4s-native_2.10-3.2.4.jar;%APP_LIB_DIR%\org.json4s.json4s-core_2.10-3.2.4.jar;%APP_LIB_DIR%\org.json4s.json4s-ast_2.10-3.2.4.jar;%APP_LIB_DIR%\com.thoughtworks.paranamer.paranamer-2.5.2.jar;%APP_LIB_DIR%\org.scala-lang.scalap-2.10.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-compiler-2.10.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-reflect-2.10.0.jar;%APP_LIB_DIR%\c3p0.c3p0-0.9.1.2.jar"
set "APP_MAIN_CLASS=infothunder.reactive.ReactiveSystem"

if defined CUSTOM_MAIN_CLASS (
    set MAIN_CLASS=!CUSTOM_MAIN_CLASS!
) else (
    set MAIN_CLASS=!APP_MAIN_CLASS!
)

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" !_JAVA_OPTS! !MUSIC_INTERFACE_OPTS! -cp "%APP_CLASSPATH%" %MAIN_CLASS% !_APP_ARGS!
if ERRORLEVEL 1 goto error
goto end

@endlocal


:end

exit /B %ERRORLEVEL%