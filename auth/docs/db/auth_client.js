db.getCollection("auth_client").drop();
db.createCollection("auth_client", {
    validator: {
        $jsonSchema: {
            title: "客户端",
            description: "客户端",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                id: {
                    bsonType: "string",
                    title: "主键ID",
                    description: "必填，字符串"
                },
                appId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用ID",
                    description: "必填（可为 null），字符串，所属应用的唯一标识"
                },
                endpointId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "终端ID",
                    description: "必填（可为 null），字符串，所属终端 ID，为空时表示客户端不绑定特定终端"
                },
                clientId: {
                    bsonType: "string",
                    title: "客户端ID",
                    description: "必填，字符串，OAuth2 客户端 ID，用于客户端认证时标识身份，具有唯一索引"
                },
                clientSecret: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "OAuth2 客户端密钥",
                    description: "可空，字符串，用于 client_secret_basic / client_secret_post 等认证方式"
                },
                clientName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "客户端名称",
                    description: "必填（可为 null），字符串，用于管理后台展示和识别"
                },
                clientAuthenticationMethods: {
                    bsonType: "array",
                    items: {
                        bsonType: "string"
                    },
                    title: "客户端认证方式列表（如 client_secret_basic、client_secret_post、private_key_jwt 等）",
                    description: "必填，字符串数组"
                },
                authorizationGrantTypes: {
                    bsonType: "array",
                    items: {
                        bsonType: "string"
                    },
                    title: "授权类型列表（如 authorization_code、client_credentials、refresh_token 等）",
                    description: "必填，字符串数组"
                },
                scopes: {
                    bsonType: "array",
                    items: {
                        bsonType: "string"
                    },
                    title: "权限范围列表",
                    description: "必填，字符串数组，定义客户端可请求的资源访问权限"
                },
                redirectUris: {
                    bsonType: "array",
                    items: {
                        bsonType: "string"
                    },
                    title: "OAuth2 授权码回调地址白名单",
                    description: "必填，字符串数组"
                },
                authenticationTypes: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    title: "身份类型列表",
                    description: "可空，数组，用于区分客户端适用的认证场景（如密码、短信、社交等）"
                },
                accountSnsProviderIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    title: "账号第三方社交认证供应商 ID 列表",
                    description: "可空，数组，关联 auth_sns_provider 集合中的供应商"
                },
                clientSettings: {
                    bsonType: "object",
                    title: "客户端行为配置（PKCE、授权同意页、JWK 等）",
                    description: "必填，对象"
                },
                tokenSettings: {
                    bsonType: "object",
                    title: "各类令牌的格式、有效期及刷新策略配置",
                    description: "必填，对象"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），是否启用，禁用后客户端无法进行认证和授权"
                },
                loginTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "最近一次成功认证时间",
                    description: "可空，日期时间，为空表示从未使用过"
                },
                requireProofKey: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否要求 PKCE（Proof Key for Code Exchange），公开客户端应设为 true",
                    description: "可空，布尔值（true/false）"
                },
                requireAuthorizationConsent: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否要求用户授权同意确认，设为 true 时授权流程中会展示同意页面",
                    description: "可空，布尔值（true/false）"
                },
                jwkSetUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "JSON Web Key Set URL，用于 private_key_jwt 认证方式时获取客户端公钥",
                    description: "可空，字符串"
                },
                tokenEndpointAuthenticationSigningAlgorithm: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Token 端点认证签名算法（如 RS256），用于 private_key_jwt 认证方式",
                    description: "可空，字符串"
                },
                idTokenSignatureAlgorithm: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "ID Token 签名算法（如 RS256、ES256）",
                    description: "可空，字符串"
                },
                idTokenFormat: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "ID Token 格式（如 self-contained JWT）",
                    description: "可空，字符串"
                },
                idTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "ID Token 有效期",
                    description: "可空，字符串"
                },
                accessTokenFormat: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Access Token 格式（如 self-contained JWT 或 opaque 不透明令牌）",
                    description: "可空，字符串"
                },
                accessTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Access Token 有效期",
                    description: "可空，字符串"
                },
                refreshTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Refresh Token 有效期",
                    description: "可空，字符串"
                },
                reuseRefreshTokens: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "true 表示同一个 Refresh Token 可多次使用，false 表示每次刷新后旧 Token 失效",
                    description: "可空，布尔值（true/false）"
                },
                accountAccessTokenFormat: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Account Access Token 格式",
                    description: "可空，字符串"
                },
                accountAccessTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Account Access Token 有效期",
                    description: "可空，字符串"
                },
                accountRefreshTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Account Refresh Token 有效期",
                    description: "可空，字符串"
                },
                reuseAccountRefreshTokens: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否允许重用 Account Refresh Token 刷新 Access Token",
                    description: "可空，布尔值（true/false）"
                },
                appUserAccessTokenFormat: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "App Endpoint User Access Token 格式",
                    description: "可空，字符串"
                },
                appUserAccessTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "App Endpoint User Access Token 有效期",
                    description: "可空，字符串"
                },
                appUserRefreshTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "App Endpoint User Refresh Token 有效期",
                    description: "可空，字符串"
                },
                reuseAppUserRefreshTokens: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否允许重用 App Endpoint User Refresh Token 刷新 Access Token",
                    description: "可空，布尔值（true/false）"
                },
                tenantAppUserAccessTokenFormat: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Tenant App Endpoint User Access Token 格式",
                    description: "可空，字符串"
                },
                tenantAppUserAccessTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Tenant App Endpoint User Access Token 有效期",
                    description: "可空，字符串"
                },
                tenantAppUserRefreshTokenTimeToLive: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "Tenant App Endpoint User Refresh Token 有效期",
                    description: "可空，字符串"
                },
                reuseTenantAppUserRefreshTokens: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否允许重用 Tenant App Endpoint User Refresh Token 刷新 Access Token",
                    description: "可空，布尔值（true/false）"
                },
                metadata: {
                    bsonType: "object",
                    title: "元信息",
                    description: "元信息，记录创建与更新的操作者及时间",
                    properties: {
                        createTime: {
                            bsonType: "date",
                            title: "创建时间",
                            description: "必填，日期时间，记录创建时间"
                        },
                        updateTime: {
                            bsonType: "date",
                            title: "更新时间",
                            description: "必填，日期时间，记录最后更新时间"
                        },
                        createUserId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "创建用户ID",
                            description: "必填（可为 null），字符串，创建该记录的用户ID"
                        },
                        updateUserId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "更新用户ID",
                            description: "必填（可为 null），字符串，最后更新该记录的用户ID"
                        }
                    },
                    required: [
                        "createUserId",
                        "createTime",
                        "updateUserId",
                        "updateTime"
                    ]
                }
            },
            required: [
                "_id",
                "id",
                "appId",
                "endpointId",
                "clientId",
                "clientName",
                "clientAuthenticationMethods",
                "authorizationGrantTypes",
                "scopes",
                "redirectUris",
                "clientSettings",
                "tokenSettings",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_client").createIndex({
    clientId: NumberInt("1")
}, {
    name: "ix_clientId_unique",
    background: true,
    unique: true
});
db.getCollection("auth_client").createIndex({
    id: NumberInt("-1")
}, {
    name: "ix_id_unique",
    background: true,
    unique: true
});
