<!-- 设备信息 -->
<template>
  <div>
    <ContentWrap>
      <el-row :gutter="16">
        <!-- 左侧设备信息 -->
        <el-col :span="12">
          <el-card class="h-full">
            <template #header>
              <div class="flex items-center">
                <Icon icon="ep:info-filled" class="mr-2 text-primary" />
                <span>设备信息</span>
              </div>
            </template>
            <el-descriptions :column="2" border class="device-info-desc">
              <el-descriptions-item label="产品名称">{{ product.name }}</el-descriptions-item>
              <el-descriptions-item label="ProductKey">
                {{ product.productKey }}
              </el-descriptions-item>
              <el-descriptions-item label="设备类型">
                <dict-tag :type="DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE" :value="product.deviceType" />
              </el-descriptions-item>
              <el-descriptions-item label="定位类型">
                <dict-tag :type="DICT_TYPE.IOT_LOCATION_TYPE" :value="device.locationType" />
              </el-descriptions-item>
              <el-descriptions-item label="设备名称">
                {{ device.deviceName || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="设备编码">{{ device.serialNumber }}</el-descriptions-item>
              <el-descriptions-item label="设备二维码" :span="2">
                <div v-if="device.qrCode" class="device-qr-box">
                  <Qrcode :text="device.qrCode" :width="120" />
                </div>
                <span v-else>-</span>
              </el-descriptions-item>
              <el-descriptions-item label="设备位置">
                {{ deviceLocationName }}
              </el-descriptions-item>
              <el-descriptions-item label="使用状态">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_USE_STATUS" :value="device.useStatus" />
              </el-descriptions-item>
              <el-descriptions-item label="创建时间">
                {{ formatDate(device.createTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="当前状态">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_STATE" :value="device.state" />
              </el-descriptions-item>
              <el-descriptions-item label="激活时间">
                {{ formatDate(device.activeTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="最后上线时间">
                {{ formatDate(device.onlineTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="最后离线时间">
                {{ formatDate(device.offlineTime) }}
              </el-descriptions-item>
              <el-descriptions-item label="认证信息">
                <el-button type="primary" @click="handleAuthInfoDialogOpen" plain size="small"
                  >查看</el-button
                >
              </el-descriptions-item>
            </el-descriptions>
          </el-card>
        </el-col>

        <!-- 右侧地图 -->
        <el-col :span="12">
          <el-card class="h-full">
            <template #header>
              <div class="flex items-center justify-between">
                <div class="flex items-center">
                  <Icon icon="ep:location" class="mr-2 text-primary" />
                  <span>设备位置</span>
                </div>
                <div class="text-[14px] text-[var(--el-text-color-secondary)]">
                  最后上线时间：
                  {{ device.onlineTime ? formatDate(device.onlineTime) : '--' }}
                </div>
              </div>
            </template>
            <div class="h-[400px] w-full">
              <TiandituGeoJsonPreview
                :geo-json="deviceGeoJson"
                :active="true"
                :height="400"
                :center="deviceMapCenter"
                :zoom="14"
                :label-text="device.deviceName || device.nickname || ''"
                :highlight="true"
                empty-text="暂无位置信息"
              />
            </div>
          </el-card>
        </el-col>
      </el-row>
    </ContentWrap>

    <ContentWrap>
      <el-card>
        <template #header>
          <div class="flex items-center">
            <Icon icon="ep:files" class="mr-2 text-primary" />
            <span>台账信息</span>
          </div>
        </template>
        <el-descriptions :column="3" border class="device-ledger-desc">
          <el-descriptions-item label="设备型号">
            {{ device.equipmentModel || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="生产厂家">
            {{ device.manufacturer || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="安装日期">
            {{ formatDate(device.installDate, 'YYYY-MM-DD') || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="上次养护时间">
            {{ formatDate(device.lastMaintainTime, 'YYYY-MM-DD') || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="养护周期">
            {{ getMaintainCycleLabel(device.maintainCycleDays) }}
          </el-descriptions-item>
          <el-descriptions-item label="负责人姓名">
            {{ device.ownerName || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="负责人联系方式">
            {{ device.ownerPhone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="3">
            {{ device.remark || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="设备图片" :span="3">
            <div v-if="device.picUrl?.length" class="flex flex-wrap gap-2">
              <el-image
                v-for="(url, index) in device.picUrl"
                :key="`${url}-${index}`"
                :src="url"
                :preview-src-list="device.picUrl"
                class="w-20 h-20 rounded"
                fit="cover"
              />
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </ContentWrap>

    <!-- 认证信息弹框 -->
    <Dialog
      title="设备认证信息"
      v-model="authDialogVisible"
      width="640px"
      :before-close="handleAuthInfoDialogClose"
    >
      <el-form :model="authInfo" label-width="120px">
        <el-form-item label="clientId">
          <el-input v-model="authInfo.clientId" readonly>
            <template #append>
              <el-button @click="copyToClipboard(authInfo.clientId)" type="primary">
                <Icon icon="ph:copy" />
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="username">
          <el-input v-model="authInfo.username" readonly>
            <template #append>
              <el-button @click="copyToClipboard(authInfo.username)" type="primary">
                <Icon icon="ph:copy" />
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="password">
          <el-input
            v-model="authInfo.password"
            readonly
            :type="authPasswordVisible ? 'text' : 'password'"
          >
            <template #append>
              <el-button @click="authPasswordVisible = !authPasswordVisible" type="primary">
                <Icon :icon="authPasswordVisible ? 'ph:eye-slash' : 'ph:eye'" />
              </el-button>
              <el-button @click="copyToClipboard(authInfo.password)" type="primary">
                <Icon icon="ph:copy" />
              </el-button>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleAuthInfoDialogClose">关闭</el-button>
      </template>
    </Dialog>
  </div>

  <!-- TODO 待开发：设备标签 -->
</template>
<script setup lang="ts">
import { DICT_TYPE } from '@/utils/dict'
import { ProductVO } from '@/api/iot/product/product'
import { formatDate } from '@/utils/formatTime'
import { DeviceVO } from '@/api/iot/device/device'
import { DeviceApi, IotDeviceAuthInfoVO } from '@/api/iot/device/device'
import { DeviceLocationApi } from '@/api/iot/device/location'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { Qrcode } from '@/components/Qrcode'
import { computed, ref, watch } from 'vue'

const message = useMessage() // 消息提示

const { product, device } = defineProps<{ product: ProductVO; device: DeviceVO }>() // 定义 Props
const emit = defineEmits(['refresh']) // 定义 Emits

const authDialogVisible = ref(false) // 定义设备认证信息弹框的可见性
const authPasswordVisible = ref(false) // 定义密码可见性状态
const authInfo = ref<IotDeviceAuthInfoVO>({} as IotDeviceAuthInfoVO) // 定义设备认证信息对象
const deviceLocationName = ref('-') // 设备位置名称

const DEFAULT_DEVICE_CENTER = [32.272258, 119.184766] as [number, number]
const maintainCycleOptions = [
  { label: '1个月', value: 30 },
  { label: '3个月', value: 90 },
  { label: '6个月', value: 180 },
  { label: '1年', value: 365 },
  { label: '3年', value: 1095 }
]

const getMaintainCycleLabel = (days?: number | null) => {
  if (!days) return '-'
  return maintainCycleOptions.find((item) => item.value === days)?.label || `${days}天`
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

const deviceGeoJson = computed(() => {
  return buildPointGeoJson(device.longitude, device.latitude)
})

const deviceMapCenter = computed<[number, number]>(() => {
  const lon = normalizeNumber(device.longitude)
  const lat = normalizeNumber(device.latitude)
  if (Number.isFinite(lon) && Number.isFinite(lat)) {
    return [lat, lon]
  }
  return DEFAULT_DEVICE_CENTER
})

const loadDeviceLocationName = async () => {
  if (device.address === undefined || device.address === null || device.address === '') {
    deviceLocationName.value = '-'
    return
  }
  const id = String(device.address)
  try {
    const info = await DeviceLocationApi.getDeviceLocation(id)
    deviceLocationName.value = info?.name || '-'
  } catch (error) {
    deviceLocationName.value = '-'
  }
}

watch(
  () => device.address,
  () => {
    loadDeviceLocationName()
  },
  { immediate: true }
)

/** 复制到剪贴板方法 */
const copyToClipboard = async (text: string) => {
  try {
    await navigator.clipboard.writeText(text)
    message.success('复制成功')
  } catch (error) {
    message.error('复制失败')
  }
}

/** 打开设备认证信息弹框的方法 */
const handleAuthInfoDialogOpen = async () => {
  try {
    authInfo.value = await DeviceApi.getDeviceAuthInfo(device.id)
    // 显示设备认证信息弹框
    authDialogVisible.value = true
  } catch (error) {
    console.error('获取设备认证信息出错：', error)
    message.error('获取设备认证信息失败，请检查网络连接或联系管理员')
  }
}

/** 关闭设备认证信息弹框的方法 */
const handleAuthInfoDialogClose = () => {
  authDialogVisible.value = false
}
</script>

<style scoped>
.device-info-desc :deep(.el-descriptions__label),
.device-ledger-desc :deep(.el-descriptions__label) {
  min-width: 180px;
  width: 180px;
  white-space: nowrap;
  word-break: keep-all;
}

.device-qr-box {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
