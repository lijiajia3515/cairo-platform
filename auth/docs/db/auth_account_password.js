db.getCollection("auth_account_password").drop();
db.createCollection("auth_account_password", {
    validator: {
        $jsonSchema: {
            title: "账号密码",
            description: "账号密码",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                accountId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "账号ID",
                    description: "必填（可为 null），字符串，所属账号的唯一标识"
                },
                type: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "类型",
                    description: "必填（可为 null），字符串，类型标识"
                },
                password: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "密码",
                    description: "必填（可为 null），字符串"
                },
                passwordFailCount: {
                    bsonType: [
                        "null",
                        "int"
                    ],
                    title: "密码错误次数",
                    description: "可空，整数"
                },
                passwordFailTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "密码错误时间",
                    description: "可空，日期时间"
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
                "accountId",
                "type",
                "password",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_account_password").createIndex({
    accountId: NumberInt("-1"),
    type: NumberInt("1")
}, {
    name: "ix_accountId_type_unique",
    background: true,
    unique: true
});
