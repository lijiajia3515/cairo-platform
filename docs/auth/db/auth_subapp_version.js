db.getCollection("auth_subapp_version").drop();
db.createCollection("auth_subapp_version", {
    validator: {
        $jsonSchema: {
            title: "子应用版本",
            description: "子应用版本",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                subappId: {
                    bsonType: "string",
                    title: "子应用ID",
                    description: "必填，字符串，所属子应用的唯一标识"
                },
                subappVersion: {
                    bsonType: "string",
                    title: "子应用版本",
                    description: "必填，字符串，所属子应用的版本号"
                },
                subappRemark: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "子应用备注",
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
                "subappId",
                "subappVersion",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_subapp_version").createIndex({
    subappId: NumberInt("1"),
    enabled: NumberInt("1")
}, {
    name: "ix_subappId_enabled"
});

db.getCollection("auth_subapp_version").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1")
}, {
    name: "ix_appId_endpointId_subappId_subappVersion_unique",
    unique: true
});
