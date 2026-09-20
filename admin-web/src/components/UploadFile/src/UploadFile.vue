<template>
  <div v-if="!disabled" class="upload-file">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      :action="uploadUrl"
      :auto-upload="autoUpload"
      :before-upload="beforeUpload"
      :disabled="disabled"
      :drag="drag"
      :http-request="httpRequest"
      :limit="props.limit"
      :multiple="props.limit > 1"
      :on-error="excelUploadError"
      :on-exceed="handleExceed"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :on-success="handleFileSuccess"
      :show-file-list="true"
      class="upload-file-uploader"
      name="file"
    >
      <el-button type="primary">
        <Icon icon="ep:upload-filled" />
        选择文件
      </el-button>
      <template v-if="isShowTip" #tip>
        <div style="font-size: 12px">
          大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b>
        </div>
        <div style="font-size: 12px">
          格式为 <b style="color: #f56c6c">{{ fileType.join('/') }}</b> 的文件
        </div>
      </template>
      <template #file="row">
        <div class="flex items-center">
          <span>{{ row.file.name }}</span>
          <div class="ml-10px">
            <el-link :href="row.file.url" :underline="false" download target="_blank" type="primary">
              下载
            </el-link>
          </div>
          <div class="ml-10px">
            <el-button link type="danger" @click="handleRemove(row.file)">删除</el-button>
          </div>
        </div>
      </template>
    </el-upload>
  </div>

  <!-- 上传操作禁用时 -->
  <div v-if="disabled" class="upload-file">
    <div v-for="(file, index) in fileList" :key="index" class="flex items-center file-list-item">
      <span>{{ file.name }}</span>
      <div class="ml-10px">
        <el-link :href="file.url" :underline="false" download target="_blank" type="primary">
          下载
        </el-link>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
import type { UploadInstance, UploadProps, UploadRawFile, UploadUserFile } from 'element-plus'
import { isString } from '@/utils/is'
import { useUpload } from '@/components/UploadFile/src/useUpload'
import { UploadFile } from 'element-plus/es/components/upload/src/upload'

defineOptions({ name: 'UploadFile' })

const message = useMessage() // 消息弹窗
const emit = defineEmits(['update:modelValue'])

const props = defineProps({
  modelValue: propTypes.oneOfType<string | string[]>([String, Array<String>]).isRequired,
  fileType: propTypes.array.def(['doc', 'xls', 'ppt', 'txt', 'pdf']), // 文件类型，例如 ['png', 'jpg', 'jpeg']
  fileSize: propTypes.number.def(5), // 大小限制(MB)
  limit: propTypes.number.def(5), // 数量限制
  autoUpload: propTypes.bool.def(true), // 自动上传
  drag: propTypes.bool.def(false), // 拖拽上传
  isShowTip: propTypes.bool.def(true), // 是否显示提示
  disabled: propTypes.bool.def(false), // 是否禁用上传组件 ==> 非必填（默认为 false）
  directory: propTypes.string.def(undefined) // 上传目录 ==> 非必填（默认为 undefined）
})

// ========== 上传相关 ==========
const uploadRef = ref<UploadInstance>()
const uploadList = ref<UploadUserFile[]>([])
const fileList = ref<UploadUserFile[]>([])
const uploadNumber = ref<number>(0)

const { uploadUrl, httpRequest } = useUpload(props.directory)

// 文件上传之前判断
const beforeUpload: UploadProps['beforeUpload'] = (file: UploadRawFile) => {
  if (fileList.value.length >= props.limit) {
    message.error(`上传文件数量不能超过${props.limit}个`)
    return false
  }
  let fileExtension = ''
  if (file.name.lastIndexOf('.') > -1) {
    fileExtension = file.name.slice(file.name.lastIndexOf('.') + 1)
  }
  const isTypeAllowed = props.fileType.some((type: string) => {
    if (file.type.indexOf(type) > -1) return true
    return !!(fileExtension && fileExtension.indexOf(type) > -1)
  })
  const isLimit = file.size < props.fileSize * 1024 * 1024
  if (!isTypeAllowed) {
    message.error(`文件格式不正确，请上传 ${props.fileType.join('/')} 格式！`)
    return false
  }
  if (!isLimit) {
    message.error(`上传文件大小不能超过${props.fileSize}MB！`)
    return false
  }
  message.success('正在上传文件，请稍候...')
  // 只有在校验通过后才增加计数器
  uploadNumber.value++
  return true
}

// 文件上传成功
const resolveUploadUrl = (res: any, uploadFile?: UploadFile): string | undefined => {
  if (res && typeof res === 'object') {
    if (typeof res.data === 'string') return res.data
    if (res.data && typeof res.data.url === 'string') return res.data.url
    if (typeof res.url === 'string') return res.url
  }
  if (typeof res === 'string') {
    return res
  }
  if (uploadFile?.url) {
    return uploadFile.url
  }
  const responseUrl = (uploadFile as any)?.response?.data
  return typeof responseUrl === 'string' ? responseUrl : undefined
}

const handleFileSuccess: UploadProps['onSuccess'] = (
  res: any,
  uploadFile?: UploadFile,
  uploadFiles?: UploadFile[]
): void => {
  message.success('上传成功')
  const url = resolveUploadUrl(res, uploadFile)
  if (uploadFile && url) {
    if (!uploadFile.url || uploadFile.url.startsWith('blob:')) {
      uploadFile.url = url
    }
  }
  if (url) {
    uploadList.value.push({ name: url, url })
  }
  // 兼容 beforeUpload 未触发的情况
  const total = uploadNumber.value > 0 ? uploadNumber.value : uploadList.value.length
  if (total > 0 && uploadList.value.length >= total) {
    if (uploadFiles && uploadFiles.length) {
      fileList.value = uploadFiles
        .map((file) => ({
          name: file.name,
          url: resolveUploadUrl((file as any)?.response, file as any) || file.url
        }))
        .filter((file) => file.url)
    } else {
      fileList.value.push(...uploadList.value)
    }
    uploadList.value = []
    uploadNumber.value = 0
    emitUpdateModelValue()
  }
}

const handleExceed: UploadProps['onExceed'] = (): void => {
  message.error(`上传文件数量不能超过${props.limit}个`)
}
// 上传错误提示
const excelUploadError: UploadProps['onError'] = (): void => {
  message.error('上传失败，请您重新上传！')
  // 上传失败时减少计数器，避免后续上传被阻塞
  uploadNumber.value = Math.max(0, uploadNumber.value - 1)
}
// 删除上传文件
const handleRemove = (file: UploadFile) => {
  const index = fileList.value.map((f) => f.name).indexOf(file.name)
  if (index > -1) {
    fileList.value.splice(index, 1)
    emitUpdateModelValue()
  }
}
const handlePreview: UploadProps['onPreview'] = (uploadFile) => {
  console.log(uploadFile)
}

// 监听模型绑定值变化
watch(
  () => props.modelValue,
  (val: string | string[]) => {
    if (!val) {
      // fix：处理掉缓存，表单重置后上传组件的内容并没有重置
      fileList.value = []
      return
    }

    fileList.value = []
    // 情况1：字符串
    if (isString(val)) {
      fileList.value.push(
        ...val.split(',').map((url) => ({ name: url.substring(url.lastIndexOf('/') + 1), url }))
      )
      return
    }
    // 情况2：数组
    fileList.value.push(
      ...(val as string[]).map((url) => ({ name: url.substring(url.lastIndexOf('/') + 1), url }))
    )
  },
  { immediate: true, deep: true }
)
// 发送文件链接列表更新
const emitUpdateModelValue = () => {
  // 情况1：数组结果
  let result: string | string[] = fileList.value
    .map((file) => file.url)
    .filter((url): url is string => Boolean(url))
  // 情况2：逗号分隔的字符串
  if (props.limit === 1 || isString(props.modelValue)) {
    result = result.join(',')
  }
  emit('update:modelValue', result)
}
</script>
<style lang="scss" scoped>
.upload-file-uploader {
  margin-bottom: 5px;
}

:deep(.upload-file-list .el-upload-list__item) {
  position: relative;
  margin-bottom: 10px;
  line-height: 2;
  border: 1px solid #e4e7ed;
}

:deep(.el-upload-list__item-file-name) {
  max-width: 250px;
}

:deep(.upload-file-list .ele-upload-list__item-content) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}

:deep(.ele-upload-list__item-content-action .el-link) {
  margin-right: 10px;
}

.file-list-item {
  border: 1px dashed var(--el-border-color-darker);
  border-radius: 8px;
}
</style>
