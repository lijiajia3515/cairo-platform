db.getCollection("auth_permission").drop();
db.createCollection("auth_permission", {
    validator: {
        $jsonSchema: {
            title: "功能权限",
            description: "功能权限",
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
                subappId: {
                    bsonType: "string",
                    title: "子应用ID",
                    description: "必填，字符串，所属子应用的唯一标识"
                },
                subappVersion: {
                    bsonType: "string",
                    title: "子应用版本",
                    description: "必填，字符串，所属子应用的版本号"
                },
                menuId: {
                    bsonType: "string",
                    title: "菜单ID",
                    description: "必填，字符串，所属菜单的唯一标识"
                },
                permissionId: {
                    bsonType: "string",
                    title: "功能权限ID 必填",
                    description: "必填，字符串，clientId+permission=唯一"
                },
                permissionName: {
                    bsonType: "string",
                    title: "功能权限名称 必填",
                    description: "必填，字符串"
                },
                authorities: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string"
                    },
                    title: "权限值",
                    description: "必填（可为 null），字符串数组，选填"
                },
                type: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "类型",
                    description: "可空，字符串，类型（read=读，write=写，operator=操作）选填"
                },
                defaultPermission: {
                    bsonType: "bool",
                    title: "是否默认权限",
                    description: "必填，布尔值（true/false）"
                },
                hiddenPermission: {
                    bsonType: "bool",
                    title: "是否隐藏权限",
                    description: "必填，布尔值（true/false）"
                },
                icon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "图标",
                    description: "可空，字符串，图标资源地址"
                },
                sort: {
                    bsonType: [
                        "null",
                        "long"
                    ],
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
                "appId",
                "endpointId",
                "subappId",
                "subappVersion",
                "menuId",
                "permissionId",
                "permissionName",
                "authorities",
                "defaultPermission",
                "hiddenPermission",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_permission").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1")
}, {
    name: "ix_appId_endpointId_subappId_subappVersion",
    background: true
});
db.getCollection("auth_permission").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1"),
    permissionId: NumberInt("1")
}, {
    name: "ix_appId_endpointId_subappId_subappVersion_permissionId_unique",
    background: true,
    unique: true
});
