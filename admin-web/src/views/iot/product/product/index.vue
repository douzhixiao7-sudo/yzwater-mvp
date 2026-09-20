<template>
  <ContentWrap>
    <el-form
      ref="queryFormRef"
      :inline="true"
      :model="queryParams"
      class="-mb-15px"
      label-width="68px"
    >
      <el-form-item label="产品名称" prop="name">
        <el-input
          v-model="queryParams.name"
          class="!w-240px"
          clearable
          placeholder="请输入产品名称"
          @keyup.enter="handleQuery"
        />
      </el-form-item>
      <el-form-item label="是否发布" prop="status">
        <el-select
          v-model="queryParams.status"
          class="!w-240px"
          clearable
          placeholder="请选择发布状态"
          @change="handleQuery"
        >
          <el-option
            v-for="dict in getIntDictOptions(DICT_TYPE.IOT_PRODUCT_STATUS)"
            :key="dict.value"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery">
          <Icon class="mr-5px" icon="ep:search" />
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon class="mr-5px" icon="ep:refresh" />
          重置
        </el-button>
        <el-button
          v-hasPermi="['iot:product:create']"
          plain
          type="primary"
          @click="openForm('create')"
        >
          <Icon class="mr-5px" icon="ep:plus" />
          新增
        </el-button>
        <el-button
          v-hasPermi="['iot:product:export']"
          :loading="exportLoading"
          plain
          type="success"
          @click="handleExport"
        >
          <Icon class="mr-5px" icon="ep:download" />
          导出
        </el-button>
        <el-button
          v-hasPermi="['iot:product:update']"
          :loading="batchPublishLoading"
          plain
          type="warning"
          @click="handleBatchPublish"
        >
          <Icon class="mr-5px" icon="ep:upload" />
          批量发布
        </el-button>
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
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-row v-if="viewMode === 'card'" :gutter="16">
      <el-col v-for="item in list" :key="item.id" :lg="6" :md="12" :sm="12" :xs="24" class="mb-4">
        <el-card :body-style="{ padding: '0' }" class="h-full transition-colors">
          <div class="p-4">
            <div class="flex items-center mb-3">
              <div class="mr-2.5 flex items-center">
                <el-image :src="item.icon || defaultIconUrl" class="w-[35px] h-[35px]" />
              </div>
              <div class="text-[16px] font-600">{{ item.name }}</div>
            </div>

            <div class="flex items-center text-[14px]">
              <div class="flex-1">
                <div class="mb-2.5 last:mb-0">
                  <span class="text-[#717c8e] mr-2.5">产品分类</span>
                  <span class="text-[#0070ff]">{{ item.categoryName }}</span>
                </div>
                <div class="mb-2.5 last:mb-0">
                  <span class="text-[#717c8e] mr-2.5">设备类型</span>
                  <dict-tag :type="DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE" :value="item.deviceType" />
                </div>
                <div class="mb-2.5 last:mb-0">
                  <span class="text-[#717c8e] mr-2.5">协议类型</span>
                  <dict-tag :type="protocolDictType" :value="item.protocolType" />
                </div>
                <div class="mb-2.5 last:mb-0">
                  <span class="text-[#717c8e] mr-2.5">序列化类型</span>
                  <dict-tag :type="DICT_TYPE.IOT_CODEC_TYPE" :value="item.codecType" />
                </div>
                <div class="mb-2.5 last:mb-0">
                  <span class="text-[#717c8e] mr-2.5">是否发布</span>
                  <dict-tag :type="DICT_TYPE.IOT_PRODUCT_STATUS" :value="item.status" />
                </div>
              </div>
              <div class="w-[100px] h-[100px]">
                <el-image :src="item.picUrl || defaultPicUrl" class="w-full h-full" />
              </div>
            </div>

            <el-divider class="!my-3" />

            <div class="flex items-center px-0">
              <el-button
                v-hasPermi="['iot:product:update']"
                class="flex-1 !px-2 !h-[32px] text-[13px]"
                plain
                type="primary"
                @click="openForm('update', item.id)"
              >
                <Icon class="mr-1" icon="ep:edit-pen" />
                编辑
              </el-button>
              <el-button
                class="flex-1 !px-2 !h-[32px] !ml-[10px] text-[13px]"
                plain
                type="warning"
                @click="openDetail(item.id)"
              >
                <Icon class="mr-1" icon="ep:view" />
                详情
              </el-button>
              <el-button
                class="flex-1 !px-2 !h-[32px] !ml-[10px] text-[13px]"
                plain
                type="success"
                @click="openObjectModel(item)"
              >
                <Icon class="mr-1" icon="ep:scale-to-original" />
                物模型
              </el-button>
              <div class="mx-[10px] h-[20px] w-[1px] bg-[#dcdfe6]"></div>
              <el-button
                v-hasPermi="['iot:product:delete']"
                :disabled="item.status === 1"
                class="!px-2 !h-[32px] text-[13px]"
                plain
                type="danger"
                @click="handleDelete(item.id)"
              >
                <Icon icon="ep:delete" />
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-table v-else v-loading="loading" :data="list" :show-overflow-tooltip="true" :stripe="true">
      <el-table-column align="center" label="ID" prop="id" />
      <el-table-column align="center" label="是否发布" prop="status">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_PRODUCT_STATUS" :value="scope.row.status" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="产品名称" prop="name" />
      <el-table-column align="center" label="品类" prop="categoryName" />
      <el-table-column align="center" label="设备类型" prop="deviceType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE" :value="scope.row.deviceType" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="协议类型" prop="protocolType">
        <template #default="scope">
          <dict-tag :type="protocolDictType" :value="scope.row.protocolType" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="序列化类型" prop="codecType">
        <template #default="scope">
          <dict-tag :type="DICT_TYPE.IOT_CODEC_TYPE" :value="scope.row.codecType" />
        </template>
      </el-table-column>
      <el-table-column align="center" label="产品图片" prop="picture">
        <template #default="scope">
          <el-image
            v-if="scope.row.picUrl"
            :preview-src-list="[scope.row.picture]"
            :src="scope.row.picUrl"
            class="w-40px h-40px"
          />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column
        :formatter="dateFormatter"
        align="center"
        label="创建时间"
        prop="createTime"
        width="180px"
      />
      <el-table-column align="center" label="操作">
        <template #default="scope">
          <el-button
            v-hasPermi="['iot:product:query']"
            link
            type="primary"
            @click="openDetail(scope.row.id)"
          >
            查看
          </el-button>
          <el-button
            v-hasPermi="['iot:product:update']"
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            编辑
          </el-button>
          <el-button
            v-hasPermi="['iot:product:delete']"
            :disabled="scope.row.status === 1"
            link
            type="danger"
            @click="handleDelete(scope.row.id)"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      v-model:limit="queryParams.pageSize"
      v-model:page="queryParams.pageNo"
      :total="total"
      :page-sizes="pageSizes"
      @pagination="getList"
    />
  </ContentWrap>

  <ProductForm ref="formRef" @success="getList" />
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import { ProductApi, ProductVO } from '@/api/iot/product/product'
import ProductForm from './ProductForm.vue'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import download from '@/utils/download'
import defaultPicUrl from '@/assets/imgs/iot/device.png'
import defaultIconUrl from '@/assets/svgs/iot/cube.svg'

defineOptions({ name: 'IoTProduct' })

const message = useMessage()
const { t } = useI18n()
const { push } = useRouter()
const route = useRoute()
const protocolDictType = 'iot_protocol_type'

const loading = ref(true)
const activeName = ref('info')
const list = ref<ProductVO[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 12,
  name: undefined,
  status: undefined
})
const queryFormRef = ref()
const exportLoading = ref(false)
const batchPublishLoading = ref(false)
const viewMode = ref<'card' | 'list'>('card')
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

const getList = async () => {
  loading.value = true
  try {
    const data = await ProductApi.getProductPage(queryParams)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryFormRef.value.resetFields()
  handleQuery()
}

const formRef = ref()
const openForm = (type: string, id?: number) => {
  formRef.value.open(type, id)
}

const openDetail = (id: number) => {
  push({ name: 'IoTProductDetail', params: { id } })
}

const openObjectModel = (item: ProductVO) => {
  push({
    name: 'IoTProductDetail',
    params: { id: item.id },
    query: { tab: 'thingModel' }
  })
}

const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await ProductApi.deleteProduct(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const handleExport = async () => {
  try {
    await message.exportConfirm()
    exportLoading.value = true
    const data = await ProductApi.exportProduct(queryParams)
    download.excel(data, '物联网产品.xls')
  } catch {
  } finally {
    exportLoading.value = false
  }
}

const handleBatchPublish = async () => {
  try {
    await message.confirm('确认发布所有未发布的产品吗？')
    batchPublishLoading.value = true
    const count = await ProductApi.publishAllUnpublished()
    if (count > 0) {
      message.success(`成功发布 ${count} 个产品`)
    } else {
      message.success('暂无未发布的产品')
    }
    await getList()
  } catch {
  } finally {
    batchPublishLoading.value = false
  }
}

watch(viewMode, (mode) => {
  if (syncPageSizeForView(mode)) {
    getList()
  }
})

onMounted(() => {
  syncPageSizeForView(viewMode.value)
  getList()
  const { tab } = route.query
  if (tab) {
    activeName.value = tab as string
  }
})
</script>
