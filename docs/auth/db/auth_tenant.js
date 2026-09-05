db.getCollection("auth_tenant").drop();
db.createCollection("auth_tenant", {
    validator: {
        $jsonSchema: {
            title: "企业",
            description: "企业",
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
                tenantName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "名称",
                    description: "必填（可为 null），字符串，企业名称"
                },
                aliasName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "别名",
                    description: "可空，字符串"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                icon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "图标",
                    description: "可空，字符串，图标资源地址"
                },
                ownerAccountId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "拥有者账号ID",
                    description: "必填（可为 null），字符串，所属账号的唯一标识"
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
                "tenantName",
                "enabled",
                "ownerAccountId",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_tenant").createIndex({
    aliasName: NumberInt("1")
}, {
    name: "ix_aliasName_unique",
    background: true,
    unique: true
});
db.getCollection("auth_tenant").createIndex({
    tenantId: NumberInt("1")
}, {
    name: "ix_tenantId_unique",
    background: true,
    unique: true
});
db.getCollection("auth_tenant").createIndex({
    tenantName: NumberInt("1")
}, {
    name: "ix_tenantName_unique",
    background: true,
    unique: true
});
