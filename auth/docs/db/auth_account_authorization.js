db.getCollection("auth_account_authorization").drop();
db.createCollection("auth_account_authorization", {
    validator: {
        $jsonSchema: {
            title: "账号会话",
            description: "账号会话",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                tokenId: {
                    bsonType: "string",
                    title: "会话ID",
                    description: "必填，字符串，令牌的唯一标识"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                accountId: {
                    bsonType: "string",
                    title: "账号ID",
                    description: "必填，字符串，所属账号的唯一标识"
                },
                accountName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "账号名称",
                    description: "可空，字符串，所属账号的名称"
                },
                loginType: {
                    bsonType: "string",
                    title: "登录方式",
                    description: "必填，字符串，登录类型"
                },
                snsType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "第三方认证类型",
                    description: "可空，字符串，第三方登录类型"
                },
                clientId: {
                    bsonType: "string",
                    title: "客户端ID",
                    description: "必填，字符串，所属客户端的唯一标识"
                },
                registeredClientId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "客户端ID",
                    description: "可空，字符串，注册客户端ID"
                },
                authorizationGrantType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "授权类型",
                    description: "可空，字符串"
                },
                authorizedScopes: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    title: "已授权的范围",
                    description: "可空，数组，已授权的范围集合"
                },
                accessToken: {
                    bsonType: [
                        "null",
                        "object"
                    ],
                    title: "访问令牌",
                    description: "可空，对象，访问令牌信息"
                },
                refreshToken: {
                    bsonType: [
                        "null",
                        "object"
                    ],
                    title: "刷新令牌",
                    description: "可空，对象，刷新令牌信息"
                },
                attributes: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "属性",
                    description: "可空，字符串，扩展属性"
                },
                status: {
                    bsonType: "string",
                    title: "状态",
                    description: "必填，字符串，参考 AccountAuthorizationStatus"
                },
                ip: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "客户端IP",
                    description: "可空，字符串，客户端IP地址"
                },
                region: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "区域",
                    description: "可空，字符串，客户端所属区域"
                },
                agent: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "原始agent",
                    description: "可空，字符串，原始 User-Agent"
                },
                os: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "操作系统",
                    description: "可空，字符串，客户端操作系统"
                },
                platform: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "平台",
                    description: "可空，字符串，客户端平台"
                },
                engine: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "引擎",
                    description: "可空，字符串，客户端引擎"
                },
                app: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "程序名称",
                    description: "可空，字符串，客户端程序名称"
                },
                mobile: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否手机端",
                    description: "可空，布尔值（true/false），true 表示手机端"
                },
                loginTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "登录时间",
                    description: "可空，日期时间，最后登录时间"
                },
                logoutTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "登出时间",
                    description: "可空，日期时间"
                },
                createTime: {
                    bsonType: "date",
                    title: "创建时间",
                    description: "必填，日期时间，记录创建时间"
                },
                updateTime: {
                    bsonType: "date",
                    title: "更新时间",
                    description: "必填，日期时间，记录最后更新时间"
                }
            },
            required: [
                "_id",
                "tokenId",
                "appId",
                "accountId",
                "loginType",
                "clientId",
                "status",
                "createTime",
                "updateTime"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_account_authorization").createIndex({
    tokenId: NumberInt("1")
}, {
    name: "ix_tokenId_unique",
    background: true,
    unique: true
});
