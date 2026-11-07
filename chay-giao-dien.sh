#!/bin/sh
# Bấm đúp (hoặc chuột phải → Run as a Program) để chạy. Cần Java 17+ và Maven.
cd "$(dirname "$0")"
if [ ! -f target/restaurant-ordering.jar ]; then
  echo "Đang build lần đầu..."
  mvn -q package -DskipTests || { echo "Build lỗi"; read x; exit 1; }
fi
java -jar target/restaurant-ordering.jar --gui
