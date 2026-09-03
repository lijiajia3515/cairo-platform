db.getCollection("auth_app_role").drop();
db.createCollection("auth_app_role", {
    validator: {
        $jsonSchema: {
            title: "应用角色",
            description: "应用角色",
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
                    description: "必填（可为 null），字符串，所属应用的唯一标识"
                },
                roleId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "角色ID",
                    description: "必填（可为 null），字符串，所属角色的唯一标识"
                },
                roleName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "名称",
                    description: "必填（可为 null），字符串，所属角色的名称"
                },
                enabled: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "启用状态",
                    description: "必填（可为 null），布尔值（true/false），启用为 true，禁用为 false"
                },
                remark: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "备注",
                    description: "可空，字符串，备注信息"
                },
                sort: {
                    bsonType: [
                        "null",
                        "long"
                    ],
                    title: "排序值",
                    description: "可空，整数，用于列表展示排序"
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
db.getCollection("auth_app_role").createIndex({
    appId: NumberInt("1")
}, {
    name: "ix_appId",
    background: true
});
db.getCollection("auth_app_role").createIndex({
    appId: NumberInt("1"),
    roleId: NumberInt("1")
}, {
    name: "ix_appId_roleId_unique",
    background: true,
    unique: true
});
