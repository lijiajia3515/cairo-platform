db.getCollection("auth_subapp").drop();
db.createCollection("auth_subapp", {
    validator: {
        $jsonSchema: {
            title: "子应用",
            description: "子应用",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                id: {
                    bsonType: "string",
                    title: "主键ID",
                    description: "必填，字符串"
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
                subappId: {
                    bsonType: "string",
                    title: "子应用ID",
                    description: "必填，字符串，(appId,endpointId,subappId) 联合唯一"
                },
                subappName: {
                    bsonType: "string",
                    title: "子应用名称",
                    description: "必填，字符串"
                },
                subappIcon: {
                    bsonType: "string",
                    title: "图标",
                    description: "必填，字符串"
                },
                scope: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "准入范围",
                    description: "可空，字符串，模块开通策略（public 随终端自动可用 / tenant 需企业按模块开通），缺省视为 public"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                sort: {
                    bsonType: "int",
                    title: "排序值",
                    description: "可空，整数，用于列表展示排序"
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
                "id",
                "appId",
                "endpointId",
                "subappId",
                "subappName",
                "subappIcon",
                "enabled",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_subapp").createIndex({
    id: NumberInt("1")
}, {
    name: "ix_id_unique",
    background: true,
    unique: true
});
db.getCollection("auth_subapp").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1")
}, {
    name: "ix_appId_endpointId_subappId_unique",
    background: true,
    unique: true
});
