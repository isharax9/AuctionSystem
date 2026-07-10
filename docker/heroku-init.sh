#!/usr/bin/env bash
set -euo pipefail

HTTP_PORT="${PORT:-8080}"
DOMAIN_XML="/opt/glassfish7/glassfish/domains/domain1/config/domain.xml"

# GlassFish defaults to a 512 MB heap, which leaves no native-memory headroom
# on a 512 MB Heroku Basic dyno. Apply the limit before the first JVM starts.
sed -i 's/-Xmx512m/-Xmx192m/g' "${DOMAIN_XML}"

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
