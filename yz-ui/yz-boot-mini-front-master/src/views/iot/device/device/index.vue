<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="68px"
    >
      <el-form-item label="产品" prop="productId">
        <el-select
          v-model="queryParams.productId"
          placeholder="请选择产品"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="product in products"
            :key="product.id"
            :label="product.name"
            :value="product.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备名称" prop="deviceName">
        <el-input
          v-model="queryParams.deviceName"
          placeholder="请输入设备名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="设备编码" prop="serialNumber">
        <el-input
          v-model="queryParams.serialNumber"
          placeholder="请输入设备编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item label="所属站点" prop="stationId">
        <el-select
          v-model="queryParams.stationId"
          placeholder="请选择所属站点"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备分类" prop="deviceType">
        <el-select
          v-model="queryParams.deviceType"
          placeholder="请选择设备类型"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_DEVICE_TYPE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="使用状态" prop="useStatus">
        <el-select
          v-model="queryParams.useStatus"
          placeholder="请选择使用状态"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_USE_STATUS)"
            :key="String(dict.value)"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备状态" prop="status">
        <el-select
          v-model="queryParams.status"
          placeholder="请选择设备状态"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_DEVICE_STATE)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="设备分组" prop="groupId">
        <el-select
          v-model="queryParams.groupId"
          placeholder="请选择设备分组"
          clearable
          class="!w-240px"
        >
          <el-option
            v-for="group in deviceGroups"
            :key="group.id"
            :label="group.name"
            :value="group.id"
          />
        </el-select>
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
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon icon="ep:search" class="mr-5px" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" class="mr-5px" />
          重置
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
          v-hasPermi="['iot:device:create']"
        >
          <Icon icon="ep:plus" class="mr-5px" />
          新增
        </el-button>
        <el-button
          type="success"
          plain
          @click="handleExport"
          :loading="exportLoading"
          v-hasPermi="['iot:device:export']"
        >
          <Icon icon="ep:download" class="mr-5px" /> 导出
        </el-button>
        <el-button type="warning" plain @click="handleImport" v-hasPermi="['iot:device:import']">
          <Icon icon="ep:upload" /> 导入
        </el-button>
        <el-button
          type="primary"
          plain
          @click="openGroupForm"
          :disabled="selectedIds.length === 0"
          v-hasPermi="['iot:device:update']"
        >
          <Icon icon="ep:folder-add" class="mr-5px" /> 添加到分组
        </el-button>
        <el-button
          type="danger"
          plain
          @click="handleDeleteList"
          :disabled="selectedIds.length === 0"
          v-hasPermi="['iot:device:delete']"
        >
          <Icon icon="ep:delete" class="mr-5px" /> 批量删除
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <template v-if="viewMode === 'card'">
      <el-row :gutter="16">
        <el-col v-for="item in list" :key="item.id" :xs="24" :sm="12" :md="12" :lg="6" class="mb-4">
          <el-card
            class="h-full transition-colors relative overflow-hidden"
            :body-style="{ padding: '0' }"
          >
            <!-- 添加渐变背景层 -->
            <div
              class="absolute top-0 left-0 right-0 h-[50px] pointer-events-none"
              :class="[
                item.state === DeviceStateEnum.ONLINE
                  ? 'bg-gradient-to-b from-[#eefaff] to-transparent'
                  : 'bg-gradient-to-b from-[#fff1f1] to-transparent'
              ]"
            >
            </div>
            <div class="p-4 relative">
              <!-- 标题区域 -->
              <div class="flex items-center mb-3">
                <div class="mr-2.5 flex items-center">
                  <el-image :src="defaultIconUrl" class="w-[18px] h-[18px]" />
                </div>
                <div class="text-[16px] font-600 flex-1">
                  {{ item.deviceName || '-' }}
                </div>
                <!-- 添加设备状态标签 -->
                <div class="inline-flex items-center">
                  <div
                    class="w-1 h-1 rounded-full mr-1.5"
                    :class="
                      item.state === DeviceStateEnum.ONLINE
                        ? 'bg-[var(--el-color-success)]'
                        : 'bg-[var(--el-color-danger)]'
                    "
                  >
                  </div>
                  <el-text
                    class="!text-xs font-bold"
                    :type="item.state === DeviceStateEnum.ONLINE ? 'success' : 'danger'"
                  >
                    {{ getDictLabel(DICT_TYPE.IOT_DEVICE_STATE, item.state) }}
                  </el-text>
                </div>
              </div>

              <!-- 信息区域 -->
              <div class="flex items-center text-[14px]">
                <div class="flex-1">
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">所属产品</span>
                    <el-link class="text-[#0070ff]" @click="openProductDetail(item.productId)">
                      {{ products.find((p) => p.id === item.productId)?.name }}
                    </el-link>
                  </div>
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">设备分类</span>
                    <dict-tag :type="DICT_TYPE.IOT_DEVICE_TYPE" :value="item.deviceType" />
                  </div>
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">使用状态</span>
                    <dict-tag :type="DICT_TYPE.IOT_DEVICE_USE_STATUS" :value="item.useStatus" />
                  </div>
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">所属站点</span>
                    <span
                      class="text-[var(--el-text-color-primary)] inline-block align-middle overflow-hidden text-ellipsis whitespace-nowrap max-w-[130px]"
                    >
                      {{ getDictLabel(DICT_TYPE.IOT_ZD_SBZD, item.stationId) || '-' }}
                    </span>
                  </div>
                  <div class="mb-2.5 last:mb-0">
                    <span class="text-[#717c8e] mr-2.5">设备位置</span>
                    <span
                      class="text-[var(--el-text-color-primary)] inline-block align-middle overflow-hidden text-ellipsis whitespace-nowrap max-w-[130px]"
                    >
                      {{ resolveLocationName(item.address) }}
                    </span>
                  </div>
                </div>
                <div class="w-[100px] h-[100px]">
                  <el-image :src="item.picUrl?.[0] || defaultPicUrl" class="w-full h-full" />
                </div>
              </div>

              <!-- 分隔线 -->
              <el-divider class="!my-3" />

              <!-- 按钮 -->
              <div class="flex items-center px-0">
                <el-button
                  class="flex-1 !px-2 !h-[32px] text-[13px]"
                  type="primary"
                  plain
                  @click="openForm('update', item.id)"
                  v-hasPermi="['iot:device:update']"
                >
                  <Icon icon="ep:edit-pen" class="mr-1" />
                  编辑
                </el-button>
                <el-button
                  class="flex-1 !px-2 !h-[32px] !ml-[10px] text-[13px]"
                  type="warning"
                  plain
                  @click="openDetail(item.id)"
                >
                  <Icon icon="ep:view" class="mr-1" />
                  详情
                </el-button>
                <el-button
                  class="flex-1 !px-2 !h-[32px] !ml-[10px] text-[13px]"
                  type="info"
                  plain
                  @click="openModel(item.id)"
                >
                  <Icon icon="ep:tickets" class="mr-1" />
                  数据
                </el-button>
                <div class="mx-[10px] h-[20px] w-[1px] bg-[#dcdfe6]"></div>
                <el-button
                  class="!px-2 !h-[32px] text-[13px]"
                  type="danger"
                  plain
                  @click="handleDelete(item.id)"
                  v-hasPermi="['iot:device:delete']"
                >
                  <Icon icon="ep:delete" />
                </el-button>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 列表视图 -->
    <el-table
      v-else
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55" />
      <el-table-column label="设备名称" align="center" prop="deviceName">
        <template #default="scope">
          <el-link @click="openDetail(scope.row.id)">
            {{ scope.row.deviceName || '-' }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="设备编码" align="center" prop="serialNumber" />
      <el-table-column label="所属站点" align="center" prop="stationId">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_ZD_SBZD" :value="scope.row.stationId" />
        </template>
      </el-table-column>
      <el-table-column label="所属产品" align="center" prop="productId">
        <template #default="scope">
          <el-link @click="openProductDetail(scope.row.productId)">
            {{ products.find((p) => p.id === scope.row.productId)?.name || '-' }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column label="设备分类" align="center" prop="deviceType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_TYPE" :value="scope.row.deviceType" />
        </template>
      </el-table-column>
      <el-table-column label="使用状态" align="center" prop="useStatus">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_USE_STATUS" :value="scope.row.useStatus" />
        </template>
      </el-table-column>
      <el-table-column label="设备状态" align="center" prop="state">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_DEVICE_STATE" :value="scope.row.state" />
        </template>
      </el-table-column>
      <el-table-column
        label="最后上线时间"
        align="center"
        prop="onlineTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="120px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openDetail(scope.row.id)"
            v-hasPermi="['iot:product:query']"
          >
            查看
          </el-button>
          <el-button link type="primary" @click="openModel(scope.row.id)"> 日志 </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
            v-hasPermi="['iot:device:update']"
          >
            编辑
          </el-button>
          <el-button
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
            v-hasPermi="['iot:device:delete']"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      :page-sizes="pageSizes"
      @pagination="getList"
    />
  </ContentWrap>

  <!-- 表单弹窗：添加/修改 -->
  <DeviceForm ref="formRef" @success="getList" />
  <!-- 分组表单组件 -->
  <DeviceGroupForm ref="groupFormRef" @success="getList" />
  <!-- 导入表单组件 -->
  <DeviceImportForm ref="importFormRef" @success="getList" />
</template>

<script setup lang="ts">
import { DICT_TYPE, getIntDictOptions, getStrDictOptions, getDictLabel } from '@/utils/dict'
import { dateFormatter } from '@/utils/formatTime'
import { DeviceApi, DeviceVO, DeviceStateEnum } from '@/api/iot/device/device'
import DeviceForm from './DeviceForm.vue'
import { ProductApi, ProductVO } from '@/api/iot/product/product'
import { DeviceGroupApi, DeviceGroupVO } from '@/api/iot/device/group'
import { DeviceLocationApi, DeviceLocationNodeRespVO } from '@/api/iot/device/location'
import download from '@/utils/download'
import DeviceGroupForm from './DeviceGroupForm.vue'
import DeviceImportForm from './DeviceImportForm.vue'
import defaultPicUrl from '@/assets/imgs/iot/device.png'
import defaultIconUrl from '@/assets/svgs/iot/card-fill.svg'

/** IoT 设备列表 */
defineOptions({ name: 'IoTDevice' })

const message = useMessage() // 消息弹窗
const { t } = useI18n() // 国际化
const route = useRoute() // 路由对象

const loading = ref(true) // 列表加载中
const list = ref<DeviceVO[]>([]) // 列表的数据
const total = ref(0) // 列表的总页数
const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  deviceName: undefined,
  serialNumber: undefined,
  productId: undefined as string | number | undefined,
  deviceType: undefined,
  stationId: undefined,
  useStatus: undefined,
  status: undefined,
  groupId: undefined
})
const queryFormRef = ref() // 搜索的表单
const exportLoading = ref(false) // 导出加载状态
const products = ref<ProductVO[]>([]) // 产品列表
const deviceGroups = ref<DeviceGroupVO[]>([]) // 设备分组列表
const locationTree = ref<DeviceLocationNodeRespVO[]>([]) // 设备位置树
const locationNameMap = ref<Record<string, string>>({})
const selectedIds = ref<Array<string | number>>([]) // 选中的设备编号数组
const viewMode = ref<'card' | 'list'>('card') // 视图模式状态
// 卡片视图按 4 列对齐页尺寸，避免出现空白占位
const listPageSizes = [10, 20, 30, 50, 100]
const cardPageSizes = [12, 16, 20, 24, 28]
const pageSizes = computed(() => (viewMode.value === 'card' ? cardPageSizes : listPageSizes))

const syncPageSizeForView = (mode: 'card' | 'list') => {
  const sizes = mode === 'card' ? cardPageSizes : listPageSizes
  const nextSize = sizes.includes(queryParams.pageSize) ? queryParams.pageSize : sizes[0]
  if (nextSize !== queryParams.pageSize) {
    queryParams.pageSize = nextSize
    queryParams.pageNo = 1
    return true
  }
  return false
}

const buildLocationNameMap = (nodes: DeviceLocationNodeRespVO[]) => {
  const map: Record<string, string> = {}
  const loop = (list: DeviceLocationNodeRespVO[]) => {
    list?.forEach((item) => {
      if (item && item.id !== undefined && item.id !== null) {
        map[String(item.id)] = item.name || ''
      }
      if (item?.children?.length) {
        loop(item.children)
      }
    })
  }
  loop(nodes || [])
  return map
}

const resolveLocationName = (value?: string | number) => {
  if (value === undefined || value === null || value === '') return '-'
  const id = String(value)
  return locationNameMap.value[id] || '-'
}

const loadLocationTree = async () => {
  locationTree.value = await DeviceLocationApi.getDeviceLocationTree()
  locationNameMap.value = buildLocationNameMap(locationTree.value)
}

/** 查询列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await DeviceApi.getDevicePage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索按钮操作 */
const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

/** 重置按钮操作 */
const resetQuery = () => {
  queryFormRef.value.resetFields()
  selectedIds.value = [] // 清空选择
  handleQuery()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: string, id?: string | number) => {
  formRef.value.open(type, id)
}

/** 打开详情 */
const { push } = useRouter()
const DETAIL_RETURN_QUERY_KEY = 'returnState'
const DETAIL_RESTORE_QUERY_KEY = 'restoreState'

interface DeviceListReturnState {
  viewMode: 'card' | 'list'
  queryParams: {
    pageNo: number
    pageSize: number
    deviceName?: string
    serialNumber?: string
    productId?: string | number
    deviceType?: string | number
    stationId?: string | number
    useStatus?: string | number
    status?: string | number
    groupId?: string | number
  }
}

const buildDetailReturnState = () => {
  const state: DeviceListReturnState = {
    viewMode: viewMode.value,
    queryParams: {
      pageNo: Number(queryParams.pageNo) || 1,
      pageSize: Number(queryParams.pageSize) || 12,
      deviceName: queryParams.deviceName || undefined,
      serialNumber: queryParams.serialNumber || undefined,
      productId: queryParams.productId || undefined,
      deviceType: queryParams.deviceType || undefined,
      stationId: queryParams.stationId || undefined,
      useStatus: queryParams.useStatus || undefined,
      status: queryParams.status || undefined,
      groupId: queryParams.groupId || undefined
    }
  }
  return encodeURIComponent(JSON.stringify(state))
}

const parseDetailReturnState = (raw: unknown): DeviceListReturnState | null => {
  if (typeof raw !== 'string' || !raw) {
    return null
  }
  try {
    const parsed = JSON.parse(decodeURIComponent(raw))
    if (!parsed || typeof parsed !== 'object') {
      return null
    }
    return parsed as DeviceListReturnState
  } catch {
    return null
  }
}

const applyDetailReturnState = (state: DeviceListReturnState) => {
  viewMode.value = state.viewMode === 'list' ? 'list' : 'card'
  const nextPageNo = Number(state.queryParams.pageNo)
  const nextPageSize = Number(state.queryParams.pageSize)
  queryParams.pageNo = nextPageNo > 0 ? nextPageNo : 1
  queryParams.pageSize = nextPageSize > 0 ? nextPageSize : cardPageSizes[0]
  queryParams.deviceName = state.queryParams.deviceName || undefined
  queryParams.serialNumber = state.queryParams.serialNumber || undefined
  queryParams.productId = state.queryParams.productId || undefined
  queryParams.deviceType = state.queryParams.deviceType || undefined
  queryParams.stationId = state.queryParams.stationId || undefined
  queryParams.useStatus = state.queryParams.useStatus || undefined
  queryParams.status = state.queryParams.status || undefined
  queryParams.groupId = state.queryParams.groupId || undefined
}

const openDetail = (id: string | number) => {
  push({
    name: 'IoTDeviceDetail',
    params: { id },
    query: { [DETAIL_RETURN_QUERY_KEY]: buildDetailReturnState() }
  })
}

/** 跳转到产品详情页面 */
const openProductDetail = (productId: string | number) => {
  push({ name: 'IoTProductDetail', params: { id: productId } })
}

/** 删除按钮操作 */
const handleDelete = async (id: string | number) => {
  try {
    // 删除的二次确认
    await message.delConfirm()
    // 起删除
    await DeviceApi.deleteDevice(id)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 导出方法 */
const handleExport = async () => {
  try {
    // 导出的二次确认
    await message.exportConfirm()
    // 发起导出
    exportLoading.value = true
    const data = await DeviceApi.exportDeviceExcel(queryParams)
    download.excel(data, '物联网设备.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

/** 多选框选中数据 */
const handleSelectionChange = (selection: DeviceVO[]) => {
  selectedIds.value = selection.map((item) => item.id)
}

/** 批量删除按钮操作 */
const handleDeleteList = async () => {
  try {
    await message.delConfirm()
    // 执行批量删除
    await DeviceApi.deleteDeviceList(selectedIds.value)
    message.success(t('common.delSuccess'))
    // 刷新列表
    await getList()
  } catch {}
}

/** 添加到分组操作 */
const groupFormRef = ref()
const openGroupForm = () => {
  groupFormRef.value.open(selectedIds.value)
}

/** 打开物模型数据 */
const openModel = (id: string | number) => {
  push({
    name: 'IoTDeviceDetail',
    params: { id },
    query: {
      tab: 'model',
      [DETAIL_RETURN_QUERY_KEY]: buildDetailReturnState()
    }
  })
}

/** 设备导入 */
const importFormRef = ref()
const handleImport = () => {
  importFormRef.value.open()
}

watch(viewMode, (mode) => {
  if (syncPageSizeForView(mode)) {
    getList()
  }
})

/** 初始化 **/
onMounted(async () => {
  const restoreState = parseDetailReturnState(route.query[DETAIL_RESTORE_QUERY_KEY])
  if (restoreState) {
    applyDetailReturnState(restoreState)
  }

  // 处理路由参数中的 productId
  const productId = route.query.productId
  if (productId && !restoreState) {
    queryParams.productId = String(productId)
  }
  
  syncPageSizeForView(viewMode.value)
  getList()

  // 获取产品列表
  products.value = await ProductApi.getSimpleProductList()
  // 获取分组列表
  deviceGroups.value = await DeviceGroupApi.getSimpleDeviceGroupList()
  // 获取设备位置树
  await loadLocationTree()
})
</script>
