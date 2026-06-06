SET NAMES utf8mb4;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS employee_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE employee_db;

-- 创建管理员用户表
CREATE TABLE IF NOT EXISTS admin_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员用户表';

-- 创建部门表
CREATE TABLE IF NOT EXISTS department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '部门ID',
    name VARCHAR(50) NOT NULL COMMENT '部门全称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '部门编码',
    parent_id BIGINT DEFAULT NULL COMMENT '上级部门ID',
    manager_id BIGINT DEFAULT NULL COMMENT '责任人ID',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注说明',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id),
    INDEX idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 创建员工表
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '员工ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) NOT NULL UNIQUE COMMENT '手机号',
    department_id BIGINT DEFAULT NULL COMMENT '部门ID',
    department VARCHAR(50) DEFAULT NULL COMMENT '部门名称(冗余字段)',
    position VARCHAR(50) NOT NULL COMMENT '职位',
    hire_date DATE NOT NULL COMMENT '入职日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=在职, 0=离职',
    password VARCHAR(100) DEFAULT NULL COMMENT '登录密码',
    is_active TINYINT DEFAULT 0 COMMENT '激活状态: 1=已激活, 0=待激活',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工表';

-- 插入管理员账号 (用户名: admin, 密码: 123456, BCrypt加密)
INSERT INTO admin_user (username, password) VALUES ('admin', '$2a$10$dkilqkZsW9ZIi5OvuwpEYOtLQmO9EeqRH9zMeeTJ4q46g3xGh8m4e');

-- 插入部门示例数据
INSERT INTO department (name, code, parent_id, manager_id, remark, sort_order) VALUES
('总公司', 'HQ', NULL, NULL, '公司总部', 1),
('技术中心', 'TECH', 1, 10, '负责产品研发和技术支持', 1),
('产品部', 'PROD', 1, 2, '负责产品规划和设计', 2),
('市场部', 'MARKET', 1, 4, '负责市场营销和销售', 3),
('人力资源部', 'HR', 1, 5, '负责人力资源管理', 4),
('财务部', 'FIN', 1, 6, '负责财务管理', 5),
('后端开发组', 'DEV-BACKEND', 2, 1, '负责后端系统开发', 1),
('前端开发组', 'DEV-FRONTEND', 2, 3, '负责前端界面开发', 2),
('测试组', 'QA', 2, 7, '负责质量保证和测试', 3),
('设计部', 'DESIGN', 3, 8, '负责UI/UX设计', 4),
('运营部', 'OPS', 1, 9, '负责产品运营和客户服务', 6);

-- 插入员工示例数据 (初始密码为手机号后6位, BCrypt加密)
INSERT INTO employee (name, email, phone, department_id, department, position, hire_date, status, password, is_active) VALUES
('张伟', 'zhangwei@company.com', '13800138001', 7, '后端开发组', 'Java开发工程师', '2022-03-15', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('李娜', 'lina@company.com', '13800138002', 3, '产品部', '产品经理', '2021-08-20', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('王强', 'wangqiang@company.com', '13800138003', 8, '前端开发组', '前端开发工程师', '2023-01-10', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('赵敏', 'zhaomin@company.com', '13800138004', 4, '市场部', '市场专员', '2022-11-05', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('刘洋', 'liuyang@company.com', '13800138005', 5, '人力资源部', '人力资源经理', '2020-06-15', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('陈静', 'chenjing@company.com', '13800138006', 6, '财务部', '财务主管', '2021-04-20', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('杨帆', 'yangfan@company.com', '13800138007', 9, '测试组', '测试工程师', '2023-05-08', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('黄磊', 'huanglei@company.com', '13800138008', 10, '设计部', 'UI设计师', '2022-09-12', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('周芳', 'zhoufang@company.com', '13800138009', 11, '运营部', '运营专员', '2023-02-28', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0),
('吴涛', 'wutao@company.com', '13800138010', 2, '技术中心', '架构师', '2019-12-01', 1, '$2a$10$7eJZ1s3qKp5b6N7M8P9R0eO0iUuYyTtRrEeWwQqAaSsDdFfGgHhJj', 0);

-- 证件类型表
CREATE TABLE IF NOT EXISTS document_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '证件类型ID',
    type_code VARCHAR(50) NOT NULL UNIQUE COMMENT '证件类型编码',
    type_name VARCHAR(50) NOT NULL COMMENT '证件类型名称',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标名称',
    is_required TINYINT DEFAULT 0 COMMENT '是否必备: 1=是, 0=否',
    has_expiry TINYINT DEFAULT 1 COMMENT '是否有有效期: 1=是, 0=否',
    require_both_sides TINYINT DEFAULT 0 COMMENT '是否需要正反面: 1=是, 0=否',
    field_config TEXT COMMENT '字段配置JSON',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证件类型表';

-- 证件表
CREATE TABLE IF NOT EXISTS document (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '证件ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    document_type_id BIGINT NOT NULL COMMENT '证件类型ID',
    document_number VARCHAR(100) DEFAULT NULL COMMENT '证件号码',
    front_image VARCHAR(255) DEFAULT NULL COMMENT '正面图片路径',
    back_image VARCHAR(255) DEFAULT NULL COMMENT '反面图片路径',
    issue_authority VARCHAR(100) DEFAULT NULL COMMENT '签发机关',
    issue_date DATE DEFAULT NULL COMMENT '签发日期/获证日期',
    expiry_date DATE DEFAULT NULL COMMENT '有效期至',
    is_permanent TINYINT DEFAULT 0 COMMENT '是否长期有效: 1=是, 0=否',
    school VARCHAR(100) DEFAULT NULL COMMENT '毕业院校',
    major VARCHAR(100) DEFAULT NULL COMMENT '专业',
    graduation_date DATE DEFAULT NULL COMMENT '毕业时间',
    certificate_name VARCHAR(100) DEFAULT NULL COMMENT '证书名称',
    extra_info TEXT COMMENT '扩展信息JSON',
    audit_status TINYINT DEFAULT 0 COMMENT '审核状态: 0=待审核, 1=已通过, 2=已拒绝',
    audit_reason VARCHAR(500) DEFAULT NULL COMMENT '审核拒绝理由',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    version INT DEFAULT 1 COMMENT '版本号',
    is_archived TINYINT DEFAULT 0 COMMENT '是否归档: 1=是, 0=否',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_employee_id (employee_id),
    INDEX idx_document_type_id (document_type_id),
    INDEX idx_audit_status (audit_status),
    INDEX idx_expiry_date (expiry_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证件表';

-- 证件历史版本表
CREATE TABLE IF NOT EXISTS document_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '历史记录ID',
    document_id BIGINT NOT NULL COMMENT '证件ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    document_type_id BIGINT NOT NULL COMMENT '证件类型ID',
    document_number VARCHAR(100) DEFAULT NULL COMMENT '证件号码',
    front_image VARCHAR(255) DEFAULT NULL COMMENT '正面图片路径',
    back_image VARCHAR(255) DEFAULT NULL COMMENT '反面图片路径',
    issue_authority VARCHAR(100) DEFAULT NULL COMMENT '签发机关',
    issue_date DATE DEFAULT NULL COMMENT '签发日期',
    expiry_date DATE DEFAULT NULL COMMENT '有效期至',
    is_permanent TINYINT DEFAULT 0 COMMENT '是否长期有效',
    school VARCHAR(100) DEFAULT NULL COMMENT '毕业院校',
    major VARCHAR(100) DEFAULT NULL COMMENT '专业',
    graduation_date DATE DEFAULT NULL COMMENT '毕业时间',
    certificate_name VARCHAR(100) DEFAULT NULL COMMENT '证书名称',
    extra_info TEXT COMMENT '扩展信息JSON',
    audit_status TINYINT DEFAULT 0 COMMENT '审核状态',
    audit_reason VARCHAR(500) DEFAULT NULL COMMENT '审核拒绝理由',
    auditor_id BIGINT DEFAULT NULL COMMENT '审核人ID',
    audit_time DATETIME DEFAULT NULL COMMENT '审核时间',
    version INT NOT NULL COMMENT '版本号',
    replaced_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '替换时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='证件历史版本表';

-- 初始化证件类型数据
INSERT INTO document_type (type_code, type_name, icon, is_required, has_expiry, require_both_sides, field_config, sort_order) VALUES
('ID_CARD', '身份证', 'idcard', 1, 1, 1, '{"fields":[{"name":"document_number","label":"证件号码","required":true,"placeholder":"请输入18位身份证号码","pattern":"^[0-9]{17}[0-9Xx]$"},{"name":"issue_authority","label":"签发机关","required":true,"placeholder":"请输入签发机关"},{"name":"expiry_date","label":"有效期","required":true,"type":"date","allowPermanent":true}]}', 1),
('EDUCATION', '学历证书', 'education', 1, 0, 0, '{"fields":[{"name":"school","label":"毕业院校","required":true,"placeholder":"请输入毕业院校"},{"name":"major","label":"专业","required":true,"placeholder":"请输入专业名称"},{"name":"graduation_date","label":"毕业时间","required":true,"type":"date"},{"name":"document_number","label":"证书编号","required":true,"placeholder":"请输入证书编号"}]}', 2),
('PROFESSIONAL', '职业资格证书', 'certificate', 0, 1, 0, '{"fields":[{"name":"certificate_name","label":"证书名称","required":true,"placeholder":"请输入证书名称"},{"name":"issue_authority","label":"发证机关","required":true,"placeholder":"请输入发证机关"},{"name":"issue_date","label":"获证时间","required":true,"type":"date"},{"name":"expiry_date","label":"有效期","required":false,"type":"date","allowPermanent":true}]}', 3);

-- 信息类别表
CREATE TABLE IF NOT EXISTS announcement_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '类别ID',
    category_name VARCHAR(50) NOT NULL COMMENT '类别名称',
    category_code VARCHAR(50) NOT NULL UNIQUE COMMENT '类别编码',
    icon VARCHAR(50) DEFAULT NULL COMMENT '图标名称',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信息类别表';

-- 信息表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '信息ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    category_id BIGINT NOT NULL COMMENT '类别ID',
    content TEXT COMMENT '详细内容（富文本）',
    importance TINYINT DEFAULT 1 COMMENT '重要程度: 1=常规, 2=关注, 3=紧急',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶: 1=是, 0=否',
    publish_date DATE COMMENT '起始展示日期',
    expiry_date DATE COMMENT '截止展示日期',
    is_force_confirm TINYINT DEFAULT 0 COMMENT '是否强制确认: 1=是, 0=否',
    attachments TEXT COMMENT '附件信息JSON',
    publish_status TINYINT DEFAULT 0 COMMENT '发布状态: 0=待发布, 1=已发布, 2=已撤回',
    publisher_id BIGINT NOT NULL COMMENT '发布人ID',
    publisher_name VARCHAR(50) COMMENT '发布人姓名',
    publish_time DATETIME COMMENT '发布时间',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category_id (category_id),
    INDEX idx_publish_status (publish_status),
    INDEX idx_importance (importance),
    INDEX idx_is_top (is_top),
    INDEX idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信息表';

-- 阅览记录表
CREATE TABLE IF NOT EXISTS announcement_read_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    announcement_id BIGINT NOT NULL COMMENT '信息ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) COMMENT '员工姓名',
    department_id BIGINT COMMENT '部门ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已阅: 1=是, 0=否',
    read_time DATETIME COMMENT '阅读时间',
    is_confirmed TINYINT DEFAULT 0 COMMENT '是否确认: 1=是, 0=否',
    confirm_time DATETIME COMMENT '确认时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_announcement_employee (announcement_id, employee_id),
    INDEX idx_announcement_id (announcement_id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='阅览记录表';

-- 信息接收范围表
CREATE TABLE IF NOT EXISTS announcement_target (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    announcement_id BIGINT NOT NULL COMMENT '信息ID',
    target_type TINYINT NOT NULL COMMENT '目标类型: 1=全公司, 2=部门, 3=个人',
    target_id BIGINT COMMENT '目标ID（部门ID或员工ID，全公司时为NULL）',
    target_name VARCHAR(100) COMMENT '目标名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_announcement_id (announcement_id),
    INDEX idx_target_type (target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='信息接收范围表';

-- 初始化信息类别数据
INSERT INTO announcement_category (category_name, category_code, icon, sort_order) VALUES
('政策解读', 'POLICY', 'policy', 1),
('活动预告', 'ACTIVITY', 'activity', 2),
('制度变更', 'REGULATION', 'regulation', 3),
('紧急事项', 'URGENT', 'urgent', 4),
('通知公告', 'NOTICE', 'notice', 5);

-- 登录日志表
CREATE TABLE IF NOT EXISTS login_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '日志ID',
    user_type TINYINT NOT NULL COMMENT '用户类型: 1=管理员, 2=员工',
    username VARCHAR(100) NOT NULL COMMENT '用户名/邮箱',
    user_id BIGINT COMMENT '用户ID',
    ip_address VARCHAR(50) COMMENT '登录IP地址',
    device_type VARCHAR(20) COMMENT '设备类型',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    login_result TINYINT NOT NULL COMMENT '登录结果: 1=成功, 0=失败',
    failure_reason VARCHAR(200) COMMENT '失败原因',
    is_abnormal TINYINT DEFAULT 0 COMMENT '是否异常登录: 1=是, 0=否',
    abnormal_type VARCHAR(50) COMMENT '异常类型',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_username (username),
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    INDEX idx_login_result (login_result),
    INDEX idx_is_abnormal (is_abnormal)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- 用户会话表
CREATE TABLE IF NOT EXISTS user_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '会话ID',
    user_type TINYINT NOT NULL COMMENT '用户类型: 1=管理员, 2=员工',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    token VARCHAR(500) NOT NULL COMMENT 'Token标识',
    ip_address VARCHAR(50) COMMENT '登录IP地址',
    device_type VARCHAR(20) COMMENT '设备类型',
    login_time DATETIME NOT NULL COMMENT '登录时间',
    expire_time DATETIME NOT NULL COMMENT '过期时间',
    is_active TINYINT DEFAULT 1 COMMENT '是否有效: 1=有效, 0=失效',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_token (token(100)),
    INDEX idx_is_active (is_active),
    INDEX idx_expire_time (expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 为员工表添加锁定相关字段
ALTER TABLE employee ADD COLUMN  lock_status TINYINT DEFAULT 0 COMMENT '锁定状态: 0=正常, 1=已锁定';
ALTER TABLE employee ADD COLUMN  lock_time DATETIME COMMENT '锁定时间';
ALTER TABLE employee ADD COLUMN  unlock_time DATETIME COMMENT '解锁时间';

-- 为管理员表添加锁定相关字段
ALTER TABLE admin_user ADD COLUMN  lock_status TINYINT DEFAULT 0 COMMENT '锁定状态: 0=正常, 1=已锁定';
ALTER TABLE admin_user ADD COLUMN  lock_time DATETIME COMMENT '锁定时间';
ALTER TABLE admin_user ADD COLUMN  unlock_time DATETIME COMMENT '解锁时间';

-- 问卷表
CREATE TABLE IF NOT EXISTS survey (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '问卷ID',
    title VARCHAR(200) NOT NULL COMMENT '问卷标题',
    description TEXT COMMENT '问卷说明',
    deadline DATETIME NOT NULL COMMENT '截止日期',
    is_anonymous TINYINT DEFAULT 0 COMMENT '是否匿名: 1=匿名, 0=实名',
    status TINYINT DEFAULT 1 COMMENT '状态: 0=草稿, 1=发布中, 2=已结束',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_by_name VARCHAR(50) COMMENT '创建人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_status (status),
    INDEX idx_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷表';

-- 问卷题目表
CREATE TABLE IF NOT EXISTS survey_question (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '题目ID',
    survey_id BIGINT NOT NULL COMMENT '问卷ID',
    question_type TINYINT NOT NULL COMMENT '题目类型: 1=评分题, 2=单选题, 3=开放题',
    title VARCHAR(500) NOT NULL COMMENT '题目标题',
    sort_order INT DEFAULT 0 COMMENT '排序',
    required TINYINT DEFAULT 1 COMMENT '是否必填: 1=是, 0=否',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_survey_id (survey_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷题目表';

-- 题目选项表
CREATE TABLE IF NOT EXISTS survey_question_option (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选项ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    option_text VARCHAR(200) NOT NULL COMMENT '选项文本',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_question_id (question_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目选项表';

-- 问卷发放范围表
CREATE TABLE IF NOT EXISTS survey_target (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'ID',
    survey_id BIGINT NOT NULL COMMENT '问卷ID',
    target_type TINYINT NOT NULL COMMENT '目标类型: 1=全体员工, 2=部门',
    target_id BIGINT COMMENT '目标ID（部门ID，全体员工时为NULL）',
    target_name VARCHAR(100) COMMENT '目标名称',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_survey_id (survey_id),
    INDEX idx_target_type (target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷发放范围表';

-- 问卷回答表
CREATE TABLE IF NOT EXISTS survey_answer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '回答ID',
    survey_id BIGINT NOT NULL COMMENT '问卷ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) COMMENT '员工姓名',
    department_id BIGINT COMMENT '部门ID',
    department_name VARCHAR(50) COMMENT '部门名称',
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_survey_employee (survey_id, employee_id),
    INDEX idx_survey_id (survey_id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_department_id (department_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='问卷回答表';

-- 答案详情表
CREATE TABLE IF NOT EXISTS survey_answer_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '详情ID',
    answer_id BIGINT NOT NULL COMMENT '回答ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    question_type TINYINT NOT NULL COMMENT '题目类型',
    option_id BIGINT COMMENT '选项ID（单选题时使用）',
    rating_score INT COMMENT '评分分数（评分题时使用）',
    rating_item VARCHAR(100) COMMENT '评分项名称（评分题时使用）',
    text_content TEXT COMMENT '文本内容（开放题时使用）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_answer_id (answer_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='答案详情表';

-- 意见箱表
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '意见ID',
    feedback_no VARCHAR(50) NOT NULL UNIQUE COMMENT '意见编号',
    category VARCHAR(50) NOT NULL COMMENT '意见分类: 制度建议/薪酬福利/工作环境/团队管理/其他',
    content TEXT NOT NULL COMMENT '意见内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 1=是, 0=否',
    read_time DATETIME COMMENT '阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    INDEX idx_category (category),
    INDEX idx_is_read (is_read),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='意见箱表';

ALTER TABLE employee ADD COLUMN birthday_date DATE DEFAULT NULL COMMENT '生日日期';

UPDATE employee SET birthday_date = '1995-06-15' WHERE id = 1;
UPDATE employee SET birthday_date = '1993-06-20' WHERE id = 2;
UPDATE employee SET birthday_date = '1997-01-10' WHERE id = 3;
UPDATE employee SET birthday_date = '1994-11-05' WHERE id = 4;
UPDATE employee SET birthday_date = '1990-06-15' WHERE id = 5;
UPDATE employee SET birthday_date = '1996-04-20' WHERE id = 6;
UPDATE employee SET birthday_date = '1998-05-08' WHERE id = 7;
UPDATE employee SET birthday_date = '1995-09-12' WHERE id = 8;
UPDATE employee SET birthday_date = '1997-02-28' WHERE id = 9;
UPDATE employee SET birthday_date = '1991-12-01' WHERE id = 10;

CREATE TABLE IF NOT EXISTS birthday_wish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '祝福ID',
    recipient_id BIGINT NOT NULL COMMENT '接收祝福的员工ID',
    sender_id BIGINT NOT NULL COMMENT '发送祝福的员工ID',
    sender_name VARCHAR(50) COMMENT '发送者姓名',
    sender_avatar VARCHAR(255) COMMENT '发送者头像',
    content TEXT NOT NULL COMMENT '祝福内容',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统自动发送: 1=是, 0=否',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生日祝福表';

CREATE TABLE IF NOT EXISTS birthday_wish_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '点赞ID',
    wish_id BIGINT NOT NULL COMMENT '祝福ID',
    employee_id BIGINT NOT NULL COMMENT '点赞员工ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_wish_employee (wish_id, employee_id),
    INDEX idx_wish_id (wish_id),
    INDEX idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='祝福点赞表';

CREATE TABLE IF NOT EXISTS birthday_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID',
    recipient_id BIGINT NOT NULL COMMENT '接收者员工ID',
    recipient_type TINYINT DEFAULT 2 COMMENT '接收者类型: 1=管理员, 2=员工',
    title VARCHAR(200) NOT NULL COMMENT '消息标题',
    content TEXT COMMENT '消息内容',
    type VARCHAR(50) COMMENT '消息类型: BIRTHDAY_WISH/PARTY_INVITE/PARTY_REMINDER/NEW_WISH',
    related_id BIGINT COMMENT '关联ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 1=是, 0=否',
    read_time DATETIME COMMENT '阅读时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_recipient_id (recipient_id),
    INDEX idx_is_read (is_read),
    INDEX idx_type (type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='站内消息表';

CREATE TABLE IF NOT EXISTS birthday_party (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '生日会ID',
    theme VARCHAR(200) NOT NULL COMMENT '活动主题',
    party_year INT NOT NULL COMMENT '年份',
    party_month INT NOT NULL COMMENT '月份',
    event_time DATETIME NOT NULL COMMENT '举办时间',
    location VARCHAR(200) NOT NULL COMMENT '举办地点',
    flow TEXT COMMENT '活动流程',
    cover_image VARCHAR(255) COMMENT '活动封面图',
    status TINYINT DEFAULT 0 COMMENT '状态: 0=未开始, 1=进行中, 2=已结束',
    created_by BIGINT NOT NULL COMMENT '创建人ID',
    created_by_name VARCHAR(50) COMMENT '创建人姓名',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_year_month (party_year, party_month),
    INDEX idx_status (status),
    INDEX idx_event_time (event_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生日会表';

CREATE TABLE IF NOT EXISTS birthday_party_participant (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '参与ID',
    party_id BIGINT NOT NULL COMMENT '生日会ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    employee_name VARCHAR(50) COMMENT '员工姓名',
    department VARCHAR(50) COMMENT '部门',
    avatar VARCHAR(255) COMMENT '头像',
    participation_status TINYINT DEFAULT 2 COMMENT '参与状态: 0=不参加, 1=参加, 2=待定',
    remark VARCHAR(500) COMMENT '参与备注',
    checkin_status TINYINT DEFAULT 0 COMMENT '签到状态: 0=未签到, 1=已签到',
    checkin_time DATETIME COMMENT '签到时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_party_employee (party_id, employee_id),
    INDEX idx_party_id (party_id),
    INDEX idx_employee_id (employee_id),
    INDEX idx_participation_status (participation_status),
    INDEX idx_checkin_status (checkin_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生日会参与者表';

CREATE TABLE IF NOT EXISTS birthday_party_photo (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '照片ID',
    party_id BIGINT NOT NULL COMMENT '生日会ID',
    photo_url VARCHAR(255) NOT NULL COMMENT '照片路径',
    uploaded_by BIGINT NOT NULL COMMENT '上传者ID',
    uploaded_by_name VARCHAR(50) COMMENT '上传者姓名',
    like_count INT DEFAULT 0 COMMENT '点赞数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
    INDEX idx_party_id (party_id),
    INDEX idx_uploaded_by (uploaded_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生日会照片表';

ALTER TABLE birthday_party ADD COLUMN highlights VARCHAR(100) DEFAULT NULL COMMENT '活动亮点';

CREATE TABLE IF NOT EXISTS birthday_yearly_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '寄语ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    year INT NOT NULL COMMENT '年份',
    content VARCHAR(200) NOT NULL COMMENT '寄语内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_employee_year (employee_id, year),
    INDEX idx_employee_id (employee_id),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工年度生日寄语表';

CREATE TABLE IF NOT EXISTS birthday_admin_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '管理员寄语ID',
    employee_id BIGINT NOT NULL COMMENT '员工ID',
    admin_id BIGINT NOT NULL COMMENT '管理员ID',
    admin_name VARCHAR(50) COMMENT '管理员姓名',
    year INT NOT NULL COMMENT '年份',
    content VARCHAR(500) NOT NULL COMMENT '寄语内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_employee_id (employee_id),
    INDEX idx_admin_id (admin_id),
    INDEX idx_year (year)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员年度生日寄语表';

CREATE TABLE IF NOT EXISTS birthday_party_photo_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '照片点赞ID',
    photo_id BIGINT NOT NULL COMMENT '照片ID',
    employee_id BIGINT NOT NULL COMMENT '点赞员工ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    UNIQUE KEY uk_photo_employee (photo_id, employee_id),
    INDEX idx_photo_id (photo_id),
    INDEX idx_employee_id (employee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='照片点赞表';
