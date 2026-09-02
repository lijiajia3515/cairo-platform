db.getCollection("auth_notify_template").drop();
db.createCollection("auth_notify_template", {
    validator: {
        $jsonSchema: {
            title: "通知消息模板",
            description: "通知消息模板",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                templateId: {
                    bsonType: "string",
                    title: "模板ID",
                    description: "必填，字符串"
                },
                templateName: {
                    bsonType: "string",
                    title: "模板名称",
                    description: "必填，字符串"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                categoryId: {
                    bsonType: "string",
                    title: "分类ID",
                    description: "必填，字符串，所属分类的唯一标识"
                },
                messageCode: {
                    bsonType: "string",
                    title: "消息编码",
                    description: "必填，字符串"
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
                messageType: {
                    bsonType: "string",
                    title: "消息类型（0-提醒消息",
                    description: "必填，字符串，1-文本消息，2-模板消息）"
                },
                messageAlert: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "消息提醒（对消息类型=0/1/2 生效）",
                    description: "可空，字符串，消息提醒方式"
                },
                messageContent: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "消息内容（对应消息类型=2生效）",
                    description: "可空，字符串，消息内容"
                },
                linkType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "跳转类型(0-不跳转",
                    description: "可空，字符串，1-跳转页面，2-跳转内部链接地址，3-跳转外部链接地址)"
                },
                pageUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "页面地址（对应消息类型=2生效）",
                    description: "可空，字符串，页面地址"
                },
                linkUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "内部网站地址（对应消息类型=3/4生效）",
                    description: "可空，字符串，跳转链接地址"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），是否启用（启用后，可以发送，未启用不会发送）"
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
                "templateId",
                "templateName",
                "appId",
                "categoryId",
                "messageCode",
                "messageType",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_notify_template").createIndex({
    appId: NumberInt("1"),
    messageCode: NumberInt("1")
}, {
    name: "ix_appId_messageCode_unique",
    background: true,
    unique: true
});
db.getCollection("auth_notify_template").createIndex({
    appId: NumberInt("1"),
    templateId: NumberInt("1")
}, {
    name: "ix_appId_templateId",
    background: true
});
