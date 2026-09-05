db.getCollection("auth_biz_dict").drop();
db.createCollection("auth_biz_dict", {
    validator: {
        $jsonSchema: {
            title: "业务级字典",
            description: "业务级字典",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                tenantId: {
                    bsonType: "string",
                    title: "企业ID",
                    description: "必填，字符串，所属企业的唯一标识"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串，所属应用的唯一标识"
                },
                dictId: {
                    bsonType: "string",
                    title: "字典ID",
                    description: "必填，字符串，所属数据字典的唯一标识"
                },
                dictName: {
                    bsonType: "string",
                    title: "字典名称",
                    description: "必填，字符串"
                },
                icon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "图标",
                    description: "可空，字符串，图标资源地址"
                },
                leftNo: {
                    bsonType: [
                        "null",
                        "int"
                    ],
                    title: "左值",
                    description: "可空，整数，树结构左值"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                rightNo: {
                    bsonType: [
                        "null",
                        "int"
                    ],
                    title: "右值",
                    description: "可空，整数，树结构右值"
                },
                isCreateItem: {
                    bsonType: "bool",
                    title: "是否允许添加子项",
                    description: "必填，布尔值（true/false）"
                },
                reductionDictName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "还原字典名称",
                    description: "可空，字符串"
                },
                reductionIcon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "还原图标值",
                    description: "可空，字符串"
                },
                reductionVersion: {
                    bsonType: [
                        "null",
                        "long"
                    ],
                    title: "还原版本",
                    description: "可空，整数"
                },
                syncVersion: {
                    bsonType: [
                        "null",
                        "long"
                    ],
                    title: "同步版本",
                    description: "可空，整数"
                },
                isSyncIcon: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否同步图标",
                    description: "可空，布尔值（true/false）"
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
                "tenantId",
                "appId",
                "dictId",
                "dictName",
                "enabled",
                "isCreateItem",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_biz_dict").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    dictId: NumberInt("1")
}, {
    name: "ix_tenantId_appId_dictId_unique",
    unique: true
});
db.getCollection("auth_biz_dict").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    dictName: NumberInt("1")
}, {
    name: "ix_tenantId_appId_dictName_unique",
    unique: true
});
