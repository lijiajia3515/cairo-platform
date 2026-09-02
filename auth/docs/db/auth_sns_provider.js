db.getCollection("auth_sns_provider").drop();
db.createCollection("auth_sns_provider", {
    validator: {
        $jsonSchema: {
            title: "第三方认证提供方",
            description: "第三方认证提供方",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                appId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用ID",
                    description: "可空，字符串，所属应用的唯一标识"
                },
                snsProviderId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "第三方认证提供商ID",
                    description: "必填（可为 null），字符串，所属第三方登录提供者的唯一标识"
                },
                snsProviderName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "名称",
                    description: "必填（可为 null），字符串"
                },
                snsProviderType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "类型",
                    description: "必填（可为 null），字符串"
                },
                snsProviderPartner: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "厂商",
                    description: "必填（可为 null），字符串"
                },
                clientId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "客户端ID",
                    description: "可空，字符串，所属客户端的唯一标识"
                },
                clientSecret: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "OAuth2 客户端密钥",
                    description: "可空，字符串"
                },
                enabled: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "启用状态",
                    description: "必填（可为 null），布尔值（true/false），启用为 true，禁用为 false"
                },
                isAutoRegister: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否自动注册",
                    description: "必填（可为 null），布尔值（true/false）"
                },
                icon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "图标",
                    description: "可空，字符串"
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
                "snsProviderId",
                "snsProviderName",
                "snsProviderType",
                "snsProviderPartner",
                "enabled",
                "isAutoRegister",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_sns_provider").createIndex({
    appId: NumberInt("1"),
    snsProviderPartner: NumberInt("1"),
    snsProviderType: NumberInt("1")
}, {
    name: "ix_appId_snsProviderPartner_snsProviderType_unique",
    unique: true
});
db.getCollection("auth_sns_provider").createIndex({
    appId: NumberInt("1"),
    snsProviderPartner: NumberInt("1"),
    snsProviderType: NumberInt("1"),
    enabled: NumberInt("1")
}, {
    name: "ix_appId_snsProviderPartner_snsProviderType_enabled"
});
db.getCollection("auth_sns_provider").createIndex({
    snsProviderId: NumberInt("1")
}, {
    name: "ix_snsProviderId_unique",
    unique: true
});
db.getCollection("auth_sns_provider").createIndex({
    snsProviderName: NumberInt("1")
}, {
    name: "ix_snsProviderName_unique",
    unique: true
});
