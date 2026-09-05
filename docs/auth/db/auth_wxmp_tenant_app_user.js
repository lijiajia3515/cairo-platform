db.getCollection("auth_wxmp_tenant_app_user").drop();
db.createCollection("auth_wxmp_tenant_app_user", {
    validator: {
        $jsonSchema: {
            title: "企业应用级用户公众号链接",
            description: "企业应用级用户公众号链接",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                tenantId: {
                    bsonType: "string",
                    title: "企业ID",
                    description: "必填，字符串"
                },
                userId: {
                    bsonType: "string",
                    title: "用户ID",
                    description: "必填，字符串"
                },
                wxProviderId: {
                    bsonType: "string",
                    title: "微信ID",
                    description: "必填，字符串"
                },
                openId: {
                    bsonType: "string",
                    title: "第三方认证唯一标识-openId",
                    description: "必填，字符串"
                },
                bindTime: {
                    bsonType: "date",
                    title: "绑定时间",
                    description: "必填，日期时间"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false）"
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
                "tenantId",
                "userId",
                "wxProviderId",
                "openId",
                "bindTime",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_wxmp_tenant_app_user").createIndex({
    tenantId: NumberInt("1"),
    userId: NumberInt("1")
}, {
    name: "ix_tenantId_userId",
    background: true
});
db.getCollection("auth_wxmp_tenant_app_user").createIndex({
    wxProviderId: NumberInt("1"),
    openId: NumberInt("1")
}, {
    name: "ix_wxProviderId_openId_unique",
    background: true,
    unique: true
});
