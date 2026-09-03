db.getCollection("auth_sys_dict").drop();
db.createCollection("auth_sys_dict", {
    validator: {
        $jsonSchema: {
            title: "系统级字典",
            description: "系统级字典",
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
                dictId: {
                    bsonType: "string",
                    title: "字典ID",
                    description: "必填，字符串，所属数据字典的唯一标识"
                },
                dictType: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "字典类型(SYSTEM,BIZ_TEMPLATE)",
                    description: "可空，字符串"
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
                enabled: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "启用状态",
                    description: "可空，布尔值（true/false），启用为 true，禁用为 false"
                },
                leftNo: {
                    bsonType: [
                        "null",
                        "int"
                    ],
                    title: "左值",
                    description: "可空，整数，树结构左值"
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
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否允许添加子项",
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
                        createAccountId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "创建账号ID",
                            description: "必填（可为 null），字符串，创建该记录的账号ID"
                        },
                        updateAccountId: {
                            bsonType: [
                                "null",
                                "string"
                            ],
                            title: "更新账号ID",
                            description: "必填（可为 null），字符串，最后更新该记录的账号ID"
                        }
                    },
                    required: [
                        "createAccountId",
                        "createTime",
                        "updateAccountId",
                        "updateTime"
                    ]
                }
            },
            required: [
                "_id",
                "appId",
                "dictId",
                "dictName",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_sys_dict").createIndex({
    appId: NumberInt("1"),
    dictId: NumberInt("1")
}, {
    name: "ix_appId_dictId_unique",
    unique: true
});
