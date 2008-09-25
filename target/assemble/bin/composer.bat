@REM ----------------------------------------------------------------------------
@REM Copyright 2001-2004 The Apache Software Foundation.
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM      http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM ----------------------------------------------------------------------------
@REM

@echo off

set ERROR_CODE=0

:init
@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" @setlocal

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set CMD_LINE_ARGS=%*
goto WinNTGetScriptDir

@REM The 4NT Shell from jp software
:4NTArgs
set CMD_LINE_ARGS=%$
goto WinNTGetScriptDir

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto Win9xGetScriptDir
set CMD_LINE_ARGS=%CMD_LINE_ARGS% %1
shift
goto Win9xApp

:Win9xGetScriptDir
set SAVEDIR=%CD%
%0\
cd %0\..\.. 
set BASEDIR=%CD%
cd %SAVEDIR%
set SAVE_DIR=
goto repoSetup
 
:WinNTGetScriptDir
set BASEDIR=%~dp0\..
 
:repoSetup


if "%REPO%"=="" set REPO=%BASEDIR%\repo

set CLASSPATH="%BASEDIR%"\etc;"%REPO%"\org\apache\xmlbeans\xmlbeans\2.3.0\xmlbeans-2.3.0.jar;"%REPO%"\commons-logging\commons-logging\1.1.1\commons-logging-1.1.1.jar;"%REPO%"\commons-collections\commons-collections\2.1\commons-collections-2.1.jar;"%REPO%"\org\mortbay\jetty\jetty\6.1.7\jetty-6.1.7.jar;"%REPO%"\aspectj\aspectjweaver\1.5.3\aspectjweaver-1.5.3.jar;"%REPO%"\stax\stax-api\1.0.1\stax-api-1.0.1.jar;"%REPO%"\com\hp\hpl\jena\iri\0.5\iri-0.5.jar;"%REPO%"\smack\smackx\3.0.4\smackx-3.0.4.jar;"%REPO%"\net\sourceforge\jtds\jtds\1.2\jtds-1.2.jar;"%REPO%"\db4o\db4o\7.2\db4o-7.2.jar;"%REPO%"\commons-cli\commons-cli\1.1\commons-cli-1.1.jar;"%REPO%"\com\hp\hpl\jena\arq\2.3\arq-2.3.jar;"%REPO%"\commons-dbcp\commons-dbcp\1.2.1\commons-dbcp-1.2.1.jar;"%REPO%"\com\hp\hpl\jena\jena\2.5.6\jena-2.5.6.jar;"%REPO%"\commons-httpclient\commons-httpclient\3.1\commons-httpclient-3.1.jar;"%REPO%"\com\cafepress\logging\cperrorlog\1.1.2\cperrorlog-1.1.2.jar;"%REPO%"\javax\activation\activation\1.1\activation-1.1.jar;"%REPO%"\com\hp\hpl\jena\concurrent-jena\1.3.2\concurrent-jena-1.3.2.jar;"%REPO%"\xerces\xercesImpl\2.7.1\xercesImpl-2.7.1.jar;"%REPO%"\itext\itext\2.0.4\itext-2.0.4.jar;"%REPO%"\commons-codec\commons-codec\1.3\commons-codec-1.3.jar;"%REPO%"\org\composer\composer\0.4\composer-0.4.jar;"%REPO%"\cglib\cglib-nodep\2.1_3\cglib-nodep-2.1_3.jar;"%REPO%"\smack\smack\3.0.4\smack-3.0.4.jar;"%REPO%"\xerces\xmlParserAPIs\2.0.2\xmlParserAPIs-2.0.2.jar;"%REPO%"\javax\mail\mail\1.4\mail-1.4.jar;"%REPO%"\com\ibm\icu\icu4j\3.4.4\icu4j-3.4.4.jar;"%REPO%"\antlr\antlr\2.7.5\antlr-2.7.5.jar;"%REPO%"\org\mortbay\jetty\servlet-api-2.5\6.1.7\servlet-api-2.5-6.1.7.jar;"%REPO%"\org\springframework\spring\2.5\spring-2.5.jar;"%REPO%"\xml-apis\xml-apis\1.0.b2\xml-apis-1.0.b2.jar;"%REPO%"\org\mortbay\jetty\jetty-util\6.1.7\jetty-util-6.1.7.jar;"%REPO%"\aspectj\aspectjrt\1.5.3\aspectjrt-1.5.3.jar;"%REPO%"\commons-pool\commons-pool\1.2\commons-pool-1.2.jar;"%REPO%"\log4j\log4j\1.2.14\log4j-1.2.14.jar;"%REPO%"\jpox\jpox-core\1.2.2\jpox-core-1.2.2.jar;"%REPO%"\jpox\jpox-enhancer\1.2.2\jpox-enhancer-1.2.2.jar
set EXTRA_JVM_ARGUMENTS=-Xms128M -Xmx256M -Dpsb.config=config/composer.properties
goto endInit

@REM Reaching here means variables are defined and arguments have been captured
:endInit

java %JAVA_OPTS% %EXTRA_JVM_ARGUMENTS% -classpath %CLASSPATH_PREFIX%;%CLASSPATH% -Dapp.name="composer" -Dapp.repo="%REPO%" -Dbasedir="%BASEDIR%" org.composer.Main %CMD_LINE_ARGS%
if ERRORLEVEL 1 goto error
goto end

:error
if "%OS%"=="Windows_NT" @endlocal
set ERROR_CODE=1

:end
@REM set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" goto endNT

@REM For old DOS remove the set variables from ENV - we assume they were not set
@REM before we started - at least we don't leave any baggage around
set CMD_LINE_ARGS=
goto postExec

:endNT
@endlocal

:postExec

if "%FORCE_EXIT_ON_ERROR%" == "on" (
  if %ERROR_CODE% NEQ 0 exit %ERROR_CODE%
)

exit /B %ERROR_CODE%
