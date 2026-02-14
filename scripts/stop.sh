#!/bin/bash
# Stop Pi-Car on Raspberry Pi

PI_HOST="${PI_HOST:-iiddd@REMOVED_IP}"

echo "🛑 Stopping Pi-Car..."
ssh "$PI_HOST" "pkill -f 'server.KtorApplicationKt' 2>/dev/null && echo '✅ Stopped' || echo '⚠️ Not running'"

