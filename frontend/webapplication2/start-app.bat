@echo off
cd /d %~dp0\code
start http://localhost:3000
npm start
pause
