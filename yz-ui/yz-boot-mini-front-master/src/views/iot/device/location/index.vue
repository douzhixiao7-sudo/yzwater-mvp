<template>
  <!-- 搜索与操作栏 -->
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="关键字" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入位置名称"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
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
        <el-button type="danger" plain @click="toggleExpandAll">
          <Icon icon="ep:sort" class="mr-5px" /> 展开/折叠
        </el-button>
      </el-form-item>
    </el-form>
  </ContentWrap>

  <!-- 列表 -->
  <ContentWrap>
    <el-table
      v-loading="loading"
      :data="pagedList"
      row-key="id"
      :tree-props="{ children: 'children' }"
      :default-expand-all="isExpandAll"
      v-if="refreshTable"
    >
      <el-table-column prop="name" label="位置名称" min-width="220" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" align="center" width="260">
        <template #default="scope">
          <el-button
            link
            type="primary"
            @click="openForm('update', scope.row.id)"
          >
            修改
          </el-button>
          <el-button
            link
            type="primary"
            @click="openForm('create', undefined, scope.row.id)"
          >
            新增下级
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
      @pagination="handlePagination"
    />
  </ContentWrap>

  <!-- 表单弹窗：新增/修改/查看 -->
  <LocationForm ref="formRef" @success="getList" />
</template>

<script setup lang="ts">
import LocationForm from './LocationForm.vue'
import * as LocationApi from '@/api/iot/device/location'
import { filter } from '@/utils/tree'

defineOptions({ name: 'IotDeviceLocation' })

const message = useMessage()

const loading = ref(true)
const list = ref<LocationApi.DeviceLocationNodeRespVO[]>([])
const queryParams = reactive({
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref()

const isExpandAll = ref(false)
const refreshTable = ref(true)

const tableList = computed<LocationApi.DeviceLocationNodeRespVO[]>(() => {
  const keyword = (queryParams.keyword || '').trim()
  if (!keyword) {
    return list.value
  }
  return filter(list.value, (n: any) => {
    const name = String(n?.name || '')
    return name.includes(keyword)
  })
})

const total = computed(() => (tableList.value ? tableList.value.length : 0))

const pagedList = computed<LocationApi.DeviceLocationNodeRespVO[]>(() => {
  const pageNo = queryParams.pageNo || 1
  const pageSize = queryParams.pageSize || 10
  const start = (pageNo - 1) * pageSize
  const end = start + pageSize
  return (tableList.value || []).slice(start, end)
})

/** 获得位置树列表 */
const getList = async () => {
  loading.value = true
  try {
    list.value = await LocationApi.DeviceLocationApi.getDeviceLocationTree()
    fixPageNo()
  } finally {
    loading.value = false
  }
}

const fixPageNo = () => {
  const pageSize = queryParams.pageSize || 10
  const pages = Math.max(1, Math.ceil(total.value / pageSize))
  if (queryParams.pageNo > pages) {
    queryParams.pageNo = pages
  }
  if (queryParams.pageNo < 1) {
    queryParams.pageNo = 1
  }
}

const toggleExpandAll = () => {
  refreshTable.value = false
  isExpandAll.value = !isExpandAll.value
  nextTick(() => {
    refreshTable.value = true
  })
}

const handleQuery = () => {
  queryParams.keyword = (queryParams.keyword || '').trim()
  queryParams.pageNo = 1
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.pageNo = 1
  queryFormRef.value?.resetFields?.()
}

const handlePagination = () => {
  fixPageNo()
}

/** 新增/修改/查看操作 */
const formRef = ref()
const openForm = (type: 'create' | 'update' | 'view', id?: number, parentId?: number) => {
  formRef.value.open(type, id, parentId)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await LocationApi.DeviceLocationApi.deleteDeviceLocation(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 初始化 */
onMounted(() => {
  getList()
})
</script>
