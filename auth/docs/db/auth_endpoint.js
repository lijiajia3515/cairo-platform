db.getCollection("auth_endpoint").drop();
db.createCollection("auth_endpoint", {
    validator: {
        $jsonSchema: {
            title: "终端",
            description: "终端",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                id: {
                    bsonType: "string",
                    title: "主键ID",
                    description: "必填，字符串"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                endpointId: {
                    bsonType: "string",
                    title: "终端ID",
                    description: "必填，字符串，所属终端的唯一标识"
                },
                endpointName: {
                    bsonType: "string",
                    title: "终端名称",
                    description: "必填，字符串，所属终端的名称"
                },
                type: {
                    bsonType: "string",
                    title: "终端类型",
                    description: "必填，字符串，类型标识"
                },
                scope: {
                    bsonType: "string",
                    title: "终端范围",
                    description: "必填，字符串，业务范围"
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
                icon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "图标",
                    description: "可空，字符串，图标资源地址"
                },
                websiteUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "网站Url",
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
                "id",
                "appId",
                "endpointId",
                "endpointName",
                "type",
                "scope",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_endpoint").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1")
}, {
    name: "ix_appId_endpointId_unique",
    background: true,
    unique: true
});