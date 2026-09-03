db.getCollection("auth_sns_token").drop();
db.createCollection("auth_sns_token", {
    validator: {
        $jsonSchema: {
            title: "第三方账号信息表",
            description: "第三方账号信息表",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                token: {
                    bsonType: "string",
                    title: "记录ID",
                    description: "必填，字符串"
                },
                status: {
                    bsonType: "string",
                    title: "状态",
                    description: "必填，字符串，状态标识"
                },
                expiredTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "过期时间",
                    description: "必填（可为 null），日期时间"
                },
                partnerId: {
                    bsonType: "string",
                    title: "厂商ID",
                    description: "必填，字符串"
                },
                providerId: {
                    bsonType: "string",
                    title: "供应商ID",
                    description: "必填，字符串"
                },
                partnerOpenId: {
                    bsonType: "string",
                    title: "厂商用户ID",
                    description: "必填，字符串"
                },
                providerOpenId: {
                    bsonType: "string",
                    title: "供应商用户ID",
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
                avatarUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "头像",
                    description: "可空，字符串，头像资源地址"
                },
                sex: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "性别",
                    description: "可空，字符串"
                },
                createTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "创建时间",
                    description: "可空，日期时间，记录创建时间"
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
                "token",
                "status",
                "expiredTime",
                "partnerId",
                "providerId",
                "partnerOpenId",
                "providerOpenId"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_sns_token").createIndex({
    token: NumberInt("1")
}, {
    name: "ix_token_unique",
    background: true,
    unique: true
});
