<template>
  <div class="river-course-info">
    <!-- 条件筛选容器 -->
    <div class="filter-container">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <div class="search-left">
          <el-select 
            v-model="selectedEcologicalType" 
            placeholder="生态类型"
            class="ecological-select"
            @change="handleEcologicalTypeChange"
          >
            <el-option
              v-for="item in ecologicalTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div class="search-divider"></div>
        <div class="search-right">
          <el-input
            v-model="searchKeyword"
            placeholder="请输入河道名称或河段名称"
            class="search-input"
            clearable
            @input="handleSearchInput"
            @clear="handleSearchClear"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
      </div>
    </div>
    
    <!-- 表格容器（独立的，与筛选容器分开） -->
    <div class="table-content">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-text">加载中...</div>
      </div>
      
      <!-- 表格 -->
      <div v-else class="table-wrapper">
        <table class="river-table">
          <thead>
            <tr>
              <th class="col-river-name">河道名称</th>
              <th class="col-river-length">河道长度</th>
              <th class="col-river-section">河段信息</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="tableData.length === 0">
              <td colspan="3" class="empty-cell">暂无数据</td>
            </tr>
            <tr v-for="(item, index) in tableData" :key="index">
              <td 
                v-if="item.showRiverName" 
                :rowspan="item.rowspan"
                class="river-name-cell"
              >
                {{ item.riverName }}
              </td>
              <td 
                v-if="item.showRiverLength" 
                :rowspan="item.rowspan"
                class="river-length-cell"
              >
                {{ item.riverLength }}
              </td>
              <td class="river-section-cell">
                <div
                  class="section-clickable"
                  :class="{ disabled: item.sectionInfo === '——' }"
                  @click.stop="handleRiverItemClick(item)"
                >
                  <span>{{ item.sectionInfo }}</span>
                  <img
                    v-if="item.sectionInfo !== '——'"
                    :src="goToRiverIcon"
                    alt="去定位"
                    class="goto-river-icon"
                  />
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 分页器 -->
      <div v-if="!loading && pagination.totalCount > 0" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          :total="pagination.totalCount"
          :page-size="pagination.pageSize"
          :pager-count="pagerCount"
          layout="prev, pager, next"
          @current-change="handleCurrentChange"
          class="pagination"
        />
      </div>
    </div>
  </div>
</template>

<script>
import { ElSelect, ElOption, ElInput, ElIcon, ElPagination, ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import goToRiverIcon from '@/assets/img/goToRiver.png'

export default {
  name: 'RiverCourseInfo',
  components: {
    ElSelect,
    ElOption,
    ElInput,
    ElIcon,
    ElPagination,
    Search
  },
  emits: ['river-name-click', 'close-drawer'], // 声明事件
  data() {
    return {
      goToRiverIcon,
      searchKeyword: '', // 搜索关键词
      selectedEcologicalType: '', // 选中的生态类型
      ecologicalTypes: [
        { label: '全部', value: '' }
      ],
      // 表格数据
      tableData: [],
      // 原始数据
      rawData: [],
      // 加载状态
      loading: false,
      // 分页信息
      pagination: {
        pageNo: 1,
        pageSize: 10,
        totalCount: 0
      },
      // 搜索防抖定时器
      searchTimer: null
    }
  },
  computed: {
    // 计算总页数
    totalPages() {
      return Math.ceil(this.pagination.totalCount / this.pagination.pageSize);
    },
    // 动态计算页码按钮数量
    pagerCount() {
      const total = this.totalPages;
      // 如果总页数小于等于5，显示所有页码
      if (total <= 5) {
        return total;
      }
      // 如果总页数大于5，最多显示5个页码按钮
      return 5;
    }
  },
  mounted() {
    // 加载生态类型字典数据
    this.loadEcologicalTypes();
    // 加载数据
    this.loadRiverChannelList();
  },
  methods: {
    // 加载生态类型字典数据
    async loadEcologicalTypes() {
      try {
        const response = await this.$http.get('/admin-api/river/channel/dict', {
          dictType: 'zd_stlx'
        });
        
        console.log('生态类型字典响应:', response);
        
        if (response && response.code === 0 && response.data && Array.isArray(response.data)) {
          // 将接口返回的数据转换为下拉框需要的格式
          const types = response.data.map(item => ({
            label: item.label || '',
            value: item.value || ''
          }));
          
          // 在"全部"选项后面添加生态类型选项
          this.ecologicalTypes = [
            { label: '全部', value: '' },
            ...types
          ];
          
          console.log('生态类型列表:', this.ecologicalTypes);
        } else {
          console.error('获取生态类型字典失败:', response);
        }
      } catch (error) {
        console.error('加载生态类型字典失败:', error);
      }
    },
    
    // 加载河道渠道列表
    async loadRiverChannelList() {
      try {
        this.loading = true;
        
        // 构建请求参数
        const params = {
          pageNo: this.pagination.pageNo,
          pageSize: this.pagination.pageSize
        };
        
        // 如果选择了生态类型（不是"全部"），添加 ecologyType 参数
        if (this.selectedEcologicalType) {
          params.ecologyType = this.selectedEcologicalType;
        } else {
          // 如果是"全部"，传递空字符串
          params.ecologyType = '';
        }
        
        // 如果有关键词搜索，添加 riverName 参数
        if (this.searchKeyword && this.searchKeyword.trim()) {
          params.riverName = this.searchKeyword.trim();
        } else {
          // 如果没有关键词，传递空字符串
          params.riverName = '';
        }
        
        console.log('请求参数:', params);
        
        // 传递分页参数和筛选参数
        const response = await this.$http.get('/admin-api/screen/statistics/river-channel-list', params);
        
        console.log('河道渠道列表响应:', response);
        console.log('请求参数 - pageNo:', this.pagination.pageNo, 'pageSize:', this.pagination.pageSize);
        
        if (response && response.code === 0 && response.data) {
          if (response.data.list) {
            this.rawData = response.data.list;
            
            // 更新分页信息：使用接口返回的 totalCount
            if (response.data.totalCount !== undefined && response.data.totalCount !== null) {
              this.pagination.totalCount = response.data.totalCount;
            } else {
              // 如果接口没有返回 totalCount，使用实际数据长度
              this.pagination.totalCount = response.data.list.length;
            }
            
            console.log('分页信息 - totalCount:', this.pagination.totalCount, 'list长度:', response.data.list.length);
            
            // 处理数据，转换为表格格式（不再需要前端分页）
            this.processTableData();
          } else {
            this.rawData = [];
            this.tableData = [];
            this.pagination.totalCount = 0;
          }
        } else {
          console.error('获取河道渠道列表失败:', response);
          this.rawData = [];
          this.tableData = [];
          this.pagination.totalCount = 0;
        }
      } catch (error) {
        console.error('加载河道渠道列表失败:', error);
        this.tableData = [];
      } finally {
        this.loading = false;
      }
    },
    
    // 根据河段名称匹配公示牌坐标
    getLocationBySection(item, sectionName = '') {
      const signboardLocations = Array.isArray(item.signboardLocations) ? item.signboardLocations : [];
      if (signboardLocations.length === 0) {
        return null;
      }

      // 优先按 ownerName 匹配“河道/河段”
      const matched = signboardLocations.find(loc => {
        const ownerName = loc?.ownerName || '';
        return sectionName && (
          ownerName === sectionName ||
          ownerName.endsWith(`/${sectionName}`) ||
          ownerName.includes(sectionName)
        );
      });

      const target = matched || signboardLocations[0];
      return {
        signboardId: target?.signboardId,
        signboardLongitude: target?.longitude,
        signboardLatitude: target?.latitude,
        ownerName: target?.ownerName || ''
      };
    },

    // 处理表格数据，计算 rowspan
    processTableData() {
      const processedData = [];
      
      this.rawData.forEach(item => {
        const riverLength = item.lengthKm ? `${item.lengthKm}km` : '——';
        const sectionNames = item.sectionNames || [];
        
        // 如果没有河段信息，显示一行，河段信息显示 "——"
        if (!sectionNames || sectionNames.length === 0) {
          const location = this.getLocationBySection(item, '');
          processedData.push({
            riverName: item.riverName || '——',
            riverLength: riverLength,
            sectionInfo: '——',
            rowspan: 1,
            showRiverName: true,
            showRiverLength: true,
            // 保存原始数据，用于定位和加载 geom
            originalData: {
              id: item.id,
              riverName: item.riverName,
              riverLevel: item.riverLevel,
              sectionName: '',
              signboardId: location?.signboardId,
              signboardLongitude: location?.signboardLongitude,
              signboardLatitude: location?.signboardLatitude
            }
          });
        } else {
          // 如果有河段信息，为每个河段生成一行
          sectionNames.forEach((sectionName, index) => {
            const location = this.getLocationBySection(item, sectionName);
            processedData.push({
              riverName: item.riverName || '——',
              riverLength: riverLength,
              sectionInfo: sectionName || '——',
              rowspan: index === 0 ? sectionNames.length : 0, // 第一行设置 rowspan，其他行设为 0
              showRiverName: index === 0, // 只有第一行显示河道名称
              showRiverLength: index === 0, // 只有第一行显示河道长度
              // 保存原始数据，用于定位和加载 geom（每一行都保存，用于逐条定位）
              originalData: {
                id: item.id,
                riverName: item.riverName,
                riverLevel: item.riverLevel,
                sectionName: sectionName || '',
                signboardId: location?.signboardId,
                signboardLongitude: location?.signboardLongitude,
                signboardLatitude: location?.signboardLatitude
              }
            });
          });
        }
      });
      
      // 接口已经做了分页，直接使用返回的数据
      this.tableData = processedData;
      
      console.log('表格数据 - 显示数据:', this.tableData.length, 'totalCount:', this.pagination.totalCount, '当前页:', this.pagination.pageNo, '每页:', this.pagination.pageSize, '总页数:', this.totalPages);
    },
    
    // 生态类型改变
    handleEcologicalTypeChange(value) {
      console.log('生态类型改变:', value);
      // 重置到第一页
      this.pagination.pageNo = 1;
      // 重新加载数据
      this.loadRiverChannelList();
    },
    
    // 搜索输入处理（带防抖）
    handleSearchInput(value) {
      // 清除之前的定时器
      if (this.searchTimer) {
        clearTimeout(this.searchTimer);
      }
      
      // 设置防抖，500ms 后执行搜索
      this.searchTimer = setTimeout(() => {
        console.log('搜索关键词:', value);
        // 重置到第一页
        this.pagination.pageNo = 1;
        // 重新加载数据
        this.loadRiverChannelList();
      }, 500);
    },
    
    // 搜索框清空处理
    handleSearchClear() {
      // 清除防抖定时器
      if (this.searchTimer) {
        clearTimeout(this.searchTimer);
      }
      
      console.log('搜索框已清空');
      // 重置到第一页
      this.pagination.pageNo = 1;
      // 重新加载数据
      this.loadRiverChannelList();
    },
    
    // 当前页改变
    handleCurrentChange(val) {
      this.pagination.pageNo = val;
      // 重新加载数据
      this.loadRiverChannelList();
    },
    
    // 点击河道信息行（支持逐条河段定位）
    handleRiverItemClick(item) {
      console.log('点击河道信息:', item);
      if (!item || !item.originalData) {
        return;
      }
      
      const { signboardLongitude, signboardLatitude, riverLevel, riverName, sectionName, id, signboardId } = item.originalData;
      console.log('坐标信息:', signboardLongitude, signboardLatitude, riverLevel, riverName, sectionName, id, signboardId);
      // 检查是否有坐标信息
      if (!signboardLongitude || !signboardLatitude) {
        // 显示提示信息并关闭抽屉
        ElMessage.info('位置不存在');
        this.$emit('close-drawer');
        return;
      }
      
      // 触发事件，传递坐标和河道级别
      this.$emit('river-name-click', {
        longitude: signboardLongitude,
        latitude: signboardLatitude,
        riverLevel: riverLevel,
        riverName: riverName,
        sectionName: sectionName || '',
        id: id, // 传递河道 ID，用于查找对应的公示牌
        signboardId: signboardId || null
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.river-course-info {
  width: 100%;
  height: 100%;
  padding: 0; // 移除所有 padding
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
    padding: 0 24px; // 筛选容器内部添加 padding
  }

  // 搜索栏
  .search-bar {
    width: 100%;
    height: 60px;
    background: #f5f5f5;
    display: flex;
    align-items: center;

    .search-left {
      flex: 0 0 38%;
      display: flex;
      align-items: center;

      :deep(.ecological-select) {
        width: 100%;

        .el-select__wrapper {
          box-shadow: none;
          background-color: #f5f5f5;
        }

        .el-input__wrapper {          box-shadow: none;
          border: none;
          padding: 0;

          .el-input__inner {
            font-size: 24px;
            color: #3D3D3D;
            height: auto;
            line-height: normal;
          }
        }

        &.is-focus {
          .el-select__wrapper {
            box-shadow: none;
          }
          .el-input__wrapper {
            box-shadow: none;
            border: none;
          }
        }
      }
    }

    .search-divider {
      width: 1px;
      height: 40px;
      background-color: #e0e0e0;
      margin-right:10px;
    }

    .search-right {
      flex: 0 0 57%;
      display: flex;
      align-items: center;

      :deep(.search-input) {
        width: 100%;

        .el-input__wrapper {
          background: #f5f5f5;
          box-shadow: none;
          border: none;
          padding: 0;
          font-size:20px;

          .el-input__inner {
            font-size: 24px;
            color: #3D3D3D;
            height: auto;
            line-height: normal;

            &::placeholder {
              color: #999;
            }
          }

          .el-input__prefix {
            .el-icon {
              font-size: 20px;
              color: #999;
            }
          }
        }

        &.is-focus .el-input__wrapper {
          box-shadow: none;
          border: none;
        }
      }
    }
  }

  // 表格容器（独立的，与筛选容器分开）
  .table-content {
    flex: 1;
    width: 100%;
    padding: 24px; // 移除所有 padding，让表格背景色撑满
    overflow-y: auto;
    background-color: #f5f5f5;

    // 加载状态
    .loading-container {
      width: 100%;
      padding: 60px 24px;
      text-align: center;
      background: #ffffff;
      border-radius: 0; // 移除圆角，让背景色撑满

      .loading-text {
        font-size: 28px;
        color: #999999;
      }
    }

    // 表格容器
    .table-wrapper {
      width: 100%;
      overflow-x: auto;
      background: #ffffff;
      border-radius: 0; // 移除圆角，让背景色撑满
      padding: 0; // 移除 padding
    }

    // 表格样式
    .river-table {
      width: 100%;
      border-collapse: collapse;
      font-size: 24px;
      table-layout: fixed;

      thead {
        background-color: #e6f4ff;

        th {
          padding: 20px 16px;
          text-align: left;
          font-weight: 500;
          color: #333;
          border: 1px solid #e0e0e0;
          font-size: 24px;

          &.col-river-name {
            width: 28%;
          }

          &.col-river-length {
            width: 32%;
          }

          &.col-river-section {
            width: 40%;
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
            padding: 20px 16px;
            border: 1px solid #e0e0e0;
            color: #333;
            font-size: 24px;
            word-break: break-word;
          }

          .river-name-cell {
            vertical-align: middle;
            color: #333333;
            font-weight: 500;
          }

          .river-length-cell {
            vertical-align: middle;
            color: #3D3D3D;
            font-weight: 400;
          }

          .river-section-cell {
            color: #333;
            text-align: center;
            vertical-align: middle;

            .section-clickable {
              display: inline-flex;
              align-items: center;
              justify-content: center;
              gap: 8px;
              color: #349DFF;
              cursor: pointer;
              transition: opacity 0.2s;

              &:hover {
                opacity: 0.8;
                text-decoration: underline;
              }

              &:active {
                opacity: 0.6;
              }

              &.disabled {
                color: #999;
                cursor: default;
                text-decoration: none;
              }

              .goto-river-icon {
                width: 20px;
                height: 20px;
                object-fit: contain;
                flex-shrink: 0;
              }
            }
          }

          .empty-cell {
            text-align: center;
            color: #999;
            padding: 40px 16px;
          }
        }
      }
    }

    // 分页器样式
    .pagination-wrapper {
      width: 100%;
      padding: 24px;
      display: flex;
      justify-content: center;
      background-color: #f5f5f5;

      :deep(.pagination) {
        .btn-prev,
        .btn-next {
          font-size: 24px;
          min-width: 60px;
          height: 60px;
          padding: 0 20px;
        }

        .el-pager {
          li {
            font-size: 24px;
            min-width: 60px;
            height: 60px;
            line-height: 60px;
            margin: 0 4px;
          }
        }
      }
    }
  }
}
</style>

