<template>
  <Dialog v-model="dialogVisible" :title="dialogTitle">
    <el-form
      ref="formRef"
      v-loading="formLoading"
      :model="formData"
      :rules="formRules"
      label-width="90px"
      :disabled="formType === 'view'"
    >
      <el-form-item label="上级节点" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="areaTree"
          :props="defaultProps"
          check-strictly
          default-expand-all
          placeholder="请选择上级节点"
          value-key="id"
          class="!w-360px"
        />
      </el-form-item>

      <el-form-item label="区域编码" prop="id">
        <el-input-number
          v-model="formData.id"
          :min="0"
          :max="2147483647"
          :precision="0"
          controls-position="right"
          placeholder="请输入区域编码"
          class="!w-360px"
          :disabled="formType !== 'create'"
        />
      </el-form-item>

      <el-form-item label="区域名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入区域名称" class="!w-360px" />
      </el-form-item>

      <el-form-item label="类型" prop="type">
        <el-input-number
          v-model="formData.type"
          :min="0"
          :precision="0"
          controls-position="right"
          placeholder="请输入类型"
          class="!w-360px"
        />
      </el-form-item>

      <el-form-item label="排序" prop="sort">
        <el-input-number
          v-model="formData.sort"
          :min="0"
          :precision="0"
          controls-position="right"
          placeholder="请输入排序"
          class="!w-360px"
        />
      </el-form-item>
    </el-form>

    <div v-if="formType === 'view'" class="area-map">
      <div class="section-title">区域边界</div>
      <TiandituGeoJsonPreview
        :geo-json="viewGeoJson"
        :active="dialogVisible"
        :height="360"
        :center="yizhengCenter"
        :zoom="11"
        :label-text="formData.name"
        empty-text="暂无边界数据，默认定位到仪征"
        :highlight="true"
      />
    </div>

    <template #footer>
      <el-button v-if="formType !== 'view'" type="primary" @click="submitForm">确 定</el-button>
      <el-button @click="dialogVisible = false">{{ formType === 'view' ? '关 闭' : '取 消' }}</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { defaultProps } from '@/utils/tree'
import * as AreaApi from '@/api/system/area'
import { FormRules } from 'element-plus'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'

defineOptions({ name: 'SystemAreaForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref<'create' | 'update' | 'view'>('create')

const areaTree = ref<AreaApi.AreaNodeRespVO[]>([])
const viewGeoJson = ref('')
const yizhengCenter = [32.272258, 119.184766] as [number, number]

const formData = ref<AreaApi.AreaSaveReqVO>({
  id: undefined as any,
  parentId: 321081,
  name: '',
  type: 0,
  sort: 0
})

const formRules = reactive<FormRules>({
  parentId: [{ required: true, message: '上级节点不能为空', trigger: 'change' }],
  id: [{ required: true, message: '区域编码不能为空', trigger: 'blur' }],
  name: [{ required: true, message: '区域名称不能为空', trigger: 'blur' }]
})

const formRef = ref()

const loadAreaTree = async () => {
  const children = await AreaApi.getAreaTree()
  const rootName = await resolveRootName()
  areaTree.value = [
    {
      id: 321081,
      name: rootName,
      children: children || []
    }
  ]
}

const resolveRootName = async () => {
  try {
    const root = await AreaApi.getArea(321081)
    return root?.name || '仪征市'
  } catch {
    return '仪征市'
  }
}

const resetForm = () => {
  formData.value = {
    id: undefined as any,
    parentId: 321081,
    name: '',
    type: 0,
    sort: 0
  }
  viewGeoJson.value = ''
  formRef.value?.resetFields()
}

const open = async (type: 'create' | 'update' | 'view', id?: number, parentId?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = type === 'create' ? '新增区域' : type === 'update' ? '修改区域' : '查看区域'
  resetForm()

  formLoading.value = true
  try {
    await loadAreaTree()
    if (type === 'create') {
      formData.value.parentId = parentId ?? 321081
      return
    }
    if (!id) return
    const detail = await AreaApi.getArea(id)
    formData.value = {
      id: detail.id,
      parentId: detail.parentId ?? 0,
      name: detail.name || '',
      type: detail.type ?? 0,
      sort: detail.sort ?? 0
    }
    viewGeoJson.value = detail?.gemoGeoJson || ''
  } finally {
    formLoading.value = false
  }
}

defineExpose({ open })

const emit = defineEmits<{
  (e: 'success'): void
}>()

const submitForm = async () => {
  if (!formRef.value) return
  const valid = await formRef.value.validate()
  if (!valid) return

  formLoading.value = true
  try {
    const payload: AreaApi.AreaSaveReqVO = {
      id: Number(formData.value.id),
      parentId: formData.value.parentId ?? 0,
      name: (formData.value.name || '').trim(),
      type: formData.value.type ?? 0,
      sort: formData.value.sort ?? 0
    }
    if (formType.value === 'create') {
      await AreaApi.createArea(payload)
      message.success('新增成功')
    } else {
      await AreaApi.updateArea(payload)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>

<style scoped>
.section-title {
  margin: 8px 0;
  font-weight: 600;
  color: #303133;
}

.area-map {
  margin-top: 12px;
}
</style>
