#!/usr/bin/env bash
set -euo pipefail

HTTP_PORT="${PORT:-8080}"
DOMAIN_XML="/opt/glassfish7/glassfish/domains/domain1/config/domain.xml"

# GlassFish defaults to a 512 MB heap, which leaves no native-memory headroom
# on a 512 MB Heroku Basic dyno. Apply the limit before the first JVM starts.
sed -i 's/-Xmx512m/-Xmx192m/g' "${DOMAIN_XML}"
sed -E -i \
    '/<network-listener protocol="http-listener-1"/ s/port="[0-9]+"/port="'"${HTTP_PORT}"'"/' \
    "${DOMAIN_XML}"
