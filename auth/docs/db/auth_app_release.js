db.getCollection("auth_app_release").drop();
db.createCollection("auth_app_release", {
    validator: {
        $jsonSchema: {
            title: "应用发行",
            description: "应用发行",
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
                endpointId: {
                    bsonType: "string",
                    title: "终端ID",
                    description: "必填，字符串，所属终端的唯一标识"
                },
                type: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "类型",
                    description: "可空，字符串，类型标识"
                },
                appVersion: {
                    bsonType: "string",
                    title: "app版本",
                    description: "必填，字符串，应用版本号"
                },
                releaseVersion: {
                    bsonType: "bool",
                    title: "是否发行版本",
                    description: "必填，布尔值（true/false），true-是，否-预览版本"
                },
                latestVersion: {
                    bsonType: "bool",
                    title: "是否未最新版本",
                    description: "必填，布尔值（true/false），true 表示最新版本"
                },
                title: {
                    bsonType: "string",
                    title: "标题",
                    description: "必填，字符串"
                },
                remark: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "备注",
                    description: "可空，字符串，备注信息"
                },
                force: {
                    bsonType: "bool",
                    title: "是否强制更新",
                    description: "必填，布尔值（true/false），true 表示强制更新"
                },
                webUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "网页端访问地址",
                    description: "可空，字符串，Web 端地址"
                },
                androidApkUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "安卓安装包下载地址",
                    description: "可空，字符串，Android 安装包地址"
                },
                iosAppStoreUrl: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "ios应用商店跳转地址",
                    description: "可空，字符串，iOS App Store 地址"
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
                "endpointId",
                "appVersion",
                "releaseVersion",
                "latestVersion",
                "title",
                "force",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_app_release").createIndex({
    endpointId: NumberInt("1"),
    appId: NumberInt("1"),
    type: NumberInt("1")
}, {
    name: "ix_endpointId_appId_type",
    background: true
});
db.getCollection("auth_app_release").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    type: NumberInt("1"),
    releaseVersion: NumberInt("1"),
    appVersion: NumberInt("1")
}, {
    name: "ix_appId_endpointId_type_releaseVersion_appVersion_unique",
    unique: true
});
