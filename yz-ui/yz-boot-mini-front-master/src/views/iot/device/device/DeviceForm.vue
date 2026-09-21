<template>
  <Dialog :title="dialogTitle" v-model="dialogVisible" width="1100px" class="device-dialog">
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="120px"
      v-loading="formLoading"
      class="device-form"
    >
      <el-tabs v-model="activeTab" class="device-tabs" type="border-card" stretch>
        <el-tab-pane label="基础信息" name="basic">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="产品" prop="productId">
                <el-select
                  v-model="formData.productId"
                  placeholder="请选择产品"
                  :disabled="formType === 'update'"
                  clearable
                  class="!w-full"
                  @change="handleProductChange"
                >
                  <el-option
                    v-for="product in products"
                    :key="product.id"
                    :label="product.name"
                    :value="product.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="产品分类">
                <el-input :model-value="productCategoryName" disabled class="!w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="24" v-if="isMqttSourceProduct">
              <el-alert
                title="当前产品协议为 MQTT 订阅采集"
                type="info"
                show-icon
                :closable="false"
                description="设备保存后，可在设备详情页的 MQTT采集 页签维护数据源、订阅主题和属性映射。"
              />
              <el-descriptions
                v-if="formType === 'update'"
                class="mqtt-summary"
                :column="3"
                border
                size="small"
              >
                <el-descriptions-item label="MQTT数据源">
                  {{ mqttConfigSummary?.sourceId || '未配置' }}
                </el-descriptions-item>
                <el-descriptions-item label="订阅主题">
                  {{ mqttConfigSummary?.topic || '未配置' }}
                </el-descriptions-item>
                <el-descriptions-item label="负载模式">
                  {{ mqttConfigSummary?.payloadMode === 'flat_json' ? '扁平 JSON' : mqttConfigSummary?.payloadMode || '未配置' }}
                </el-descriptions-item>
              </el-descriptions>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备名称" prop="deviceName">
                <el-input
                  v-model="formData.deviceName"
                  placeholder="请输入设备名称"
                  :disabled="formType === 'update'"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备编码" prop="serialNumber">
                <el-input
                  v-model="formData.serialNumber"
                  placeholder="请输入设备编码"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="formType === 'update'">
              <el-form-item label="设备二维码">
                <div v-if="formData.qrCode" class="device-qr-box">
                  <Qrcode :text="formData.qrCode" :width="120" />
                </div>
                <span v-else class="device-qr-empty">暂无设备二维码</span>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="所属站点" prop="stationId">
                <el-select
                  v-model="formData.stationId"
                  placeholder="请选择所属站点"
                  clearable
                  class="!w-full"
                >
                  <el-option
                    v-for="dict in getStrDictOptions(DICT_TYPE.IOT_ZD_SBZD)"
                    :key="String(dict.value)"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备分类" prop="deviceType">
                <el-select
                  v-model="formData.deviceType"
                  placeholder="请选择设备分类"
                  clearable
                  class="!w-full"
                >
                  <el-option
                    v-for="dict in getIntDictOptions(DICT_TYPE.IOT_DEVICE_TYPE)"
                    :key="dict.value"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="使用状态" prop="useStatus">
                <el-select
                  v-model="formData.useStatus"
                  placeholder="请选择使用状态"
                  clearable
                  class="!w-full"
                >
                  <el-option
                    v-for="dict in getStrDictOptions(DICT_TYPE.IOT_DEVICE_USE_STATUS)"
                    :key="String(dict.value)"
                    :label="dict.label"
                    :value="dict.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12" v-if="productDeviceType === DeviceTypeEnum.GATEWAY_SUB">
              <el-form-item label="网关设备" prop="gatewayId">
                <el-select
                  v-model="formData.gatewayId"
                  placeholder="子设备可选择父设备"
                  clearable
                  class="!w-full"
                >
                  <el-option
                    v-for="gateway in gatewayDevices"
                    :key="gateway.id"
                    :label="gateway.deviceName || gateway.nickname"
                    :value="gateway.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="台账信息" name="ledger">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="设备型号" prop="equipmentModel">
                <el-input v-model="formData.equipmentModel" placeholder="请输入设备型号" class="!w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="生产厂家" prop="manufacturer">
                <el-input v-model="formData.manufacturer" placeholder="请输入生产厂家" class="!w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="安装日期" prop="installDate">
                <el-date-picker
                  v-model="formData.installDate"
                  type="datetime"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :default-time="DEFAULT_TIME"
                  :show-confirm="true"
                  :show-now="false"
                  popper-class="date-only-picker"
                  placeholder="请选择安装日期"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="上次维护时间" prop="lastMaintainTime">
                <el-date-picker
                  v-model="formData.lastMaintainTime"
                  type="datetime"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  :default-time="DEFAULT_TIME"
                  :show-confirm="true"
                  :show-now="false"
                  popper-class="date-only-picker"
                  placeholder="请选择上次维护时间"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="维护周期" prop="maintainCycleDays">
                <el-select
                  v-model="formData.maintainCycleDays"
                  placeholder="请选择维护周期"
                  clearable
                  class="!w-full"
                >
                  <el-option
                    v-for="option in maintainCycleOptions"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责人姓名" prop="ownerName">
                <el-input v-model="formData.ownerName" placeholder="请输入负责人姓名" class="!w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="负责人联系方式" prop="ownerPhone">
                <el-input v-model="formData.ownerPhone" placeholder="请输入负责人联系方式" class="!w-full" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备图片" prop="picUrl">
                <UploadImgs v-model="formData.picUrl" :limit="6" :drag="false" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="备注" prop="remark">
                <el-input
                  v-model="formData.remark"
                  type="textarea"
                  rows="3"
                  placeholder="请输入备注"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="位置定位" name="location" lazy>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="设备位置" prop="locationId">
                <el-tree-select
                  v-model="formData.locationId"
                  :data="locationTreeOptions"
                  :props="defaultProps"
                  check-strictly
                  default-expand-all
                  placeholder="请选择设备位置"
                  value-key="id"
                  class="!w-full"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备经度" prop="longitude" type="number">
                <el-input-number
                  v-model="formData.longitude"
                  :precision="6"
                  :step="0.000001"
                  controls-position="right"
                  class="!w-full"
                  placeholder="请输入设备经度"
                  @blur="updateLocationFromCoordinates"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="设备纬度" prop="latitude" type="number">
                <el-input-number
                  v-model="formData.latitude"
                  :precision="6"
                  :step="0.000001"
                  controls-position="right"
                  class="!w-full"
                  placeholder="请输入设备纬度"
                  @blur="updateLocationFromCoordinates"
                />
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <TiandituGeoJsonEditor
                v-if="showMap && activeTab === 'location'"
                v-model="deviceGeoJson"
                :active="dialogVisible"
                :height="360"
                :center="deviceMapCenter"
                :zoom="14"
                :draw-modes="['POINT']"
              />
              <div class="map-tip" v-if="showMap && activeTab === 'location'">
                仅支持绘制点位；可直接输入经纬度，或在地图上绘制点位自动回填。
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="更多配置" name="advanced">
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="设备分组" prop="groupIds">
                <el-select
                  v-model="formData.groupIds"
                  placeholder="请选择设备分组"
                  multiple
                  clearable
                  class="!w-full"
                >
                  <el-option
                    v-for="group in deviceGroups"
                    :key="group.id"
                    :label="group.name"
                    :value="group.id"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="24">
              <el-form-item label="设备参数" prop="config">
                <el-table :data="configItems" border size="small" class="config-table">
                  <el-table-column label="键" min-width="200">
                    <template #default="scope">
                      <el-input v-model="scope.row.key" placeholder="请输入键" />
                    </template>
                  </el-table-column>
                  <el-table-column label="值" min-width="220">
                    <template #default="scope">
                      <el-input v-model="scope.row.value" placeholder="请输入值" />
                    </template>
                  </el-table-column>
                  <el-table-column label="操作" width="100" align="center">
                    <template #default="scope">
                      <el-button link type="danger" @click="removeConfigItem(scope.$index)">
                        删除
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
                <div class="items-action">
                  <el-button type="primary" plain @click="addConfigItem">+ 添加参数</el-button>
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </el-tab-pane>

        <el-tab-pane label="关联记录" name="records">
          <div v-if="formType === 'update' && formData.id" class="device-records-section">
            <DeviceRelatedRecords
              :device-id="formData.id"
              :device-type="formData.deviceType"
              :last-maintain-time="formData.lastMaintainTime"
              :maintain-cycle-days="formData.maintainCycleDays"
            />
          </div>
          <el-empty v-else description="新增设备后可查看关联记录" />
        </el-tab-pane>
      </el-tabs>
    </el-form>
    <template #footer>
      <div class="device-form-footer">
        <el-button @click="submitForm" type="primary" :disabled="formLoading">确认</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
      </div>
    </template>
  </Dialog>
</template>
<script setup lang="ts">
import { DeviceApi, DeviceVO } from '@/api/iot/device/device'
import { DeviceGroupApi } from '@/api/iot/device/group'
import { DeviceLocationApi, DeviceLocationNodeRespVO } from '@/api/iot/device/location'
import { DeviceTypeEnum, ProductApi, ProductVO, ProtocolTypeEnum } from '@/api/iot/product/product'
import { DeviceMqttConfigApi, DeviceMqttConfigVO } from '@/api/iot/device/mqtt/config'
import { UploadImgs } from '@/components/UploadFile'
import { Qrcode } from '@/components/Qrcode'
import { DICT_TYPE, getIntDictOptions, getStrDictOptions } from '@/utils/dict'
import { defaultProps } from '@/utils/tree'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import DeviceRelatedRecords from './components/DeviceRelatedRecords.vue'
import dayjs from 'dayjs'
import { computed, reactive, ref, watch } from 'vue'

/** IoT 设备表单 */
defineOptions({ name: 'IoTDeviceForm' })

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const dialogVisible = ref(false) // 弹窗是否显示
const dialogTitle = ref('') // 弹窗标题
const formLoading = ref(false) // 表单加载中（修改时数据加载与提交时禁用）
const formType = ref('') // 表单类型：create - 新增，update - 修改
const showMap = ref(false) // 是否显示地图组件
const deviceGeoJson = ref('')
const activeTab = ref('basic')
const DEFAULT_DEVICE_CENTER = [32.272258, 119.184766] as [number, number]
const DEFAULT_TIME = new Date(2000, 0, 1, 0, 0, 0) // 日期默认时间，保证精确到天
const maintainCycleOptions = [
  { label: '1个月', value: 30 },
  { label: '3个月', value: 90 },
  { label: '6个月', value: 180 },
  { label: '1年', value: 365 },
  { label: '3年', value: 1095 }
]

const configItems = ref<Array<{ key: string; value: string }>>([])

const parseConfigItems = (config?: string) => {
  if (!config) return []
  try {
    const parsed = JSON.parse(config)
    if (Array.isArray(parsed)) {
      return parsed
        .filter((item) => item && typeof item.key === 'string')
        .map((item) => ({ key: item.key, value: item.value ?? '' }))
    }
    if (parsed && typeof parsed === 'object') {
      return Object.entries(parsed).map(([key, value]) => ({ key, value: String(value) }))
    }
    return [{ key: 'value', value: String(parsed) }]
  } catch {
    return [{ key: 'value', value: config }]
  }
}

const buildConfigFromItems = () => {
  const result: Record<string, string> = {}
  configItems.value.forEach((item) => {
    if (!item.key) return
    result[item.key] = item.value ?? ''
  })
  return Object.keys(result).length ? JSON.stringify(result) : undefined
}

const addConfigItem = () => {
  configItems.value.push({ key: '', value: '' })
}

const removeConfigItem = (index: number) => {
  configItems.value.splice(index, 1)
}

const resolveLocationIdType = (nodes: DeviceLocationNodeRespVO[]) => {
  const queue = [...(nodes || [])]
  while (queue.length) {
    const node = queue.shift()
    if (node && node.id !== undefined && node.id !== null) {
      return typeof node.id
    }
    if (node?.children?.length) {
      queue.push(...node.children)
    }
  }
  return undefined
}

const syncLocationIdType = () => {
  const current = formData.value.locationId
  if (current === undefined || current === null || current === '') return
  const idType = resolveLocationIdType(locationTree.value)
  if (!idType) return
  if (idType === 'string') {
    formData.value.locationId = String(current)
    return
  }
  if (idType === 'number') {
    const num = Number(current)
    formData.value.locationId = Number.isFinite(num) ? num : undefined
  }
}

const loadLocationTree = async () => {
  locationTree.value = await DeviceLocationApi.getDeviceLocationTree()
  syncLocationIdType()
}

const normalizeDateToString = (value?: string | number | Date | number[]) => {
  if (value === null || value === undefined || value === '') return undefined
  if (Array.isArray(value)) {
    const [year, month, day] = value
    if (!year || !month || !day) return undefined
    return dayjs(new Date(year, month - 1, day)).format('YYYY-MM-DD')
  }
  if (value instanceof Date) return dayjs(value).format('YYYY-MM-DD')
  if (typeof value === 'number') return dayjs(value).format('YYYY-MM-DD')
  if (typeof value === 'string') return dayjs(value).format('YYYY-MM-DD')
  return undefined
}

const normalizeDateToMillis = (value?: string | number | Date) => {
  if (value === null || value === undefined || value === '') return undefined
  if (typeof value === 'number') return Number.isFinite(value) ? value : undefined
  const parsed = dayjs(value, 'YYYY-MM-DD')
  return parsed.isValid() ? parsed.startOf('day').valueOf() : undefined
}

const normalizeNumber = (val: unknown) => {
  if (val === null || val === undefined) return NaN
  if (typeof val === 'number') return val
  if (typeof val === 'string') return Number(val)
  return NaN
}

const buildPointGeoJson = (longitude?: unknown, latitude?: unknown) => {
  const lon = normalizeNumber(longitude)
  const lat = normalizeNumber(latitude)
  if (!Number.isFinite(lon) || !Number.isFinite(lat)) return ''
  return JSON.stringify({ type: 'Point', coordinates: [lon, lat] })
}

const parsePointGeoJson = (geoJson?: string) => {
  if (!geoJson) return undefined
  let parsed: any
  try {
    parsed = JSON.parse(geoJson)
  } catch {
    return undefined
  }
  if (!parsed || parsed.type !== 'Point' || !Array.isArray(parsed.coordinates) || parsed.coordinates.length < 2) {
    return undefined
  }
  const lon = normalizeNumber(parsed.coordinates[0])
  const lat = normalizeNumber(parsed.coordinates[1])
  if (!Number.isFinite(lon) || !Number.isFinite(lat)) return undefined
  return { lon, lat }
}

const deviceMapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(formData.value.longitude)
  const lat = normalizeNumber(formData.value.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_DEVICE_CENTER
})

const productDeviceType = computed(() => {
  return selectedProduct.value?.deviceType
})

const productCategoryName = computed(() => {
  return selectedProduct.value?.categoryName || '-'
})

const selectedProduct = computed(() => {
  if (!formData.value.productId) return undefined
  return products.value?.find((item) => item.id === formData.value.productId)
})

const isMqttSourceProduct = computed(() => {
  return selectedProduct.value?.protocolType === ProtocolTypeEnum.MQTT_SOURCE
})

const formData = ref({
  id: undefined,
  productId: undefined,
  deviceName: undefined,
  stationId: undefined,
  nickname: undefined,
  serialNumber: undefined,
  qrCode: undefined,
  useStatus: undefined,
  equipmentModel: undefined,
  manufacturer: undefined,
  installDate: undefined,
  lastMaintainTime: undefined,
  maintainCycleDays: undefined,
  ownerUserId: undefined,
  ownerName: undefined,
  ownerPhone: undefined,
  deptId: undefined,
  remark: undefined,
  picUrl: [] as string[],
  gatewayId: undefined,
  deviceType: undefined as number | undefined,
  locationType: undefined as number | undefined,
  longitude: undefined,
  latitude: undefined,
  location: '',
  locationId: undefined as string | number | undefined,
  address: undefined,
  config: undefined,
  groupIds: [] as number[]
})

let syncingFromGeoJson = false
// 监听经纬度变化，更新 location 与地图数据
watch([() => formData.value.longitude, () => formData.value.latitude], ([newLong, newLat]) => {
  const lon = normalizeNumber(newLong)
  const lat = normalizeNumber(newLat)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    formData.value.location = `${lon},${lat}`
  } else {
    formData.value.location = ''
  }
  showMap.value = true
  if (syncingFromGeoJson) return
  const nextGeoJson = buildPointGeoJson(lon, lat)
  if (nextGeoJson === deviceGeoJson.value) return
  deviceGeoJson.value = nextGeoJson
})

watch(
  () => deviceGeoJson.value,
  (val) => {
    const point = parsePointGeoJson(val)
    syncingFromGeoJson = true
    if (point) {
      formData.value.longitude = point.lon
      formData.value.latitude = point.lat
      formData.value.location = `${point.lon},${point.lat}`
    } else {
      formData.value.longitude = undefined
      formData.value.latitude = undefined
      formData.value.location = ''
    }
    syncingFromGeoJson = false
  }
)

const formRules = reactive({
  productId: [{ required: true, message: '产品不能为空', trigger: 'change' }],
  deviceName: [{ required: true, message: '设备名称不能为空', trigger: 'blur' }],
  stationId: [{ required: true, message: '所属站点不能为空', trigger: 'change' }],
  deviceType: [{ required: true, message: '设备分类不能为空', trigger: 'change' }],
  useStatus: [{ required: true, message: '使用状态不能为空', trigger: 'change' }]
})
const formRef = ref()
const products = ref<ProductVO[]>([])
const gatewayDevices = ref<DeviceVO[]>([])
const deviceGroups = ref<any[]>([])
const locationTree = ref<DeviceLocationNodeRespVO[]>([])
const locationTreeOptions = computed(() => locationTree.value || [])
const mqttConfigSummary = ref<DeviceMqttConfigVO | undefined>()

const fieldTabMap: Record<string, string> = {
  productId: 'basic',
  deviceName: 'basic',
  serialNumber: 'basic',
  stationId: 'basic',
  deviceType: 'basic',
  useStatus: 'basic',
  installDate: 'ledger',
  longitude: 'location',
  latitude: 'location',
  locationId: 'location'
}

/** 打开弹窗 */
const open = async (type: string, id?: string | number) => {
  dialogVisible.value = true
  dialogTitle.value = t('action.' + type)
  formType.value = type
  resetForm()

  activeTab.value = 'basic'
  showMap.value = false

  if (id) {
    formLoading.value = true
    try {
      formData.value = await DeviceApi.getDevice(id)
      formData.value.picUrl = formData.value.picUrl || []
      formData.value.groupIds = formData.value.groupIds || []
      formData.value.installDate = normalizeDateToString(formData.value.installDate)
      formData.value.lastMaintainTime = normalizeDateToString(formData.value.lastMaintainTime)
      configItems.value = parseConfigItems(formData.value.config)
      deviceGeoJson.value = buildPointGeoJson(formData.value.longitude, formData.value.latitude)
      const addressValue = formData.value.address
      formData.value.locationId =
        addressValue !== undefined && addressValue !== null && addressValue !== ''
          ? String(addressValue)
          : undefined
      syncLocationIdType()

      if (formData.value.longitude && formData.value.latitude) {
        formData.value.location = `${formData.value.longitude},${formData.value.latitude}`
      }
    } finally {
      formLoading.value = false
    }
  }
  showMap.value = true

  gatewayDevices.value = await DeviceApi.getSimpleDeviceList(DeviceTypeEnum.GATEWAY)
  products.value = await ProductApi.getSimpleProductList()
  await loadMqttConfigSummary()
  deviceGroups.value = await DeviceGroupApi.getSimpleDeviceGroupList()
  await loadLocationTree()
}
defineExpose({ open })

/** 提交表单 */
const emit = defineEmits(['success'])
const submitForm = async () => {
  try {
    await formRef.value.validate()
  } catch (error: any) {
    const fields = error || {}
    const firstField = Object.keys(fields)[0]
    if (firstField && fieldTabMap[firstField]) {
      activeTab.value = fieldTabMap[firstField]
    }
    return
  }

  formLoading.value = true
  try {
    const data = { ...formData.value } as unknown as DeviceVO
    if (!data.nickname) {
      data.nickname = data.deviceName || data.serialNumber
    }
    data.installDate = normalizeDateToString(formData.value.installDate)
    data.lastMaintainTime = normalizeDateToMillis(formData.value.lastMaintainTime)
    data.config = buildConfigFromItems()
    data.address = formData.value.locationId ? String(formData.value.locationId) : undefined
    delete (data as any).qrCode
    delete (data as any).locationId

    if (formType.value === 'create') {
      await DeviceApi.createDevice(data)
      message.success(t('common.createSuccess'))
    } else {
      await DeviceApi.updateDevice(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}

/** 重置表单 */
const resetForm = () => {
  formData.value = {
    id: undefined,
    productId: undefined,
    deviceName: undefined,
    stationId: undefined,
    nickname: undefined,
    serialNumber: undefined,
    qrCode: undefined,
    useStatus: undefined,
    equipmentModel: undefined,
    manufacturer: undefined,
    installDate: undefined,
    lastMaintainTime: undefined,
    maintainCycleDays: undefined,
    ownerUserId: undefined,
    ownerName: undefined,
    ownerPhone: undefined,
    deptId: undefined,
    remark: undefined,
    picUrl: [],
    gatewayId: undefined,
    deviceType: undefined,
    locationType: undefined,
    longitude: undefined,
    latitude: undefined,
    location: '',
    locationId: undefined,
    address: undefined,
    config: undefined,
    groupIds: []
  }
  activeTab.value = 'basic'
  deviceGeoJson.value = ''
  configItems.value = []
  formRef.value?.resetFields()
  showMap.value = false
}

/** 产品选择变化 */
const handleProductChange = (productId: number) => {
  mqttConfigSummary.value = undefined
  if (!productId) {
    return
  }
  const product = products.value?.find((item) => item.id === productId)
  formData.value.locationType = product?.locationType
}

const loadMqttConfigSummary = async () => {
  mqttConfigSummary.value = undefined
  if (formType.value !== 'update' || !formData.value.id || !isMqttSourceProduct.value) {
    return
  }
  mqttConfigSummary.value =
    (await DeviceMqttConfigApi.getMqttConfig(Number(formData.value.id))) || undefined
}

/** 根据经纬度更新地图位置 */
const updateLocationFromCoordinates = () => {
  const lon = normalizeNumber(formData.value.longitude)
  const lat = normalizeNumber(formData.value.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    formData.value.location = `${lon},${lat}`
    deviceGeoJson.value = buildPointGeoJson(lon, lat)
  }
}
</script>

<style scoped>
.device-dialog :deep(.el-dialog__body) {
  padding: 12px 20px 8px;
}

.device-form :deep(.el-form-item__label) {
  min-width: 120px;
  width: 120px;
  white-space: nowrap;
  word-break: keep-all;
  font-weight: 500;
  color: var(--el-text-color-regular);
}

.device-form :deep(.el-form-item) {
  margin-bottom: 14px;
}

.device-form :deep(.el-input__wrapper),
.device-form :deep(.el-select__wrapper),
.device-form :deep(.el-textarea__inner),
.device-form :deep(.el-input-number .el-input__wrapper),
.device-form :deep(.el-tree-select .el-input__wrapper) {
  border-radius: 4px;
}

.device-form :deep(.date-only-picker .el-date-picker__time-header),
.device-form :deep(.date-only-picker .el-date-picker__time) {
  display: none;
}

.device-tabs :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.device-tabs :deep(.el-tabs__item) {
  font-weight: 600;
}

.device-tabs :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary);
}

.device-tabs :deep(.el-tabs__content) {
  padding: 14px 10px 6px;
}

.mqtt-summary {
  margin-top: 12px;
}

.device-form-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.device-form-footer :deep(.el-button) {
  min-width: 88px;
}

.map-tip {
  margin-top: 8px;
  font-size: 13px;
  line-height: 1.5;
  color: #606266;
}

.config-table {
  margin-bottom: 10px;
}

.config-table :deep(.el-table__header-wrapper th) {
  background: var(--el-fill-color-lighter);
  color: var(--el-text-color-primary);
}

.items-action {
  display: flex;
  justify-content: flex-start;
}

.device-records-section {
  margin-top: 8px;
}

.device-qr-box {
  display: flex;
  align-items: center;
  gap: 12px;
}

.device-qr-empty {
  color: var(--el-text-color-secondary);
}

@media (max-width: 768px) {
  .device-dialog :deep(.el-dialog__body) {
    padding: 10px 12px 6px;
  }

  .device-form :deep(.el-form-item__label) {
    min-width: 102px;
    width: 102px;
  }

  .device-tabs :deep(.el-tabs__header) {
    margin-bottom: 10px;
  }

  .device-tabs :deep(.el-tabs__content) {
    padding: 10px 6px 4px;
  }

  .device-form-footer {
    gap: 8px;
  }
}
</style>
