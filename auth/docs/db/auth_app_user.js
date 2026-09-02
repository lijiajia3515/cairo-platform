db.getCollection("auth_app_user").drop();
db.createCollection("auth_app_user", {
    validator: {
        $jsonSchema: {
            title: "应用用户",
            description: "应用用户",
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
                userId: {
                    bsonType: "string",
                    title: "用户ID",
                    description: "必填，字符串，所属用户的唯一标识"
                },
                nickname: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "昵称",
                    description: "必填（可为 null），字符串"
                },
                phoneNumber: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "手机号",
                    description: "必填（可为 null），字符串"
                },
                roleIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "角色ID",
                        description: "角色ID"
                    },
                    title: "角色标识",
                    description: "必填（可为 null），字符串数组，关联角色的唯一标识数组"
                },
                tagIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "标签ID",
                        description: "标签ID"
                    },
                    title: "标签标识",
                    description: "必填（可为 null），字符串数组"
                },
                admin: {
                    bsonType: "bool",
                    title: "是否管理员",
                    description: "必填，布尔值（true/false）"
                },
                position: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "职位",
                    description: "可空，字符串"
                },
                mainDepartmentId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "主部门ID",
                    description: "可空，字符串"
                },
                departmentIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "部门ID",
                        description: "部门ID"
                    },
                    title: "部门标识",
                    description: "必填（可为 null），字符串数组"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                joinTime: {
                    bsonType: "date",
                    title: "加入时间",
                    description: "必填，日期时间"
                },
                loginTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "登录时间",
                    description: "可空，日期时间，最后登录时间"
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
                    title: "注销时间",
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
                transferAccountTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "转移账号时间",
                    description: "可空，日期时间"
                },
                accountId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "账号ID",
                    description: "必填（可为 null），字符串，所属账号的唯一标识"
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
                "userId",
                "nickname",
                "phoneNumber",
                "roleIds",
                "tagIds",
                "admin",
                "departmentIds",
                "enabled",
                "joinTime",
                "accountId",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_app_user").createIndex({
    appId: NumberInt("1")
}, {
    name: "ix_appId",
    background: true
});
db.getCollection("auth_app_user").createIndex({
    appId: NumberInt("1"),
    accountId: NumberInt("1")
}, {
    name: "ix_appId_accountId_unique",
    background: true,
    unique: true,
    partialFilterExpression: {
        accountId: {
            $exists: true,
            $type : "string"
        }
    }
});
db.getCollection("auth_app_user").createIndex({
    appId: NumberInt("1"),
    userId: NumberInt("1")
}, {
    name: "ix_appId_userId_unique",
    unique: true
});
