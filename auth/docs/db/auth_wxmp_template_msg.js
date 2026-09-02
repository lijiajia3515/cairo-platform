db.getCollection("auth_wxmp_template_msg").drop();
db.createCollection("auth_wxmp_template_msg", {
    validator: {
        $jsonSchema: {
            title: "微信消息模板",
            description: "微信消息模板",
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
                bizId: {
                    bsonType: "string",
                    title: "业务ID",
                    description: "必填，字符串，业务标识"
                },
                templateCode: {
                    bsonType: "string",
                    title: "模板编号",
                    description: "必填，字符串，模板编码"
                },
                templateName: {
                    bsonType: "string",
                    title: "模板名称",
                    description: "必填，字符串"
                },
                templateType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "模板类型",
                    description: "可空，字符串"
                },
                templateText: {
                    bsonType: "string",
                    title: "模板内容",
                    description: "必填，字符串"
                },
                wxmpProviderId: {
                    bsonType: "string",
                    title: "公众号管理ID",
                    description: "必填，字符串，所属微信小程序提供者的唯一标识"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），是否启用（启用后，可以发送，未启用不会发送）"
                },
                jumpUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "跳转链接",
                    description: "可空，字符串，跳转链接地址"
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
                "bizId",
                "templateCode",
                "templateName",
                "templateText",
                "wxmpProviderId",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_wxmp_template_msg").createIndex({
    appId: NumberInt("1"),
    bizId: NumberInt("1")
}, {
    name: "ix_appId_bizId_unique",
    unique: true
});
db.getCollection("auth_wxmp_template_msg").createIndex({
    appId: NumberInt("1"),
    templateName: NumberInt("1")
}, {
    name: "ix_appId_templateName_unique",
    unique: true
});
db.getCollection("auth_wxmp_template_msg").createIndex({
    appId: NumberInt("1"),
    wxmpProviderId: NumberInt("1")
}, {
    name: "ix_appId_wxmpProviderId"
});
