@echo off
set SPRING_PROFILES_ACTIVE=dev
cd /d C:\Users\PC\IdeaProjects\ses\isemid-demo
java -jar target\app.jar --server.port=18090 >> logs\app-run.log 2>> logs\app-run-err.log
