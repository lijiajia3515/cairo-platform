db.getCollection("auth_tenant_app_role_permission").drop();
db.createCollection("auth_tenant_app_role_permission", {
    validator: {
        $jsonSchema: {
            title: "企业角色权限",
            description: "企业角色权限",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                tenantId: {
                    bsonType: "string",
                    title: "企业ID",
                    description: "必填，字符串"
                },
                appId: {
                    bsonType: "string",
                    title: "应用ID",
                    description: "必填，字符串"
                },
                roleId: {
                    bsonType: "string",
                    title: "角色ID",
                    description: "必填，字符串"
                },
                endpointId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "终端ID",
                    description: "必填（可为 null），字符串"
                },
                subappId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "子应用ID",
                    description: "必填（可为 null），字符串"
                },
                subappVersion: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "子应用版本",
                    description: "必填（可为 null），字符串"
                },
                permissionIds: {
                    bsonType: "array",
                    items: {
                        bsonType: "string",
                        title: "功能权限ID",
                        description: "功能权限ID"
                    },
                    title: "功能权限ID数组",
                    description: "必填，字符串数组"
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
db.getCollection("auth_tenant_app_role_permission").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1")
}, {
    name: "ix_tenantId_appId",
    background: true
});
db.getCollection("auth_tenant_app_role_permission").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    roleId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1")
}, {
    name: "ix_tenantId_appId_roleId_endpointId_subappId_subappVersion_unique",
    background: true,
    unique: true
});
