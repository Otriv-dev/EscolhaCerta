#!/bin/sh
set -eu

if [ -f /etc/secrets/ca.pem ]; then
  rm -f /tmp/aiven-truststore.p12
  keytool -importcert -noprompt \
    -alias aiven-ca \
    -file /etc/secrets/ca.pem \
    -keystore /tmp/aiven-truststore.p12 \
    -storetype PKCS12 \
    -storepass changeit
fi

exec java -jar /app/app.jar
