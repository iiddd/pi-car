#!/bin/bash
# Pi-Car Deployment Script
# Deploys and runs the application on Raspberry Pi
#
# Configuration via environment variables:
#   PI_HOST - SSH connection string (user@hostname)
#   PI_PATH - Deployment path on Pi
#
# Example usage:
#   PI_HOST=myuser@192.168.1.100 ./scripts/deploy.sh
#   source .env && ./scripts/deploy.sh

if [ -z "$PI_HOST" ]; then
    echo "❌ ERROR: PI_HOST environment variable is not set!"
    echo ""
    echo "Set it before running:"
    echo "  export PI_HOST=user@pi-hostname"
    echo ""
    echo "Or create a .env file (see .env.example) and run:"
    echo "  source .env && ./scripts/deploy.sh"
    exit 1
fi

PI_PATH="${PI_PATH:-/home/$(echo $PI_HOST | cut -d@ -f1)/IdeaProjects/pi-car}"
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

