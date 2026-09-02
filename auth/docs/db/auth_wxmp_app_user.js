db.getCollection("auth_wxmp_app_user").drop();
db.createCollection("auth_wxmp_app_user", {
    validator: {
        $jsonSchema: {
            title: "公众号应用用户三方连接",
            description: "公众号应用用户三方连接",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                userId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "用户ID",
                    description: "可空，字符串，所属用户的唯一标识"
                },
                wxProviderId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "微信ID",
                    description: "必填（可为 null），字符串，微信服务提供商标识"
                },
                openId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "第三方认证唯一标识-openId",
                    description: "可空，字符串，微信 OpenID"
                },
                bindTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "绑定时间",
                    description: "必填（可为 null），日期时间"
                },
                enabled: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "启用状态",
                    description: "必填（可为 null），布尔值（true/false），启用为 true，禁用为 false"
                },
                "Metadata ": {
                    bsonType: "object",
                    properties: {
                        createUserId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "创建用户ID",
                            description: "必填（可为 null），字符串"
                        },
                        updateUserId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "更新用户ID",
                            description: "必填（可为 null），字符串"
                        },
                        createTime: {
                            bsonType: "date",
                            title: "创建时间",
                            description: "必填，日期时间"
                        },
                        updateTime: {
                            bsonType: "date",
                            title: "更新时间",
                            description: "必填，日期时间"
                        }
                    },
                    required: [
                        "createUserId",
                        "createTime",
                        "updateUserId",
                        "updateTime"
                    ],
                    title: "元信息",
                    description: "元信息"
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
                "wxProviderId",
                "bindTime",
                "enabled"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_wxmp_app_user").createIndex({
    wxProviderId: NumberInt("1")
}, {
    name: "ix_wxProviderId"
});
