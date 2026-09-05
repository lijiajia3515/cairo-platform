db.getCollection("auth_tenant_app_department").drop();
db.createCollection("auth_tenant_app_department", {
    validator: {
        $jsonSchema: {
            title: "部门",
            description: "部门",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
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
                parentId: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "父级ID",
                    description: "必填（可为 null），字符串，父级节点的唯一标识"
                },
                root: {
                    bsonType: "bool",
                    title: "根节点",
                    description: "可空，布尔值（true/false），根节点标识"
                },
                departmentId: {
                    bsonType: "string",
                    title: "部门ID",
                    description: "必填，字符串，所属部门的唯一标识"
                },
                departmentName: {
                    bsonType: "string",
                    title: "部门名称",
                    description: "必填，字符串"
                },
                remark: {
                    bsonType: [
                        "null",
                        "string"
                    ],
                    title: "备注",
                    description: "必填（可为 null），字符串，备注信息"
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
                "tenantId",
                "appId",
                "parentId",
                "departmentId",
                "departmentName",
                "remark",
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
db.getCollection("auth_tenant_app_department").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1")
}, {
    name: "ix_tenantId_appId",
    background: true
});
db.getCollection("auth_tenant_app_department").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    departmentId: NumberInt("-1")
}, {
    name: "ix_tenantId_appId_departmentId_unique",
    background: true,
    unique: true
});
db.getCollection("auth_tenant_app_department").createIndex({
    tenantId: NumberInt("1"),
    appId: NumberInt("1"),
    parentId: NumberInt("1"),
    departmentName: NumberInt("1")
}, {
    name: "ix_tenantId_appId_parentId_departmentName_unique",
    background: true,
    unique: true
});
