db.getCollection("auth_notify_record_tenant_app").drop();
db.createCollection("auth_notify_record_tenant_app", {
    validator: {
        $jsonSchema: {
            title: "企业应用通知消息记录",
            description: "企业应用通知消息记录",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                msgId: {
                    bsonType: "string",
                    title: "消息ID",
                    description: "必填，字符串"
                },
                messageTime: {
                    bsonType: "date",
                    title: "消息时间",
                    description: "必填，日期时间"
                },
                tenantId: {
                    bsonType: "string",
                    title: "企业ID",
                    description: "必填，字符串"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串"
                },
                endpointId: {
                    bsonType: "string",
                    title: "终端ID",
                    description: "必填，字符串"
                },
                userId: {
                    bsonType: "string",
                    title: "用户ID",
                    description: "必填，字符串"
                },
                deviceId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "设备ID",
                    description: "可空，字符串"
                },
                categoryId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "分类ID",
                    description: "可空，字符串"
                },
                categoryName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "分类名称",
                    description: "可空，字符串"
                },
                categoryIcon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "分类图标",
                    description: "可空，字符串"
                },
                messageCode: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "消息编码",
                    description: "可空，字符串"
                },
                messageIcon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "消息图标",
                    description: "可空，字符串"
                },
                messageTitle: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "消息标题",
                    description: "可空，字符串"
                },
                messageAlert: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "消息提醒",
                    description: "可空，字符串"
                },
                messageType: {
                    bsonType: "string",
                    title: "消息类型",
                    description: "必填，字符串"
                },
                messageContent: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "消息内容",
                    description: "可空，字符串"
                },
                alertArgs: {
                    bsonType: [
                        "null",
                        "object"
                    ],
                    title: "提醒参数值",
                    description: "可空，对象"
                },
                contentArgs: {
                    bsonType: [
                        "null",
                        "object"
                    ],
                    title: "内容参数值",
                    description: "可空，对象"
                },
                templateArgs: {
                    bsonType: [
                        "null",
                        "object"
                    ],
                    title: "模板参数值",
                    description: "可空，对象"
                },
                linkType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "跳转类型",
                    description: "可空，字符串"
                },
                pageUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "页面地址",
                    description: "可空，字符串"
                },
                linkUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "内部网站地址",
                    description: "可空，字符串"
                },
                extras: {
                    bsonType: [
                        "null",
                        "object"
                    ],
                    title: "扩展参数",
                    description: "可空，对象"
                },
                pushStatus: {
                    bsonType: "bool",
                    title: "推送状态",
                    description: "必填，布尔值（true/false）"
                },
                pushTime: {
                    bsonType: "bool",
                    title: "推送时间",
                    description: "可空，布尔值（true/false）"
                },
                pushFailReason: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "推送失败原因",
                    description: "可空，字符串"
                },
                pushFailCount: {
                    bsonType: "long",
                    title: "推送失败次数",
                    description: "必填，整数"
                },
                readStatus: {
                    bsonType: "bool",
                    title: "是否已读",
                    description: "必填，布尔值（true/false）"
                },
                readTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "已读时间",
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
                "messageTime",
                "tenantId",
                "appId",
                "endpointId",
                "userId",
                "messageType",
                "pushStatus",
                "pushFailCount",
                "readStatus",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_notify_record_tenant_app").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    userId: NumberInt("1")
}, {
    name: "ix_tenantId_appId_endpointId_userId",
    background: true
});
db.getCollection("auth_notify_record_tenant_app").createIndex({
    msgId: NumberInt("1")
}, {
    name: "ix_msgId_unique",
    background: true,
    unique: true
});
