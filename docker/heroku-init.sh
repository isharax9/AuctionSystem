#!/usr/bin/env bash
set -euo pipefail

HTTP_PORT="${PORT:-8080}"

asadmin start-domain domain1

cleanup() {
    asadmin stop-domain domain1 >/dev/null 2>&1 || true
}
trap cleanup EXIT

asadmin set \
    configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.port="${HTTP_PORT}"

if ! asadmin list-jms-resources | grep -qx 'jms/ConnectionFactory'; then
    asadmin create-jms-resource \
        --restype jakarta.jms.ConnectionFactory \
        jms/ConnectionFactory
fi

if ! asadmin list-jms-resources | grep -qx 'jms/topic/BidUpdates'; then
    asadmin create-jms-resource \
        --restype jakarta.jms.Topic \
        jms/topic/BidUpdates
fi

asadmin deploy \
    --force=true \
    --name AuctionSystem \
    /deploy/AuctionSystem.war

asadmin stop-domain domain1
trap - EXIT

