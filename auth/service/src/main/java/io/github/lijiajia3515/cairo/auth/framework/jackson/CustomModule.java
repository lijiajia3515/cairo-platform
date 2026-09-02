package io.github.lijiajia3515.cairo.auth.framework.jackson;

import com.fasterxml.jackson.core.json.PackageVersion;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;
import org.springframework.security.oauth2.core.AuthorizationGrantType;

public class CustomModule extends SimpleModule {public CustomModule() {
	super(PackageVersion.VERSION);
	addSerializer(ObjectId.class, new ObjectIdSerializer());
	addDeserializer(ObjectId.class, new ObjectIdDeserializer());

	addSerializer(AuthorizationGrantType.class, new AuthorizationGrantTypeSerializer());
	addDeserializer(AuthorizationGrantType.class, new AuthorizationGrantTypeDeserializer());
}
	@Override
	public String getModuleName() {
		return getClass().getSimpleName();
	}


}
