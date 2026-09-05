// ============================================================================
// Cairo Platform - MongoDB Schema Initialization Script
// ============================================================================
// Usage (must run from THIS directory, because mongosh load() resolves
//   relative paths against the current working directory):
//   cd docs/auth/db
//   mongosh <connection_string> --file init.js
//   or in mongosh shell: load("init.js")
//
// WARNING: This script will DROP and RECREATE all collections.
//          All existing data will be lost!
// ============================================================================

print("========================================");
print("Cairo Platform - DB Schema Init");
print("========================================");
print("WARNING: All collections will be dropped and recreated.");
print("----------------------------------------");

// --------------------------------------------------------------------------
// Auth - Account
// --------------------------------------------------------------------------
print("[1/71] auth_account.js");
load("auth_account.js");
print("[2/71] auth_account_authorization.js");
load("auth_account_authorization.js");
print("[3/71] auth_account_login_log.js");
load("auth_account_login_log.js");
print("[4/71] auth_account_password.js");
load("auth_account_password.js");
print("[5/71] auth_account_sns.js");
load("auth_account_sns.js");

// --------------------------------------------------------------------------
// Auth - Action Permission
// --------------------------------------------------------------------------
print("[6/71] auth_permission.js");
load("auth_permission.js");

// --------------------------------------------------------------------------
// Auth - App
// --------------------------------------------------------------------------
print("[7/71] auth_app.js");
load("auth_app.js");
print("[8/71] auth_app_department.js");
load("auth_app_department.js");
print("[9/71] auth_endpoint.js");
load("auth_endpoint.js");
print("[10/71] auth_app_role.js");
load("auth_app_role.js");
print("[11/71] auth_app_role_permission.js");
load("auth_app_role_permission.js");

// --------------------------------------------------------------------------
// Auth - App User
// --------------------------------------------------------------------------
print("[12/71] auth_app_user.js");
load("auth_app_user.js");
print("[13/71] auth_app_user_authorization.js");
load("auth_app_user_authorization.js");
print("[14/71] auth_app_user_login_log.js");
load("auth_app_user_login_log.js");
print("[15/71] auth_app_user_tag.js");
load("auth_app_user_tag.js");

// --------------------------------------------------------------------------
// Auth - Client
// --------------------------------------------------------------------------
print("[16/71] auth_client.js");
load("auth_client.js");
print("[17/71] auth_client_login_log.js");
load("auth_client_login_log.js");

// --------------------------------------------------------------------------
// Auth - Organization
// --------------------------------------------------------------------------
print("[18/71] auth_menu.js");
load("auth_menu.js");

// --------------------------------------------------------------------------
// Auth - OAuth2 / SNS / Serial
// --------------------------------------------------------------------------
print("[19/71] auth_oauth2_authorization.js");
load("auth_oauth2_authorization.js");
print("[20/71] auth_sns_provider.js");
load("auth_sns_provider.js");
print("[21/71] auth_sns_token.js");
load("auth_sns_token.js");
print("[22/71] auth_serial.js");
load("auth_serial.js");

// --------------------------------------------------------------------------
// Auth - Sub App
// --------------------------------------------------------------------------
print("[23/71] auth_subapp.js");
load("auth_subapp.js");
print("[24/71] auth_subapp_version.js");
load("auth_subapp_version.js");

// --------------------------------------------------------------------------
// Auth - Tenant
// --------------------------------------------------------------------------
print("[25/71] auth_tenant.js");
load("auth_tenant.js");
print("[26/71] auth_tenant_app.js");
load("auth_tenant_app.js");
print("[27/71] auth_tenant_app_department.js");
load("auth_tenant_app_department.js");
print("[28/71] auth_tenant_app_department_template.js");
load("auth_tenant_app_department_template.js");
print("[29/71] auth_tenant_endpoint.js");
load("auth_tenant_endpoint.js");

// --------------------------------------------------------------------------
// Auth - Tenant App Role
// --------------------------------------------------------------------------
print("[30/71] auth_tenant_app_role.js");
load("auth_tenant_app_role.js");
print("[31/71] auth_tenant_app_role_permission.js");
load("auth_tenant_app_role_permission.js");
print("[32/71] auth_tenant_app_role_template.js");
load("auth_tenant_app_role_template.js");
print("[33/71] auth_tenant_app_role_template_permission.js");
load("auth_tenant_app_role_template_permission.js");

// --------------------------------------------------------------------------
// Auth - Tenant App User
// --------------------------------------------------------------------------
print("[34/71] auth_tenant_app_user.js");
load("auth_tenant_app_user.js");
print("[35/71] auth_tenant_app_user_authorization.js");
load("auth_tenant_app_user_authorization.js");
print("[36/71] auth_tenant_app_user_login_log.js");
load("auth_tenant_app_user_login_log.js");
print("[37/71] auth_tenant_app_user_sns.js");
load("auth_tenant_app_user_sns.js");
print("[38/71] auth_tenant_app_user_tag.js");
load("auth_tenant_app_user_tag.js");
print("[39/71] auth_tenant_app_user_template.js");
load("auth_tenant_app_user_template.js");

// --------------------------------------------------------------------------
// Auth - Tenant Organization
// --------------------------------------------------------------------------
print("[40/71] auth_tenant_subapp.js");
load("auth_tenant_subapp.js");

// --------------------------------------------------------------------------
// System - App Release
// --------------------------------------------------------------------------
print("[41/71] auth_app_release.js");
load("auth_app_release.js");

// --------------------------------------------------------------------------
// System - Area
// --------------------------------------------------------------------------
print("[42/71] auth_area.js");
load("auth_area.js");
print("[43/71] auth_area_level4.js");
load("auth_area_level4.js");

// --------------------------------------------------------------------------
// System - Biz Dict
// --------------------------------------------------------------------------
print("[44/71] auth_biz_dict.js");
load("auth_biz_dict.js");
print("[45/71] auth_biz_dict_item.js");
load("auth_biz_dict_item.js");

// --------------------------------------------------------------------------
// System - Biz Log
// --------------------------------------------------------------------------
print("[46/71] auth_biz_log_account.js");
load("auth_biz_log_account.js");
print("[47/71] auth_biz_log_app.js");
load("auth_biz_log_app.js");
print("[48/71] auth_biz_log_client.js");
load("auth_biz_log_client.js");
print("[49/71] auth_biz_log_open.js");
load("auth_biz_log_open.js");
print("[50/71] auth_biz_log_subapp.js");
load("auth_biz_log_subapp.js");
print("[51/71] auth_biz_log_tenant_app.js");
load("auth_biz_log_tenant_app.js");
print("[52/71] auth_biz_log_tenant_subapp.js");
load("auth_biz_log_tenant_subapp.js");

// --------------------------------------------------------------------------
// System - Link
// --------------------------------------------------------------------------
print("[53/71] auth_link.js");
load("auth_link.js");

// --------------------------------------------------------------------------
// System - Notification
// --------------------------------------------------------------------------
print("[54/71] auth_notify_category.js");
load("auth_notify_category.js");
print("[55/71] auth_notify_record_app.js");
load("auth_notify_record_app.js");
print("[56/71] auth_notify_record_tenant_app.js");
load("auth_notify_record_tenant_app.js");
print("[57/71] auth_notify_template.js");
load("auth_notify_template.js");
print("[58/71] auth_notify_template_args.js");
load("auth_notify_template_args.js");

// --------------------------------------------------------------------------
// System - Office File
// --------------------------------------------------------------------------
print("[59/71] auth_office_file.js");
load("auth_office_file.js");
print("[60/71] auth_office_file_version.js");
load("auth_office_file_version.js");

// --------------------------------------------------------------------------
// System - SMS
// --------------------------------------------------------------------------
print("[61/71] auth_sms_msg.js");
load("auth_sms_msg.js");
print("[62/71] auth_sms_template.js");
load("auth_sms_template.js");
print("[63/71] auth_sms_template_arg.js");
load("auth_sms_template_arg.js");

// --------------------------------------------------------------------------
// System - Dict
// --------------------------------------------------------------------------
print("[64/71] auth_sys_dict.js");
load("auth_sys_dict.js");
print("[65/71] auth_sys_dict_item.js");
load("auth_sys_dict_item.js");

// --------------------------------------------------------------------------
// System - WXMP (WeChat Mini Program)
// --------------------------------------------------------------------------
print("[66/71] auth_wxmp_app_user.js");
load("auth_wxmp_app_user.js");
print("[67/71] auth_wxmp_provider.js");
load("auth_wxmp_provider.js");
print("[68/71] auth_wxmp_template_msg.js");
load("auth_wxmp_template_msg.js");
print("[69/71] auth_wxmp_template_msg_args.js");
load("auth_wxmp_template_msg_args.js");
print("[70/71] auth_wxmp_template_msg_record.js");
load("auth_wxmp_template_msg_record.js");
print("[71/71] auth_wxmp_tenant_app_user.js");
load("auth_wxmp_tenant_app_user.js");

print("----------------------------------------");
print("========================================");
print("DB Schema Init Complete - 71 collections initialized.");
print("========================================");

