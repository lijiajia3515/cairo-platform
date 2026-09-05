// 服务端字段校验错误 -> 表单内联标红（TDesign t-form）
//
// 用法：表单提交的 catch 里，遇到 `Params.ValidationFailed`（400，data 为字段错误数组）
// 调 applyFieldErrors(formRef, err.data)，把后端字段错误映射到对应 t-form-item。
// 前提：t-form-item 必须带 name 属性且与后端 DTO 字段名一致。
//
// fieldErrors 元素结构（后端 CairoRestControllerAdvice.methodArgumentNotValidException）：
//   { field: 'menuName', valid: 'NotBlank', message: '菜单名不能为空', rejectValue: null }
export function applyFieldErrors(formRef, fieldErrors) {
  if (!formRef?.value || !Array.isArray(fieldErrors) || !fieldErrors.length) return;
  const validateMessage = {};
  fieldErrors.forEach(({ field, message: msg }) => {
    if (!field) return;
    validateMessage[field] = [{ type: 'error', message: msg || '参数校验失败' }];
  });
  if (Object.keys(validateMessage).length) {
    formRef.value.setValidateMessage(validateMessage);
  }
}

// 判断被拒绝的错误（status.js default 分支 reject 的 error.response.data）是否为字段校验失败
export function isValidationFailed(err) {
  return err?.code === 'Params.ValidationFailed' && Array.isArray(err?.data);
}
