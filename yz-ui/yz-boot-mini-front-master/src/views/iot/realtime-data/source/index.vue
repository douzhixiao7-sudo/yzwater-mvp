<template>
  <ContentWrap>
    <!-- 搜索工作栏 -->
    <el-form
      class="-mb-15px"
      :model="queryParams"
      ref="queryFormRef"
      :inline="true"
      label-width="80px"
    >
      <el-form-item label="名称" prop="name">
        <el-select
          v-model="queryParams.name"
          placeholder="请选择采集源名称"
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
      <el-form-item label="启用状态" prop="enabled">
        <el-select v-model="queryParams.enabled" placeholder="请选择启用状态" clearable class="!w-160px">
          <el-option
            v-for="item in enabledOptions"
            :key="String(item.value)"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="创建时间" prop="createTime">
        <el-date-picker
          v-model="queryParams.createTime"
          value-format="YYYY-MM-DD HH:mm:ss"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :default-time="[new Date('1 00:00:00'), new Date('1 23:59:59')]"
          class="!w-220px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button
          type="primary"
          plain
          @click="openForm('create')"
        >
          <Icon icon="ep:plus" class="mr-5px" /> 新增
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <ContentWrap>
    <el-table
      row-key="id"
      v-loading="loading"
      :data="list"
      :stripe="true"
      :show-overflow-tooltip="true"
    >
      <el-table-column label="编号" align="center" prop="id" width="80" />
      <el-table-column label="采集源名称" align="center" prop="name" min-width="140">
        <template #default="scope">
          {{ getDictLabel(DICT_TYPE.IOT_ZD_SBZD, scope.row.name) || scope.row.name || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="拉取地址" align="center" prop="url" min-width="200" />
      <el-table-column label="启用状态" align="center" prop="enabled" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.enabled ? 'success' : 'info'">
            {{ scope.row.enabled ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column
        label="创建时间"
        align="center"
        prop="createTime"
        :formatter="dateFormatter"
        width="180px"
      />
      <el-table-column label="操作" align="center" min-width="160px">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row)"
          >
            编辑
          </el-button>
          <el-button
            link
            type="primary"
            @click="openMapping(scope.row.id)"
          >
            点位配置
          </el-button>
          <el-button
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
      :total="total"
      v-model:page="queryParams.pageNo"
      v-model:limit="queryParams.pageSize"
      @pagination="getList"
    />
  </ContentWrap>

  <RealtimeDataSourceForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import { dateFormatter } from '@/utils/formatTime'
import { RealtimeDataSourceApi, RealtimeDataSource } from '@/api/iot/realtime-data/source'
import RealtimeDataSourceForm from './RealtimeDataSourceForm.vue'
import { useRouter } from 'vue-router'
import { DICT_TYPE, getDictLabel, getStrDictOptions } from '@/utils/dict'

/** IoT 实时数据采集源 列表 */
defineOptions({ name: 'IotRealtimeDataSource' })

const message = useMessage()
const { t } = useI18n()
const router = useRouter()

const loading = ref(true)
const list = ref<RealtimeDataSource[]>([])
const total = ref(0)
const queryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  enabled: undefined,
  createTime: []
})
const queryFormRef = ref()

const enabledOptions = [
  { label: '启用', value: true },
  { label: '停用', value: false }
]

const getList = async () => {
  loading.value = true
  try {
    const data = await RealtimeDataSourceApi.getSourcePage(queryParams)
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
const openForm = (type: string, row?: RealtimeDataSource) => {
  formRef.value.open(type, row)
}

const handleDelete = async (id: string) => {
  try {
    await message.delConfirm()
    await RealtimeDataSourceApi.deleteSource(id)
    message.success(t('common.delSuccess'))
    await getList()
  } catch {}
}

const openMapping = (id: string) => {
  router.push({ path: '/iot/realtime-data/mapping', query: { sourceId: id } })
}

onMounted(() => {
  getList()
})
</script>
