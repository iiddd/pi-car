#!/bin/bash
# Check Pi-Car status on Raspberry Pi
#
# Configuration via environment variables:
#   PI_HOST - SSH connection string (user@hostname)
#
# Example: PI_HOST=myuser@192.168.1.100 ./scripts/status.sh

if [ -z "$PI_HOST" ]; then
    echo "❌ ERROR: PI_HOST environment variable is not set!"
    echo ""
    echo "Set it before running:"
    echo "  export PI_HOST=user@pi-hostname"
    echo ""
    echo "Or: source .env && ./scripts/status.sh"
    exit 1
fi

PI_IP="${PI_HOST#*@}"

echo "🔍 Checking Pi-Car status..."

# Check if process is running
if ssh "$PI_HOST" "pgrep -f 'server.KtorApplicationKt'" > /dev/null 2>&1; then
    echo "✅ Pi-Car is running"
    echo ""
    echo "📡 Testing API..."
    curl -s "http://$PI_IP:8080/status" && echo ""
else
    echo "❌ Pi-Car is not running"
fi

