import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import MainLayout from '@/layouts/MainLayout.vue'
import Login from '@/views/Login.vue'
import FirstLogin from '@/views/FirstLogin.vue'
import EmployeeManage from '@/views/EmployeeManage.vue'
import DepartmentManage from '@/views/DepartmentManage.vue'
import OrganizationChart from '@/views/OrganizationChart.vue'
import DepartmentStatistics from '@/views/DepartmentStatistics.vue'
import Profile from '@/views/Profile.vue'
import Documents from '@/views/Documents.vue'
import AdminDocuments from '@/views/AdminDocuments.vue'
import EmployeeAnnouncements from '@/views/EmployeeAnnouncements.vue'
import AdminAnnouncements from '@/views/AdminAnnouncements.vue'
import LoginLogs from '@/views/LoginLogs.vue'
import AbnormalLoginLogs from '@/views/AbnormalLoginLogs.vue'
import AdminSurveyList from '@/views/AdminSurveyList.vue'
import SurveyEdit from '@/views/SurveyEdit.vue'
import SurveyStatistics from '@/views/SurveyStatistics.vue'
import AdminFeedback from '@/views/AdminFeedback.vue'
import MySurveys from '@/views/MySurveys.vue'
import SurveyFill from '@/views/SurveyFill.vue'
import EmployeeFeedback from '@/views/EmployeeFeedback.vue'
import BirthdayWall from '@/views/BirthdayWall.vue'
import BirthdayPartyList from '@/views/BirthdayPartyList.vue'
import BirthdayPartyDetail from '@/views/BirthdayPartyDetail.vue'
import BirthdayProfile from '@/views/BirthdayProfile.vue'
import BirthdayStatistics from '@/views/BirthdayStatistics.vue'
import BirthdayMilestones from '@/views/BirthdayMilestones.vue'
import EvaluationCycleManage from '@/views/EvaluationCycleManage.vue'
import EvaluationProgress from '@/views/EvaluationProgress.vue'
import EvaluationRecords from '@/views/EvaluationRecords.vue'
import EvaluationStatistics from '@/views/EvaluationStatistics.vue'
import AdminEvaluate from '@/views/AdminEvaluate.vue'
import GrowthReport from '@/views/GrowthReport.vue'
import GrowthWorkbench from '@/views/GrowthWorkbench.vue'
import GoalSetting from '@/views/GoalSetting.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/login'
    },
    {
      path: '/login',
      name: 'Login',
      component: Login
    },
    {
      path: '/first-login',
      name: 'FirstLogin',
      component: FirstLogin,
      meta: { requiresAuth: true, role: 'employee' }
    },
    {
      path: '/',
      component: MainLayout,
      meta: { requiresAuth: true },
      children: [
        {
          path: 'employee',
          name: 'Employee',
          component: EmployeeManage,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'department',
          name: 'Department',
          component: DepartmentManage,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'organization-chart',
          name: 'OrganizationChart',
          component: OrganizationChart,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'department-statistics',
          name: 'DepartmentStatistics',
          component: DepartmentStatistics,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'profile',
          name: 'Profile',
          component: Profile,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'documents',
          name: 'Documents',
          component: Documents,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'admin-documents',
          name: 'AdminDocuments',
          component: AdminDocuments,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'announcements',
          name: 'EmployeeAnnouncements',
          component: EmployeeAnnouncements,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'admin-announcements',
          name: 'AdminAnnouncements',
          component: AdminAnnouncements,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'login-logs',
          name: 'LoginLogs',
          component: LoginLogs,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'abnormal-logs',
          name: 'AbnormalLoginLogs',
          component: AbnormalLoginLogs,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'surveys',
          name: 'AdminSurveyList',
          component: AdminSurveyList,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'survey/edit/:id?',
          name: 'SurveyEdit',
          component: SurveyEdit,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'survey/statistics/:id',
          name: 'SurveyStatistics',
          component: SurveyStatistics,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'feedbacks',
          name: 'AdminFeedback',
          component: AdminFeedback,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'my-surveys',
          name: 'MySurveys',
          component: MySurveys,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'survey/fill/:id',
          name: 'SurveyFill',
          component: SurveyFill,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'feedback',
          name: 'EmployeeFeedback',
          component: EmployeeFeedback,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'birthday-wall',
          name: 'BirthdayWall',
          component: BirthdayWall,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'birthday-profile',
          name: 'BirthdayProfile',
          component: BirthdayProfile,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'emp-birthday-party-detail/:id',
          name: 'EmpBirthdayPartyDetail',
          component: BirthdayPartyDetail,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'admin-birthday-wall',
          name: 'AdminBirthdayWall',
          component: BirthdayWall,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'birthday-party',
          name: 'BirthdayPartyList',
          component: BirthdayPartyList,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'birthday-party-detail/:id',
          name: 'BirthdayPartyDetail',
          component: BirthdayPartyDetail,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'birthday-statistics',
          name: 'BirthdayStatistics',
          component: BirthdayStatistics,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'birthday-milestones',
          name: 'BirthdayMilestones',
          component: BirthdayMilestones,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'evaluation-cycles',
          name: 'EvaluationCycleManage',
          component: EvaluationCycleManage,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'evaluation-progress/:id',
          name: 'EvaluationProgress',
          component: EvaluationProgress,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'evaluation-records/:id',
          name: 'EvaluationRecords',
          component: EvaluationRecords,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'evaluation-statistics/:id',
          name: 'EvaluationStatistics',
          component: EvaluationStatistics,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'admin-evaluate/:cycleId/:employeeId',
          name: 'AdminEvaluate',
          component: AdminEvaluate,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'growth-report/:cycleId/:employeeId',
          name: 'GrowthReport',
          component: GrowthReport,
          meta: { requiresAuth: true, role: 'admin' }
        },
        {
          path: 'growth-workbench',
          name: 'GrowthWorkbench',
          component: GrowthWorkbench,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'goal-setting/:id',
          name: 'GoalSetting',
          component: GoalSetting,
          meta: { requiresAuth: true, role: 'employee' }
        },
        {
          path: 'my-report/:cycleId',
          name: 'MyReport',
          component: GrowthReport,
          meta: { requiresAuth: true, role: 'employee' }
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const hasToken = !!userStore.getToken()

  if (to.meta.requiresAuth && !hasToken) {
    next('/login')
  } else if (to.path === '/login' && hasToken) {
    if (userStore.role === 'admin') {
      next('/employee')
    } else {
      if (userStore.isActive === 0) {
        next('/first-login')
      } else {
        next('/profile')
      }
    }
  } else if (to.meta.role && to.meta.role !== userStore.role) {
    if (userStore.role === 'admin') {
      next('/employee')
    } else if (userStore.role === 'employee') {
      if (userStore.isActive === 0) {
        next('/first-login')
      } else {
        next('/profile')
      }
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router
