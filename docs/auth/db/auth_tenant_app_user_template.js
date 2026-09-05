db.getCollection("auth_tenant_app_user_template").drop();
db.createCollection("auth_tenant_app_user_template", {
    validator: {
        $jsonSchema: {
            title: "企业应用级用户模板",
            description: "企业应用级用户模板",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                tenantAppUserTemplateId: {
                    bsonType: "string",
                    title: "企业应用级用户模板ID",
                    description: "必填，字符串"
                },
                nickname: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "昵称",
                    description: "可空，字符串"
                },
                phoneNumber: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "手机号",
                    description: "可空，字符串"
                },
                tenantAppRoleTemplateIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "角色模板ID",
                        description: "角色模板ID"
                    },
                    title: "角色标识",
                    description: "可空，字符串数组"
                },
                admin: {
                    bsonType: "bool",
                    title: "是否管理员",
                    description: "必填，布尔值（true/false）"
                },
                position: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "职位",
                    description: "可空，字符串"
                },
                tenantMainDepartmentTemplateId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "主部门ID",
                    description: "可空，字符串"
                },
                tenantAppDepartmentTemplateIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "部门模板ID",
                        description: "部门模板ID"
                    },
                    title: "部门标识",
                    description: "可空，字符串数组"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                accountId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "账号ID",
                    description: "可空，字符串，所属账号的唯一标识"
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
                "appId",
                "tenantAppUserTemplateId",
                "admin",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_tenant_app_user_template").createIndex({
    appId: NumberInt("1")
}, {
    name: "ix_appId",
    background: true
});
db.getCollection("auth_tenant_app_user_template").createIndex({
    appId: NumberInt("1"),
    tenantAppUserTemplateId: NumberInt("1")
}, {
    name: "ix_appId_tenantAppUserTemplateId_unique",
    background: true,
    unique: true
});
