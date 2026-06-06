import {
  TeamOutlined,
  ApartmentOutlined,
  ShareAltOutlined,
  BarChartOutlined,
  NotificationOutlined,
  FileProtectOutlined,
  SecurityScanOutlined,
  AlertOutlined,
  UserOutlined,
  FileTextOutlined,
  ReadOutlined,
  FormOutlined,
  MessageOutlined,
  InboxOutlined,
  HistoryOutlined,
  GiftOutlined,
  HeartOutlined,
  TrophyOutlined,
  RiseOutlined,
  AuditOutlined,
  DashboardOutlined
} from '@ant-design/icons-vue'
import { h } from 'vue'

const menuConfig = [
  {
    key: 'personnel',
    label: '人员管理',
    icon: () => h(TeamOutlined),
    roles: ['admin'],
    children: [
      {
        key: 'employee',
        label: '员工管理',
        path: '/employee',
        roles: ['admin']
      },
      {
        key: 'department',
        label: '部门管理',
        path: '/department',
        roles: ['admin']
      }
    ]
  },
  {
    key: 'organization',
    label: '组织架构',
    icon: () => h(ApartmentOutlined),
    roles: ['admin'],
    children: [
      {
        key: 'organization-chart',
        label: '组织架构图',
        path: '/organization-chart',
        roles: ['admin']
      },
      {
        key: 'department-statistics',
        label: '部门统计',
        path: '/department-statistics',
        roles: ['admin']
      }
    ]
  },
  {
    key: 'information',
    label: '信息管理',
    icon: () => h(NotificationOutlined),
    roles: ['admin'],
    children: [
      {
        key: 'admin-announcements',
        label: '公告管理',
        path: '/admin-announcements',
        roles: ['admin']
      },
      {
        key: 'admin-documents',
        label: '文档审核',
        path: '/admin-documents',
        roles: ['admin']
      }
    ]
  },
  {
    key: 'security',
    label: '安全监控',
    icon: () => h(SecurityScanOutlined),
    roles: ['admin'],
    children: [
      {
        key: 'login-logs',
        label: '登录日志',
        path: '/login-logs',
        roles: ['admin']
      },
      {
        key: 'abnormal-logs',
        label: '异常日志',
        path: '/abnormal-logs',
        roles: ['admin']
      }
    ]
  },
  {
    key: 'survey',
    label: '调查管理',
    icon: () => h(FormOutlined),
    roles: ['admin'],
    children: [
      {
        key: 'surveys',
        label: '问卷管理',
        path: '/surveys',
        roles: ['admin']
      },
      {
        key: 'feedbacks',
        label: '意见箱管理',
        path: '/feedbacks',
        roles: ['admin']
      }
    ]
  },
  {
    key: 'birthday-admin',
    label: '生日关怀',
    icon: () => h(GiftOutlined),
    roles: ['admin'],
    children: [
      {
        key: 'admin-birthday-wall',
        label: '生日墙',
        path: '/admin-birthday-wall',
        roles: ['admin']
      },
      {
        key: 'birthday-party',
        label: '生日会管理',
        path: '/birthday-party',
        roles: ['admin']
      },
      {
        key: 'birthday-statistics',
        label: '生日统计',
        path: '/birthday-statistics',
        roles: ['admin']
      },
      {
        key: 'birthday-milestones',
        label: '生日大事记',
        path: '/birthday-milestones',
        roles: ['admin']
      }
    ]
  },
  {
    key: 'evaluation-admin',
    label: '成长评价',
    icon: () => h(RiseOutlined),
    roles: ['admin'],
    children: [
      {
        key: 'evaluation-cycles',
        label: '评价周期管理',
        path: '/evaluation-cycles',
        roles: ['admin']
      }
    ]
  },
  {
    key: 'personal',
    label: '个人中心',
    icon: () => h(UserOutlined),
    roles: ['employee'],
    children: [
      {
        key: 'profile',
        label: '个人资料',
        path: '/profile',
        roles: ['employee']
      },
      {
        key: 'growth-workbench',
        label: '成长工作台',
        path: '/growth-workbench',
        roles: ['employee']
      },
      {
        key: 'birthday-wall',
        label: '生日墙',
        path: '/birthday-wall',
        roles: ['employee']
      },
      {
        key: 'birthday-profile',
        label: '生日档案',
        path: '/birthday-profile',
        roles: ['employee']
      },
      {
        key: 'documents',
        label: '我的文档',
        path: '/documents',
        roles: ['employee']
      },
      {
        key: 'announcements',
        label: '公告浏览',
        path: '/announcements',
        roles: ['employee']
      },
      {
        key: 'my-surveys',
        label: '我的问卷',
        path: '/my-surveys',
        roles: ['employee']
      },
      {
        key: 'feedback',
        label: '意见箱',
        path: '/feedback',
        roles: ['employee']
      }
    ]
  }
]

export function getMenuByRole(role) {
  if (!role) return []

  return menuConfig
    .filter(group => group.roles.includes(role))
    .map(group => {
      const filteredChildren = group.children.filter(item => item.roles.includes(role))
      if (filteredChildren.length === 0) return null
      return {
        ...group,
        children: filteredChildren
      }
    })
    .filter(Boolean)
}

export function findMenuGroupByKey(key) {
  for (const group of menuConfig) {
    for (const child of group.children) {
      if (child.key === key) {
        return group.key
      }
    }
  }
  return null
}

export function findMenuKeyByPath(path) {
  for (const group of menuConfig) {
    for (const child of group.children) {
      if (child.path === path) {
        return child.key
      }
    }
  }
  return null
}

export default menuConfig
