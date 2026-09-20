<template>
  <ContentWrap class="mb-10px">
    <el-button @click="handleBackToList">
      <Icon icon="ep:back" class="mr-5px" />
      返回
    </el-button>
  </ContentWrap>
  <DeviceDetailsHeader
    :loading="loading"
    :product="product"
    :device="device"
    @refresh="getDeviceData"
  />
  <el-col class="device-detail-page">
    <el-tabs v-model="activeTab" class="device-detail-tabs">
      <el-tab-pane label="设备信息" name="info">
        <DeviceDetailsInfo v-if="activeTab === 'info'" :product="product" :device="device" />
      </el-tab-pane>
      <el-tab-pane label="物模型数据" name="model">
        <DeviceDetailsThingModel
          v-if="activeTab === 'model'"
          :device-id="device.id"
          :thing-model-list="thingModelList"
        />
      </el-tab-pane>
      <el-tab-pane label="子设备管理" v-if="product.deviceType === DeviceTypeEnum.GATEWAY" />
      <el-tab-pane label="设备消息" name="log">
        <DeviceDetailsMessage v-if="activeTab === 'log'" :device-id="device.id" />
      </el-tab-pane>
      <el-tab-pane label="模拟设备" name="simulator">
        <DeviceDetailsSimulator
          v-if="activeTab === 'simulator'"
          :product="product"
          :device="device"
          :thing-model-list="thingModelList"
        />
      </el-tab-pane>
      <el-tab-pane label="设备配置" name="config">
        <DeviceDetailConfig v-if="activeTab === 'config'" :device="device" @success="getDeviceData" />
      </el-tab-pane>
      <el-tab-pane
        v-if="product.protocolType === ProtocolTypeEnum.GENESIS64_HTTP"
        label="GENESIS64采集"
        name="genesis"
      >
        <DeviceGenesisConfig
          v-if="activeTab === 'genesis'"
          :device="device"
          :thing-model-list="thingModelList"
        />
      </el-tab-pane>
      <el-tab-pane
        v-if="product.protocolType === ProtocolTypeEnum.MQTT_SOURCE"
        label="MQTT采集"
        name="mqttSource"
      >
        <DeviceMqttConfig
          v-if="activeTab === 'mqttSource'"
          :device="device"
          :thing-model-list="thingModelList"
        />
      </el-tab-pane>
      <el-tab-pane label="故障记录" name="fault">
        <DeviceRelatedRecords
          v-if="activeTab === 'fault'"
          :device-id="device.id"
          :device-type="device.deviceType"
          mode="fault"
        />
      </el-tab-pane>
      <el-tab-pane label="养护记录" name="maintenance">
        <DeviceRelatedRecords
          v-if="activeTab === 'maintenance'"
          :device-id="device.id"
          :device-type="device.deviceType"
          :last-maintain-time="device.lastMaintainTime"
          :maintain-cycle-days="device.maintainCycleDays"
          mode="maintenance"
        />
      </el-tab-pane>
      <el-tab-pane label="技术文档" name="doc">
        <DeviceRelatedRecords
          v-if="activeTab === 'doc'"
          :device-id="device.id"
          :device-type="device.deviceType"
          mode="doc"
        />
      </el-tab-pane>
      <el-tab-pane label="库存备件" name="spare">
        <DeviceRelatedRecords
          v-if="activeTab === 'spare'"
          :device-id="device.id"
          :device-type="device.deviceType"
          mode="spare"
        />
      </el-tab-pane>
      <el-tab-pane label="设备评级" name="rating">
        <DeviceDetailsRating v-if="activeTab === 'rating'" :device-id="device.id" />
      </el-tab-pane>
      <el-tab-pane label="事故登记" name="accident">
        <DeviceDetailsAccident
          v-if="activeTab === 'accident'"
          :device-id="device.id"
          :device-address="device.address"
        />
      </el-tab-pane>
    </el-tabs>
  </el-col>
</template>
<script lang="ts" setup>
import { useTagsViewStore } from '@/store/modules/tagsView'
import { DeviceApi, DeviceVO } from '@/api/iot/device/device'
import { DeviceTypeEnum, ProductApi, ProductVO, ProtocolTypeEnum } from '@/api/iot/product/product'
import { ThingModelApi, ThingModelData } from '@/api/iot/thingmodel'
import DeviceDetailsHeader from './DeviceDetailsHeader.vue'
import DeviceDetailsInfo from './DeviceDetailsInfo.vue'
import DeviceDetailsThingModel from './DeviceDetailsThingModel.vue'
import DeviceDetailsMessage from './DeviceDetailsMessage.vue'
import DeviceDetailsSimulator from './DeviceDetailsSimulator.vue'
import DeviceDetailConfig from './DeviceDetailConfig.vue'
import DeviceDetailsRating from './DeviceDetailsRating.vue'
import DeviceDetailsAccident from './DeviceDetailsAccident.vue'
import DeviceGenesisConfig from './DeviceGenesisConfig.vue'
import DeviceMqttConfig from './DeviceMqttConfig.vue'
import DeviceRelatedRecords from '../components/DeviceRelatedRecords.vue'

defineOptions({ name: 'IoTDeviceDetail' })

const route = useRoute()
const message = useMessage()
const DETAIL_RETURN_QUERY_KEY = 'returnState'
const DETAIL_RESTORE_QUERY_KEY = 'restoreState'
const id = route.params.id as string | number | undefined
const loading = ref(true)
const product = ref<ProductVO>({} as ProductVO)
const device = ref<DeviceVO>({} as DeviceVO)
const activeTab = ref('info')
const thingModelList = ref<ThingModelData[]>([])

const getDeviceData = async () => {
  loading.value = true
  try {
    if (!id) {
      return
    }
    device.value = await DeviceApi.getDevice(id)
    await getProductData(device.value.productId)
    await getThingModelList(device.value.productId)
  } finally {
    loading.value = false
  }
}

const getProductData = async (id: string | number) => {
  product.value = await ProductApi.getProduct(id)
}

const getThingModelList = async (productId: string | number) => {
  try {
    const data = await ThingModelApi.getThingModelList({
      productId: productId
    })
    thingModelList.value = data || []
  } catch (error) {
    console.error('获取物模型列表失败', error)
    thingModelList.value = []
  }
}

const { delView } = useTagsViewStore()
const router = useRouter()
const { currentRoute } = router

const handleBackToList = () => {
  const returnState = route.query[DETAIL_RETURN_QUERY_KEY]
  if (typeof returnState === 'string' && returnState) {
    router.push({
      path: '/iot/device/device',
      query: { [DETAIL_RESTORE_QUERY_KEY]: returnState }
    })
    return
  }
  router.push({ path: '/iot/device/device' })
}

onMounted(async () => {
  if (!id) {
    message.warning('参数错误，产品不能为空！')
    delView(unref(currentRoute))
    return
  }
  await getDeviceData()
  activeTab.value = (route.query.tab as string) || 'info'
})
</script>

<style scoped>
.device-detail-page {
  padding: 0 4px;
}

.device-detail-tabs {
  --device-tab-header-bg: linear-gradient(180deg, #f8fbff 0%, #f1f5fa 100%);
  --device-tab-header-border: #dbe4ef;
  --device-tab-item-bg: #ffffff;
  --device-tab-item-border: #cfd9e6;
  --device-tab-item-text: #3d4f63;
  --device-tab-active-bg: #e8f1fb;
  --device-tab-active-border: #1f4f8c;
  --device-tab-active-text: #1f4f8c;
}

.device-detail-tabs > :deep(.el-tabs__header) {
  margin: 0 0 14px;
  padding: 8px;
  border: 1px solid var(--device-tab-header-border);
  border-radius: 10px;
  background: var(--device-tab-header-bg);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav-wrap) {
  overflow: visible;
  scrollbar-width: thin;
  scrollbar-color: #b5c7db transparent;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav-wrap::-webkit-scrollbar) {
  height: 6px;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav-wrap::-webkit-scrollbar-thumb) {
  background: #b5c7db;
  border-radius: 999px;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav-wrap::after) {
  display: none;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav-scroll) {
  overflow: visible;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav) {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(132px, 1fr));
  gap: 12px;
  width: 100%;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__item) {
  min-height: 44px;
  height: auto;
  line-height: 1.35;
  padding: 10px 14px;
  border-radius: 8px;
  border: 1px solid var(--device-tab-item-border);
  background: var(--device-tab-item-bg);
  color: var(--device-tab-item-text);
  margin-right: 0 !important;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: grid;
  place-items: center;
  text-align: center;
  white-space: normal;
  word-break: break-word;
  box-shadow: 0 1px 2px rgba(31, 79, 140, 0.06);
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__item > span) {
  display: -webkit-box;
  overflow: hidden;
  text-overflow: ellipsis;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__item.is-active) {
  color: var(--device-tab-active-text);
  border-color: var(--device-tab-active-border);
  background: var(--device-tab-active-bg);
  box-shadow: 0 1px 4px rgba(31, 79, 140, 0.12);
  font-weight: 600;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__item:not(.is-active):hover) {
  color: var(--device-tab-active-border);
  border-color: #8aa9ca;
  background: #f2f7fd;
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__item.is-active:hover),
.device-detail-tabs > :deep(.el-tabs__header .el-tabs__item.is-active:focus) {
  color: var(--device-tab-active-text);
  border-color: var(--device-tab-active-border);
  background: var(--device-tab-active-bg);
}

.device-detail-tabs > :deep(.el-tabs__header .el-tabs__active-bar) {
  display: none;
}

.device-detail-tabs > :deep(.el-tabs__content) {
  padding: 16px 16px 18px;
  border: 1px solid var(--device-tab-header-border);
  border-radius: 10px;
  background: #fff;
  box-shadow: 0 4px 12px rgba(15, 52, 86, 0.05);
}

@media (max-width: 768px) {
  .device-detail-tabs > :deep(.el-tabs__header) {
    padding: 6px;
    border-radius: 8px;
  }

  .device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav-wrap) {
    overflow-x: auto;
    overflow-y: hidden;
  }

  .device-detail-tabs > :deep(.el-tabs__header .el-tabs__nav) {
    display: flex;
    flex-wrap: nowrap;
    gap: 8px;
    min-width: max-content;
    width: auto;
  }

  .device-detail-tabs > :deep(.el-tabs__header .el-tabs__item) {
    min-height: 34px;
    height: 34px;
    line-height: 34px;
    padding: 0 12px;
    font-size: 13px;
    white-space: nowrap;
    word-break: normal;
  }

  .device-detail-tabs > :deep(.el-tabs__content) {
    padding: 12px;
    border-radius: 8px;
  }
}
</style>
