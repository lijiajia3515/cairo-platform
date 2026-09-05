db.getCollection("auth_app_user_login_log").drop();
db.createCollection("auth_app_user_login_log", {
    validator: {
        $jsonSchema: {
            title: "应用级用户登录日志",
            description: "应用级用户登录日志",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                logId: {
                    bsonType: "string",
                    title: "日志ID",
                    description: "必填，字符串，日志唯一标识"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                endpointId: {
                    bsonType: "string",
                    title: "终端ID",
                    description: "必填，字符串，所属终端的唯一标识"
                },
                clientId: {
                    bsonType: "string",
                    title: "客户端ID",
                    description: "必填，字符串，所属客户端的唯一标识"
                },
                userId: {
                    bsonType: "string",
                    title: "用户ID",
                    description: "必填，字符串，所属用户的唯一标识"
                },
                appUserTokenId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用级用户TokenId",
                    description: "必填（可为 null），字符串，应用级用户令牌唯一标识"
                },
                loginTime: {
                    bsonType: "date",
                    title: "登录时间",
                    description: "必填，日期时间，最后登录时间"
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
                success: {
                    bsonType: "bool",
                    title: "是否成功",
                    description: "必填，布尔值（true/false），操作成功为 true，失败为 false"
                },
                errMsg: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "错误原因",
                    description: "可空，字符串，失败时的错误信息"
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
                    description: "必填（可为 null），布尔值（true/false），true 表示手机端"
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
                "logId",
                "appId",
                "endpointId",
                "clientId",
                "userId",
                "appUserTokenId",
                "loginTime",
                "loginType",
                "success",
                "mobile"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_app_user_login_log").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1")
}, {
    name: "ix_appId_endpointId",
    background: true
});
db.getCollection("auth_app_user_login_log").createIndex({
    appId: NumberInt("1"),
    userId: NumberInt("1")
}, {
    name: "ix_appId_userId"
});
db.getCollection("auth_app_user_login_log").createIndex({
    logId: NumberInt("1")
}, {
    name: "ix_logId_unique",
    background: true,
    unique: true
});
