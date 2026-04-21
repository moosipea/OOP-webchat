keytool -genkeypair -alias my-server-key -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore keystore.p12 -validity 365
keytool -exportcert -alias my-server-key -keystore keystore.p12 -file server-public-cert.cer
keytool -importcert -alias my-server-key -file server-public-cert.cer -keystore client-truststore.p12