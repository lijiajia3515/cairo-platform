db.getCollection("auth_tenant_app_role").drop();
db.createCollection("auth_tenant_app_role", {
    validator: {
        $jsonSchema: {
            title: "企业应用角色",
            description: "企业应用角色",
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
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串"
                },
                roleId: {
                    bsonType: "string",
                    title: "角色ID",
                    description: "必填，字符串"
                },
                roleName: {
                    bsonType: "string",
                    title: "角色名称",
                    description: "必填，字符串"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false）"
                },
                remark: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "备注",
                    description: "可空，字符串"
                },
                sort: {
                    bsonType: [
                        "null",
                        "long"
                    ],
                    title: "排序值",
                    description: "可空，整数"
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
                "appId",
                "roleId",
                "roleName",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_tenant_app_role").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1")
}, {
    name: "ix_tenantId_appId",
    background: true
});
db.getCollection("auth_tenant_app_role").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    roleId: NumberInt("1")
}, {
    name: "ix_tenantId_appId_roleId_unique",
    background: true,
    unique: true
});
