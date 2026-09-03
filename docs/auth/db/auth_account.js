db.getCollection("auth_account").drop();
db.createCollection("auth_account", {
    validator: {
        $jsonSchema: {
            title: "账号",
            description: "账号",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                accountId: {
                    bsonType: "string",
                    title: "账号ID",
                    description: "必填，字符串，所属账号的唯一标识"
                },
                nickname: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "昵称",
                    description: "必填（可为 null），字符串"
                },
                avatarUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "头像",
                    description: "必填（可为 null），字符串，头像资源地址"
                },
                username: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "用户名",
                    description: "必填（可为 null），字符串，登录名"
                },
                email: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "邮箱号码",
                    description: "必填（可为 null），字符串，邮箱地址"
                },
                phoneNumber: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "手机号",
                    description: "必填（可为 null），字符串"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                locked: {
                    bsonType: "bool",
                    title: "账号锁定",
                    description: "必填，布尔值（true/false）"
                },
                lockedTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "账号锁定时间",
                    description: "可空，日期时间"
                },
                joinTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "加入时间",
                    description: "可空，日期时间"
                },
                loginTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "最后登录时间",
                    description: "可空，日期时间"
                },
                logoffStatus: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "注销状态",
                    description: "可空，字符串"
                },
                logoffPendingTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "注销等待时间",
                    description: "可空，日期时间，注销等待开始时间"
                },
                logoffSuccessTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "注销成功时间",
                    description: "可空，日期时间，注销完成时间"
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
                "nickname",
                "avatarUrl",
                "username",
                "email",
                "phoneNumber",
                "enabled",
                "locked",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_account").createIndex({
    accountId: NumberInt("-1")
}, {
    name: "ix_accountId_unique",
    background: true,
    unique: true
});
db.getCollection("auth_account").createIndex({
    email: NumberInt("1")
}, {
    name: "ix_email_unique",
    background: true,
    unique: true,
    partialFilterExpression: {
        email: {
            $exists: true,
            $type : "string"
        }
    }
});
db.getCollection("auth_account").createIndex({
    phoneNumber: NumberInt("1")
}, {
    name: "ix_phoneNumber_unique",
    background: true,
    unique: true,
    partialFilterExpression: {
        phoneNumber: {
            $exists: true,
            $type : "string"
        }
    }
});
db.getCollection("auth_account").createIndex({
    username: NumberInt("1")
}, {
    name: "ix_username_unique",
    unique: true,
    partialFilterExpression: {
        username: {
            $exists: true,
            $type : "string"
        }
    }
});
