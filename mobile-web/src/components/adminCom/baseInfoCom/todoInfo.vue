<template>
  <div class="todo-info">
    <!-- 条件筛选容器 -->
    <div class="filter-container">
      <!-- 日期选择器 -->
      <el-config-provider :locale="zhCn">
        <div class="date-picker-wrapper">
          <el-date-picker
            v-model="startDate"
            type="date"
            placeholder="开始时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="date-picker"
            :clearable="true"
            @change="handleDateChange"
          />
          <span class="date-separator">-</span>
          <el-date-picker
            v-model="endDate"
            type="date"
            placeholder="截止时间"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            class="date-picker"
            :clearable="true"
            @change="handleDateChange"
          />
        </div>
      </el-config-provider>
    </div>
    
    <div class="info-content">
      <!-- 表格 -->
      <div class="table-wrapper">
        <table class="todo-table">
          <thead>
            <tr>
              <th class="col-problem-type">问题类型</th>
              <th class="col-river-section">水利设施</th>
              <th class="col-feedback-time">反馈时间</th>
              <th class="col-status">状态</th>
              <th class="col-action">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="tableData.length === 0">
              <td colspan="5" style="text-align: center; padding: 40px 0; color: #999;">
                暂无数据
              </td>
            </tr>
            <tr v-for="(item, index) in tableData" :key="index">
              <td class="problem-type-cell">{{ item.problemType }}</td>
              <td class="river-section-cell">{{ item.riverSection }}</td>
              <td class="feedback-time-cell">{{ item.feedbackTime }}</td>
              <td class="status-cell" :style="{ color: getStatusColor(item.statusLabel || item.status) }">
                {{ item.statusLabel || item.status }}
              </td>
              <td class="action-cell">
                <span class="action-link" @click="handleGoToDetail(item)">前往</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
import { ElDatePicker, ElConfigProvider, ElMessage } from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.js'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'

// 设置 Day.js 为中文
dayjs.locale('zh-cn')

export default {
  name: 'TodoInfo',
  components: {
    ElDatePicker,
    ElConfigProvider
  },
  data() {
    return {
      startDate: '', // 开始时间
      endDate: '', // 截止时间
      zhCn, // Element Plus 中文语言包
      // 表格数据
      tableData: [],
      // 分页参数
      pagination: {
        pageNo: 1,
        pageSize: 100
      },
      // 加载状态
      loading: false
    }
  },
  mounted() {
    // 初始化加载数据
    this.loadFeedBackData()
  },
  methods: {
    // 日期变化处理
    handleDateChange() {
      // 验证日期选择：如果选择了日期，必须同时选择开始和结束日期
      if ((this.startDate && !this.endDate) || (!this.startDate && this.endDate)) {
        ElMessage.warning('请选择开始日期和结束日期')
        return
      }
      
      // 如果两个日期都选择了，验证结束日期不能早于开始日期
      if (this.startDate && this.endDate) {
        const start = new Date(this.startDate)
        const end = new Date(this.endDate)
        if (end < start) {
          ElMessage.warning('结束日期不能早于开始日期')
          // 清空结束日期，让用户重新选择
          this.endDate = null
          return
        }
      }
      
      // 重置到第一页
      this.pagination.pageNo = 1
      // 重新加载数据
      this.loadFeedBackData()
    },

    // 加载反馈数据
    loadFeedBackData() {
      // 验证日期选择：如果选择了日期，必须同时选择开始和结束日期
      if ((this.startDate && !this.endDate) || (!this.startDate && this.endDate)) {
        ElMessage.warning('请同时选择开始日期和结束日期')
        this.loading = false
        return
      }
      
      // 如果两个日期都选择了，验证结束日期不能早于开始日期
      if (this.startDate && this.endDate) {
        const start = new Date(this.startDate)
        const end = new Date(this.endDate)
        if (end < start) {
          ElMessage.warning('结束日期不能早于开始日期')
          this.loading = false
          return
        }
      }
      
      this.loading = true
      
      // 手动构建查询字符串
      let queryParams = []
      
      // 必填参数
      queryParams.push(`pageNo=${this.pagination.pageNo}`)
      queryParams.push(`pageSize=${this.pagination.pageSize}`)
      
      // 构建 createTime 参数（字符串格式）
      // 格式为: createTime=2024-10-11,2024-10-12 (开始日期和结束日期用逗号分隔)
      // 只有在两个日期都选择时才添加 createTime 参数
      if (this.startDate && this.endDate) {
        // 使用字符串格式，开始日期和结束日期用逗号分隔
        queryParams.push(`createTime=${this.startDate},${this.endDate}`)
      }

      // 拼接完整的 URL
      const url = `/admin-api/problem/feedback/page?${queryParams.join('&')}`
      
      console.log('请求 URL:', url)

      this.$http.get(url).then(res => {
        console.log('反馈数据:', res.data)
        if (res.data && res.data.list) {
          // 将 API 返回的数据映射到表格数据
          this.tableData = (res.data.list || []).map(item => {
            return {
              ...item, // 保留原始数据，用于跳转详情（包括原始的 status 数字字段）
              problemType: item.feedbackTypeLabel || '--',
              riverSection: this.formatRiverSection(item),
              feedbackTime: this.formatFeedbackTime(item.createTime),
              statusLabel: item.statusLabel || '--' // 使用 statusLabel 作为显示字段，不覆盖 status
            }
          })
        } else {
          this.tableData = []
        }
      }).catch(error => {
        console.error('获取反馈数据失败:', error)
        ElMessage.error('获取反馈数据失败')
        this.tableData = []
      }).finally(() => {
        this.loading = false
      })
    },

    // 格式化反馈时间
    formatFeedbackTime(timestamp) {
      if (!timestamp) return '--'
      // 格式化为 MM/DD HH:mm:ss
      const date = new Date(timestamp)
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      const hours = String(date.getHours()).padStart(2, '0')
      const minutes = String(date.getMinutes()).padStart(2, '0')
      const seconds = String(date.getSeconds()).padStart(2, '0')
      return `${month}/${day} ${hours}:${minutes}:${seconds}`
    },

    // 格式化水利设施字段
    formatRiverSection(item) {
      if (!item) return '--'
      
      const { referenceType, referenceName, riverName, riverSectionName } = item
      
      // 如果是 reservoir 或 river，直接展示 referenceName
      if (referenceType === 'reservoir' || referenceType === 'river') {
        return referenceName || '--'
      }
      
      // 如果是 river_section，需要 riverName 拼接 riverSectionName，中间使用 '-'
      if (referenceType === 'river_section') {
        const parts = []
        if (riverName) parts.push(riverName)
        if (riverSectionName) parts.push(riverSectionName)
        return parts.length > 0 ? parts.join('-') : '--'
      }
      
      // 其他情况，返回 referenceName 或 '--'
      return referenceName || '--'
    },

    // 获取状态颜色
    getStatusColor(status) {
      const colorMap = {
        '待受理': '#FF5100',
        '待审核': '#FF5100',
        '处理中': '#349DFF',
        '待处理': '#349DFF',
        '待核验': '#FFA500',
        '已办结': '#3CB298',
        '已结办': '#3CB298',
        '已驳回': '#999999'
      }
      return colorMap[status] || '#333333'
    },
    
    // 处理前往详情 - GIS定位
    handleGoToDetail(item) {
      // 发送事件到父组件（AdminMap），触发GIS定位
      // 传递完整的 item 数据，以便后续跳转到详情页
      this.$emit('locate-feedback', {
        latitude: item.problemLatitude || item.latitude,
        longitude: item.problemLongitude || item.longitude,
        feedbackTypeLabel: item.feedbackTypeLabel || '其他',
        feedbackType: item.feedbackType,
        feedbackItem: item // 传递完整的反馈项数据，用于跳转详情页
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.todo-info {
  width: 100%;
  height: 100%;
  padding: 0px 0px;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;

  // 条件筛选容器
  .filter-container {
    height: 100px;
    background: #ffffff;
    flex-shrink: 0;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    padding: 0 24px;
  }

  // 日期选择器容器
  .date-picker-wrapper {
    width: 100%;
    display: flex;
    align-items: center;
    gap: 16px;

    .date-picker {
      flex: 1;

      :deep(.el-input__wrapper) {
        background: #f5f5f5;
        box-shadow: none;
        border: none;
        padding: 0 16px;
        height: 60px;

        .el-input__inner {
          font-size: 24px;
          color: #3D3D3D;
          height: auto;
          line-height: normal;

          &::placeholder {
            color: #999;
          }
        }
      }

      &.is-focus .el-input__wrapper {
        box-shadow: none;
        border: none;
      }
    }

    .date-separator {
      font-size: 24px;
      color: #333;
      flex-shrink: 0;
    }
  }

  .info-content {
    flex: 1;
    width: 100%;
    padding: 24px;
    overflow-y: auto;
    background-color: #f5f5f5;

    .info-title {
      font-size: 36px;
      font-weight: 500;
      color: #333;
      margin-bottom: 24px;
    }

    // 表格容器
    .table-wrapper {
      width: 100%;
      overflow-x: auto;
      background: #ffffff;
      border-radius: 8px;
    }

    // 表格样式
    .todo-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 24px;
      table-layout: fixed;

      thead {
        background-color: #e6f4ff;

        th {
          padding: 20px 16px;
          text-align: center;
          font-weight: 500;
          color: #333;
          border: 1px solid #e0e0e0;
          font-size: 24px;

          &.col-problem-type {
            width: 25%;
          }

          &.col-river-section {
            width: 25%;
          }

          &.col-feedback-time {
            width: 190px;
          }

          &.col-status {
            width: 90px;
          }

          &.col-action {
            width: 100px;
          }
        }
      }

      tbody {
        tr {
          &:nth-child(even) {
            background-color: #fafafa;
          }

          &:nth-child(odd) {
            background-color: #ffffff;
          }

          td {
            text-align: center;
            padding: 20px 0px;
            border: 1px solid #e0e0e0;
            color: #333;
            font-size: 24px;
            word-break: break-word;
          }

          .problem-type-cell {
            color: #333;
          }

          .river-section-cell {
            color: #333;
          }

          .feedback-time-cell {
            color: #333;
            white-space: nowrap;
          }

          .status-cell {
            font-weight: 400;
          }

          .action-cell {
            .action-link {
              color: #349DFF;
              cursor: pointer;
              font-size: 24px;
              text-decoration: none;
              transition: opacity 0.3s;

              &:hover {
                opacity: 0.8;
              }

              &:active {
                opacity: 0.6;
              }
            }
          }
        }
      }
    }
  }
}
</style>

