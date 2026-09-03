db.getCollection("auth_notify_category").drop();
db.createCollection("auth_notify_category", {
    validator: {
        $jsonSchema: {
            title: "通知消息分类",
            description: "通知消息分类",
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
                categoryId: {
                    bsonType: "string",
                    title: "分类ID",
                    description: "必填，字符串，所属分类的唯一标识"
                },
                categoryName: {
                    bsonType: "string",
                    title: "分类名称",
                    description: "必填，字符串"
                },
                categoryIcon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "分类图标",
                    description: "可空，字符串"
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
                "categoryId",
                "categoryName",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_notify_category").createIndex({
    appId: NumberInt("1"),
    categoryId: NumberInt("1")
}, {
    name: "ix_appId_categoryId_unique",
    background: true,
    unique: true
});
db.getCollection("auth_notify_category").createIndex({
    appId: NumberInt("1"),
    categoryName: NumberInt("1")
}, {
    name: "ix_appId_categoryName_unique",
    background: true,
    unique: true
});
