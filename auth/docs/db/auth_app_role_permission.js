db.getCollection("auth_app_role_permission").drop();
db.createCollection("auth_app_role_permission", {
    validator: {
        $jsonSchema: {
            title: "角色权限",
            description: "角色权限",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                appId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "应用ID",
                    description: "必填（可为 null），字符串，所属应用的唯一标识"
                },
                roleId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "角色ID",
                    description: "必填（可为 null），字符串，所属角色的唯一标识"
                },
                endpointId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "终端ID",
                    description: "必填（可为 null），字符串，所属终端的唯一标识"
                },
                subappId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "子应用ID",
                    description: "必填（可为 null），字符串，所属子应用的唯一标识"
                },
                subappVersion: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "子应用版本",
                    description: "必填（可为 null），字符串，所属子应用的版本号"
                },
                permissionIds: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "功能权限ID",
                        description: "功能权限ID"
                    },
                    title: "功能权限集合",
                    description: "必填（可为 null），字符串数组，关联功能权限的唯一标识数组"
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
                "roleId",
                "endpointId",
                "subappId",
                "subappVersion",
                "permissionIds",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_app_role_permission").createIndex({
    appId: NumberInt("1")
}, {
    name: "ix_appId",
    background: true
});
db.getCollection("auth_app_role_permission").createIndex({
    appId: NumberInt("1"),
    roleId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1")
}, {
    name: "ix_appId_roleId_endpointId_subappId_subappVersion_unique",
    background: true,
    unique: true
});
