// ============================================================================
// Cairo Platform - MongoDB Schema Initialization Script
// ============================================================================
// Usage:
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
print(");

// --------------------------------------------------------------------------
// Auth - Account
// --------------------------------------------------------------------------
print("[1/71] auth_account");
load("auth_account.js");
print("[2/71] auth_account_authorization");
load("auth_account_authorization.js");
print("[3/71] auth_account_login_log");
load("auth_account_login_log.js");
print("[4/71] auth_account_password");
load("auth_account_password.js");
print("[5/71] auth_account_sns");
load("auth_account_sns.js");

// --------------------------------------------------------------------------
// Auth - Action Permission
// --------------------------------------------------------------------------
print("[6/71] auth_permission");
load("auth_permission.js");

// --------------------------------------------------------------------------
// Auth - App
// --------------------------------------------------------------------------
print("[7/71] auth_app");
load("auth_app.js");
print("[8/71] auth_app_department");
load("auth_app_department.js");
print("[9/71] auth_endpoint");
load("auth_endpoint.js");
print("[10/71] auth_app_role");
load("auth_app_role.js");
print("[11/71] auth_app_role_permission");
load("auth_app_role_permission.js");

// --------------------------------------------------------------------------
// Auth - App User
// --------------------------------------------------------------------------
print("[12/71] auth_app_user");
load("auth_app_user.js");
print("[13/71] auth_app_user_authorization");
load("auth_app_user_authorization.js");
print("[14/71] auth_app_user_login_log");
load("auth_app_user_login_log.js");
print("[15/71] auth_app_user_tag");
load("auth_app_user_tag.js");

// --------------------------------------------------------------------------
// Auth - Client
// --------------------------------------------------------------------------
print("[16/71] auth_client");
load("auth_client.js");
print("[17/71] auth_client_login_log");
load("auth_client_login_log.js");

// --------------------------------------------------------------------------
// Auth - Organization
// --------------------------------------------------------------------------
print("[18/71] auth_menu");
load("auth_menu.js");

// --------------------------------------------------------------------------
// Auth - OAuth2 / SNS / Serial
// --------------------------------------------------------------------------
print("[19/71] auth_oauth2_authorization");
load("auth_oauth2_authorization.js");
print("[20/71] auth_sns_provider");
load("auth_sns_provider.js");
print("[21/71] auth_sns_token");
load("auth_sns_token.js");
print("[22/71] auth_serial");
load("auth_serial.js");

// --------------------------------------------------------------------------
// Auth - Role
// --------------------------------------------------------------------------

// --------------------------------------------------------------------------
// Auth - Sub App
// --------------------------------------------------------------------------
print("[23/71] auth_subapp");
load("auth_subapp.js");
print("[24/71] auth_subapp_version");
load("auth_subapp_version.js");

// --------------------------------------------------------------------------
// Auth - Tenant
// --------------------------------------------------------------------------
print("[25/71] auth_tenant");
load("auth_tenant.js");
print("[26/71] auth_tenant_app");
load("auth_tenant_app.js");
print("[27/71] auth_tenant_app_department");
load("auth_tenant_app_department.js");
print("[28/71] auth_tenant_app_department_template");
load("auth_tenant_app_department_template.js");
print("[29/71] auth_tenant_endpoint");
load("auth_tenant_endpoint.js");

// --------------------------------------------------------------------------
// Auth - Tenant App Role
// --------------------------------------------------------------------------
print("[30/71] auth_tenant_app_role");
load("auth_tenant_app_role.js");
print("[31/71] auth_tenant_app_role_permission");
load("auth_tenant_app_role_permission.js");
print("[32/71] auth_tenant_app_role_template");
load("auth_tenant_app_role_template.js");
print("[33/71] auth_tenant_app_role_template_permission");
load("auth_tenant_app_role_template_permission.js");

// --------------------------------------------------------------------------
// Auth - Tenant App User
// --------------------------------------------------------------------------
print("[34/71] auth_tenant_app_user");
load("auth_tenant_app_user.js");
print("[35/71] auth_tenant_app_user_authorization");
load("auth_tenant_app_user_authorization.js");
print("[36/71] auth_tenant_app_user_login_log");
load("auth_tenant_app_user_login_log.js");
print("[37/71] auth_tenant_app_user_sns");
load("auth_tenant_app_user_sns.js");
print("[38/71] auth_tenant_app_user_tag");
load("auth_tenant_app_user_tag.js");
print("[39/71] auth_tenant_app_user_template");
load("auth_tenant_app_user_template.js");

// --------------------------------------------------------------------------
// Auth - Tenant Organization
// --------------------------------------------------------------------------
print("[40/71] auth_tenant_subapp");
load("auth_tenant_subapp.js");

// --------------------------------------------------------------------------
// Auth - User
// --------------------------------------------------------------------------

// --------------------------------------------------------------------------
// System - App Release
// --------------------------------------------------------------------------
print("[41/71] auth_app_release");
load("auth_app_release.js");

// --------------------------------------------------------------------------
// System - Area
// --------------------------------------------------------------------------
print("[42/71] auth_area");
load("auth_area.js");
print("[43/71] auth_area_level4");
load("auth_area_level4.js");

// --------------------------------------------------------------------------
// System - Biz Dict
// --------------------------------------------------------------------------
print("[44/71] auth_biz_dict");
load("auth_biz_dict.js");
print("[45/71] auth_biz_dict_item");
load("auth_biz_dict_item.js");

// --------------------------------------------------------------------------
// System - Biz Log
// --------------------------------------------------------------------------
print("[46/71] auth_biz_log_account");
load("auth_biz_log_account.js");
print("[47/71] auth_biz_log_app");
load("auth_biz_log_app.js");
print("[49/71] auth_biz_log_client");
load("auth_biz_log_client.js");
print("[50/71] auth_biz_log_open");
load("auth_biz_log_open.js");
print("[51/71] auth_biz_log_subapp");
load("auth_biz_log_subapp.js");
print("[52/71] auth_biz_log_tenant_app");
load("auth_biz_log_tenant_app.js");
print("[54/71] auth_biz_log_tenant_subapp");
load("auth_biz_log_tenant_subapp.js");

// --------------------------------------------------------------------------
// System - Link
// --------------------------------------------------------------------------
print("[55/71] auth_link");
load("auth_link.js");

// --------------------------------------------------------------------------
// System - Notification
// --------------------------------------------------------------------------
print("[56/71] auth_notify_category");
load("auth_notify_category.js");
print("[57/71] auth_notify_record_app");
load("auth_notify_record_app.js");
print("[58/71] auth_notify_record_tenant_app");
load("auth_notify_record_tenant_app.js");
print("[59/71] auth_notify_template");
load("auth_notify_template.js");
print("[60/71] auth_notify_template_args");
load("auth_notify_template_args.js");

// --------------------------------------------------------------------------
// System - Office File
// --------------------------------------------------------------------------
print("[61/71] auth_office_file");
load("auth_office_file.js");
print("[62/71] auth_office_file_version");
load("auth_office_file_version.js");

// --------------------------------------------------------------------------
// System - Serial
// --------------------------------------------------------------------------

// --------------------------------------------------------------------------
// System - SMS
// --------------------------------------------------------------------------
print("[63/71] auth_sms_msg");
load("auth_sms_msg.js");
print("[64/71] auth_sms_template");
load("auth_sms_template.js");
print("[65/71] auth_sms_template_arg");
load("auth_sms_template_arg.js");

// --------------------------------------------------------------------------
// System - Dict
// --------------------------------------------------------------------------
print("[66/71] auth_sys_dict");
load("auth_sys_dict.js");
print("[67/71] auth_sys_dict_item");
load("auth_sys_dict_item.js");

// --------------------------------------------------------------------------
// System - WPS File
// --------------------------------------------------------------------------

// --------------------------------------------------------------------------
// System - WXMP (WeChat Mini Program)
// --------------------------------------------------------------------------
print("[68/71] auth_wxmp_app_user");
load("auth_wxmp_app_user.js");
print("[69/71] auth_wxmp_provider");
load("auth_wxmp_provider.js");
print("[70/71] auth_wxmp_template_msg");
load("auth_wxmp_template_msg.js");
print("[71/71] auth_wxmp_template_msg_args");
load("auth_wxmp_template_msg_args.js");
print("[72/71] auth_wxmp_template_msg_record");
load("auth_wxmp_template_msg_record.js");
print("[73/71] auth_wxmp_tenant_app_user");
load("auth_wxmp_tenant_app_user.js");

print(");
print("========================================");
print("DB Schema Init Complete - 89 collections initialized.");
print("========================================");
