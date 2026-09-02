db.getCollection("auth_biz_log_client").drop();
db.createCollection("auth_biz_log_client", {
    validator: {
        $jsonSchema: {
            title: "客户端日志",
            description: "客户端日志",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                logId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "日志ID",
                    description: "必填（可为 null），字符串，日志唯一标识"
                },
                appId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用ID",
                    description: "必填（可为 null），字符串，所属应用的唯一标识"
                },
                clientId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "客户端ID",
                    description: "必填（可为 null），字符串，所属客户端的唯一标识"
                },
                clientTokenId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "客户端TokenId",
                    description: "可空，字符串，客户端令牌唯一标识"
                },
                bizId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "业务ID",
                    description: "必填（可为 null），字符串，业务标识"
                },
                scope: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "范围",
                    description: "必填（可为 null），字符串，业务范围"
                },
                params: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "参数字符串",
                    description: "必填（可为 null），字符串，业务参数"
                },
                success: {
                    bsonType: "bool",
                    title: "是否成功",
                    description: "必填，布尔值（true/false），操作成功为 true，失败为 false"
                },
                errorMessage: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "错误信息",
                    description: "可空，字符串，失败时的错误信息"
                },
                ip: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "客户端IP",
                    description: "必填（可为 null），字符串，客户端IP地址"
                },
                startTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "开始时间",
                    description: "必填（可为 null），日期时间"
                },
                endTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "结束时间",
                    description: "必填（可为 null），日期时间"
                },
                mills: {
                    bsonType: [
                        "null",
                        "long"
                    ],
                    title: "持续时长（毫秒）",
                    description: "必填（可为 null），整数，耗时（毫秒）"
                },
                metadata: {
                    bsonType: "object",
                    title: "元信息",
                    description: "元信息，记录创建与更新的操作者及时间",
                    properties: {
                        appId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "应用ID",
                            description: "必填（可为 null），字符串，所属应用的唯一标识"
                        },
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
                        createClientId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "创建客户端ID",
                            description: "必填（可为 null），字符串，创建该记录的客户端ID"
                        },
                        updateClientId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "更新客户端ID",
                            description: "必填（可为 null），字符串，最后更新该记录的客户端ID"
                        }
                    },
                    required: [
                        "appId",
                        "createClientId",
                        "createTime",
                        "updateClientId",
                        "updateTime"
                    ]
                }
            },
            required: [
                "_id",
                "logId",
                "appId",
                "clientId",
                "bizId",
                "scope",
                "params",
                "success",
                "ip",
                "startTime",
                "endTime",
                "mills"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_biz_log_client").createIndex({
    appId: NumberInt("1"),
    clientId: NumberInt("1")
}, {
    name: "ix_appId_clientId",
    background: true
});
db.getCollection("auth_biz_log_client").createIndex({
    logId: NumberInt("-1")
}, {
    name: "ix_logId_unique",
    background: true,
    unique: true
});
