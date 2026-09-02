import { useCookie } from "v3hooks";


export const setToken = (value) => {
  const token = useCookie(_this.cookie.token, {
    defaultValue: value,
    watch: true,
    expires: 999,
  });
  return token;
}

export const getToken = () => {
  const state = useCookie(_this.cookie.token);
  return state;
}


export const setRefreshToken = (value) => {
  const token = useCookie(_this.cookie.refresh_token, {
    defaultValue: value,
    watch: true,
    expires: 20,
  });
  return token;
}

export const getRefreshToken = () => {
  const state = useCookie(_this.cookie.refresh_token);
  return state;
}


export const setTokenType = (value) => {
  const token = useCookie(_this.cookie.token_type, {
    defaultValue: value,
    watch: true,
    expires: 999,
  });
  return token;
}

export const getTokenType = () => {
  const state = useCookie(_this.cookie.token_type);
  return state;
}

export const setAuthType = (value) => {
  const token = useCookie(_this.cookie.auth_type, {
    defaultValue: value,
    watch: true,
    expires: 999,
  });
  return token;
}

export const getAuthType = () => {
  const state = useCookie(_this.cookie.auth_type);
  return state;
}

export const setAppId = (value) => {
  const token = useCookie(_this.cookie.app_id, {
    defaultValue: value,
    watch: true,
    expires: 999,
  });
  return token;
}

export const getAppId = () => {
  const state = useCookie(_this.cookie.app_id);
  return state;
}

export const setEndpointId = (value) => {
  const token = useCookie(_this.cookie.endpoint_id, {
    defaultValue: value,
    watch: true,
    expires: 999,
  });
  return token;
}

export const getEndpointId = () => {
  const state = useCookie(_this.cookie.endpoint_id);
  return state;
}