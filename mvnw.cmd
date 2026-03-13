@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.2
@REM
@REM Optional ENV vars:
@REM   JAVA_HOME - location of a JDK home dir, required when type is not auto
@REM   MVNW_REPOURL - repo url base for downloading maven distribution
@REM   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM   MVNW_VERBOSE - true: enable verbose log; others: silence the output
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET "BASE_DIR=%~dp0") ELSE SET "BASE_DIR=%__MVNW_ARG0_NAME__%"

@SET MAVEN_PROJECTBASEDIR=%MAVEN_BASEDIR%
@IF NOT "%MAVEN_PROJECTBASEDIR%"=="" GOTO endDetectBaseDir

@SET EXEC_DIR=%CD%
@SET WDIR=%EXEC_DIR%
:findBaseDir
@IF EXIST "%WDIR%"\.mvn GOTO baseDirFound
@CD ..
@IF "%WDIR%"=="%CD%" GOTO baseDirNotFound
@SET "WDIR=%CD%"
@GOTO findBaseDir

:baseDirFound
SET MAVEN_PROJECTBASEDIR=%WDIR%
@CD "%EXEC_DIR%"
@GOTO endDetectBaseDir

:baseDirNotFound
SET MAVEN_PROJECTBASEDIR=%EXEC_DIR%
@CD "%EXEC_DIR%"

:endDetectBaseDir

@IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties" (
  @ECHO "No .mvn\wrapper\maven-wrapper.properties found; cannot continue."
  @EXIT /B 1
)

@SET MVNW_VERBOSE=false
@IF "%MVNW_VERBOSE%"=="true" (
  @ECHO # ------------------------------------------------------------------- #
  @ECHO # Maven wrapper properties:                                            #
  @ECHO # -------------------------------------------------------------------  #
  @TYPE "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"
  @ECHO # ------------------------------------------------------------------- #
  @ECHO.
)

@FOR /F "usebackq tokens=1,2 delims==" %%a IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO @(
  @IF "%%a"=="distributionUrl" SET distributionUrl=%%b
  @IF "%%a"=="distributionSha256Sum" SET distributionSha256Sum=%%b
)

@IF "%distributionUrl%"=="" (
  @ECHO "distributionUrl not set in .mvn\wrapper\maven-wrapper.properties"
  @EXIT /B 1
)

@SET "MAVEN_DISTRIBUTION_NAME=%distributionUrl:~-22%"
@FOR /F "tokens=1* delims=/" %%a IN ("%distributionUrl%") DO @SET "MAVEN_DISTRIBUTION_NAME_SHORT=%%b"

@SET MAVEN_USER_HOME=%USERPROFILE%\.m2
@IF NOT "%MAVEN_USER_HOME%"=="" GOTO skipMavenUserHome
@SET MAVEN_USER_HOME=%USERPROFILE%\.m2
:skipMavenUserHome

@SET MAVEN_HOME=%MAVEN_USER_HOME%\wrapper\dists
@IF NOT EXIST "%MAVEN_HOME%" (
  @MKDIR "%MAVEN_HOME%"
)

@FOR /F "usebackq tokens=1,2 delims==" %%a IN ("%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties") DO @(
  @IF "%%a"=="distributionUrl" (
    @SET distributionUrl=%%b
  )
)

@SET DISTRIBUTION_NAME_FULL=%distributionUrl:~-22%
@FOR /F "tokens=*" %%i IN ('powershell -Command "[System.IO.Path]::GetFileNameWithoutExtension('%distributionUrl%'.Split('/')[-1]).TrimEnd('-bin')"') DO SET MAVEN_DISTRIBUTION_NAME_MAIN=%%i

@SET MAVEN_UNZIP_DIR=%MAVEN_HOME%\%MAVEN_DISTRIBUTION_NAME_MAIN%

@IF EXIST "%MAVEN_UNZIP_DIR%\bin\mvn.cmd" (
  @SET "MAVEN_EXEC=%MAVEN_UNZIP_DIR%\bin\mvn.cmd"
  @GOTO execute
)

@ECHO Downloading Apache Maven to: %MAVEN_UNZIP_DIR%
@ECHO From: %distributionUrl%

@SET DOWNLOAD_FILE=%TEMP%\maven-wrapper-download-%RANDOM%.zip
@powershell -Command "& {$wc = New-Object System.Net.WebClient; if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) { $wc.Credentials = New-Object System.Net.NetworkCredential($env:MVNW_USERNAME, $env:MVNW_PASSWORD) }; $wc.DownloadFile('%distributionUrl%', '%DOWNLOAD_FILE%')}"
@IF ERRORLEVEL 1 (
  @ECHO Could not download %distributionUrl%
  @EXIT /B 1
)

@ECHO Extracting to: %MAVEN_UNZIP_DIR%
@powershell -Command "Expand-Archive -Path '%DOWNLOAD_FILE%' -DestinationPath '%MAVEN_HOME%' -Force"
@IF ERRORLEVEL 1 (
  @ECHO Failed to extract Maven distribution
  @EXIT /B 1
)
@DEL "%DOWNLOAD_FILE%"

@SET "MAVEN_EXEC=%MAVEN_UNZIP_DIR%\bin\mvn.cmd"

:execute
@IF NOT EXIST "%MAVEN_EXEC%" (
  @ECHO Maven executable not found at: %MAVEN_EXEC%
  @EXIT /B 1
)

@SET MAVEN_OPTS=%MAVEN_OPTS%
@"%MAVEN_EXEC%" %*
