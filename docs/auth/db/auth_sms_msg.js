db.getCollection("auth_sms_msg").drop();
db.createCollection("auth_sms_msg", {
    validator: {
        $jsonSchema: {
            title: "短信消息",
            description: "短信消息",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                msgId: {
                    bsonType: "string",
                    title: "记录ID",
                    description: "必填，字符串，消息唯一标识"
                },
                time: {
                    bsonType: "date",
                    title: "时间",
                    description: "必填，日期时间"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                bizId: {
                    bsonType: "string",
                    title: "业务ID",
                    description: "必填，字符串，业务标识"
                },
                phoneNumber: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "手机号",
                    description: "可空，字符串"
                },
                type: {
                    bsonType: "string",
                    title: "发送类型",
                    description: "必填，字符串，类型标识"
                },
                text: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "短信文本",
                    description: "可空，字符串，文本内容"
                },
                bizArgs: {
                    bsonType: "string",
                    title: "业务参数",
                    description: "必填，字符串"
                },
                providerType: {
                    bsonType: "string",
                    title: "供应商类型",
                    description: "必填，字符串，服务提供商类型"
                },
                providerSign: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "供应商签名",
                    description: "可空，字符串"
                },
                providerTemplateCode: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "供应商模板类型",
                    description: "可空，字符串，服务提供商模板编码"
                },
                providerArgs: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "供应商参数",
                    description: "可空，字符串，服务提供商参数"
                },
                providerMsgId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "供应商发送回执ID",
                    description: "可空，字符串，服务提供商消息标识"
                },
                success: {
                    bsonType: "bool",
                    title: "是否成功",
                    description: "必填，布尔值（true/false），操作成功为 true，失败为 false"
                },
                reason: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "失败原因",
                    description: "可空，字符串，原因说明"
                },
                version: {
                    bsonType: "long",
                    title: "版本",
                    description: "可空，整数"
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
                "msgId",
                "time",
                "appId",
                "bizId",
                "type",
                "bizArgs",
                "providerType",
                "success"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_sms_msg").createIndex({
    appId: NumberInt("1"),
    bizId: NumberInt("1")
}, {
    name: "ix_appId_bizId",
    background: true
});
db.getCollection("auth_sms_msg").createIndex({
    msgId: NumberInt("1")
}, {
    name: "ix_msgId_unique",
    unique: true
});
