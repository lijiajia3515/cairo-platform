db.getCollection("auth_serial").drop();
db.createCollection("auth_serial", {
    validator: {
        $jsonSchema: {
            title: "序列号",
            description: "序列号",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                namespace: {
                    bsonType: "string",
                    title: "命名空间",
                    description: "必填，字符串"
                },
                key: {
                    bsonType: "string",
                    title: "标识",
                    description: "必填，字符串"
                },
                value: {
                    bsonType: "long",
                    title: "值",
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
                "namespace",
                "key",
                "value"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_serial").createIndex({
    namespace: NumberInt("1"),
    key: NumberInt("1")
}, {
    name: "ix_namespace_key_unique",
    background: true,
    unique: true
});
