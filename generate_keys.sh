keytool -genkeypair \
  -alias my-server-key \
  -keyalg EC \
  -groupname secp256r1 \
  -sigalg SHA256withECDSA \
  -storetype PKCS12 \
  -keystore keystore.p12 \
  -validity 365
keytool -exportcert -alias my-server-key -keystore keystore.p12 -file server-public-cert.cer
keytool -importcert -alias my-server-key -file server-public-cert.cer -keystore client-truststore.p12