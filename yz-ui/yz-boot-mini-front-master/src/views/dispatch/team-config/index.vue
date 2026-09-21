<template>
  <div class="team-config-page">
    <ContentWrap class="query-wrap">
      <el-form ref="queryFormRef" :inline="true" :model="queryParams" class="-mb-15px" label-width="88px">
        <el-form-item label="班组名称" prop="teamName">
          <el-input
            v-model="queryParams.teamName"
            clearable
            class="!w-220px"
            placeholder="请输入班组名称"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="班组长" prop="leaderUserName">
          <el-input
            v-model="queryParams.leaderUserName"
            clearable
            class="!w-220px"
            placeholder="请输入班组长"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="所属站点" prop="stationId">
          <el-select v-model="queryParams.stationId" clearable filterable class="!w-180px" placeholder="请选择所属站点">
            <el-option
              v-for="option in stationOptions"
              :key="String(option.value)"
              :label="option.label"
              :value="String(option.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button @click="handleQuery">
            <Icon icon="ep:search" class="mr-4px" />
            查询
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-4px" />
            重置
          </el-button>
          <el-button v-hasPermi="['iot:shift-team:create']" type="primary" plain @click="openForm('create')">
            <Icon icon="ep:plus" class="mr-4px" />
            新增班组
          </el-button>
          <el-button
            v-hasPermi="['iot:shift-team:export']"
            type="success"
            plain
            :loading="exportLoading"
            @click="handleExport"
          >
            <Icon icon="ep:download" class="mr-4px" />
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table v-loading="loading" :data="list" stripe>
        <el-table-column label="班组编号" min-width="130">
          <template #default="{ row }">
            <span class="team-no">{{ row.teamNo || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="班组名称" prop="teamName" min-width="150" show-overflow-tooltip />
        <el-table-column label="所属站点" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ resolveStationLabel(row.stationId) || '-' }}</template>
        </el-table-column>
        <el-table-column label="班组长" prop="leaderUserName" min-width="120" show-overflow-tooltip />
        <el-table-column label="班组人数" prop="memberCount" min-width="100" />
        <el-table-column label="备注" prop="remark" min-width="220" show-overflow-tooltip />
        <el-table-column label="创建人" prop="creator" min-width="120" show-overflow-tooltip />
        <el-table-column label="创建时间" min-width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openForm('detail', row.id)">详情</el-button>
            <el-button v-hasPermi="['iot:shift-team:update']" link type="primary" @click="openForm('update', row.id)">
              编辑
            </el-button>
            <el-button v-hasPermi="['iot:shift-team:delete']" link type="danger" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getList"
      />
    </ContentWrap>

    <Dialog v-model="formVisible" :title="formTitle" width="860px">
      <el-form
        ref="formRef"
        v-loading="formLoading"
        :model="formData"
        :rules="formRules"
        :disabled="formReadonly"
        label-width="96px"
      >
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="班组编号">
              <el-input :model-value="formData.teamNo || '系统自动生成'" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班组名称" prop="teamName">
              <el-input v-model="formData.teamName" maxlength="64" placeholder="请输入班组名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属站点" prop="stationId">
              <el-select v-model="formData.stationId" filterable clearable class="!w-full" placeholder="请选择所属站点">
                <el-option
                  v-for="option in stationOptions"
                  :key="String(option.value)"
                  :label="option.label"
                  :value="String(option.value)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="!formReadonly" :span="24">
            <el-form-item label="所属部门">
              <div class="dept-select-wrap">
                <el-select
                  v-model="selectedDeptId"
                  filterable
                  clearable
                  class="dept-select"
                  placeholder="请选择部门"
                >
                  <el-option
                    v-for="dept in availableDeptOptions"
                    :key="String(dept.id)"
                    :label="dept.name"
                    :value="String(dept.id)"
                  />
                </el-select>
                <el-button type="primary" plain :disabled="!selectedDeptId" @click="handleAppendDeptMembers">
                  添加部门成员
                </el-button>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="成员列表" prop="memberUserIds">
              <el-select
                v-model="formData.memberUserIds"
                multiple
                filterable
                clearable
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="6"
                class="!w-full"
                placeholder="请选择班组成员"
                @change="handleMembersChange"
              >
                <el-option
                  v-for="user in memberCandidateOptions"
                  :key="String(user.id)"
                  :label="buildUserLabel(user)"
                  :value="String(user.id)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班组长" prop="leaderUserId">
              <el-select
                v-model="formData.leaderUserId"
                filterable
                clearable
                class="!w-full"
                placeholder="请选择班组长"
                :disabled="!selectedMemberOptions.length"
              >
                <el-option
                  v-for="user in selectedMemberOptions"
                  :key="String(user.id)"
                  :label="buildUserLabel(user)"
                  :value="String(user.id)"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班组人数">
              <el-input :model-value="String(formData.memberUserIds?.length || 0)" disabled />
            </el-form-item>
          </el-col>
          <el-col v-if="formReadonly" :span="24">
            <el-form-item label="成员详情">
              <div class="member-list">
                <template v-if="memberDetailList.length">
                  <el-tag
                    v-for="member in memberDetailList"
                    :key="`${member.userId}`"
                    class="member-tag"
                    :type="member.leader ? 'warning' : 'info'"
                  >
                    {{ member.userName || member.userId }}{{ member.leader ? '（班组长）' : '' }}
                  </el-tag>
                </template>
                <span v-else class="text-gray-500">暂无成员</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="备注" prop="remark">
              <el-input
                v-model="formData.remark"
                type="textarea"
                :rows="3"
                maxlength="500"
                show-word-limit
                placeholder="请输入备注"
              />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">{{ formReadonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!formReadonly" type="primary" :loading="submitLoading" @click="submitForm">保存</el-button>
      </template>
    </Dialog>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import download from '@/utils/download'
import * as UserApi from '@/api/system/user'
import type { UserVO } from '@/api/system/user'
import * as DeptApi from '@/api/system/dept'
import type { DeptVO } from '@/api/system/dept'
import { ShiftTeamApi, type ShiftTeamMemberVO, type ShiftTeamPageReqVO, type ShiftTeamVO } from '@/api/iot/shift/team'
import { DICT_TYPE, getStrDictOptions } from '@/utils/dict'
import type { FormInstance, FormRules } from 'element-plus'

defineOptions({ name: 'IotShiftTeam' })

type IdType = string | number
type FormMode = 'create' | 'update' | 'detail'
type OptionItem = {
  label: string
  value: string | number
}
type ShiftTeamFormVO = ShiftTeamVO & {
  memberUserIds: string[]
  leaderUserId?: string
}

const message = useMessage()

const loading = ref(false)
const exportLoading = ref(false)
const list = ref<ShiftTeamVO[]>([])
const total = ref(0)
const queryFormRef = ref<FormInstance>()
const queryParams = reactive<ShiftTeamPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  stationId: undefined,
  teamName: undefined,
  leaderUserName: undefined
})
const stationOptions = computed<OptionItem[]>(() => {
  const dictOptions = getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)
  return dictOptions.map((dict) => ({ label: dict.label, value: dict.value }))
})

const formatDateTime = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return '-'
  const raw = typeof value === 'string' && /^\d+$/.test(value) ? Number(value) : value
  const timestamp = typeof raw === 'number' && raw > 0 && raw < 1000000000000 ? raw * 1000 : raw
  const parsed = dayjs(timestamp)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : '-'
}

const normalizeId = (value?: IdType | null) => {
  if (value === undefined || value === null || value === '') return ''
  return String(value)
}

const getList = async () => {
  loading.value = true
  try {
    const data = await ShiftTeamApi.getPage(queryParams)
    list.value = data.list || []
    total.value = Number(data.total || 0)
  } finally {
    loading.value = false
  }
}

const handleQuery = async () => {
  queryParams.pageNo = 1
  await getList()
}

const resetQuery = async () => {
  queryFormRef.value?.resetFields()
  queryParams.pageNo = 1
  await getList()
}

const handleDelete = async (id?: IdType) => {
  if (!id) return
  await message.delConfirm('确认删除该班组吗？')
  await ShiftTeamApi.remove(id)
  message.success('删除成功')
  await getList()
}

const handleExport = async () => {
  await message.exportConfirm()
  exportLoading.value = true
  try {
    const data = await ShiftTeamApi.exportExcel(queryParams)
    download.excel(data, `班组管理_${dayjs().format('YYYYMMDD_HHmmss')}.xls`)
  } finally {
    exportLoading.value = false
  }
}

const crowdDeptName = '群众部门'
const userOptions = ref<UserVO[]>([])
const deptOptions = ref<DeptVO[]>([])
const selectedDeptId = ref('')

const availableDeptOptions = computed(() => {
  return (deptOptions.value || []).filter((item) => (item.name || '').trim() !== crowdDeptName)
})

const deptChildrenMap = computed(() => {
  const childrenMap = new Map<string, string[]>()
  ;(availableDeptOptions.value || []).forEach((dept) => {
    const parentId = normalizeId(dept.parentId)
    const deptId = normalizeId(dept.id)
    if (!parentId || !deptId) return
    const children = childrenMap.get(parentId) || []
    children.push(deptId)
    childrenMap.set(parentId, children)
  })
  return childrenMap
})

const collectDeptScopeIds = (deptId?: string) => {
  const rootId = normalizeId(deptId)
  const scopeIds = new Set<string>()
  if (!rootId) return scopeIds
  const queue: string[] = [rootId]
  while (queue.length) {
    const currentId = queue.shift() as string
    if (scopeIds.has(currentId)) continue
    scopeIds.add(currentId)
    const children = deptChildrenMap.value.get(currentId) || []
    children.forEach((childId) => {
      if (!scopeIds.has(childId)) {
        queue.push(childId)
      }
    })
  }
  return scopeIds
}
const resolveStationLabel = (stationId?: string) => {
  if (!stationId) return ''
  return stationOptions.value.find((item) => String(item.value) === String(stationId))?.label || stationId
}

const buildUserLabel = (user?: UserVO | null) => {
  if (!user) return '-'
  const nickname = user.nickname || user.username || String(user.id)
  const mobile = user.mobile ? `（${user.mobile}）` : ''
  return `${nickname}${mobile}`
}

const loadUserOptions = async () => {
  userOptions.value = await UserApi.getSimpleUserList({ excludeRoleName: '游客' })
}

const loadDeptOptions = async () => {
  deptOptions.value = await DeptApi.getSimpleDeptList()
}

const formVisible = ref(false)
const formMode = ref<FormMode>('create')
const formRef = ref<FormInstance>()
const formLoading = ref(false)
const submitLoading = ref(false)

const createEmptyFormData = (): ShiftTeamFormVO => ({
  id: undefined,
  teamNo: '',
  teamName: '',
  stationId: undefined,
  leaderUserId: undefined,
  leaderUserName: '',
  memberCount: 0,
  memberUserIds: [],
  members: [],
  remark: ''
})
const formData = reactive<ShiftTeamFormVO>(createEmptyFormData())

const selectedMemberOptions = computed(() => {
  const selectedIds = new Set((formData.memberUserIds || []).map((item) => String(item)))
  return userOptions.value.filter((user) => selectedIds.has(String(user.id)))
})

const memberCandidateOptions = computed(() => {
  const currentDeptId = normalizeId(selectedDeptId.value)
  if (!currentDeptId) {
    return userOptions.value
  }
  const deptScopeIds = collectDeptScopeIds(currentDeptId)
  const deptUsers = userOptions.value.filter((user) => deptScopeIds.has(normalizeId(user.deptId)))
  const merged = new Map<string, UserVO>()
  ;[...deptUsers, ...selectedMemberOptions.value].forEach((user) => {
    merged.set(String(user.id), user)
  })
  return Array.from(merged.values())
})

const memberDetailList = computed(() => formData.members || [])

const validateMembers = (_rule: any, _value: any, callback: (error?: Error) => void) => {
  const memberIds = (formData.memberUserIds || []).map((item) => String(item))
  if (memberIds.length === 0) {
    callback(new Error('请先选择班组成员'))
    return
  }
  const leaderId = normalizeId(formData.leaderUserId)
  if (leaderId && !memberIds.includes(leaderId)) {
    callback(new Error('班组长必须是该班组成员'))
    return
  }
  callback()
}

const validateLeader = (_rule: any, _value: any, callback: (error?: Error) => void) => {
  const leaderId = normalizeId(formData.leaderUserId)
  if (!leaderId) {
    callback(new Error('班组长不能为空'))
    return
  }
  const memberIds = (formData.memberUserIds || []).map((item) => String(item))
  if (!memberIds.includes(leaderId)) {
    callback(new Error('班组长必须是该班组成员'))
    return
  }
  callback()
}

const formRules: FormRules<ShiftTeamFormVO> = {
  teamName: [{ required: true, message: '班组名称不能为空', trigger: 'blur' }],
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  memberUserIds: [{ validator: validateMembers, trigger: 'change' }],
  leaderUserId: [{ validator: validateLeader, trigger: 'change' }]
}

const formReadonly = computed(() => formMode.value === 'detail')
const formTitle = computed(() => {
  if (formMode.value === 'create') return '新增班组'
  if (formMode.value === 'update') return '编辑班组'
  return '班组详情'
})

const resetFormData = () => {
  Object.assign(formData, createEmptyFormData())
  selectedDeptId.value = ''
  formRef.value?.clearValidate()
}

const handleMembersChange = () => {
  const memberIds = new Set((formData.memberUserIds || []).map((item) => String(item)))
  const leaderId = normalizeId(formData.leaderUserId)
  if (leaderId && !memberIds.has(leaderId)) {
    formData.leaderUserId = undefined
  }
  if (!formReadonly.value) {
    formRef.value?.validateField('memberUserIds')
    formRef.value?.validateField('leaderUserId')
  }
}

const handleAppendDeptMembers = () => {
  const currentDeptId = normalizeId(selectedDeptId.value)
  if (!currentDeptId) {
    message.warning('请先选择所属部门')
    return
  }
  const deptScopeIds = collectDeptScopeIds(currentDeptId)
  const deptUserIds = memberCandidateOptions.value
    .filter((user) => deptScopeIds.has(normalizeId(user.deptId)))
    .map((user) => normalizeId(user.id))
    .filter((id) => !!id)
  if (!deptUserIds.length) {
    message.warning('当前部门下暂无可选成员')
    return
  }
  const beforeSize = (formData.memberUserIds || []).length
  const mergedIds = new Set([...(formData.memberUserIds || []), ...deptUserIds])
  formData.memberUserIds = Array.from(mergedIds)
  handleMembersChange()
  if (mergedIds.size === beforeSize) {
    message.info('该部门成员已全部在列表中')
    return
  }
  message.success(`已添加 ${mergedIds.size - beforeSize} 名部门成员`)
}

const syncSelectedDeptByLeader = () => {
  const leaderId = normalizeId(formData.leaderUserId)
  if (!leaderId) {
    selectedDeptId.value = ''
    return
  }
  const leader = userOptions.value.find((user) => normalizeId(user.id) === leaderId)
  const leaderDeptId = normalizeId(leader?.deptId)
  if (!leaderDeptId) {
    selectedDeptId.value = ''
    return
  }
  const exists = availableDeptOptions.value.some((dept) => normalizeId(dept.id) === leaderDeptId)
  selectedDeptId.value = exists ? leaderDeptId : ''
}

const fillFormByResp = (data: ShiftTeamVO) => {
  Object.assign(formData, {
    ...createEmptyFormData(),
    ...data
  })
  const memberIds = (data.memberUserIds || []).map((item) => String(item))
  if (!memberIds.length && data.members?.length) {
    data.members.forEach((member) => {
      if (member.userId !== undefined && member.userId !== null) {
        memberIds.push(String(member.userId))
      }
    })
  }
  formData.memberUserIds = Array.from(new Set(memberIds))
  formData.leaderUserId = normalizeId(data.leaderUserId) || undefined
  formData.members = data.members || []
  syncSelectedDeptByLeader()
}

const openForm = async (mode: FormMode, id?: IdType) => {
  formMode.value = mode
  formVisible.value = true
  resetFormData()

  if (!userOptions.value.length) {
    await loadUserOptions()
  }
  if (!deptOptions.value.length) {
    await loadDeptOptions()
  }
  if (mode === 'create') return
  if (!id) return

  formLoading.value = true
  try {
    const data = await ShiftTeamApi.get(id)
    fillFormByResp(data)
  } finally {
    formLoading.value = false
  }
}

const submitForm = async () => {
  if (formReadonly.value) return
  await formRef.value?.validate()

  submitLoading.value = true
  try {
    const payload: ShiftTeamVO = {
      id: formData.id,
      teamName: formData.teamName?.trim(),
      stationId: formData.stationId,
      leaderUserId: formData.leaderUserId,
      memberUserIds: formData.memberUserIds || [],
      remark: formData.remark?.trim()
    }
    if (formMode.value === 'create') {
      delete payload.id
      await ShiftTeamApi.create(payload)
      message.success('新增成功')
    } else {
      await ShiftTeamApi.update(payload)
      message.success('更新成功')
    }
    formVisible.value = false
    await getList()
  } finally {
    submitLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([getList(), loadUserOptions(), loadDeptOptions()])
})
</script>

<style scoped lang="scss">
.team-config-page {
  .query-wrap {
    border: 1px solid rgba(148, 163, 184, 0.32);
    border-radius: 14px;
    background: #ffffff;
    box-shadow: 0 8px 28px rgba(15, 23, 42, 0.08);
  }

  .team-no {
    display: inline-flex;
    align-items: center;
    height: 24px;
    padding: 0 10px;
    border-radius: 999px;
    border: 1px solid #ffd8d8;
    background: #fff5f5;
    color: #c62828;
    font-size: 12px;
    font-weight: 600;
  }

  .dept-select-wrap {
    display: flex;
    gap: 8px;
    width: 100%;
    align-items: center;

    .dept-select {
      flex: 1;
    }
  }

  .member-list {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
    min-height: 32px;
    align-items: center;
  }

  .member-tag {
    margin: 0;
  }
}
</style>
