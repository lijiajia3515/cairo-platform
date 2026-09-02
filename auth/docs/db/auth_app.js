db.getCollection("auth_app").drop();
db.createCollection("auth_app", {
    validator: {
        $jsonSchema: {
            title: "应用",
            description: "应用",
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
                appName: {
                    bsonType: "string",
                    title: "名称",
                    description: "必填，字符串"
                },
                scopes: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "scopes项",
                        description: "scopes项"
                    },
                    title: "应用范围",
                    description: "可空，字符串数组"
                },
                privateApp: {
                    bsonType: "bool",
                    title: "是否私有应用",
                    description: "必填，布尔值（true/false）"
                },
                icon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "图标",
                    description: "可空，字符串，图标资源地址"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                adminAccountIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "adminAccountIds项",
                        description: "adminAccountIds项"
                    },
                    title: "绑定管理员账号ID",
                    description: "可空，字符串数组，管理员账号的唯一标识数组"
                },
                autoRegister: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "开通自动注册",
                    description: "可空，布尔值（true/false），true 表示自动注册"
                },
                tenantAppDepartmentTemplateStatus: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "企业部门模板状态",
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
                "appId",
                "appName",
                "privateApp",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_app").createIndex({
    appId: NumberInt("1")
}, {
    name: "ix_appId_unique",
    background: true,
    unique: true
});
