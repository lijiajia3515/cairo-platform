db.getCollection("auth_area").drop();
db.createCollection("auth_area", {
    validator: {
        $jsonSchema: {
            title: "区域",
            description: "区域",
            properties: {
                _id: {
                    bsonType: "objectId",
                    title: "数据库标识",
                    description: "必填，对象ID，MongoDB 文档主键"
                },
                areaId: {
                    bsonType: "string",
                    title: "区域ID（唯一",
                    description: "必填，字符串，主键ID）"
                },
                areaName: {
                    bsonType: "string",
                    title: "区域名称",
                    description: "必填，字符串"
                },
                shortAreaName: {
                    bsonType: "string",
                    title: "区域名称简称",
                    description: "必填，字符串"
                },
                pinYinPrefix: {
                    bsonType: "string",
                    title: "拼音前缀",
                    description: "必填，字符串"
                },
                pinYin: {
                    bsonType: "string",
                    title: "拼音",
                    description: "必填，字符串"
                },
                depth: {
                    bsonType: "int",
                    title: "深度",
                    description: "必填，整数，层级（1-省，2-市，3-区，4-街道）"
                },
                hot: {
                    bsonType: "bool",
                    title: "热门",
                    description: "必填，布尔值（true/false）"
                },
                enabled: {
                    bsonType: "bool",
                    title: "启用状态",
                    description: "必填，布尔值（true/false），启用为 true，禁用为 false"
                },
                sort: {
                    bsonType: [
                        "int",
                        "long"
                    ],
                    title: "排序值",
                    description: "必填，整数，用于列表展示排序"
                },
                parentAreaId: {
                    bsonType: "string",
                    title: "父级区域ID",
                    description: "必填，字符串"
                },
                areaIds: {
                    bsonType: "array",
                    title: "区域ID",
                    description: "必填，数组"
                },
                areaNames: {
                    bsonType: "array",
                    title: "区域名称集合",
                    description: "必填，数组"
                },
                shortAreaNames: {
                    bsonType: "array",
                    title: "区域名称简称集合",
                    description: "必填，数组"
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
                "areaId",
                "areaName",
                "shortAreaName",
                "pinYinPrefix",
                "pinYin",
                "depth",
                "hot",
                "enabled",
                "sort",
                "parentAreaId",
                "areaIds",
                "areaNames",
                "shortAreaNames"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
db.getCollection("auth_area").createIndex({
    areaId: NumberInt("1")
}, {
    name: "ix_areaId_unique",
    unique: true
});
db.getCollection("auth_area").createIndex({
    parentAreaId: NumberInt("1")
}, {
    name: "ix_parentAreaId",
    background: true
});
db.getCollection("auth_area").createIndex({
    parentAreaId: NumberInt("1"),
    areaName: NumberInt("1")
}, {
    name: "ix_parentAreaId_areaName_unique",
    background: true,
    unique: true
});
db.getCollection("auth_area").createIndex({
    parentAreaId: NumberInt("1"),
    shortAreaName: NumberInt("1")
}, {
    name: "ix_parentAreaId_shortAreaName_unique",
    background: true,
    unique: true
});
db.getCollection("auth_area").createIndex({
    parentAreaId: NumberInt("1"),
    sort: NumberInt("1")
}, {
    name: "ix_parentAreaId_sort_unique",
    background: true,
    unique: true
});
