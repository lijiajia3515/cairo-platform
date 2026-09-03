db.getCollection("auth_oauth2_authorization").drop();
db.createCollection("auth_oauth2_authorization", {
    validator: {
        $jsonSchema: {
            title: "oauth2授权",
            description: "oauth2授权",
            properties: {
                authorizedScopes: {
                    bsonType: "array",
                    items: {
                        bsonType: "string",
                        title: "范围值",
                        description: "范围值"
                    },
                    title: "授权范围",
                    description: "必填，字符串数组"
                },
                _id: {
                    bsonType: "string",
                    title: "数据库标识",
                    description: "必填，字符串，MongoDB 文档主键"
                },
                registeredClientId: {
                    bsonType: "string",
                    title: "客户端标识",
                    description: "必填，字符串"
                },
                principalName: {
                    bsonType: "string",
                    title: "凭证标识",
                    description: "必填，字符串"
                },
                authorizationGrantType: {
                    bsonType: "string",
                    title: "授权类型",
                    description: "必填，字符串"
                }
            },
            required: [
                "authorizedScopes",
                "_id",
                "registeredClientId",
                "principalName",
                "authorizationGrantType"
            ]
        }
    },
    validationLevel: "strict",
    validationAction: "error"
});
