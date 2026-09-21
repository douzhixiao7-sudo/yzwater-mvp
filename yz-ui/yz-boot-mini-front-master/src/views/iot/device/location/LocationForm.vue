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
      <el-form-item v-if="formType !== 'create'" label="位置编号">
        <el-input v-model="formData.id" disabled class="!w-360px" />
      </el-form-item>

      <el-form-item label="上级位置" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="locationTree"
          :props="defaultProps"
          check-strictly
          default-expand-all
          placeholder="请选择上级位置"
          value-key="id"
          class="!w-360px"
        />
      </el-form-item>

      <el-form-item label="位置名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入位置名称" class="!w-360px" />
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

    <template #footer>
      <el-button v-if="formType !== 'view'" type="primary" @click="submitForm">确定</el-button>
      <el-button @click="dialogVisible = false">{{ formType === 'view' ? '关闭' : '取消' }}</el-button>
    </template>
  </Dialog>
</template>

<script lang="ts" setup>
import { defaultProps } from '@/utils/tree'
import * as LocationApi from '@/api/iot/device/location'
import { FormRules } from 'element-plus'

defineOptions({ name: 'IotDeviceLocationForm' })

const message = useMessage()

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formLoading = ref(false)
const formType = ref<'create' | 'update' | 'view'>('create')

const locationTree = ref<LocationApi.DeviceLocationNodeRespVO[]>([])

const formData = ref<LocationApi.DeviceLocationSaveReqVO>({
  id: undefined,
  parentId: 0,
  name: '',
  sort: 0
})

const formRules = reactive<FormRules>({
  parentId: [{ required: true, message: '上级位置不能为空', trigger: 'change' }],
  name: [{ required: true, message: '位置名称不能为空', trigger: 'blur' }]
})

const formRef = ref()

const loadLocationTree = async () => {
  const tree = await LocationApi.DeviceLocationApi.getDeviceLocationTree()
  locationTree.value = [
    {
      id: 0,
      name: '根节点',
      children: tree || []
    }
  ]
}

const resetForm = () => {
  formData.value = {
    id: undefined,
    parentId: 0,
    name: '',
    sort: 0
  }
  formRef.value?.resetFields?.()
}

const open = async (type: 'create' | 'update' | 'view', id?: number, parentId?: number) => {
  dialogVisible.value = true
  formType.value = type
  dialogTitle.value = type === 'create' ? '新增位置' : type === 'update' ? '修改位置' : '查看位置'
  resetForm()

  formLoading.value = true
  try {
    await loadLocationTree()
    if (type === 'create') {
      formData.value.parentId = parentId ?? 0
      return
    }
    if (!id) return
    const detail = await LocationApi.DeviceLocationApi.getDeviceLocation(id)
    formData.value = {
      id: detail.id,
      parentId: detail.parentId ?? 0,
      name: detail.name || '',
      sort: detail.sort ?? 0
    }
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
    const payload: LocationApi.DeviceLocationSaveReqVO = {
      id: formData.value.id,
      parentId: formData.value.parentId ?? 0,
      name: (formData.value.name || '').trim(),
      sort: formData.value.sort ?? 0
    }
    if (formType.value === 'create') {
      await LocationApi.DeviceLocationApi.createDeviceLocation(payload)
      message.success('新增成功')
    } else {
      await LocationApi.DeviceLocationApi.updateDeviceLocation(payload)
      message.success('修改成功')
    }
    dialogVisible.value = false
    emit('success')
  } finally {
    formLoading.value = false
  }
}
</script>
