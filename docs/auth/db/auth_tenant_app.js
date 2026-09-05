db.getCollection("auth_tenant_app").drop();
db.createCollection("auth_tenant_app", {
    validator: {
        $jsonSchema: {
            title: "企业应用",
            description: "企业应用",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                tenantId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "企业ID",
                    description: "必填（可为 null），字符串，所属企业的唯一标识"
                },
                appId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用ID",
                    description: "必填（可为 null），字符串，所属应用的唯一标识"
                },
                adminAccountIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    title: "绑定管理员账号ID",
                    description: "必填（可为 null），数组，管理员账号的唯一标识数组"
                },
                autoRegister: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "开通自动注册",
                    description: "可空，布尔值（true/false），true 表示自动注册"
                },
                enabled: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "启用状态",
                    description: "必填（可为 null），布尔值（true/false），启用为 true，禁用为 false"
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
                        createAccountId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "创建账号ID",
                            description: "必填（可为 null），字符串，创建该记录的账号ID"
                        },
                        updateAccountId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "更新账号ID",
                            description: "必填（可为 null），字符串，最后更新该记录的账号ID"
                        }
                    },
                    required: [
                        "createAccountId",
                        "createTime",
                        "updateAccountId",
                        "updateTime"
                    ]
                }
            },
            required: [
                "_id",
                "tenantId",
                "appId",
                "adminAccountIds",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_tenant_app").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1")
}, {
    name: "ix_tenantId_appId_unique",
    background: true,
    unique: true
});
