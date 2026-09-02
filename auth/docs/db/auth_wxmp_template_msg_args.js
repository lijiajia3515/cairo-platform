db.getCollection("auth_wxmp_template_msg_args").drop();
db.createCollection("auth_wxmp_template_msg_args", {
    validator: {
        $jsonSchema: {
            title: "微信模板参数",
            description: "微信模板参数",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串"
                },
                bizId: {
                    bsonType: "string",
                    title: "业务ID",
                    description: "必填，字符串"
                },
                argCode: {
                    bsonType: "string",
                    title: "业务定义参数编码",
                    description: "必填，字符串"
                },
                argName: {
                    bsonType: "string",
                    title: "参数名称",
                    description: "必填，字符串"
                },
                argType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "参数类型",
                    description: "可空，字符串"
                },
                templateArgCode: {
                    bsonType: "string",
                    title: "模板参数编码",
                    description: "必填，字符串"
                },
                defaultColor: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "默认颜色",
                    description: "可空，字符串"
                },
                sort: {
                    bsonType: "int",
                    title: "排序值",
                    description: "必填，整数"
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
                "argCode",
                "argName",
                "templateArgCode",
                "sort",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_wxmp_template_msg_args").createIndex({
    appId: NumberInt("1"),
    bizId: NumberInt("1"),
    argCode: NumberInt("1")
}, {
    name: "ix_appId_bizId_argCode_unique",
    unique: true
});
db.getCollection("auth_wxmp_template_msg_args").createIndex({
    appId: NumberInt("1"),
    bizId: NumberInt("1"),
    templateArgCode: NumberInt("1")
}, {
    name: "ix_appId_bizId_templateArgCode_unique",
    unique: true
});
