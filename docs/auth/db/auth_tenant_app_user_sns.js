db.getCollection("auth_tenant_app_user_sns").drop();
db.createCollection("auth_tenant_app_user_sns", {
    validator: {
        $jsonSchema: {
            title: "企业应用级用户-社交登录",
            description: "企业应用级用户社交登录绑定",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                recordId: {
                    bsonType: "string",
                    title: "记录ID",
                    description: "必填，字符串，记录唯一标识"
                },
                tenantId: {
                    bsonType: "string",
                    title: "企业ID",
                    description: "必填，字符串，所属企业的唯一标识"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                userId: {
                    bsonType: "string",
                    title: "用户ID",
                    description: "必填，字符串，所属用户的唯一标识"
                },
                snsProviderId: {
                    bsonType: "string",
                    title: "第三方认证提供商ID",
                    description: "必填，字符串，所属第三方登录提供者的唯一标识"
                },
                unionId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "UnionID",
                    description: "可空，字符串，微信 UnionID"
                },
                nickname: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "昵称",
                    description: "可空，字符串"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
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
                "recordId",
                "tenantId",
                "appId",
                "userId",
                "snsProviderId",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_tenant_app_user_sns").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    userId: NumberInt("1")
}, {
    name: "ix_tenantId_appId_userId",
    background: true
});
db.getCollection("auth_tenant_app_user_sns").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    snsProviderId: NumberInt("1"),
    unionId: NumberInt("1")
}, {
    name: "ix_tenantId_appId_snsProviderId_unionId_unique",
    background: true,
    unique: true
});
