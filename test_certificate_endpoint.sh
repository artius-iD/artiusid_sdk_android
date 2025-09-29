#!/bin/bash

echo "Testing Certificate Endpoint Connectivity"
echo "========================================"

# Test the certificate endpoint
CERT_URL="https://sandbox.registration.artiusid.dev/LoadCertificateFunction"
echo "Testing: $CERT_URL"

# Test basic connectivity
echo -n "Basic connectivity test: "
if curl -s --connect-timeout 10 --max-time 30 -o /dev/null -w "%{http_code}" "$CERT_URL" | grep -q "200\|400\|401\|403\|404\|405"; then
    echo "✅ Endpoint is reachable"
else
    echo "❌ Endpoint is not reachable"
fi

# Test with a sample POST request (like the app would send)
echo -n "POST request test: "
HTTP_CODE=$(curl -s --connect-timeout 10 --max-time 30 \
    -X POST \
    -H "Content-Type: application/json" \
    -H "User-Agent: ArtiusID-Android" \
    -d '{"deviceId":"test-device","csr":"test-csr"}' \
    -o /dev/null \
    -w "%{http_code}" \
    "$CERT_URL")

echo "HTTP Status: $HTTP_CODE"

if [[ "$HTTP_CODE" =~ ^[45][0-9][0-9]$ ]]; then
    echo "✅ Endpoint accepts requests (4xx/5xx expected for test data)"
elif [[ "$HTTP_CODE" =~ ^2[0-9][0-9]$ ]]; then
    echo "✅ Endpoint accepts requests (2xx - unexpected but good)"
else
    echo "❌ Endpoint connection failed"
fi

# Test verification endpoint
echo ""
VERIF_URL="https://sandbox.mobile.artiusid.dev/verifi/api/verification"
echo "Testing: $VERIF_URL"

echo -n "Verification endpoint test: "
HTTP_CODE=$(curl -s --connect-timeout 10 --max-time 30 \
    -X POST \
    -H "Content-Type: application/json" \
    -H "User-Agent: ArtiusID-Android" \
    -o /dev/null \
    -w "%{http_code}" \
    "$VERIF_URL")

echo "HTTP Status: $HTTP_CODE"

if [[ "$HTTP_CODE" =~ ^[45][0-9][0-9]$ ]]; then
    echo "✅ Endpoint accepts requests (4xx/5xx expected for test data)"
elif [[ "$HTTP_CODE" =~ ^2[0-9][0-9]$ ]]; then
    echo "✅ Endpoint accepts requests (2xx - unexpected but good)"
else
    echo "❌ Endpoint connection failed"
fi

echo ""
echo "Test completed. If endpoints show 4xx/5xx, that's expected for test data."
echo "Connection failures (timeouts, DNS issues) would indicate real problems."
