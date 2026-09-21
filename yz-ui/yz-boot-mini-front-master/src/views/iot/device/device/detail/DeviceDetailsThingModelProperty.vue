<!-- 设备属性管理 -->
<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
      @submit.prevent
    >
      <el-form-item label="" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入属性名称、标志符"
          clearable
          class="!w-240px"
          @keyup.enter="handleQuery"
          @clear="handleQuery"
        />
      </el-form-item>
      <el-form-item class="float-right !mr-0 !mb-0">
        <el-button-group>
          <el-button :type="viewMode === 'card' ? 'primary' : 'default'" @click="viewMode = 'card'">
            <Icon icon="ep:grid" />
          </el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
            <Icon icon="ep:list" />
          </el-button>
        </el-button-group>
      </el-form-item>
      <!-- TODO @芋艿：参考阿里云，实时刷新！ -->
      <el-form-item>
        <el-switch
          size="large"
          width="80"
          v-model="autoRefresh"
          class="-ml-15px"
          inline-prompt
          active-text="定时刷新"
          inactive-text="定时刷新"
          style="--el-switch-on-color: #13ce66"
        />
      </el-form-item>
    </el-form>
  </ContentWrap>
  <ContentWrap>
    <!-- 卡片视图 -->
    <template v-if="viewMode === 'card'">
      <el-row :gutter="16" v-loading="loading">
        <el-col
          v-for="item in list"
          :key="item.identifier"
          :xs="24"
          :sm="12"
          :md="12"
          :lg="6"
          class="mb-4"
        >
          <el-card
            class="h-full transition-colors relative overflow-hidden"
            :body-style="{ padding: '0' }"
          >
            <!-- 添加渐变背景层 -->
            <div
              class="absolute top-0 left-0 right-0 h-[50px] pointer-events-none bg-gradient-to-b from-[#eefaff] to-transparent"
            >
            </div>
            <div class="p-4 relative">
              <!-- 标题区域 -->
              <div class="flex flex-wrap items-start mb-3">
                <div class="mr-2.5 flex items-center">
                  <Icon icon="ep:cpu" class="text-[18px] text-[#0070ff]" />
                </div>
                <div class="text-[16px] font-600 flex-1 min-w-0 break-words leading-5 pr-2">
                  {{ item.name }}
                </div>
                <!-- 标识符 -->
                <div class="inline-flex items-center mr-2 mt-1">
                  <el-tag size="small" type="primary">
                    {{ item.identifier }}
                  </el-tag>
                </div>
                <!-- 数据类型标签 -->
                <div class="inline-flex items-center mr-2 mt-1">
                  <el-tag size="small" type="info">
                    {{ item.dataType }}
                  </el-tag>
                </div>
                <!-- 数据图标 - 可点击 -->
                <div
                  class="cursor-pointer flex items-center justify-center w-8 h-8 rounded-full hover:bg-blue-50 transition-colors mt-1"
                  @click="openHistory(props.deviceId, item.identifier, item.dataType)"
                >
                  <Icon icon="ep:data-line" class="text-[18px] text-[#0070ff]" />
                </div>
              </div>

              <!-- 信息区域 -->
              <div class="text-[14px]">
                <div class="mb-2.5 last:mb-0 flex items-center">
                  <span class="text-[#717c8e] mr-2.5">标签名称</span>
                  <template v-if="isTagEditing(item)">
                    <el-input
                      v-model="item.tagName"
                      placeholder="请输入标签名称"
                      clearable
                      class="!w-200px mr-2"
                    />
                    <el-button
                      size="small"
                      type="primary"
                      :loading="isSavingTag(item.identifier)"
                      @click="handleSaveTag(item)"
                    >
                      保存
                    </el-button>
                    <el-button
                      v-if="hasTagName(item)"
                      size="small"
                      @click="handleCancelTag(item)"
                    >
                      取消
                    </el-button>
                  </template>
                  <template v-else>
                    <span class="text-[var(--el-text-color-primary)] font-600 mr-2">
                      {{ item.tagName }}
                    </span>
                    <el-button size="small" @click="handleEditTag(item)">
                      编辑
                    </el-button>
                  </template>
                </div>
                <div class="mb-2.5 last:mb-0">
                  <span class="text-[#717c8e] mr-2.5">属性值</span>
                  <span class="text-[var(--el-text-color-primary)] font-600">
                    {{ formatValueWithUnit(item) }}
                  </span>
                </div>
                <div class="mb-2.5 last:mb-0">
                  <span class="text-[#717c8e] mr-2.5">更新时间</span>
                  <span class="text-[var(--el-text-color-primary)] text-[12px]">
                    {{ item.updateTime ? formatDate(item.updateTime) : '-' }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 列表视图 -->
    <el-table v-else v-loading="loading" :data="list" :stripe="true" :show-overflow-tooltip="true">
      <el-table-column label="属性标识符" align="center" prop="identifier" />
      <el-table-column label="属性名称" align="center" prop="name" min-width="260" />
      <el-table-column label="标签名称" align="center" min-width="260">
        <template #default="scope">
          <div class="flex items-center justify-center">
            <template v-if="isTagEditing(scope.row)">
              <el-input
                v-model="scope.row.tagName"
                placeholder="请输入标签名称"
                clearable
                class="!w-200px mr-2"
              />
              <el-button
                link
                type="primary"
                :loading="isSavingTag(scope.row.identifier)"
                @click="handleSaveTag(scope.row)"
              >
                保存
              </el-button>
              <el-button
                v-if="hasTagName(scope.row)"
                link
                @click="handleCancelTag(scope.row)"
              >
                取消
              </el-button>
            </template>
            <template v-else>
              <span class="text-[var(--el-text-color-primary)] mr-2">
                {{ scope.row.tagName }}
              </span>
              <el-button link type="primary" @click="handleEditTag(scope.row)">
                编辑
              </el-button>
            </template>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="数据类型" align="center" prop="dataType" />
      <el-table-column label="属性值" align="center" prop="value">
        <template #default="scope">
          {{ formatValueWithUnit(scope.row) }}
        </template>
      </el-table-column>
      <el-table-column
        label="更新时间"
        align="center"
        prop="updateTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openHistory(props.deviceId, scope.row.identifier, scope.row.dataType)"
          >
            查看数据
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 表单弹窗：添加/修改 -->
    <DeviceDetailsThingModelPropertyHistory ref="historyRef" :deviceId="props.deviceId" />
  </ContentWrap>
</template>
<script setup lang="ts">
import { DeviceApi, IotDevicePropertyDetailRespVO } from '@/api/iot/device/device'
import { dateFormatter, formatDate } from '@/utils/formatTime'
import DeviceDetailsThingModelPropertyHistory from './DeviceDetailsThingModelPropertyHistory.vue'

const props = defineProps<{ deviceId: string | number }>()

const loading = ref(true) // 列表的加载中
const list = ref<IotDevicePropertyDetailRespVO[]>([]) // 显示的列表数据
const filterList = ref<IotDevicePropertyDetailRespVO[]>([]) // 完整的数据列表
const queryParams = reactive({
  keyword: '' as string
})
const autoRefresh = ref(false) // 自动刷新开关
let autoRefreshTimer: any = null // 定时器
const viewMode = ref<'card' | 'list'>('card') // 视图模式状态
const tagSavingMap = reactive<Record<string, boolean>>({})
const tagEditMap = reactive<Record<string, boolean>>({})
const tagOriginalMap = reactive<Record<string, string>>({})
const message = useMessage() // 消息提示

const queryFormRef = ref() // 搜索的表单

const initTagCache = (items: IotDevicePropertyDetailRespVO[]) => {
  const identifiers = new Set<string>()
  items.forEach((item) => {
    const identifier = item?.identifier
    if (!identifier) {
      return
    }
    identifiers.add(identifier)
    tagOriginalMap[identifier] = item.tagName || ''
    if (!(identifier in tagEditMap)) {
      tagEditMap[identifier] = false
    }
  })
  Object.keys(tagOriginalMap).forEach((identifier) => {
    if (!identifiers.has(identifier)) {
      delete tagOriginalMap[identifier]
      delete tagEditMap[identifier]
      delete tagSavingMap[identifier]
    }
  })
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const params = {
      deviceId: props.deviceId,
      identifier: undefined as string | undefined,
      name: undefined as string | undefined
    }
    filterList.value = await DeviceApi.getLatestDeviceProperties(params)
    initTagCache(filterList.value)
    handleFilter()
  } finally {
    loading.value = false
  }
}

/** 前端筛选数据 */
const handleFilter = () => {
  if (!queryParams.keyword.trim()) {
    list.value = filterList.value
  } else {
    const keyword = queryParams.keyword.toLowerCase()
    list.value = filterList.value.filter(
      (item) =>
        item.identifier?.toLowerCase().includes(keyword) ||
        item.name?.toLowerCase().includes(keyword) ||
        item.tagName?.toLowerCase().includes(keyword)
    )
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  handleFilter()
}

const normalizeTagName = (value?: string) => (value || '').trim()

const hasTagName = (item: IotDevicePropertyDetailRespVO) => !!normalizeTagName(item.tagName)

const isTagEditing = (item: IotDevicePropertyDetailRespVO) => {
  const identifier = item.identifier
  if (!identifier) {
    return true
  }
  if (tagEditMap[identifier]) {
    return true
  }
  const originalTagName = normalizeTagName(tagOriginalMap[identifier])
  return !originalTagName
}

const handleEditTag = (item: IotDevicePropertyDetailRespVO) => {
  const identifier = item.identifier
  if (!identifier) {
    return
  }
  tagOriginalMap[identifier] = item.tagName || ''
  tagEditMap[identifier] = true
}

const handleCancelTag = (item: IotDevicePropertyDetailRespVO) => {
  const identifier = item.identifier
  if (!identifier) {
    return
  }
  if (Object.prototype.hasOwnProperty.call(tagOriginalMap, identifier)) {
    item.tagName = tagOriginalMap[identifier]
  }
  tagEditMap[identifier] = false
}

const isTagNameDuplicate = (tagName: string, currentIdentifier?: string) => {
  if (!tagName) {
    return false
  }
  const source = filterList.value.length ? filterList.value : list.value
  return source.some(
    (item) =>
      item.identifier !== currentIdentifier && normalizeTagName(item.tagName) === tagName
  )
}

const isSavingTag = (identifier?: string) => !!(identifier && tagSavingMap[identifier])

const handleSaveTag = async (item: IotDevicePropertyDetailRespVO) => {
  const identifier = item.identifier
  if (!identifier) {
    message.error('属性标识符不能为空')
    return
  }
  const tagName = normalizeTagName(item.tagName)
  if (!tagName) {
    message.error('标签名称不能为空')
    return
  }
  if (isTagNameDuplicate(tagName, identifier)) {
    message.error('标签名称在该设备中必须唯一')
    return
  }
  tagSavingMap[identifier] = true
  try {
    await DeviceApi.saveDevicePropertyTags({
      deviceId: props.deviceId,
      items: [{ identifier, tagName }]
    })
    message.success('保存成功')
    tagEditMap[identifier] = false
    tagOriginalMap[identifier] = tagName
    await getList()
  } finally {
    tagSavingMap[identifier] = false
  }
}


/** 历史操作 */
const historyRef = ref()
const openHistory = (deviceId: string | number, identifier: string, dataType: string) => {
  historyRef.value.open(deviceId, identifier, dataType)
}

/** 格式化属性值和单位 */
const formatValueWithUnit = (item: IotDevicePropertyDetailRespVO) => {
  if (item.value === null || item.value === undefined || item.value === '') {
    return '-'
  }
  const unitName = item.dataSpecs?.unitName
  return unitName ? `${item.value} ${unitName}` : item.value
}

/** 监听自动刷新 */
watch(autoRefresh, (newValue) => {
  if (newValue) {
    autoRefreshTimer = setInterval(() => {
      getList()
    }, 5000) // 每 5 秒刷新一次
  } else {
    clearInterval(autoRefreshTimer)
    autoRefreshTimer = null
  }
})

/** 组件卸载时清除定时器 */
onBeforeUnmount(() => {
  if (autoRefreshTimer) {
    clearInterval(autoRefreshTimer)
  }
})

/** 初始化 **/
onMounted(() => {
  getList()
})
</script>
