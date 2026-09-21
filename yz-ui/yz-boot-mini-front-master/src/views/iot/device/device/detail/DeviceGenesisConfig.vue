<template>
  <div>
    <ContentWrap>
      <div class="flex items-center justify-between mb-4">
        <span class="text-lg font-medium">连接配置</span>
        <el-button type="primary" @click="handleEditConfig" v-hasPermi="['iot:device:update']">
          编辑
        </el-button>
      </div>

      <el-descriptions :column="3" border direction="horizontal">
        <el-descriptions-item label="接口地址">
          {{ genesisConfig.baseUrl || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="用户名">
          {{ genesisConfig.username || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="密码">
          {{ formatPassword(genesisConfig.password) }}
        </el-descriptions-item>
        <el-descriptions-item label="请求超时">
          {{ genesisConfig.timeout ? `${genesisConfig.timeout} ms` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="采集频率">
          {{ genesisConfig.collectInterval ? `${genesisConfig.collectInterval} ms` : '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="genesisConfig.status" />
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="3">
          {{ genesisConfig.remark || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </ContentWrap>

    <ContentWrap class="mt-4">
      <div class="flex items-center justify-between mb-4">
        <span class="text-lg font-medium">点位配置</span>
        <el-button type="primary" @click="handleAddPoint" v-hasPermi="['iot:device:update']">
          <Icon icon="ep:plus" class="mr-1" />
          新增点位
        </el-button>
      </div>

      <el-form :model="queryParams" :inline="true" class="-mb-15px">
        <el-form-item label="属性名称" prop="name">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入属性名称"
            clearable
            class="!w-200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="标识符" prop="identifier">
          <el-input
            v-model="queryParams.identifier"
            placeholder="请输入标识符"
            clearable
            class="!w-200px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="点位名称" prop="pointName">
          <el-input
            v-model="queryParams.pointName"
            placeholder="请输入 GENESIS64 点位名称"
            clearable
            class="!w-260px"
            @keyup.enter="handleQuery"
          />
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
        </el-form-item>
      </el-form>

      <el-table v-loading="pointLoading" :data="pointList" :stripe="true" class="mt-4">
        <el-table-column label="属性名称" align="center" prop="name" min-width="120" />
        <el-table-column label="标识符" align="center" prop="identifier" min-width="120">
          <template #default="scope">
            <el-tag size="small" type="primary">{{ scope.row.identifier }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="点位名称"
          align="center"
          prop="pointName"
          min-width="260"
          show-overflow-tooltip
        />
        <el-table-column label="排序号" align="center" prop="sort" min-width="90" />
        <el-table-column label="状态" align="center" prop="status" min-width="80">
          <template #default="scope">
            <dict-tag :type="DICT_TYPE.COMMON_STATUS" :value="scope.row.status" />
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" fixed="right" width="120">
          <template #default="scope">
            <el-button
              link
              type="primary"
              @click="handleEditPoint(scope.row)"
              v-hasPermi="['iot:device:update']"
            >
              编辑
            </el-button>
            <el-button
              link
              type="danger"
              @click="handleDeletePoint(scope.row.id!, scope.row.name || scope.row.pointName)"
              v-hasPermi="['iot:device:delete']"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        :total="total"
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        @pagination="getPointPage"
      />
    </ContentWrap>

    <DeviceGenesisConfigForm
      ref="configFormRef"
      :device-id="device.id"
      @success="getGenesisConfig"
    />
    <DeviceGenesisPointForm
      ref="pointFormRef"
      :device-id="device.id"
      :thing-model-list="thingModelList"
      :available-thing-model-list="availableThingModelList"
      @success="getPointPage"
    />
  </div>
</template>

<script lang="ts" setup>
import { DeviceVO } from '@/api/iot/device/device'
import { ThingModelData } from '@/api/iot/thingmodel'
import { DeviceGenesisConfigApi, DeviceGenesisConfigVO } from '@/api/iot/device/genesis/config'
import {
  DeviceGenesisPointApi,
  DeviceGenesisPointPageReqVO,
  DeviceGenesisPointVO
} from '@/api/iot/device/genesis/point'
import { DICT_TYPE } from '@/utils/dict'
import DeviceGenesisConfigForm from './DeviceGenesisConfigForm.vue'
import DeviceGenesisPointForm from './DeviceGenesisPointForm.vue'

defineOptions({ name: 'DeviceGenesisConfig' })

const props = defineProps<{
  device: DeviceVO
  thingModelList: ThingModelData[]
}>()

const message = useMessage()
const genesisConfig = ref<DeviceGenesisConfigVO>({
  deviceId: props.device.id,
  baseUrl: '',
  username: '',
  password: '',
  timeout: 5000,
  collectInterval: 15000,
  status: 0,
  remark: ''
})

const getGenesisConfig = async () => {
  genesisConfig.value =
    (await DeviceGenesisConfigApi.getGenesisConfig(props.device.id)) || {
      deviceId: props.device.id,
      baseUrl: '',
      username: '',
      password: '',
      timeout: 5000,
      collectInterval: 15000,
      status: 0,
      remark: ''
    }
}

const formatPassword = (password?: string) => {
  if (!password) {
    return '-'
  }
  if (password.length <= 2) {
    return '*'.repeat(password.length)
  }
  return `${password.slice(0, 1)}${'*'.repeat(Math.max(password.length - 2, 1))}${password.slice(-1)}`
}

const configFormRef = ref()
const handleEditConfig = () => {
  configFormRef.value?.open(genesisConfig.value)
}

const pointLoading = ref(false)
const pointList = ref<DeviceGenesisPointVO[]>([])
const total = ref(0)
const availableThingModelList = ref<ThingModelData[]>([])
const queryParams = reactive<DeviceGenesisPointPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  deviceId: props.device.id,
  name: undefined,
  identifier: undefined,
  pointName: undefined
})

const getPointPage = async () => {
  pointLoading.value = true
  try {
    const data = await DeviceGenesisPointApi.getGenesisPointPage(queryParams)
    pointList.value = data.list
    total.value = data.total
  } finally {
    pointLoading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getPointPage()
}

const resetQuery = () => {
  queryParams.name = undefined
  queryParams.identifier = undefined
  queryParams.pointName = undefined
  handleQuery()
}

const pointFormRef = ref()
const loadAvailableThingModelList = async () => {
  const data = await DeviceGenesisPointApi.getGenesisPointList({
    deviceId: props.device.id
  })
  availableThingModelList.value = data || []
}

const handleAddPoint = async () => {
  await loadAvailableThingModelList()
  if (availableThingModelList.value.length === 0) {
    message.warning('当前设备的物模型属性已全部配置为点位')
    return
  }
  pointFormRef.value?.open('create')
}

const handleEditPoint = (row: DeviceGenesisPointVO) => {
  pointFormRef.value?.open('update', row.id)
}

const handleDeletePoint = async (id: number, name: string) => {
  try {
    await message.delConfirm(`确定要删除点位【${name}】吗？`)
    await DeviceGenesisPointApi.deleteGenesisPoint(id)
    message.success('删除成功')
    await getPointPage()
  } catch {}
}

onMounted(async () => {
  await getGenesisConfig()
  await getPointPage()
})
</script>
