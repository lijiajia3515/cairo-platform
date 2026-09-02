db.getCollection("auth_menu").drop();
db.createCollection("auth_menu", {
    validator: {
        $jsonSchema: {
            title: "菜单",
            description: "菜单",
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
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "菜单ID",
                    description: "必填（可为 null），字符串，所属菜单的唯一标识"
                },
                parentId: {
                    bsonType: "string",
                    title: "父级ID",
                    description: "必填，字符串，父级节点的唯一标识"
                },
                menuName: {
                    bsonType: "string",
                    title: "菜单名称",
                    description: "必填，字符串"
                },
                path: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "前端路径/页面地址外部地址",
                    description: "可空，字符串，路由路径"
                },
                component: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "组件名",
                    description: "可空，字符串，前端组件路径"
                },
                icon: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "图标",
                    description: "可空，字符串，图标资源地址"
                },
                tags: {
                    bsonType: [
                        "null",
                        "array"
                    ],
                    items: {
                        bsonType: "string",
                        title: "tags项",
                        description: "tags项"
                    },
                    title: "标签, 非必填",
                    description: "可空，字符串数组，标签列表"
                },
                hiddenMenu: {
                    bsonType: [
                        "null",
                        "bool"
                    ],
                    title: "是否隐藏",
                    description: "必填（可为 null），布尔值（true/false）"
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
                depth: {
                    bsonType: "int",
                    title: "深度",
                    description: "必填，整数，树层级深度"
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
                "parentId",
                "menuName",
                "hiddenMenu",
                "leftNo",
                "rightNo",
                "depth",
                "metadata"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_menu").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    leftNo: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1")
}, {
    name: "ix_appId_endpointId_leftNo_subappId_subappVersion_unique",
    unique: true
});
db.getCollection("auth_menu").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    menuId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1")
}, {
    name: "ix_appId_endpointId_menuId_subappId_subappVersion_unique",
    background: true,
    unique: true
});
db.getCollection("auth_menu").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1")
}, {
    name: "ix_appId_endpointId_subappId_subappVersion"
});
db.getCollection("auth_menu").createIndex({
    appId: NumberInt("1"),
    endpointId: NumberInt("1"),
    subappId: NumberInt("1"),
    subappVersion: NumberInt("1"),
    rightNo: NumberInt("1")
}, {
    name: "ix_appId_endpointId_subappId_subappVersion_rightNo_unique",
    background: true,
    unique: true
});
