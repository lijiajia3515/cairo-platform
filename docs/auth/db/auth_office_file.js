db.getCollection("auth_office_file").drop();
db.createCollection("auth_office_file", {
    validator: {
        $jsonSchema: {
            title: "Office文件",
            description: "Office文件",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                fileId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "ID值",
                    description: "必填（可为 null），字符串"
                },
                fileName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "名称",
                    description: "必填（可为 null），字符串，文件名称"
                },
                type: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "WPS文件类型",
                    description: "可空，字符串，类型标识"
                },
                sourceFilePath: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "源地址",
                    description: "可空，字符串，源文件存储路径"
                },
                filepath: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "文件下载地址",
                    description: "必填（可为 null），字符串，文件存储路径"
                },
                fileVersion: {
                    bsonType: [
                        "null",
                        "int"
                    ],
                    title: "文件版本号",
                    description: "必填（可为 null），整数，文件版本"
                },
                size: {
                    bsonType: [
                        "null",
                        "int"
                    ],
                    title: "文件大小",
                    description: "必填（可为 null），整数，文件大小（字节）"
                },
                digest: {
                    bsonType: [
                        "null",
                        "object"
                    ],
                    title: "文档校验",
                    description: "可空，对象，文件摘要"
                },
                mode: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "对接模式",
                    description: "可空，字符串，文件模式"
                },
                tenantId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "企业ID",
                    description: "必填（可为 null），字符串，所属企业的唯一标识"
                },
                appId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用ID",
                    description: "必填（可为 null），字符串，所属应用的唯一标识"
                },
                createUserId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "创建用户ID",
                    description: "可空，字符串，创建该记录的用户ID"
                },
                updateUserId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "更新用户ID",
                    description: "可空，字符串，最后更新该记录的用户ID"
                },
                activeTime: {
                    bsonType: [
                        "null",
                        "date"
                    ],
                    title: "文件活跃时间",
                    description: "可空，日期时间，生效时间"
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
                "fileId",
                "fileName",
                "filepath",
                "fileVersion",
                "size",
                "tenantId",
                "appId",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_office_file").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1")
}, {
    name: "ix_tenantId_appId",
    background: true
});
db.getCollection("auth_office_file").createIndex({
    fileId: NumberInt("1")
}, {
    name: "ix_fileId_unique",
    background: true,
    unique: true
});
