#!/bin/bash
# Check Pi-Car status on Raspberry Pi

PI_HOST="${PI_HOST:-iiddd@REMOVED_IP}"
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

