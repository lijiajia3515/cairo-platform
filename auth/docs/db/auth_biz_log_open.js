db.getCollection("auth_biz_log_open").drop();
db.createCollection("auth_biz_log_open", {
    validator: {
        $jsonSchema: {
            title: "匿名业务日志",
            description: "匿名业务日志",
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
                    description: "可空，字符串，所属应用的唯一标识"
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
                        createTime: {
                            bsonType: "date",
                            title: "创建时间",
                            description: "必填，日期时间，记录创建时间"
                        },
                        updateTime: {
                            bsonType: "date",
                            title: "更新时间",
                            description: "必填，日期时间，记录最后更新时间"
                        }
                    },
                    required: [
                        "createTime",
                        "updateTime"
                    ]
                }
            },
            required: [
                "_id",
                "logId",
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
