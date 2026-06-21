@echo off
SET JAVA_HOME=C:\Program Files\Android\Android Studio1\jbr
SET PATH=%JAVA_HOME%\bin;%PATH%
java -version
call gradlew.bat assembleDebug
