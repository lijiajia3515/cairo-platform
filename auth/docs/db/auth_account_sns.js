db.getCollection("auth_account_sns").drop();
db.createCollection("auth_account_sns", {
    validator: {
        $jsonSchema: {
            title: "账号三方连接",
            description: "账号三方连接",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                recordId: {
                    bsonType: "string",
                    title: "记录ID",
                    description: "必填，字符串，记录唯一标识"
                },
                accountId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "账号ID",
                    description: "可空，字符串，所属账号的唯一标识"
                },
                snsPartnerId: {
                    bsonType: "string",
                    title: "第三方账号厂家ID",
                    description: "必填，字符串，第三方登录合作方标识"
                },
                snsPartnerOpenId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "第三方账号ID（微信",
                    description: "可空，字符串，（openId/unionId）支付宝：openId）"
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
                bindTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "绑定时间",
                    description: "必填（可为 null），日期时间"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                "Metadata ": {
                    bsonType: "object",
                    properties: {
                        createTime: {
                            bsonType: "date",
                            title: "创建时间",
                            description: "必填，日期时间"
                        },
                        updateTime: {
                            bsonType: "date",
                            title: "更新时间",
                            description: "必填，日期时间"
                        },
                        createAccountId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "创建账号ID",
                            description: "必填（可为 null），字符串"
                        },
                        updateAccountId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "更新账号ID",
                            description: "必填（可为 null），字符串"
                        }
                    },
                    required: [
                        "createAccountId",
                        "createTime",
                        "updateAccountId",
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
                "recordId",
                "snsPartnerId",
                "bindTime",
                "enabled"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_account_sns").createIndex({
    snsPartnerId: NumberInt("1")
}, {
    name: "ix_snsPartnerId"
});
