db.getCollection("auth_biz_dict_item").drop();
db.createCollection("auth_biz_dict_item", {
    validator: {
        $jsonSchema: {
            title: "业务级字典项",
            description: "业务级字典项",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                tenantId: {
                    bsonType: "string",
                    title: "租户ID",
                    description: "必填，字符串，所属租户的唯一标识"
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
                parentItemId: {
                    bsonType: "string",
                    title: "父级字典项ID",
                    description: "必填，字符串"
                },
                itemId: {
                    bsonType: "string",
                    title: "字典项ID",
                    description: "必填，字符串"
                },
                itemName: {
                    bsonType: "string",
                    title: "字典名称",
                    description: "必填，字符串"
                },
                editable: {
                    bsonType: "bool",
                    title: "是否允许编辑",
                    description: "必填，布尔值（true/false）"
                },
                depth: {
                    bsonType: "int",
                    title: "深度",
                    description: "必填，整数，树层级深度"
                },
                leftNo: {
                    bsonType: "int",
                    title: "左值",
                    description: "必填，整数，树结构左值"
                },
                rightNo: {
                    bsonType: "int",
                    title: "右值",
                    description: "必填，整数，树结构右值"
                },
                remark: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "备注",
                    description: "可空，字符串，备注信息"
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
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                reductionRemark: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "还原备注",
                    description: "可空，字符串"
                },
                reductionIcon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "还原图标",
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
                reductionItemName: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "还原字典名称",
                    description: "可空，字符串"
                },
                isSync: {
                    bsonType: "bool",
                    title: "是否同步字典",
                    description: "必填，布尔值（true/false）"
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
                "parentItemId",
                "itemId",
                "itemName",
                "editable",
                "depth",
                "leftNo",
                "rightNo",
                "enabled",
                "isSync",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_biz_dict_item").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    dictId: NumberInt("1"),
    itemId: NumberInt("1")
}, {
    name: "ix_tenantId_appId_dictId_itemId_unique",
    unique: true
});
db.getCollection("auth_biz_dict_item").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    dictId: NumberInt("1"),
    itemName: NumberInt("1")
}, {
    name: "ix_tenantId_appId_dictId_itemName_unique",
    unique: true
});
db.getCollection("auth_biz_dict_item").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    dictId: NumberInt("1"),
    leftNo: NumberInt("1")
}, {
    name: "ix_tenantId_appId_dictId_leftNo_unique",
    unique: true
});
db.getCollection("auth_biz_dict_item").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    dictId: NumberInt("1"),
    rightNo: NumberInt("1")
}, {
    name: "ix_tenantId_appId_dictId_rightNo_unique",
    unique: true
});
