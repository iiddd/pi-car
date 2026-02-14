#!/bin/bash
# Pi-Car Deployment Script
# Deploys and runs the application on Raspberry Pi

PI_HOST="${PI_HOST:-iiddd@REMOVED_IP}"
PI_PATH="${PI_PATH:-/home/iiddd/IdeaProjects/pi-car}"
DEBUG_MODE="${1:-}"

set -e

echo "🚀 Building Pi-Car..."
cd "$(dirname "$0")/.."
./gradlew installDist --console=plain -q

echo "📦 Deploying to $PI_HOST..."
# Stop existing instance
ssh "$PI_HOST" "pkill -f 'server.KtorApplicationKt' 2>/dev/null || true"

# Create directory if needed and sync files
ssh "$PI_HOST" "mkdir -p $PI_PATH"
rsync -az --delete build/install/pi-car/ "$PI_HOST:$PI_PATH/"

echo "🏃 Starting Pi-Car on Raspberry Pi..."
if [ "$DEBUG_MODE" = "--debug" ] || [ "$DEBUG_MODE" = "-d" ]; then
    echo "🐛 Debug mode enabled on port 5005"
    echo "   Create 'Remote JVM Debug' configuration in IntelliJ:"
    echo "   - Host: REMOVED_IP"
    echo "   - Port: 5005"
    ssh "$PI_HOST" "cd $PI_PATH && java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -cp 'lib/*' server.KtorApplicationKt"
else
    ssh "$PI_HOST" "cd $PI_PATH && ./bin/pi-car"
fi

