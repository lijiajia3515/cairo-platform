# oauth-jwk 密钥对

OAuth2 令牌签名的 RSA 密钥对存放处。**私钥严禁入库**，本目录除本说明外均被 `.gitignore` 排除。

首次使用前生成并放置：

```bash
# PKCS#8 DER 格式（配置里以 base64 使用）
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out default.key -outform DER
openssl pkey -in default.key -inform DER -pubout -out default.pem -outform PEM
```

或直接在配置中注入 base64 密钥（见 `cairo.auth.oauth2.rsa-keys`，支持 `${CAIRO_OAUTH2_RSA_PRIVATE_KEY:}` 环境变量）。

轮换密钥后已签发的 JWT 立即失效，客户端需重新获取令牌。
