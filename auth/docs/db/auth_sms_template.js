db.getCollection("auth_sms_template").drop();
db.createCollection("auth_sms_template", {
    validator: {
        $jsonSchema: {
            title: "短信模板",
            description: "短信模板",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                appId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用ID",
                    description: "必填（可为 null），字符串，所属应用的唯一标识"
                },
                bizId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "业务ID",
                    description: "必填（可为 null），字符串，业务标识"
                },
                templateSign: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "签名",
                    description: "必填（可为 null），字符串，模板签名"
                },
                templateCode: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "模板编号",
                    description: "必填（可为 null），字符串，模板编码"
                },
                templateName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "模板名称",
                    description: "必填（可为 null），字符串"
                },
                templateType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "模板类型",
                    description: "必填（可为 null），字符串"
                },
                templateText: {
                    bsonType: "string",
                    title: "模板内容",
                    description: "必填，字符串"
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
                "appId",
                "bizId",
                "templateSign",
                "templateCode",
                "templateName",
                "templateType",
                "templateText",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_sms_template").createIndex({
    appId: NumberInt("1"),
    bizId: NumberInt("1")
}, {
    name: "ix_appId_bizId_unique",
    background: true,
    unique: true
});
