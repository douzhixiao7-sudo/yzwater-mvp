<template>
  <div class="task-page">
    <ContentWrap class="query-wrap">
      <el-form :inline="true" class="query-form" @submit.prevent>
        <el-form-item label="工程名称">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入工程名称"
            clearable
            class="!w-220px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item v-if="showLevelAndFiles" label="防汛等级">
          <el-select
            v-model="queryForm.level"
            placeholder="请选择防汛等级"
            clearable
            class="!w-200px"
          >
            <el-option
              v-for="item in levelOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" class="mr-5px" />
            搜索
          </el-button>
          <el-button @click="resetQuery">
            <Icon icon="ep:refresh" class="mr-5px" />
            重置
          </el-button>
          <el-button type="primary" plain @click="openCreateDialog">新增</el-button>
        </el-form-item>
      </el-form>
    </ContentWrap>

    <ContentWrap class="table-wrap">
      <el-table v-loading="loading" :data="filteredList" row-key="id" class="task-table">
        <el-table-column type="index" label="序号" width="70" align="center" />
        <el-table-column prop="name" label="工程名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="addr" label="险工位置" min-width="220" show-overflow-tooltip />
        <el-table-column v-if="showLevelAndFiles" label="防汛等级" align="center" min-width="120">
          <template #default="{ row }">
            <el-tag v-if="row.level" :type="getLevelTagType(row.level)" effect="plain">
              {{ getLevelLabel(row.level) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="content" label="险情描述" min-width="280" show-overflow-tooltip />
        <el-table-column prop="counterMeasures" label="应对措施" min-width="280" show-overflow-tooltip />
        <el-table-column label="处理状态" align="center" width="100">
          <template #default="{ row }">
            <el-tag :type="isTaskResolved(row) ? 'success' : 'warning'" effect="plain">
              {{ isTaskResolved(row) ? '已解决' : '待处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="showLevelAndFiles" label="附件" min-width="220">
          <template #default="{ row }">
            <div v-if="row.files?.length" class="file-name-list">
              <el-tooltip
                v-for="(fileUrl, index) in row.files"
                :key="`${row.id}-${index}-${fileUrl}`"
                :content="getFileName(fileUrl)"
                placement="top-start"
              >
                <el-link :href="fileUrl" download target="_blank" type="primary" class="file-name-link">
                  {{ getFileName(fileUrl) }}
                </el-link>
              </el-tooltip>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailDialog(row.id)">详情</el-button>
            <el-button link type="primary" @click="openEditDialog(row.id)">编辑</el-button>
            <el-button
              v-if="showResolveAction && !isTaskResolved(row)"
              link
              type="success"
              @click="openResolveDialog(row)"
            >
              解决
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>

        <template #empty>
          <el-empty description="暂无风险隐患点数据" />
        </template>
      </el-table>
    </ContentWrap>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :width="showMapDrawing ? '920px' : '640px'"
      class="task-dialog"
      align-center
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="task-dialog__scroll">
      <el-form
        ref="formRef"
        class="task-form"
        :model="form"
        :rules="rules"
        label-width="98px"
        label-position="left"
      >
        <el-row :gutter="16">
          <el-col :span="24">
            <section class="task-form-section">
              <div class="task-form-section__title">基本信息</div>
              <el-row :gutter="16" class="task-form-section__body">
                <el-col :span="24">
                  <el-form-item label="工程名称" prop="name">
                    <el-input v-model="form.name" maxlength="256" placeholder="请输入工程名称" />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="险工位置" prop="addr">
                    <el-input v-model="form.addr" maxlength="256" placeholder="请输入险工位置" />
                  </el-form-item>
                </el-col>
                <el-col v-if="showRiverChannel" :span="24">
                  <el-form-item label="所属河道" prop="riverChannelId">
                    <el-select
                      v-model="form.riverChannelId"
                      class="full-width"
                      placeholder="请选择所属河道"
                      filterable
                    >
                      <el-option
                        v-for="item in riverChannelOptions"
                        :key="item.id"
                        :label="item.riverName"
                        :value="String(item.id)"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="险情描述" prop="content">
                    <el-input
                      v-model="form.content"
                      type="textarea"
                      :rows="4"
                      maxlength="2048"
                      show-word-limit
                      placeholder="请输入险情描述"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="24">
                  <el-form-item label="应对措施" prop="counterMeasures">
                    <el-input
                      v-model="form.counterMeasures"
                      type="textarea"
                      :rows="4"
                      maxlength="2048"
                      show-word-limit
                      placeholder="请输入应对措施"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
            </section>
          </el-col>
          <el-col v-if="showLevelAndFiles" :span="12">
            <el-form-item label="防汛等级" prop="level">
              <el-select v-model="form.level" class="full-width" placeholder="请选择防汛等级" clearable>
                <el-option
                  v-for="item in levelOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col v-if="showLevelAndFiles" :span="24">
            <el-form-item label="附件上传" prop="files">
              <UploadFile
                v-model="form.files"
                :limit="10"
                :file-size="20"
                :file-type="['pdf', 'doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt', 'zip', 'rar', 'jpg', 'jpeg', 'png']"
              />
            </el-form-item>
          </el-col>
          <el-col v-if="showMapDrawing" :span="24">
            <section class="task-form-section task-form-section--map">
              <div class="task-form-section__title">路径绘制</div>
              <div class="task-form-section__body">
                <el-form-item
                  class="task-form-section__map-item"
                  label-width="0"
                  prop="geometryGeoJson"
                >
                  <TiandituGeoJsonEditor
                    ref="mapEditorRef"
                    v-model="form.geometryGeoJson"
                    :active="dialogVisible"
                    :height="400"
                    :center="mapCenter"
                    :zoom="14"
                    :draw-modes="['POINT', 'LINESTRING']"
                    enhanced-line-edit
                    line-endpoint-as-location
                    start-as-hazard-point
                    :endpoint-labels="mapHazardEndpointLabels"
                    multi-line-draw
                    enable-warehouse-picker
                    compact-toolbar
                  />
                </el-form-item>
              </div>
            </section>
          </el-col>
        </el-row>
      </el-form>
      </div>

      <template #footer>
        <div class="task-dialog__footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
          <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="detailVisible"
      title="详情"
      :width="showMapDrawing ? '920px' : '640px'"
      class="task-dialog task-dialog--detail"
      align-center
      destroy-on-close
      :close-on-click-modal="false"
    >
      <div class="task-dialog__scroll">
      <el-descriptions class="task-detail-descriptions" :column="1" border>
        <el-descriptions-item label="工程名称">{{ detailData.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="险工位置">{{ detailData.addr || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="showRiverChannel" label="所属河道">{{ detailData.riverChannelName || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="showLevelAndFiles" label="防汛等级">{{
          getLevelLabel(detailData.level)
        }}</el-descriptions-item>
        <el-descriptions-item label="险情描述">
          <div class="detail-content">{{ detailData.content || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="应对措施">
          <div class="detail-content">{{ detailData.counterMeasures || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="isTaskResolved(detailData) ? 'success' : 'warning'" effect="plain">
            {{ isTaskResolved(detailData) ? '已解决' : '待处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="isTaskResolved(detailData)" label="解决说明">
          <div class="detail-content">{{ detailData.resolveRemark || '-' }}</div>
        </el-descriptions-item>
        <el-descriptions-item v-if="isTaskResolved(detailData)" label="解决时间">
          {{ formatResolveTime(detailData.resolveTime) }}
        </el-descriptions-item>
        <el-descriptions-item v-if="isTaskResolved(detailData)" label="处置附件">
          <div v-if="detailData.resolveFiles?.length" class="detail-file-list">
            <div
              v-for="(fileUrl, index) in detailData.resolveFiles"
              :key="`resolve-${index}-${fileUrl}`"
              class="detail-file-item"
            >
              <el-link :href="fileUrl" download target="_blank" type="primary">
                {{ getFileName(fileUrl) }}
              </el-link>
            </div>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="showMapDrawing" label="地图标绘">
          <TiandituGeoJsonPreview
            :geo-json="detailData.geometryGeoJson || ''"
            :active="detailVisible"
            :height="360"
            :center="detailMapCenter"
            :zoom="14"
            :label-text="detailData.name || ''"
            start-as-hazard-point
            :endpoint-labels="detailHazardEndpointLabels"
            line-endpoint-as-location
            empty-text="暂无地图标绘"
          />
        </el-descriptions-item>
        <el-descriptions-item v-if="showLevelAndFiles" label="附件下载">
          <div v-if="detailData.files?.length" class="detail-file-list">
            <div
              v-for="(fileUrl, index) in detailData.files"
              :key="`detail-${index}-${fileUrl}`"
              class="detail-file-item"
            >
              <el-link :href="fileUrl" download target="_blank" type="primary">
                {{ getFileName(fileUrl) }}
              </el-link>
            </div>
          </div>
          <span v-else>-</span>
        </el-descriptions-item>
      </el-descriptions>
      </div>

      <template #footer>
        <div class="task-dialog__footer">
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="resolveVisible"
      title="解决隐患"
      width="520px"
      class="task-resolve-dialog"
      align-center
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="resolveFormRef"
        class="task-resolve-form"
        :model="resolveForm"
        :rules="resolveRules"
        label-width="96px"
        label-position="right"
      >
        <el-form-item label="工程名称" class="task-resolve-form__item">
          <el-input
            class="task-resolve-readonly-input"
            :model-value="resolveTarget.name || '-'"
            readonly
          />
        </el-form-item>
        <el-form-item label="处理说明" prop="resolveRemark" class="task-resolve-form__item">
          <el-input
            v-model="resolveForm.resolveRemark"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            placeholder="请填写处置情况"
          />
        </el-form-item>
        <el-form-item label="处置附件" class="task-resolve-form__item">
          <UploadFile
            v-model="resolveForm.resolveFiles"
            :limit="5"
            :file-size="20"
            :file-type="['pdf', 'doc', 'docx', 'xls', 'xlsx', 'jpg', 'jpeg', 'png']"
            :is-show-tip="false"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-space>
          <el-button @click="resolveVisible = false">取消</el-button>
          <el-button type="success" :loading="resolving" @click="submitResolve">确认解决</el-button>
        </el-space>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { UploadFile } from '@/components/UploadFile'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { getDictLabel, getStrDictOptions } from '@/utils/dict'
import {
  createFxTask,
  deleteFxTask,
  FX_TASK_STATUS,
  getFxTaskDetail,
  getFxTaskList,
  resolveFxTask,
  updateFxTask,
  type FxTaskListRespVO,
  type FxTaskSaveReqVO
} from '@/api/fx/task'
import { getRiverChannelSimpleList, type RiverChannelSimpleRespVO } from '@/api/gis/riverChannel'

defineOptions({ name: 'FxRiskPoint' })

/** 防汛等级、附件：暂时隐藏，改回 `true` 可恢复 */
const showLevelAndFiles = false

/** 操作列「解决」按钮：暂时隐藏，改回 `true` 可恢复 */
const showResolveAction = false

/** 表单「所属河道」选择：暂时隐藏，改回 `true` 可恢复 */
const showRiverChannel = false

const showMapDrawing = true

const loading = ref(false)
const list = ref<FxTaskListRespVO[]>([])
const riverChannelOptions = ref<RiverChannelSimpleRespVO[]>([])

const loadRiverChannelOptions = async () => {
  try {
    const data = await getRiverChannelSimpleList()
    riverChannelOptions.value = Array.isArray(data) ? data : []
  } catch {
    riverChannelOptions.value = []
  }
}
const levelOptions = computed(() => getStrDictOptions('zd_fxdj'))

const queryForm = reactive({
  name: '',
  level: ''
})

const filteredList = computed(() => {
  const keyword = queryForm.name.trim().toLowerCase()
  const level = queryForm.level
  return list.value.filter((item) => {
    const matchName = !keyword || (item.name || '').toLowerCase().includes(keyword)
    const matchLevel = !level || item.level === level
    return matchName && matchLevel
  })
})

const normalizeTaskItem = (item: FxTaskListRespVO): FxTaskListRespVO => ({
  ...item,
  files: Array.isArray(item.files) ? item.files : [],
  resolveFiles: Array.isArray(item.resolveFiles) ? item.resolveFiles : [],
  status: item.status ?? FX_TASK_STATUS.PENDING
})

const isTaskResolved = (row?: Pick<FxTaskListRespVO, 'status'>) =>
  row?.status === FX_TASK_STATUS.RESOLVED

const fetchList = async () => {
  loading.value = true
  try {
    const data = await getFxTaskList()
    list.value = Array.isArray(data) ? data.map(normalizeTaskItem) : []
  } finally {
    loading.value = false
  }
}

const handleQuery = () => undefined

const resetQuery = () => {
  queryForm.name = ''
  queryForm.level = ''
}

const dialogVisible = ref(false)
const detailVisible = ref(false)
const resolveVisible = ref(false)
const resolving = ref(false)
const submitting = ref(false)
const resolveFormRef = ref<FormInstance>()
const resolveTarget = reactive({
  id: '',
  name: '',
  addr: ''
})
const resolveForm = reactive({
  resolveRemark: '',
  resolveFiles: [] as string[]
})
const resolveRules: FormRules = {
  resolveRemark: [{ required: true, message: '请填写处理说明', trigger: 'blur' }]
}
const formMode = ref<'create' | 'edit'>('create')
const formRef = ref<FormInstance>()
const mapEditorRef = ref<{ getCommittedGeometryGeoJson?: () => string } | null>(null)

const resolveGeometryGeoJson = () =>
  mapEditorRef.value?.getCommittedGeometryGeoJson?.() || form.geometryGeoJson || ''

/** 地图隐藏时保留已加载几何，避免编辑误清空库内标绘 */
const resolveSubmitGeometryGeoJson = () => {
  if (!showMapDrawing) {
    return form.geometryGeoJson || ''
  }
  return resolveGeometryGeoJson()
}

const form = reactive<FxTaskSaveReqVO>({
  id: '',
  code: '',
  name: '',
  addr: '',
  riverChannelId: '',
  level: '',
  content: '',
  counterMeasures: '',
  files: [],
  sort: 0,
  geometryGeoJson: ''
})

const detailData = reactive<FxTaskListRespVO>({
  id: '',
  code: '',
  name: '',
  addr: '',
  riverChannelId: '',
  riverChannelName: '',
  level: '',
  content: '',
  counterMeasures: '',
  files: [],
  sort: 0,
  geometryGeoJson: '',
  status: FX_TASK_STATUS.PENDING,
  resolveRemark: '',
  resolveTime: '',
  resolveFiles: []
})

const DEFAULT_MAP_CENTER = [32.272258, 119.184766] as [number, number]

const parseGeometryCenter = (geoJson?: string): [number, number] | undefined => {
  if (!geoJson) return undefined
  try {
    const parsed = JSON.parse(geoJson)
    if (!parsed || !parsed.type) return undefined
    if (parsed.type === 'Point' && Array.isArray(parsed.coordinates)) {
      const lon = Number(parsed.coordinates[0])
      const lat = Number(parsed.coordinates[1])
      if (Number.isFinite(lon) && Number.isFinite(lat)) return [lat, lon]
      return undefined
    }
    if (parsed.type === 'LineString' && Array.isArray(parsed.coordinates) && parsed.coordinates.length > 0) {
      const first = parsed.coordinates[0]
      if (!Array.isArray(first) || first.length < 2) return undefined
      const lon = Number(first[0])
      const lat = Number(first[1])
      if (Number.isFinite(lon) && Number.isFinite(lat)) return [lat, lon]
    }
    if (parsed.type === 'MultiLineString' && Array.isArray(parsed.coordinates) && parsed.coordinates.length > 0) {
      const firstLine = parsed.coordinates[0]
      if (!Array.isArray(firstLine) || firstLine.length === 0) return undefined
      const first = firstLine[0]
      if (!Array.isArray(first) || first.length < 2) return undefined
      const lon = Number(first[0])
      const lat = Number(first[1])
      if (Number.isFinite(lon) && Number.isFinite(lat)) return [lat, lon]
    }
  } catch {
    return undefined
  }
  return undefined
}

const DEFAULT_HAZARD_MAP_LABEL = '隐患点'

const mapHazardEndpointLabels = computed<[string, string]>(() => {
  const name = (form.name || '').trim()
  return [name || DEFAULT_HAZARD_MAP_LABEL, '']
})

const detailHazardEndpointLabels = computed<[string, string]>(() => {
  const name = (detailData.name || '').trim()
  return [name || DEFAULT_HAZARD_MAP_LABEL, '']
})

const mapCenter = computed<[number, number]>(() => parseGeometryCenter(form.geometryGeoJson) || DEFAULT_MAP_CENTER)

const detailMapCenter = computed<[number, number]>(() => parseGeometryCenter(detailData.geometryGeoJson) || DEFAULT_MAP_CENTER)

const dialogTitle = computed(() => (formMode.value === 'edit' ? '编辑' : '新增'))

const rules = computed<FormRules>(() => {
  const base: FormRules = {
    name: [{ required: true, message: '请输入工程名称', trigger: 'blur' }],
    addr: [{ required: true, message: '请输入险工位置', trigger: 'blur' }],
    content: [{ required: true, message: '请输入险情描述', trigger: 'blur' }]
  }
  if (showRiverChannel) {
    base.riverChannelId = [{ required: true, message: '请选择所属河道', trigger: 'change' }]
  }
  return base
})

const resetForm = () => {
  form.id = ''
  form.code = ''
  form.name = ''
  form.addr = ''
  form.riverChannelId = ''
  form.level = ''
  form.content = ''
  form.counterMeasures = ''
  form.files = []
  form.sort = 0
  form.geometryGeoJson = ''
  formRef.value?.clearValidate()
}

const openCreateDialog = () => {
  formMode.value = 'create'
  resetForm()
  if (showRiverChannel) {
    void loadRiverChannelOptions()
  }
  dialogVisible.value = true
}

const openEditDialog = async (id: string) => {
  formMode.value = 'edit'
  resetForm()
  dialogVisible.value = true
  if (showRiverChannel) {
    void loadRiverChannelOptions()
  }
  const detail = await getFxTaskDetail(id)
  form.id = detail.id || ''
  form.code = detail.code || ''
  form.name = detail.name || ''
  form.addr = detail.addr || ''
  form.riverChannelId = detail.riverChannelId != null ? String(detail.riverChannelId) : ''
  form.level = detail.level || ''
  form.content = detail.content || ''
  form.counterMeasures = detail.counterMeasures || ''
  form.files = Array.isArray(detail.files) ? detail.files : []
  form.sort = detail.sort ?? 0
  form.geometryGeoJson = detail.geometryGeoJson || ''
}

const applyDetailStatusFromList = (id: string) => {
  const row = list.value.find((item) => item.id === id)
  detailData.status = row?.status ?? FX_TASK_STATUS.PENDING
  detailData.resolveRemark = row?.resolveRemark || ''
  detailData.resolveTime = row?.resolveTime || ''
  detailData.resolveFiles = Array.isArray(row?.resolveFiles) ? row.resolveFiles : []
}

const openDetailDialog = async (id: string) => {
  const detail = await getFxTaskDetail(id)
  detailData.id = detail.id || ''
  detailData.code = detail.code || ''
  detailData.name = detail.name || ''
  detailData.addr = detail.addr || ''
  detailData.riverChannelId = detail.riverChannelId != null ? String(detail.riverChannelId) : ''
  detailData.riverChannelName = detail.riverChannelName || ''
  detailData.level = detail.level || ''
  detailData.content = detail.content || ''
  detailData.counterMeasures = detail.counterMeasures || ''
  detailData.files = Array.isArray(detail.files) ? detail.files : []
  detailData.sort = detail.sort ?? 0
  detailData.geometryGeoJson = detail.geometryGeoJson || ''
  applyDetailStatusFromList(id)
  detailVisible.value = true
}

const openResolveDialog = (row: FxTaskListRespVO) => {
  resolveTarget.id = row.id
  resolveTarget.name = row.name || ''
  resolveTarget.addr = row.addr || ''
  resolveForm.resolveRemark = ''
  resolveForm.resolveFiles = []
  resolveVisible.value = true
  resolveFormRef.value?.clearValidate()
}

const markTaskResolvedLocally = (id: string, resolveRemark: string, resolveFiles: string[]) => {
  const item = list.value.find((row) => row.id === id)
  if (!item) return
  item.status = FX_TASK_STATUS.RESOLVED
  item.resolveRemark = resolveRemark
  item.resolveTime = new Date().toISOString()
  item.resolveFiles = resolveFiles
}

const submitResolve = async () => {
  await resolveFormRef.value?.validate()
  resolving.value = true
  const remark = resolveForm.resolveRemark.trim()
  const resolveFiles = Array.isArray(resolveForm.resolveFiles) ? [...resolveForm.resolveFiles] : []
  try {
    try {
      await resolveFxTask({
        id: resolveTarget.id,
        resolveRemark: remark,
        resolveFiles
      })
    } catch {
      // 后端接口未就绪时，先在前端完成状态闭环
    }
    markTaskResolvedLocally(resolveTarget.id, remark, resolveFiles)
    if (detailVisible.value && detailData.id === resolveTarget.id) {
      detailData.status = FX_TASK_STATUS.RESOLVED
      detailData.resolveRemark = remark
      detailData.resolveTime = new Date().toISOString()
      detailData.resolveFiles = resolveFiles
    }
    resolveVisible.value = false
    ElMessage.success('已标记为已解决')
  } finally {
    resolving.value = false
  }
}

const formatResolveTime = (value?: string) => {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  const pad = (num: number) => String(num).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`
}

const submitForm = async () => {
  await formRef.value?.validate()
  submitting.value = true
  try {
    const riverChannelId = form.riverChannelId.trim()
    const payload: FxTaskSaveReqVO = {
      id: form.id,
      code: form.code,
      name: form.name.trim(),
      addr: form.addr.trim(),
      riverChannelId: riverChannelId || undefined,
      level: form.level,
      content: form.content.trim(),
      counterMeasures: form.counterMeasures?.trim() ?? '',
      files: form.files,
      sort: form.sort ?? 0,
      geometryGeoJson: resolveSubmitGeometryGeoJson()
    }
    if (formMode.value === 'edit') {
      await updateFxTask(payload)
      ElMessage.success('编辑成功')
    } else {
      await createFxTask(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await fetchList()
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (id: string) => {
  try {
    await ElMessageBox.confirm('确认删除该数据吗？', '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  await deleteFxTask(id)
  ElMessage.success('删除成功')
  await fetchList()
}

const getLevelLabel = (value?: string) => getDictLabel('zd_fxdj', value) || value || '-'

const getLevelTagType = (value?: string) => {
  const label = getLevelLabel(value)
  if (label.includes('一级') || label.includes('I级') || label.includes('重大')) {
    return 'danger'
  }
  if (label.includes('二级') || label.includes('II级') || label.includes('较大')) {
    return 'warning'
  }
  if (label.includes('三级') || label.includes('III级') || label.includes('一般')) {
    return 'success'
  }
  return 'info'
}

const getFileName = (fileUrl: string) => {
  const safeUrl = (fileUrl || '').split('?')[0]
  const raw = safeUrl.substring(safeUrl.lastIndexOf('/') + 1) || safeUrl
  try {
    return decodeURIComponent(raw)
  } catch {
    return raw
  }
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.task-page {
  min-height: 100%;
  --task-border: #dbe6f3;
}

.query-wrap {
  margin-bottom: 12px;
}

.table-wrap {
  margin-bottom: 12px;
}

.query-form {
  margin-bottom: -15px;
}

.task-table {
  border: 1px solid var(--task-border);
  border-radius: 8px;
}

.task-table :deep(.el-table__cell) {
  padding-top: 8px;
  padding-bottom: 8px;
  border-right: 1px solid var(--el-table-border-color);
}

.task-table :deep(.el-table__header .el-table__cell:first-child),
.task-table :deep(.el-table__body .el-table__cell:first-child) {
  border-left: 1px solid var(--el-table-border-color);
}

.task-form :deep(.el-form-item__content) {
  max-width: 100%;
}

.full-width {
  width: 100%;
}

.file-name-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 6px;
}

.file-name-link {
  display: flex;
  width: 100%;
  max-width: 100%;
  text-align: left;
  align-items: flex-start;
  padding: 2px 8px;
  border-radius: 4px;
  border: 1px solid var(--el-border-color);
  background: var(--el-fill-color);
  font-size: 12px;
  line-height: 18px;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
}

.detail-file-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.detail-file-item :deep(.el-link__inner) {
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
  text-align: left;
}

.task-dialog :deep(.el-upload-list__item-file-name) {
  max-width: none;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
  line-height: 18px;
}

.task-dialog__scroll-hint {
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.5;
}

.task-form-section {
  margin-bottom: 4px;
  padding: 12px 14px 4px;
  background: var(--el-fill-color-light);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
}

.task-form-section__title {
  margin-bottom: 10px;
  padding-left: 8px;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  color: var(--el-text-color-primary);
  border-left: 3px solid var(--el-color-primary);
}

.task-form-section__body {
  margin-bottom: -8px;
}

.task-form-section--map {
  margin-top: 12px;
  padding-bottom: 12px;
}

.task-form-section--map .task-form-section__body {
  margin-bottom: 0;
}

.task-form-section__map-item {
  margin-bottom: 0;
}

.task-form-section__map-item :deep(> .el-form-item__content) {
  margin-left: 0 !important;
  width: 100%;
}

.task-resolve-form__item {
  margin-bottom: 18px;
}

.task-resolve-form__item:last-child {
  margin-bottom: 0;
}

.task-resolve-readonly-input :deep(.el-input__wrapper) {
  background-color: var(--el-fill-color-light);
  box-shadow: 0 0 0 1px var(--el-border-color-lighter) inset;
}

.task-resolve-readonly-input :deep(.el-input__inner) {
  color: var(--el-text-color-regular);
  cursor: default;
}

.task-detail-descriptions :deep(.el-descriptions__label) {
  width: 108px;
  font-weight: 500;
  background: var(--el-fill-color-light);
}

.task-detail-descriptions :deep(.el-descriptions__content) {
  line-height: 1.7;
}

</style>

<style lang="scss">
/* el-dialog 会 teleport 到 body；align-center 弹框垂直水平居中 */
body .el-overlay.is-align-center {
  display: flex;
  align-items: center;
  justify-content: center;
}

body .el-overlay.is-align-center .el-overlay-dialog {
  display: flex;
  align-items: center;
  justify-content: center;
  position: static;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 16px;
  overflow: visible;
  box-sizing: border-box;
}

body .el-overlay.is-align-center .el-dialog.task-dialog,
body .el-overlay.is-align-center .el-dialog.task-resolve-dialog {
  margin: 0 !important;
  position: relative;
  top: auto !important;
  transform: none !important;
}

/* 大表单弹框布局 */
body .el-overlay .el-dialog.task-dialog {
  display: flex;
  flex-direction: column;
  max-width: calc(100vw - 32px);
  max-height: calc(100vh - 48px);
  border-radius: 8px;
  overflow: hidden;
}

body .el-overlay .el-dialog.task-dialog .el-dialog__header {
  flex-shrink: 0;
  margin-right: 0;
  padding-bottom: 12px;
}

body .el-overlay .el-dialog.task-dialog .el-dialog__footer {
  flex-shrink: 0;
}

body .el-overlay .el-dialog.task-dialog .task-dialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

body .el-overlay .el-dialog.task-dialog .el-dialog__body {
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
  padding-top: 8px;
  padding-bottom: 12px;
}

body .el-overlay .el-dialog.task-dialog .task-dialog__scroll {
  max-height: calc(100vh - 220px);
  overflow-x: hidden;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
  padding-right: 4px;
  padding-bottom: 4px;
}

/* 解决弹框：与项目其他表单弹框一致，内容区自适应高度 */
body .el-overlay .el-dialog.task-resolve-dialog {
  width: 520px;
  max-width: calc(100vw - 32px);
  margin: auto;
  border-radius: 8px;
}

body .el-overlay .el-dialog.task-resolve-dialog .el-dialog__body {
  padding: 10px 20px 6px;
}

body .el-overlay .el-dialog.task-resolve-dialog .el-dialog__footer {
  padding-top: 10px;
}

body .el-overlay .el-dialog.task-resolve-dialog .task-resolve-form.el-form--label-right .el-form-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 18px;
}

body .el-overlay .el-dialog.task-resolve-dialog .task-resolve-form.el-form--label-right .el-form-item:last-child {
  margin-bottom: 0;
}

body .el-overlay .el-dialog.task-resolve-dialog .task-resolve-form .el-form-item__label {
  width: 96px !important;
  min-width: 96px;
  padding-right: 12px;
  line-height: 32px;
  text-align: right;
  justify-content: flex-end;
}

body .el-overlay .el-dialog.task-resolve-dialog .task-resolve-form .el-form-item__content {
  flex: 1;
  min-width: 0;
  line-height: 32px;
}
</style>
