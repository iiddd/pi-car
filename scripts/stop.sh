#!/bin/bash
# Stop Pi-Car on Raspberry Pi
#
# Configuration via environment variables:
#   PI_HOST - SSH connection string (user@hostname)
#
# Example: PI_HOST=myuser@192.168.1.100 ./scripts/stop.sh

if [ -z "$PI_HOST" ]; then
    echo "❌ ERROR: PI_HOST environment variable is not set!"
    echo ""
    echo "Set it before running:"
    echo "  export PI_HOST=user@pi-hostname"
    echo ""
    echo "Or: source .env && ./scripts/stop.sh"
    exit 1
fi

echo "🛑 Stopping Pi-Car..."
ssh "$PI_HOST" "pkill -f 'server.KtorApplicationKt' 2>/dev/null && echo '✅ Stopped' || echo '⚠️ Not running'"

