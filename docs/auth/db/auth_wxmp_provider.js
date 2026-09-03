db.getCollection("auth_wxmp_provider").drop();
db.createCollection("auth_wxmp_provider", {
    validator: {
        $jsonSchema: {
            title: "微信公众号管理",
            description: "微信公众号管理",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                wxmpProviderId: {
                    bsonType: "string",
                    title: "标识",
                    description: "必填，字符串，所属微信小程序提供者的唯一标识"
                },
                wxmpProviderName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "名称",
                    description: "可空，字符串"
                },
                wxmpAppId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "设置微信公众号的appid.",
                    description: "可空，字符串"
                },
                wxmpSecret: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "设置微信公众号的app secret.",
                    description: "可空，字符串"
                },
                wxmpToken: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "设置微信公众号的token.",
                    description: "可空，字符串"
                },
                wxmpAesKey: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "设置微信公众号的EncodingAESKey.",
                    description: "可空，字符串"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），是否启用（启用后，可以发送，未启用不会发送）"
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
                "wxmpProviderId",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_wxmp_provider").createIndex({
    wxmpProviderId: NumberInt("1")
}, {
    name: "ix_wxmpProviderId_unique",
    unique: true
});
