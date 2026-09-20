<template>
  <!-- 搜索与操作栏 -->
  <ContentWrap>
    <el-form class="-mb-15px" :model="queryParams" ref="queryFormRef" :inline="true" label-width="68px">
      <el-form-item label="关键词" prop="keyword">
        <el-input
          v-model="queryParams.keyword"
          placeholder="请输入区域名称或编码"
          clearable
          @keyup.enter="handleQuery"
          class="!w-240px"
        />
      </el-form-item>
      <el-form-item>
        <el-button @click="handleQuery"><Icon icon="ep:search" class="mr-5px" /> 搜索</el-button>
        <el-button @click="resetQuery"><Icon icon="ep:refresh" class="mr-5px" /> 重置</el-button>
        <el-button type="primary" plain @click="openForm('create')">
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
      ref="tableRef"
      v-loading="loading"
      :data="pagedList"
      row-key="id"
      :tree-props="{ children: 'children' }"
      :indent="24"
      v-if="refreshTable"
    >
      <el-table-column label="区域编码" min-width="240" class-name="area-code-column">
        <template #default="scope">
          <span class="area-code-text" :title="String(scope.row.id)">{{ scope.row.id }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="区域名称" min-width="220" />
      <el-table-column label="操作" align="center" width="260">
        <template #default="scope">
          <el-button link type="primary" @click="openForm('view', scope.row.id)">查看</el-button>
          <el-button link type="primary" @click="openForm('update', scope.row.id)">修改</el-button>
          <el-button link type="primary" @click="openForm('create', undefined, scope.row.id)">新增下级</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

  <!-- 表单弹窗：添加/修改 -->
  <AreaForm ref="formRef" @success="getList" />
</template>
<script setup lang="ts">
import AreaForm from './AreaForm.vue'
import * as AreaApi from '@/api/system/area'
import { filter } from '@/utils/tree'

defineOptions({ name: 'SystemArea' })

const message = useMessage()

const loading = ref(true) // 列表的加载中
const list = ref<AreaApi.AreaNodeRespVO[]>([]) // 树数据
const tableRef = ref()

const queryParams = reactive({
  keyword: '',
  pageNo: 1,
  pageSize: 10
})
const queryFormRef = ref()

const isExpandAll = ref(false)
const refreshTable = ref(true)
const THIRD_LEVEL_CODE_LENGTH = 9
const trimmedKeyword = computed(() => (queryParams.keyword || '').trim())

const tableList = computed<AreaApi.AreaNodeRespVO[]>(() => {
  const keyword = trimmedKeyword.value
  if (!keyword) {
    return list.value
  }
  return filter(list.value, (n: any) => {
    const name = String(n?.name || '')
    const id = n?.id === undefined || n?.id === null ? '' : String(n.id)
    return name.includes(keyword) || id.includes(keyword)
  })
})

const total = computed(() => (tableList.value ? tableList.value.length : 0))

const pagedList = computed<AreaApi.AreaNodeRespVO[]>(() => {
  const pageNo = queryParams.pageNo || 1
  const pageSize = queryParams.pageSize || 10
  const start = (pageNo - 1) * pageSize
  const end = start + pageSize
  return (tableList.value || []).slice(start, end)
})

const collectAllExpandedKeys = (nodes: AreaApi.AreaNodeRespVO[] = []) => {
  const keys: number[] = []
  const walk = (items: AreaApi.AreaNodeRespVO[]) => {
    for (const item of items || []) {
      const children = Array.isArray(item?.children) ? item.children : []
      if (!children.length) {
        continue
      }
      keys.push(item.id)
      walk(children)
    }
  }
  walk(nodes)
  return keys
}

const collectDefaultExpandedKeys = (nodes: AreaApi.AreaNodeRespVO[] = []) => {
  const keys: number[] = []
  const walk = (items: AreaApi.AreaNodeRespVO[]) => {
    for (const item of items || []) {
      const children = Array.isArray(item?.children) ? item.children : []
      if (!children.length) {
        continue
      }
      if (String(item.id || '').length < THIRD_LEVEL_CODE_LENGTH) {
        keys.push(item.id)
        walk(children)
      }
    }
  }
  walk(nodes)
  return keys
}

const targetExpandedKeys = computed<number[]>(() => {
  if (trimmedKeyword.value) {
    return collectAllExpandedKeys(pagedList.value || [])
  }
  return isExpandAll.value
    ? collectAllExpandedKeys(pagedList.value || [])
    : collectDefaultExpandedKeys(pagedList.value || [])
})

const flattenTreeNodes = (nodes: AreaApi.AreaNodeRespVO[] = []) => {
  const result: AreaApi.AreaNodeRespVO[] = []
  const walk = (items: AreaApi.AreaNodeRespVO[]) => {
    for (const item of items || []) {
      result.push(item)
      const children = Array.isArray(item?.children) ? item.children : []
      if (children.length) {
        walk(children)
      }
    }
  }
  walk(nodes)
  return result
}

const applyExpandedRows = async () => {
  await nextTick()
  const table = tableRef.value
  if (!table || !refreshTable.value) {
    return
  }
  for (const node of flattenTreeNodes(pagedList.value || [])) {
    table.toggleRowExpansion(node, false)
  }
  await nextTick()
  const expandedKeySet = new Set(targetExpandedKeys.value)
  const expandNodes = async (nodes: AreaApi.AreaNodeRespVO[] = []) => {
    for (const node of nodes || []) {
      const children = Array.isArray(node?.children) ? node.children : []
      if (!children.length) {
        continue
      }
      if (!expandedKeySet.has(node.id)) {
        continue
      }
      table.toggleRowExpansion(node, true)
      await nextTick()
      await expandNodes(children)
    }
  }
  await expandNodes(pagedList.value || [])
}

const rerenderTable = async () => {
  refreshTable.value = false
  await nextTick()
  refreshTable.value = true
  await nextTick()
  await applyExpandedRows()
}

/** 获得数据列表 */
const getList = async () => {
  loading.value = true
  try {
    list.value = await AreaApi.getAreaTree()
    fixPageNo()
    await rerenderTable()
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
  isExpandAll.value = !isExpandAll.value
  rerenderTable()
}

const handleQuery = () => {
  queryParams.keyword = trimmedKeyword.value
  queryParams.pageNo = 1
  rerenderTable()
}

const resetQuery = () => {
  queryParams.keyword = ''
  queryParams.pageNo = 1
  queryFormRef.value?.resetFields?.()
  rerenderTable()
}

const handlePagination = () => {
  fixPageNo()
}

/** 添加/修改操作 */
const formRef = ref()
const openForm = (type: 'create' | 'update' | 'view', id?: number, parentId?: number) => {
  formRef.value.open(type, id, parentId)
}

/** 删除按钮操作 */
const handleDelete = async (id: number) => {
  try {
    await message.delConfirm()
    await AreaApi.deleteArea(id)
    message.success('删除成功')
    await getList()
  } catch {}
}

/** 初始化 **/
onMounted(() => {
  getList()
})

watch(
  () => [pagedList.value, targetExpandedKeys.value, refreshTable.value],
  () => {
    if (!refreshTable.value) {
      return
    }
    applyExpandedRows()
  },
  { deep: true }
)
</script>

<style scoped>
:deep(.area-code-column .cell) {
  display: flex;
  align-items: center;
  min-height: 24px;
  white-space: nowrap;
}

:deep(.area-code-column .cell .el-table__indent),
:deep(.area-code-column .cell .el-table__placeholder),
:deep(.area-code-column .cell .el-table__expand-icon) {
  flex: 0 0 auto;
}

:deep(.area-code-column .cell .el-table__expand-icon) {
  margin-right: 4px;
}

.area-code-text {
  display: inline-flex;
  align-items: center;
  color: var(--el-text-color-primary);
  font-variant-numeric: tabular-nums;
  letter-spacing: 0.02em;
  white-space: nowrap;
}
</style>
