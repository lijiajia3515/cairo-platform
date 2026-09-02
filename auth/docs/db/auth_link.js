db.getCollection("auth_link").drop();
db.createCollection("auth_link", {
    validator: {
        $jsonSchema: {
            title: "短链接",
            description: "短链接",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "可空，对象ID，MongoDB 文档主键"
                },
                linkId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "链接ID",
                    description: "必填（可为 null），字符串，链接唯一标识"
                },
                linkUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "链接url",
                    description: "必填（可为 null），字符串，跳转链接地址"
                },
                shortUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "短链url",
                    description: "必填（可为 null），字符串，短链接地址"
                },
                accessCount: {
                    bsonType: [
                        "int",
                        "long"
                    ],
                    title: "访问次数",
                    description: "必填，整数"
                },
                lastAccessTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "最后访问时间",
                    description: "可空，日期时间"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
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
                "linkId",
                "linkUrl",
                "shortUrl",
                "accessCount",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_link").createIndex({
    linkId: NumberInt("1")
}, {
    name: "ix_linkId_unique",
    background: true,
    unique: true
});
