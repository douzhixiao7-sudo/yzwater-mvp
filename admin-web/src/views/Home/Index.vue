<template>
  <div
    class="home-map h-[calc(100vh-var(--top-tool-height)-var(--tags-view-height)-var(--app-content-padding)-var(--app-content-padding)-2px)]"
  >
    <div ref="mapRef" class="home-map__map"></div>

    <div class="home-map__measure-tool">
      <div class="home-map__measure-actions">
        <el-button-group>
          <el-button
            size="small"
            :type="measureMode === 'distance' ? 'primary' : 'default'"
            :disabled="mapLoading"
            @click="toggleMeasure('distance')"
          >
            测距
          </el-button>
          <el-button
            size="small"
            :type="measureMode === 'area' ? 'primary' : 'default'"
            :disabled="mapLoading"
            @click="toggleMeasure('area')"
          >
            测面
          </el-button>
          <el-button size="small" :disabled="!canClearMeasure" @click="clearMeasure">清除</el-button>
        </el-button-group>
        <el-dropdown trigger="click" :disabled="mapLoading">
          <el-button size="small">
            空间查询
            <el-icon class="home-map__measure-dropdown-icon">
              <ArrowDown />
            </el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="openBufferQueryDialog">缓冲区查询</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
        <el-button v-if="bufferResult && !bufferResultVisible" size="small" type="primary" plain @click="bufferResultVisible = true">
          缓冲区结果
        </el-button>
        <el-button-group>
          <el-button
            size="small"
            :type="mapBaseLayerType === 'imagery' ? 'primary' : 'default'"
            :disabled="mapLoading"
            @click="switchMapBaseLayer('imagery')"
          >
            影像
          </el-button>
          <el-button
            size="small"
            :type="mapBaseLayerType === 'vector' ? 'primary' : 'default'"
            :disabled="mapLoading"
            @click="switchMapBaseLayer('vector')"
          >
            矢量
          </el-button>
        </el-button-group>
      </div>
      <div v-if="measureMode" class="home-map__measure-tip">
        {{ measureTipText }}
      </div>
    </div>

    <div v-if="panelHiddenByUser && !bufferPicking" class="home-map__panel-reveal">
      <el-button class="home-map__panel-reveal-btn" type="primary" plain @click="showHomePanel">显示面板</el-button>
    </div>

    <div v-show="!bufferFacilityListHidden" class="home-map__panel">
      <div class="home-map__panel-toolbar">
        <div class="home-map__panel-toolbar-row">
          <el-select
            v-model="selectedFacilityType"
            class="home-map__panel-select"
            filterable
            clearable
            placeholder="请选择水利设施"
            @change="handleFacilityTypeChanged"
          >
            <el-option
              v-for="item in facilityTypeOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>

          <el-input
            v-if="selectedFacilityType"
            v-model="facilityNameKeyword"
            class="home-map__panel-input"
            clearable
            placeholder="请输入水利设施名称"
            @keyup.enter="handleFacilityNameSearch"
          >
            <template #append>
              <el-button :loading="facilitySearchLoading" @click="handleFacilityNameSearch">搜索</el-button>
            </template>
          </el-input>

          <el-button class="home-map__panel-hide-btn" link @click="hideHomePanel">
            <el-icon><Fold /></el-icon>
            <span>隐藏</span>
          </el-button>
        </div>

        <div v-if="selectedFacilityType" class="home-map__panel-toolbar-row">
          <el-input v-model="areaKeyword" class="home-map__panel-input" clearable placeholder="搜索行政区域" />
          <el-button class="home-map__panel-toolbar-btn" @click="clearFacilityNameSearch">清空设施搜索</el-button>
          <el-button
            v-if="isCustomizeSelected"
            type="primary"
            plain
            class="home-map__panel-toolbar-btn"
            @click="openCustomizeCreateDialog"
          >
            新增图层
          </el-button>
        </div>
      </div>

      <div class="home-map__panel-head">
        <div class="home-map__panel-col-title home-map__panel-col-title--left" @click="toggleFacilityList">
          <span>水利设施</span>
          <el-icon class="home-map__panel-toggle-icon">
            <Expand v-if="facilityListCollapsed" />
            <Fold v-else />
          </el-icon>
        </div>
      </div>
      <div
        :class="[
          'home-map__panel-body',
          {
            'is-facility-collapsed': facilityListCollapsed
          }
        ]"
      >
        <div v-show="!facilityListCollapsed && !bufferFacilityListHidden" class="home-map__facility-list">
          <div
            v-for="item in facilityTypeOptions"
            :key="item.value"
            :class="['home-map__facility-item', { 'is-active': item.value === selectedFacilityType }]"
            @click="selectFacilityType(item.value)"
          >
            <div class="home-map__facility-name-wrap">
              <img
                v-if="resolveFacilityIconName(item)"
                :src="buildFacilityIconUrl(item, item.value === selectedFacilityType)"
                class="home-map__facility-icon"
                :alt="`${item.label}图标`"
                @error="handleFacilityIconError"
              />
              <span class="home-map__facility-name">{{ item.label }}</span>
            </div>
            <el-icon class="home-map__arrow"><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="home-map__area-list">
          <div class="home-map__area-list-head" @click="toggleAreaList">
            <span>行政区域</span>
            <el-icon class="home-map__panel-toggle-icon">
              <Expand v-if="areaListCollapsed" />
              <Fold v-else />
            </el-icon>
          </div>

          <div v-show="!areaListCollapsed">
            <div v-if="!selectedFacilityType" class="home-map__placeholder">请先在上方选择水利设施</div>
            <template v-else>
              <el-tree
                ref="areaTreeRef"
                class="home-map__area-tree"
                :data="areaTreeListData"
                :props="homeAreaTreeProps"
                node-key="id"
                highlight-current
                :default-expanded-keys="defaultExpandedAreaIds"
                :expand-on-click-node="false"
                :filter-node-method="filterHomeAreaNode"
                @node-click="handleAreaNodeClick"
              >
                <template #default="{ data }">
                  <div class="home-map__area-node">
                    <span class="home-map__area-name">{{ data.name }}</span>
                    <span class="home-map__area-count">（{{ getAreaCount(data.id) }}）</span>
                  </div>
                </template>
              </el-tree>
            </template>
          </div>
        </div>
      </div>
    </div>

    <el-drawer v-model="drawerVisible" direction="rtl" size="390px" :with-header="true" class="home-map__drawer">
      <template #header>
        <div class="home-map__drawer-title">
          <div class="home-map__drawer-title-main">{{ drawerTitleMain }}</div>
          <div class="home-map__drawer-title-sub">
            {{ selectedFacilityLabel }}：{{ drawerCount }}
          </div>
        </div>
      </template>

      <div class="home-map__drawer-body">
        <template v-if="showRiverOverview">
          <div class="home-map__river-overview">
            <div class="home-map__river-title">河道总览</div>
            <div class="home-map__river-kpi">
              <div class="home-map__river-kpi-row">
                <span class="home-map__river-kpi-label">河道总数：</span>
                <span class="home-map__river-kpi-value">{{ riverOverview?.totalCount ?? 0 }}条</span>
              </div>
              <div class="home-map__river-kpi-row">
                <span class="home-map__river-kpi-label">总流域面积：</span>
                <span class="home-map__river-kpi-value">{{ formatKm2(riverOverview?.totalCatchmentKm2) }}km²</span>
              </div>
              <div class="home-map__river-kpi-row">
                <span class="home-map__river-kpi-label">总长度：</span>
                <span class="home-map__river-kpi-value">{{ formatKm(riverOverview?.totalLengthKm) }}km</span>
              </div>
            </div>

            <div class="home-map__river-level">
              <div class="home-map__river-title">河道级别</div>
              <div class="home-map__river-level-grid">
                <div v-for="lv in riverLevelStats" :key="lv.value" class="home-map__river-level-item">
                  <div class="home-map__river-level-circle">
                    <div class="home-map__river-level-box-count">{{ lv.count }}条</div>
                    <div class="home-map__river-level-box-label">{{ formatRiverLevelLabel(lv) }}</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="home-map__river-list">
              <div class="home-map__river-title">河道列表</div>
              <el-table :data="riverOverview?.list || []" size="small" stripe @row-click="handleRiverRowClick">
                <el-table-column label="河道名称" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openRiverDetail(row)">
                      {{ row.riverName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="riverLevelLabel" label="河道级别" width="90" />
                <el-table-column prop="lengthKm" label="河道长度" width="90">
                  <template #default="{ row }">
                    <span>{{ formatKm(row.lengthKm) }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的河道名称，可在地图上定位并高亮显示河道。</div>
        </template>
        <template v-else-if="showRiverSearchOverview">
          <div class="home-map__river-list">
            <div class="home-map__river-title">河道列表</div>
            <el-table :data="riverSearchList" size="small" stripe @row-click="handleRiverRowClick">
              <el-table-column label="河道名称" min-width="180" show-overflow-tooltip>
                <template #default="{ row }">
                  <el-link type="primary" :underline="false" @click.stop="openRiverDetail(row)">
                    {{ row.riverName }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="riverLevelLabel" label="河道级别" width="90" />
              <el-table-column prop="lengthKm" label="河道长度" width="90">
                <template #default="{ row }">
                  <span>{{ formatKm(row.lengthKm) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的河道名称，可在地图上定位并高亮显示河道。</div>
        </template>
        <template v-else-if="showReservoirSearchList">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">水库列表</div>
              <el-table :data="reservoirOverview?.list || []" size="small" stripe @row-click="openReservoirDetail">
                <el-table-column label="水库名称" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openReservoirDetail(row)">
                      {{ row.reservoirName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="水库规模" width="90">
                  <template #default="{ row }">
                    {{ reservoirScaleLabelMap[String(row.reservoirScale || '')] || row.reservoirScale || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="总库容" width="110">
                  <template #default="{ row }">
                    {{ formatWanM3(row.totalCapacity) }}万m³
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的水库名称或地图水库区域，可查看水库详情并定位。</div>
        </template>
        <template v-else-if="showSignboardSearchList">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">公示牌列表</div>
              <el-table :data="signboardOverview?.list || []" size="small" stripe @row-click="handleSignboardRowClick">
                <el-table-column label="公示牌名称" min-width="170" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openSignboardDetail(row)">
                      {{ row.signboardName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="关联类型" width="80">
                  <template #default="{ row }">
                    <span>{{ row.referenceTypeLabel || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="关联设施" min-width="140" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span>{{ row.referenceName || '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的公示牌名称或地图点位，可查看公示牌详情并定位。</div>
        </template>
        <template v-else-if="showPumpStationSearchList">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">泵站列表</div>
              <el-table :data="pumpStationOverview?.list || []" size="small" stripe @row-click="handlePumpStationRowClick">
                <el-table-column label="泵站名称" min-width="170" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openPumpStationDetail(row)">
                      {{ row.pumpStationName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="泵站类型" width="100" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{
                      row.pumpStationTypeLabel ||
                      pumpStationTypeLabelMap[String(row.pumpStationType || '')] ||
                      row.pumpStationType ||
                      '-'
                    }}
                  </template>
                </el-table-column>
                <el-table-column label="工程等级" width="90" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{
                      row.engineeringGradeLabel ||
                      engineeringGradeLabelMap[String(row.engineeringGrade || '')] ||
                      row.engineeringGrade ||
                      '-'
                    }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的泵站名称或地图点位，可查看泵站详情并定位。</div>
        </template>
        <template v-else-if="showEmbankmentSearchList">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">堤防列表</div>
              <el-table :data="embankmentOverview?.list || []" size="small" stripe @row-click="handleEmbankmentRowClick">
                <el-table-column label="堤防名称" min-width="190" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openEmbankmentDetail(row)">
                      {{ row.embankmentName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="堤防形式" width="110" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.embankmentFormLabel || embankmentFormLabelMap[String(row.embankmentForm || '')] || row.embankmentForm || '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的堤防名称或地图点位，可查看堤防详情并定位。</div>
        </template>
        <template v-else-if="showFloodMaterialWarehouseSearchList">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">仓库列表</div>
              <el-table
                :data="floodMaterialWarehouseOverview?.list || []"
                size="small"
                stripe
                @row-click="handleFloodMaterialWarehouseRowClick"
              >
                <el-table-column label="仓库名称" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openFloodMaterialWarehouseDetail(row)">
                      {{ row.warehouseName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="归属单位" min-width="140" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span>{{ row.belongUnit || '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的仓库名称或地图点位，可查看仓库详情并定位。</div>
        </template>
        <template v-else-if="showIrrigationSearchList">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">灌区列表</div>
              <el-table
                :data="irrigationDistrictOverview?.list || []"
                size="small"
                stripe
                @row-click="handleIrrigationDistrictRowClick"
              >
                <el-table-column label="灌区名称" min-width="150" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openIrrigationDistrictDetail(row)">
                      {{ row.irrigationDistrictName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="实际灌溉面积" width="110" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ formatKm2(row.actualIrrigableArea) }}km²
                  </template>
                </el-table-column>
                <el-table-column label="基本农田面积" width="110" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ formatKm2(row.basicFarmlandAreaKm2) }}km²
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击灌区名称或地图灌区范围，可查看灌区详情并定位。</div>
        </template>
        <template v-else-if="showPondSearchList">
          <div class="home-map__pond-drawer">
            <div class="home-map__pond-section home-map__pond-section--list">
              <div class="home-map__pond-section-head">
                <span class="home-map__pond-section-title">搜索结果</span>
                <span class="home-map__pond-section-badge">{{ pondListTotal }}</span>
              </div>
              <div v-loading="pondListLoading" class="home-map__pond-list">
                <div
                  v-for="row in pondList"
                  :key="String(row.id || row.resourceCode || row.resourceName)"
                  class="home-map__pond-item"
                  @click="handlePondRowClick(row)"
                >
                  <div class="home-map__pond-item-top">
                    <div class="home-map__pond-item-main">
                      <div class="home-map__pond-item-name">{{ row.resourceName || row.resourceCode || '-' }}</div>
                      <div class="home-map__pond-item-code" v-if="row.resourceCode">{{ row.resourceCode }}</div>
                    </div>
                    <div class="home-map__pond-item-area">{{ formatNumber(row.areaSqm) }}㎡</div>
                  </div>
                  <div class="home-map__pond-item-tags">
                    <span v-if="row.ownershipType" class="home-map__pond-tag">{{ row.ownershipType }}</span>
                    <span v-if="row.resourceType" class="home-map__pond-tag home-map__pond-tag--type">{{ row.resourceType }}</span>
                  </div>
                </div>
                <el-empty v-if="!pondListLoading && !pondList.length" description="暂无匹配的坑塘" :image-size="72" />
              </div>
              <el-pagination
                v-if="pondListTotal > pondListPageSize"
                v-model:current-page="pondListPageNo"
                :page-size="pondListPageSize"
                layout="total, prev, pager, next"
                :total="pondListTotal"
                small
                class="home-map__pond-pagination"
                @current-change="fetchPondDrawerList"
              />
            </div>
          </div>
          <div class="home-map__drawer-tip">点击坑塘条目或在地图上点击面范围，可定位并高亮显示。</div>
        </template>
        <template v-else-if="showReservoirOverview">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-title">水库总览</div>
            <div class="home-map__reservoir-kpi">
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">水库总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ reservoirOverview?.totalCount ?? 0 }}座</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总库容：</span>
                <span class="home-map__reservoir-kpi-value">{{ formatWanM3(reservoirOverview?.totalCapacity) }}万m³</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总坝顶长度：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWithUnit(reservoirOverview?.totalDamTopLength, 'm') }}
                </span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总兴利库容：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWanM3(reservoirOverview?.totalActiveCapacity) }}万m³
                </span>
              </div>
            </div>

            <div class="home-map__reservoir-scale">
              <div class="home-map__reservoir-title">水库规模</div>
              <div class="home-map__reservoir-scale-grid">
                <div v-for="it in reservoirScaleStats" :key="it.value" class="home-map__reservoir-scale-item">
                  <div class="home-map__reservoir-scale-count">{{ it.count }}座</div>
                  <div class="home-map__reservoir-scale-label">{{ it.label }}</div>
                </div>
              </div>
            </div>

            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">水库列表</div>
              <el-table :data="reservoirOverview?.list || []" size="small" stripe @row-click="openReservoirDetail">
                <el-table-column label="水库名称" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openReservoirDetail(row)">
                      {{ row.reservoirName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="水库规模" width="90">
                  <template #default="{ row }">
                    {{ reservoirScaleLabelMap[String(row.reservoirScale || '')] || row.reservoirScale || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="总库容" width="110">
                  <template #default="{ row }">
                    {{ formatWanM3(row.totalCapacity) }}万m³
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的水库名称或地图水库区域，可查看水库详情并定位。</div>
        </template>
        <template v-else-if="showSignboardOverview">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-title">公示牌概览</div>
            <div class="home-map__reservoir-kpi">
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">河道总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ signboardOverview?.riverCount ?? 0 }}条</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">水库总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ signboardOverview?.reservoirCount ?? 0 }}座</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">公示牌总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ signboardOverview?.totalCount ?? 0 }}块</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">问题总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ signboardOverview?.problemTotalCount ?? 0 }}条</span>
              </div>
            </div>

            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">公示牌列表</div>
              <el-table :data="signboardOverview?.list || []" size="small" stripe @row-click="handleSignboardRowClick">
                <el-table-column label="公示牌名称" min-width="170" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openSignboardDetail(row)">
                      {{ row.signboardName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="关联类型" width="80">
                  <template #default="{ row }">
                    <span>{{ row.referenceTypeLabel || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="关联设施" min-width="140" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span>{{ row.referenceName || '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的公示牌名称或地图点位，可查看公示牌详情并定位。</div>
        </template>
        <template v-else-if="showPumpStationOverview">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-title">泵站总览</div>
            <div class="home-map__reservoir-kpi">
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">泵站总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ pumpStationOverview?.totalCount ?? 0 }}座</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总自排流量：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWithUnit(pumpStationOverview?.totalSelfFlow, 'm³/s') }}
                </span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总抽引流量：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWithUnit(pumpStationOverview?.totalInstalledFlow, 'm³/s') }}
                </span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总抽排流量：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWithUnit(pumpStationOverview?.totalPumpingFlow, 'm³/s') }}
                </span>
              </div>
            </div>

            <div class="home-map__reservoir-scale">
              <div class="home-map__reservoir-title">泵站类型</div>
              <div class="home-map__reservoir-scale-grid">
                <div v-for="it in pumpStationTypeStats" :key="it.value" class="home-map__reservoir-scale-item">
                  <div class="home-map__reservoir-scale-count">{{ it.count }}座</div>
                  <div class="home-map__reservoir-scale-label">{{ it.label }}</div>
                </div>
              </div>
            </div>

            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">泵站列表</div>
              <el-table :data="pumpStationOverview?.list || []" size="small" stripe @row-click="handlePumpStationRowClick">
                <el-table-column label="泵站名称" min-width="170" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openPumpStationDetail(row)">
                      {{ row.pumpStationName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="泵站类型" width="100" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.pumpStationTypeLabel || pumpStationTypeLabelMap[String(row.pumpStationType || '')] || row.pumpStationType || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="工程等别" width="90" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.engineeringGradeLabel || engineeringGradeLabelMap[String(row.engineeringGrade || '')] || row.engineeringGrade || '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的泵站名称或地图点位，可查看泵站详情并定位。</div>
        </template>
        <template v-else-if="showEmbankmentOverview">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-title">堤防总览</div>
            <div class="home-map__reservoir-kpi">
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">堤防总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ embankmentOverview?.totalCount ?? 0 }}条</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总堤防长度：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWithUnit(embankmentOverview?.totalLengthM, 'm') }}
                </span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">标准长度：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWithUnit(embankmentOverview?.totalStandardLengthM, 'm') }}
                </span>
              </div>
            </div>

            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">堤防列表</div>
              <el-table :data="embankmentOverview?.list || []" size="small" stripe @row-click="handleEmbankmentRowClick">
                <el-table-column label="堤防名称" min-width="190" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openEmbankmentDetail(row)">
                      {{ row.embankmentName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="堤防形式" width="110" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.embankmentFormLabel || embankmentFormLabelMap[String(row.embankmentForm || '')] || row.embankmentForm || '-' }}
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的堤防名称或地图点位，可查看堤防详情并定位。</div>
        </template>
        <template v-else-if="showFloodMaterialWarehouseOverview">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-title">防汛物资仓库</div>
            <div class="home-map__reservoir-kpi">
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">仓库总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ floodMaterialWarehouseOverview?.totalCount ?? 0 }}座</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">物资种类：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatTextOrDash(floodMaterialWarehouseOverview?.materialTypes) }}
                </span>
              </div>
            </div>

            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">仓库列表</div>
              <el-table
                :data="floodMaterialWarehouseOverview?.list || []"
                size="small"
                stripe
                @row-click="handleFloodMaterialWarehouseRowClick"
              >
                <el-table-column label="仓库名称" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openFloodMaterialWarehouseDetail(row)">
                      {{ row.warehouseName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="归属单位" min-width="140" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span>{{ row.belongUnit || '-' }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击列表中的仓库名称或地图点位，可查看仓库详情并定位。</div>
        </template>
        <template v-else-if="showIrrigationOverview">
          <div class="home-map__reservoir-overview">
            <div class="home-map__reservoir-title">灌区总览</div>
            <div class="home-map__reservoir-kpi">
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">灌区总数：</span>
                <span class="home-map__reservoir-kpi-value">{{ irrigationDistrictOverview?.totalCount ?? 0 }}个</span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总实际空间面积：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatKm2(irrigationDistrictOverview?.totalActualIrrigableArea) }}km²
                </span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总实际基本农田面积：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatKm2(irrigationDistrictOverview?.totalBasicFarmlandAreaKm2) }}km²
                </span>
              </div>
              <div class="home-map__reservoir-kpi-row">
                <span class="home-map__reservoir-kpi-label">总干渠长度：</span>
                <span class="home-map__reservoir-kpi-value">
                  {{ formatWithUnit(irrigationDistrictOverview?.totalMainCanalLengthM, 'm') }}
                </span>
              </div>
            </div>

            <div class="home-map__reservoir-list">
              <div class="home-map__reservoir-title">灌区列表</div>
              <el-table
                :data="irrigationDistrictOverview?.list || []"
                size="small"
                stripe
                @row-click="handleIrrigationDistrictRowClick"
              >
                <el-table-column label="灌区名称" min-width="150" show-overflow-tooltip>
                  <template #default="{ row }">
                    <el-link type="primary" :underline="false" @click.stop="openIrrigationDistrictDetail(row)">
                      {{ row.irrigationDistrictName }}
                    </el-link>
                  </template>
                </el-table-column>
                <el-table-column label="实际空间面积" width="110" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ formatKm2(row.actualIrrigableArea) }}km²
                  </template>
                </el-table-column>
                <el-table-column label="基本农田面积" width="110" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ formatKm2(row.basicFarmlandAreaKm2) }}km²
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
          <div class="home-map__drawer-tip">提示：点击灌区名称或地图灌区范围，可查看灌区详情并定位。</div>
        </template>
        <template v-else-if="showPondOverview">
          <div class="home-map__pond-drawer">
            <div class="home-map__pond-stats">
              <div class="home-map__pond-stat-card home-map__pond-stat-card--count">
                <div class="home-map__pond-stat-label">坑塘总数</div>
                <div class="home-map__pond-stat-value">
                  {{ pondOverview?.totalCount ?? 0 }}<span class="home-map__pond-stat-unit">个</span>
                </div>
              </div>
              <div class="home-map__pond-stat-card home-map__pond-stat-card--area">
                <div class="home-map__pond-stat-label">实测面积</div>
                <div class="home-map__pond-stat-value">
                  {{ formatCompactNumber(pondOverview?.totalAreaSqm) }}<span class="home-map__pond-stat-unit">㎡</span>
                </div>
              </div>
              <div class="home-map__pond-stat-card home-map__pond-stat-card--mu">
                <div class="home-map__pond-stat-label">折合亩数</div>
                <div class="home-map__pond-stat-value">
                  {{ formatCompactNumber(pondOverview?.totalAreaMu) }}<span class="home-map__pond-stat-unit">亩</span>
                </div>
              </div>
            </div>

            <div class="home-map__pond-section">
              <div class="home-map__pond-section-head">
                <span class="home-map__pond-section-title">筛选条件</span>
              </div>
              <div class="home-map__pond-filter-form">
                <div class="home-map__pond-filter-field">
                  <label>土地权属</label>
                  <el-select
                    v-model="pondFilterQuery.ownershipType"
                    clearable
                    filterable
                    placeholder="全部"
                    size="default"
                  >
                    <el-option v-for="item in pondOwnershipTypeOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </div>
                <div class="home-map__pond-filter-field">
                  <label>资源类型</label>
                  <el-select
                    v-model="pondFilterQuery.resourceType"
                    clearable
                    filterable
                    placeholder="全部"
                    size="default"
                  >
                    <el-option v-for="item in pondResourceTypeOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </div>
                <div class="home-map__pond-filter-field">
                  <label>使用状态</label>
                  <el-select
                    v-model="pondFilterQuery.usageStatus"
                    clearable
                    filterable
                    placeholder="全部"
                    size="default"
                  >
                    <el-option v-for="item in pondUsageStatusOptions" :key="item" :label="item" :value="item" />
                  </el-select>
                </div>
                <div class="home-map__pond-filter-actions">
                  <el-button size="default" @click="resetPondFilter">重置</el-button>
                  <el-button size="default" type="primary" @click="applyPondFilter">应用筛选</el-button>
                </div>
              </div>
            </div>

            <div class="home-map__pond-section home-map__pond-section--list">
              <div class="home-map__pond-section-head">
                <span class="home-map__pond-section-title">坑塘列表</span>
                <span class="home-map__pond-section-badge">{{ pondListTotal }}</span>
              </div>
              <div v-loading="pondListLoading" class="home-map__pond-list">
                <div
                  v-for="row in pondList"
                  :key="String(row.id || row.resourceCode || row.resourceName)"
                  class="home-map__pond-item"
                  @click="handlePondRowClick(row)"
                >
                  <div class="home-map__pond-item-top">
                    <div class="home-map__pond-item-main">
                      <div class="home-map__pond-item-name">{{ row.resourceName || row.resourceCode || '-' }}</div>
                      <div class="home-map__pond-item-code" v-if="row.resourceCode">{{ row.resourceCode }}</div>
                    </div>
                    <div class="home-map__pond-item-area">{{ formatNumber(row.areaSqm) }}㎡</div>
                  </div>
                  <div class="home-map__pond-item-tags">
                    <span v-if="row.ownershipType" class="home-map__pond-tag">{{ row.ownershipType }}</span>
                    <span v-if="row.usageStatus" class="home-map__pond-tag home-map__pond-tag--status">{{ row.usageStatus }}</span>
                  </div>
                </div>
                <el-empty v-if="!pondListLoading && !pondList.length" description="当前条件下暂无坑塘" :image-size="72" />
              </div>
              <el-pagination
                v-if="pondListTotal > pondListPageSize"
                v-model:current-page="pondListPageNo"
                :page-size="pondListPageSize"
                layout="total, prev, pager, next"
                :total="pondListTotal"
                small
                class="home-map__pond-pagination"
                @current-change="fetchPondDrawerList"
              />
            </div>
          </div>
          <div class="home-map__drawer-tip">点击坑塘条目或在地图上点击面范围，可定位并高亮显示。</div>
        </template>
        <template v-else>
          <el-table
            :data="facilityList"
            height="calc(100vh - 220px)"
            size="small"
            stripe
            @row-click="handleFacilityRowClick"
          >
            <el-table-column prop="facilityName" label="名称" min-width="200" show-overflow-tooltip />
            <el-table-column prop="geomType" label="类型" width="100" />
            <el-table-column v-if="isCustomizeSelected" label="操作" width="130" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click.stop="openCustomizeDialog(row, 'view')">详情</el-button>
                <el-button link type="warning" @click.stop="openCustomizeDialog(row, 'edit')">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="home-map__drawer-tip">提示：点击列表项可在地图上定位并高亮显示设施。</div>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="bufferDialogVisible"
      width="360px"
      align-center
      :show-close="false"
      :close-on-click-modal="false"
      :modal="false"
      class="home-map__buffer-dialog"
    >
      <template #header>
        <div class="home-map__buffer-dialog-header">
          <div class="home-map__buffer-dialog-title">空间查询</div>
          <el-button class="home-map__buffer-close" link @click="closeBufferDialog">
            <el-icon><Close /></el-icon>
          </el-button>
        </div>
      </template>
      <div class="home-map__buffer-body">
        <div class="home-map__buffer-row">
          <span class="home-map__buffer-label">缓冲区半径</span>
          <el-input v-model="bufferRadius" class="home-map__buffer-input" placeholder="请输入半径" clearable>
            <template #append>米</template>
          </el-input>
        </div>
        <div class="home-map__buffer-row home-map__buffer-row--top">
          <span class="home-map__buffer-label">查询对象：</span>
          <div class="home-map__buffer-options">
            <el-checkbox v-model="bufferSelectAll" @change="handleBufferSelectAllChanged">全部</el-checkbox>
            <el-checkbox-group v-model="bufferFacilityTypes" :disabled="bufferSelectAll">
              <el-checkbox v-for="item in bufferFacilityOptions" :key="item.value" :label="item.value">
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
        <div class="home-map__buffer-row">
          <span class="home-map__buffer-label">中心点：</span>
          <div class="home-map__buffer-center">
            <span v-if="bufferCenterPointText">{{ bufferCenterPointText }}</span>
            <span v-else class="home-map__buffer-center-placeholder">未选</span>
            <el-button link type="primary" @click="enableBufferPick">地图选点</el-button>
          </div>
        </div>
        <div class="home-map__buffer-tip">提示：点击地图选择中心点，仅支持仪征范围内。</div>
      </div>
      <template #footer>
        <div class="home-map__buffer-footer">
          <el-button @click="closeBufferDialog">取消</el-button>
          <el-button class="home-map__buffer-submit" type="primary" :loading="bufferQueryLoading" @click="submitBufferQuery">生成缓冲区</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="feedbackDialogVisible"
      width="420px"
      :modal="false"
      :append-to-body="false"
      :close-on-click-modal="false"
      class="home-map__feedback-dialog"
    >
      <template #header>
        <div class="home-map__feedback-dialog-title">公众反馈</div>
      </template>
      <div class="home-map__feedback-body">
        <div class="home-map__feedback-section">
          <div class="home-map__feedback-section-title">问题类型</div>
          <div class="home-map__feedback-checkbox-row">
            <el-checkbox
              v-model="feedbackTypeAllChecked"
              :indeterminate="feedbackTypeAllIndeterminate"
              class="home-map__feedback-checkbox-all"
              @change="handleFeedbackTypeAllChange"
            >
              全部
            </el-checkbox>
            <el-checkbox-group v-model="feedbackTypeValues" class="home-map__feedback-checkbox-group">
              <el-checkbox v-for="item in feedbackTypeOptions" :key="item.value" :label="item.value">
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
        <div class="home-map__feedback-section">
          <div class="home-map__feedback-section-title">处理状态</div>
          <div class="home-map__feedback-checkbox-row">
            <el-checkbox
              v-model="feedbackStatusAllChecked"
              :indeterminate="feedbackStatusAllIndeterminate"
              class="home-map__feedback-checkbox-all"
              @change="handleFeedbackStatusAllChange"
            >
              全部
            </el-checkbox>
            <el-checkbox-group v-model="feedbackStatusValues" class="home-map__feedback-checkbox-group">
              <el-checkbox v-for="item in feedbackStatusOptions" :key="item.value" :label="item.value">
                {{ item.label }}
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="feedbackDetailVisible" width="520px" align-center class="home-map__feedback-detail-dialog">
      <template #header>
        <div class="home-map__feedback-dialog-title">问题详情</div>
      </template>
      <div v-loading="feedbackDetailLoading" class="home-map__feedback-detail-body">
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">水利设施</span>
          <span class="home-map__feedback-detail-value">{{ feedbackFacilityTypeLabel }}</span>
        </div>
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">设施名称</span>
          <span class="home-map__feedback-detail-value">{{ feedbackFacilityName }}</span>
        </div>
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">问题类型</span>
          <span class="home-map__feedback-detail-value">{{ feedbackDetailView.feedbackTypeText }}</span>
        </div>
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">处理状态</span>
          <span class="home-map__feedback-detail-value">{{ feedbackDetailView.statusText }}</span>
        </div>
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">反馈内容</span>
          <span class="home-map__feedback-detail-value">{{ feedbackDetailView.contentText }}</span>
        </div>
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">反馈时间</span>
          <span class="home-map__feedback-detail-value">{{ feedbackDetailView.timeText }}</span>
        </div>
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">具体问题</span>
          <span class="home-map__feedback-detail-value">{{ feedbackDetailView.specificText }}</span>
        </div>
        <div class="home-map__feedback-detail-row">
          <span class="home-map__feedback-detail-label">反馈人</span>
          <span class="home-map__feedback-detail-value">{{ feedbackDetailView.personText }}</span>
        </div>
      </div>
      <div class="home-map__feedback-actions">
        <el-button type="primary" plain :disabled="!feedbackDetailAnalysisPoint" @click="handleOpenFeedbackDetailAnalysis">空间分析</el-button>
      </div>
    </el-dialog>

    <el-dialog v-model="feedbackClusterVisible" width="640px" align-center class="home-map__feedback-list-dialog">
      <template #header>
        <div class="home-map__feedback-dialog-title">问题列表</div>
      </template>
      <div class="home-map__feedback-list-body">
        <el-table :data="feedbackClusterList" size="small" stripe @row-click="handleFeedbackClusterRowClick">
          <el-table-column label="反馈时间" width="160">
            <template #default="{ row }">
              {{ formatFeedbackTime(row?.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="问题类型" min-width="120">
            <template #default="{ row }">
              {{ row?.feedbackTypeLabel || resolveFeedbackTypeLabel(row?.feedbackType) }}
            </template>
          </el-table-column>
          <el-table-column label="处理状态" min-width="100">
            <template #default="{ row }">
              {{ row?.statusLabel || resolveFeedbackStatusLabel(row?.status) }}
            </template>
          </el-table-column>
          <el-table-column label="反馈内容" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row?.feedbackContent || '-' }}
            </template>
          </el-table-column>
        </el-table>
        <div v-if="!feedbackClusterList.length" class="home-map__feedback-empty">暂无数据</div>
      </div>
      <div class="home-map__feedback-actions">
        <el-button type="primary" plain :disabled="!feedbackClusterAnalysisPoint" @click="handleOpenFeedbackClusterAnalysis">空间分析</el-button>
      </div>
    </el-dialog>

    <el-dialog
      v-model="spatialAnalysisDialogVisible"
      width="360px"
      align-center
      :close-on-click-modal="false"
      class="home-map__analysis-dialog"
    >
      <template #header>
        <div class="home-map__feedback-dialog-title">空间分析</div>
      </template>
      <div class="home-map__analysis-form">
        <div class="home-map__analysis-form-row">
          <span class="home-map__analysis-label">分析范围</span>
          <el-input-number
            v-model="spatialAnalysisRadius"
            :min="1"
            :max="50000"
            :step="10"
            controls-position="right"
            placeholder="请输入范围"
          />
          <span class="home-map__analysis-unit">米</span>
        </div>
        <div class="home-map__analysis-tip">提示：以问题坐标为中心统计范围内设施与河长信息。</div>
      </div>
      <template #footer>
        <div class="home-map__analysis-footer">
          <el-button @click="spatialAnalysisDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="spatialAnalysisLoading" @click="submitSpatialAnalysis">
            开始分析
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="spatialAnalysisResultVisible"
      width="780px"
      align-center
      @opened="handleSpatialAnalysisOpened"
      class="home-map__analysis-result-dialog"
    >
      <template #header>
        <div class="home-map__feedback-dialog-title">分析结果</div>
      </template>
      <div v-loading="spatialAnalysisLoading" class="home-map__analysis-result-body">
        <div class="home-map__analysis-summary">
          <span>中心点：{{ spatialAnalysisResult?.longitude || '-' }}, {{ spatialAnalysisResult?.latitude || '-' }}</span>
          <span>范围：{{ spatialAnalysisResult?.radiusM || '-' }}米</span>
        </div>
        <div class="home-map__analysis-section">
          <div class="home-map__analysis-section-title">问题统计</div>
          <div v-if="spatialAnalysisHasProblemTypeStats" class="home-map__analysis-chart-grid">
            <div class="home-map__analysis-chart">
              <Echart :key="spatialAnalysisChartKey" :options="spatialAnalysisPieOptions" :height="220" />
            </div>
          </div>
          <div v-else class="home-map__analysis-empty">暂无数据</div>
        </div>
        <div class="home-map__analysis-section">
          <div class="home-map__analysis-section-title">水利资产</div>
          <div class="home-map__analysis-asset-grid">
            <div class="home-map__analysis-asset-item">
              <div class="home-map__analysis-asset-count">{{ spatialAnalysisAssetStats.riverCount }}条</div>
              <div class="home-map__analysis-asset-label">河道</div>
            </div>
            <div class="home-map__analysis-asset-item">
              <div class="home-map__analysis-asset-count">{{ spatialAnalysisAssetStats.reservoirCount }}座</div>
              <div class="home-map__analysis-asset-label">水库</div>
            </div>
            <div class="home-map__analysis-asset-item">
              <div class="home-map__analysis-asset-count">{{ spatialAnalysisAssetStats.embankmentCount }}条</div>
              <div class="home-map__analysis-asset-label">堤防</div>
            </div>
            <div class="home-map__analysis-asset-item">
              <div class="home-map__analysis-asset-count">{{ spatialAnalysisAssetStats.pumpStationCount }}座</div>
              <div class="home-map__analysis-asset-label">泵站</div>
            </div>
            <div class="home-map__analysis-asset-item">
              <div class="home-map__analysis-asset-count">{{ spatialAnalysisAssetStats.irrigationDistrictCount }}个</div>
              <div class="home-map__analysis-asset-label">灌区</div>
            </div>
            <div class="home-map__analysis-asset-item">
              <div class="home-map__analysis-asset-count">{{ spatialAnalysisAssetStats.floodMaterialCount }}个</div>
              <div class="home-map__analysis-asset-label">防汛物资</div>
            </div>
          </div>
        </div>
        <div class="home-map__analysis-section">
          <div class="home-map__analysis-section-title">管理责任</div>
          <el-table v-if="analysisChiefTotal" :data="analysisChiefPageList" size="small" stripe>
            <el-table-column prop="headName" label="河长姓名" width="110" />
            <el-table-column prop="headLevelLabel" label="河长级别" width="100" />
            <el-table-column prop="referenceTypeLabel" label="关联设施" width="90" />
            <el-table-column prop="referenceName" label="关联设施名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="adminRegion" label="行政区划" min-width="160" show-overflow-tooltip />
          </el-table>
          <el-pagination
            v-if="analysisChiefTotal > analysisChiefPageSize"
            v-model:current-page="analysisChiefPageNo"
            :page-size="analysisChiefPageSize"
            layout="total, prev, pager, next"
            :total="analysisChiefTotal"
            class="home-map__analysis-pagination"
          />
          <div v-else class="home-map__analysis-empty">暂无数据</div>
        </div>
      </div>
    </el-dialog>

    <el-drawer
      v-model="bufferResultVisible"
      direction="rtl"
      size="390px"
      :with-header="true"
      class="home-map__drawer home-map__buffer-result-drawer"
    >
      <template #header>
        <div class="home-map__buffer-header">
          <div class="home-map__buffer-header-title">缓冲区结果</div>
        </div>
      </template>
      <div class="home-map__buffer-result">
        <div class="home-map__buffer-kpi">
          <div class="home-map__buffer-kpi-row">
            <span class="home-map__buffer-kpi-label">设施数量</span>
            <span class="home-map__buffer-kpi-value">{{ bufferFacilityCount }}条</span>
          </div>
          <div class="home-map__buffer-kpi-row">
            <span class="home-map__buffer-kpi-label">统计时间</span>
            <span class="home-map__buffer-kpi-value">{{ bufferStatsTimeText }}</span>
          </div>
          <div class="home-map__buffer-kpi-row">
            <span class="home-map__buffer-kpi-label">缓冲区面积</span>
            <span class="home-map__buffer-kpi-value">{{ bufferAreaText }}</span>
          </div>
        </div>
        <div class="home-map__buffer-section">
          <div class="home-map__buffer-section-title">涉及行政区</div>
          <div v-if="bufferAdminAreas.length" class="home-map__buffer-area-table">
            <table>
              <tbody>
                <tr v-for="(row, rowIndex) in bufferAdminAreaRows" :key="rowIndex">
                  <td v-for="colIndex in 3" :key="colIndex">
                    <span>{{ row[colIndex - 1]?.name || '-' }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
          <span v-else class="home-map__buffer-empty">-</span>
        </div>
        <div class="home-map__buffer-section">
          <div class="home-map__buffer-section-title">对象信息</div>
          <div v-if="bufferObjectInfoList.length" class="home-map__buffer-object-grid">
            <div
              v-for="item in bufferObjectInfoList"
              :key="item.value"
              :class="['home-map__buffer-object-item', { 'is-active': item.value === bufferObjectFilter }]"
              @click="handleBufferObjectFilter(item)"
            >
              <div class="home-map__buffer-object-count">{{ item.count }}条</div>
              <div class="home-map__buffer-object-label">{{ item.label }}</div>
            </div>
          </div>
          <span v-else class="home-map__buffer-empty">-</span>
        </div>
        <div class="home-map__buffer-section">
          <div class="home-map__buffer-section-title">设施列表</div>
          <el-table :data="bufferFacilityPageList" size="small" stripe @row-click="handleBufferFacilityRowClick">
            <el-table-column prop="facilityName" label="名称" min-width="160" show-overflow-tooltip />
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                {{ bufferFacilityTypeLabel(row.facilityType) }}
              </template>
            </el-table-column>
          </el-table>
          <el-pagination
            v-if="bufferFacilityTotal > bufferFacilityPageSize"
            v-model:current-page="bufferFacilityPageNo"
            :page-size="bufferFacilityPageSize"
            layout="total, prev, pager, next"
            :total="bufferFacilityTotal"
            class="home-map__buffer-pagination"
          />
        </div>
      </div>
    </el-drawer>

    <el-dialog
      v-model="bufferFacilityDetailVisible"
      width="500px"
      :destroy-on-close="true"
      align-center
      :show-close="false"
      append-to-body
      style="background: transparent; box-shadow: none; border: none;"
    >
      <div style="width: 500px; background: #fff; border-radius: 12px; box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15); overflow: hidden; position: relative;">
        <!-- 头部 -->
        <div style="padding: 18px 24px; border-bottom: 1px solid #e0e6ed; display: flex; justify-content: space-between; align-items: center; background: #fff;">
          <h2 style="font-size: 18px; font-weight: 600; color: #2c3e50; margin: 0;">{{ bufferFacilityDetail?.facilityName || '设施详情' }}</h2>
          <span
            style="color: #999; font-size: 20px; cursor: pointer; width: 32px; height: 32px; display: inline-flex; align-items: center; justify-content: center; border-radius: 50%; transition: all 0.2s;"
            @click="bufferFacilityDetailVisible = false"
            onmouseover="this.style.backgroundColor='#f0f2f5';this.style.color='#333'"
            onmouseout="this.style.backgroundColor='transparent';this.style.color='#999'"
          >✕</span>
        </div>

        <!-- 内容区 -->
        <div v-loading="bufferFacilityDetailLoading" style="padding: 24px; background-color: #f8faff;">
          <!-- 基础信息卡片 -->
          <div style="background: #fff; border-radius: 10px; padding: 18px; margin-bottom: 20px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);">
            <div style="font-size: 15px; font-weight: 600; color: #1a73e8; margin-bottom: 14px; padding-left: 12px; border-left: 4px solid #1a73e8; line-height: 1;">基础信息</div>
            <div style="border: 1px solid #edf2f7; border-radius: 8px; overflow: hidden;">
              <table style="width: 100%; border-collapse: collapse; background: #fff;">
                <tbody>
                <tr style="border-bottom: 1px solid #edf2f7;">
                  <td style="padding: 14px 12px; font-size: 14px; color: #5f6368; width: 120px; background-color: #f9fafb;">设施类型</td>
                  <td style="padding: 14px 12px; font-size: 14px; color: #2c3e50; font-weight: 500; background-color: #fff;">{{ bufferFacilityDetail?.facilityType || '-' }}</td>
                </tr>
                <tr style="border-bottom: 1px solid #edf2f7;">
                  <td style="padding: 14px 12px; font-size: 14px; color: #5f6368; width: 120px; background-color: #f9fafb;">行政区划</td>
                  <td style="padding: 14px 12px; font-size: 14px; color: #2c3e50; font-weight: 500; background-color: #fcfcfc;">{{ bufferFacilityAdminRegionText || '-' }}</td>
                </tr>
                <tr>
                  <td style="padding: 14px 12px; font-size: 14px; color: #5f6368; width: 120px; background-color: #f9fafb;">设施编码</td>
                  <td style="padding: 14px 12px; font-size: 14px; color: #2c3e50; font-weight: 500; background-color: #fff;">{{ bufferFacilityDetail?.facilityCode || '-' }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- 属性信息卡片 -->
          <div style="background: #fff; border-radius: 10px; padding: 18px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);">
            <div style="font-size: 15px; font-weight: 600; color: #1a73e8; margin-bottom: 14px; padding-left: 12px; border-left: 4px solid #1a73e8; line-height: 1;">属性信息</div>
            <div style="border: 1px solid #edf2f7; border-radius: 8px; overflow: hidden;">
              <table style="width: 100%; border-collapse: collapse; background: #fff;">
                <tbody>
                <template v-if="bufferFacilityAttrRows.length">
                  <tr v-for="(row, index) in bufferFacilityAttrRows" :key="index" :style="index !== bufferFacilityAttrRows.length - 1 ? 'border-bottom: 1px solid #edf2f7;' : ''">
                    <td style="padding: 14px 12px; font-size: 14px; color: #5f6368; width: 120px; background-color: #f9fafb;">{{ row.key }}</td>
                    <td :style="'padding: 14px 12px; font-size: 14px; color: #2c3e50; font-weight: 500; background-color: ' + (index % 2 === 1 ? '#fcfcfc' : '#fff') + ';'">{{ row.value }}</td>
                  </tr>
                </template>
                <tr v-else>
                  <td style="padding: 14px 12px; font-size: 14px; color: #5f6368; width: 120px; background-color: #f9fafb;">暂无属性</td>
                  <td style="padding: 14px 12px; font-size: 14px; color: #2c3e50; font-weight: 500; background-color: #fff;">-</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="customizeDialogVisible"
      :title="customizeDialogTitle"
      width="980px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :disabled="customizeReadonly" label-width="110px" label-position="left">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="图层名称" required>
              <el-input v-model="customizeForm.facilityName" placeholder="请输入" maxlength="50" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="行政区划" required>
              <el-tree-select
                v-model="customizeForm.adminRegionCode"
                :data="customizeAreaTreeData"
                :props="customizeAreaTreeProps"
                node-key="value"
                check-strictly
                filterable
                :filter-node-method="filterCustomizeAreaNode"
                clearable
                placeholder="请选择（可搜索）"
                class="select-long"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="管理单位">
              <el-input v-model="customizeForm.manageUnit" placeholder="请输入" maxlength="100" show-word-limit />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="home-map__customize-section">
          <div class="home-map__customize-section-head">
            <div class="home-map__customize-section-title">自定义属性</div>
            <el-button v-if="!customizeReadonly" size="small" type="primary" plain @click="addCustomizeAttrRow">
              新增属性
            </el-button>
          </div>
          <el-table :data="customizeAttrRows" size="small" border>
            <el-table-column label="键" width="220">
              <template #default="{ row }">
                <el-input v-if="!customizeReadonly" v-model="row.key" placeholder="例如：负责人" />
                <span v-else>{{ row.key || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="值">
              <template #default="{ row }">
                <el-input v-if="!customizeReadonly" v-model="row.value" placeholder="例如：张三" />
                <span v-else>{{ row.value || '-' }}</span>
              </template>
            </el-table-column>
            <el-table-column v-if="!customizeReadonly" label="操作" width="90" align="center">
              <template #default="{ $index }">
                <el-button link type="danger" @click="removeCustomizeAttrRow($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="home-map__customize-attr-tip">提示：键不能为空，键不可重复。</div>
        </div>

        <div class="home-map__customize-section">
          <div class="home-map__customize-section-title">图层位置</div>
          <TiandituGeoJsonPreview
            v-if="customizeReadonly"
            :geo-json="customizeForm.geometryGeoJson"
            :active="customizeDialogVisible"
            :height="360"
            :label-text="customizeForm.facilityName"
            :highlight="true"
          />
          <div v-else>
            <TiandituGeoJsonEditor
              v-model="customizeForm.geometryGeoJson"
              :active="customizeDialogVisible"
              :height="420"
              :draw-modes="['POINT', 'LINESTRING', 'POLYGON']"
            />
            <div class="home-map__customize-geo-tip">支持绘制点/线/面，保存后自动入库（SRID=4490）。</div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <div class="home-map__customize-footer">
          <el-button @click="customizeDialogVisible = false">关闭</el-button>
          <el-button v-if="customizeMode === 'view'" type="primary" plain @click="switchCustomizeToEdit">
            编辑
          </el-button>
          <el-button
            v-if="customizeMode !== 'view'"
            type="primary"
            :loading="customizeSubmitting"
            @click="submitCustomize"
          >
            保存
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="riverDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ riverDetailView?.riverName || '河道详情' }}
        </div>
      </template>
      <div v-loading="riverDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河道名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.riverName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">行政区划</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.adminRegion) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河道长度</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(riverDetailView?.lengthKm, 'km') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河道起点</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.startPoint) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河道终点</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.endPoint) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">流域面积</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(riverDetailView?.catchmentKm2, 'km²') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">所在流域</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.basinLabel) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河道类型</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.riverTypeLabel) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">生态类型</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.ecologyLabel) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河道级别</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.riverLevelLabel) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">划分河段</div>
              <div class="home-map__river-info-value">{{ riverSectionNamesText }}</div>
            </div>
            <div class="home-map__river-info-row home-map__river-info-row--full">
              <div class="home-map__river-info-label">河道概况</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverDetailView?.remarks) }}</div>
            </div>
          </div>
        </div>

        <div class="home-map__river-section">
          <div class="home-map__river-section-title">河长信息</div>
          <el-table :data="riverHeadList" size="small" stripe>
            <el-table-column prop="headName" label="河长姓名" width="90" />
            <el-table-column prop="referenceTypeLabel" label="关联设施" width="90" />
            <el-table-column prop="referenceName" label="关联设施名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="headLevelLabel" label="级别" width="90" />
            <el-table-column prop="headPosition" label="职务" min-width="160" show-overflow-tooltip />
            <el-table-column prop="headContact" label="联系电话" width="110" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="reservoirDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ reservoirDetailView?.reservoirName || '水库详情' }}
        </div>
      </template>
      <div v-loading="reservoirDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">水库名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(reservoirDetailView?.reservoirName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">规模</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(reservoirDetailView?.reservoirScaleLabel) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">管理单位</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(reservoirDetailView?.managementUnitLabel) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">水库性质</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(reservoirDetailView?.reservoirNatureLabel) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">总库容</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(reservoirDetailView?.totalCapacity, 'm³') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">兴利库容</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(reservoirDetailView?.activeCapacity, 'm³') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">死水位</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(reservoirDetailView?.deadLevel, 'm') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">行政区划</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(reservoirDetailView?.adminRegion) }}</div>
            </div>
          </div>
        </div>

        <div class="home-map__river-section">
          <div class="home-map__river-section-title">河长信息</div>
          <el-table :data="reservoirHeadList" size="small" stripe>
            <el-table-column prop="headName" label="河长姓名" width="90" />
            <el-table-column prop="referenceTypeLabel" label="关联设施" width="90" />
            <el-table-column prop="referenceName" label="关联设施名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="headLevelLabel" label="级别" width="90" />
            <el-table-column prop="headPosition" label="职务" min-width="160" show-overflow-tooltip />
            <el-table-column prop="headContact" label="联系电话" width="110" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="signboardDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ signboardDetailView?.signboardName || '公示牌详情' }}
        </div>
      </template>
      <div v-loading="signboardDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">公示牌名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.signboardName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">关联河道/河段</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.referenceText) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">具体位置</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.specificLocation) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">行政区划</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.adminRegion) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">维护单位</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.maintenanceUnit) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">责任人</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.responsiblePerson) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">管理单位</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.managementUnit) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">权属单位</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(signboardDetailView?.ownershipUnit) }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="pumpStationDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ pumpStationDetail?.pumpStationName || '泵站详情' }}
        </div>
      </template>
      <div v-loading="pumpStationDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">泵站名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(pumpStationDetail?.pumpStationName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">行政区划</div>
              <div class="home-map__river-info-value">{{ formatAdminRegionNames(pumpStationDetail?.divisionCode) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">泵站类型</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(pumpStationTypeLabelMap[String(pumpStationDetail?.pumpStationType || '')] || pumpStationDetail?.pumpStationType)
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">具体位置</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(pumpStationDetail?.pumpStationPosition) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">装机功率(KW)</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(pumpStationDetail?.installedCapacityKw, 'KW') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">装机流量(m³/s)</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(pumpStationDetail?.capacityFlow, 'm³/s') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">自排流量(m³/s)</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(pumpStationDetail?.selfFlow, 'm³/s') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">抽引流量(m³/s)</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(pumpStationDetail?.installedFlow, 'm³/s') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">抽排流量(m³/s)</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(pumpStationDetail?.pumpingFlow, 'm³/s') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">建设时间</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(pumpStationDetail?.constructionTime) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">工程等别</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(engineeringGradeLabelMap[String(pumpStationDetail?.engineeringGrade || '')] || pumpStationDetail?.engineeringGrade)
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">归口管理部门</div>
              <div class="home-map__river-info-value">
                {{ formatTextOrDash(formatManagementUnitLabels(pumpStationDetail?.managementDepartment)) }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">防洪设计标准</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(pumpStationDetail?.floodControlDesignStandard) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">闸站规模</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(pumpStationDetail?.engineeringScale) }}</div>
            </div>
            <div class="home-map__river-info-row home-map__river-info-row--full">
              <div class="home-map__river-info-label">泵站概览</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(pumpStationDetail?.pumpStationOverview) }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="embankmentDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ embankmentDetail?.embankmentName || '堤防详情' }}
        </div>
      </template>
      <div v-loading="embankmentDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">堤防名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(embankmentDetail?.embankmentName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">行政区划</div>
              <div class="home-map__river-info-value">{{ formatAdminRegionNames(embankmentDetail?.divisionCode) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河流岸别</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(
                    riverBankSideLabelMap[String(embankmentDetail?.riverBankSide || '')] || embankmentDetail?.riverBankSide
                  )
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">跨界情况</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(embankmentDetail?.crossBoundaryStatus) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">堤防类型</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(
                    embankmentTypeLabelMap[String(embankmentDetail?.embankmentType || '')] || embankmentDetail?.embankmentType
                  )
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">堤防形式</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(
                    embankmentFormLabelMap[String(embankmentDetail?.embankmentForm || '')] || embankmentDetail?.embankmentForm
                  )
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">堤防级别</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(
                    embankmentLevelLabelMap[String(embankmentDetail?.embankmentLevel || '')] || embankmentDetail?.embankmentLevel
                  )
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">堤防长度</div>
              <div class="home-map__river-info-value">{{ formatWithUnit(embankmentDetail?.lengthM, 'm') }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">归口管理部门</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(embankmentDetail?.managementDepartment) }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="floodMaterialWarehouseDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ floodMaterialWarehouseDetail?.warehouseName || '防汛物资仓库详情' }}
        </div>
      </template>
      <div v-loading="floodMaterialWarehouseDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">仓库名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(floodMaterialWarehouseDetail?.warehouseName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">行政区划</div>
              <div class="home-map__river-info-value">
                {{ formatAdminRegionNames(floodMaterialWarehouseDetail?.divisionCode) }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">具体位置</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(floodMaterialWarehouseDetail?.specificLocation) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">归属单位</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(floodMaterialWarehouseDetail?.belongUnit) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">负责人姓名</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(floodMaterialWarehouseDetail?.leaderName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">负责人电话</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(floodMaterialWarehouseDetail?.leaderPhone) }}</div>
            </div>
            <div class="home-map__river-info-row home-map__river-info-row--full">
              <div class="home-map__river-info-label">物资种类</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(floodMaterialWarehouseDetail?.materialType) }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="irrigationDistrictDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ irrigationDistrictDetail?.irrigationDistrictName || '灌区详情' }}
        </div>
      </template>
      <div v-loading="irrigationDistrictDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">灌区名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(irrigationDistrictDetail?.irrigationDistrictName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">行政区划</div>
              <div class="home-map__river-info-value">
                {{ formatAdminRegionNames(irrigationDistrictDetail?.divisionCode) }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">所在流域</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(
                    basinOptionsMap[String(irrigationDistrictDetail?.basinCode || '')] || irrigationDistrictDetail?.basinCode
                  )
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">灌区类型</div>
              <div class="home-map__river-info-value">
                {{
                  formatTextOrDash(
                    irrigationDistrictTypeLabelMap[String(irrigationDistrictDetail?.irrigationDistrictType || '')] ||
                    irrigationDistrictDetail?.irrigationDistrictType
                  )
                }}
              </div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">实际空间面积</div>
              <div class="home-map__river-info-value">{{ formatKm2(irrigationDistrictDetail?.actualIrrigableArea) }}km²</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">基本农田面积</div>
              <div class="home-map__river-info-value">{{ formatKm2(irrigationDistrictDetail?.basicFarmlandAreaKm2) }}km²</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">生态红线</div>
              <div class="home-map__river-info-value">{{ formatYesNo(irrigationDistrictDetail?.isEcologicalRedLine) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">开发边界</div>
              <div class="home-map__river-info-value">{{ formatYesNo(irrigationDistrictDetail?.isDevelopmentBoundary) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">管理单位</div>
              <div class="home-map__river-info-value">
                {{ formatTextOrDash(formatManagementUnitLabels(irrigationDistrictDetail?.managementUnit)) }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="pondDetailVisible"
      width="400px"
      top="0"
      :destroy-on-close="true"
      :modal="false"
      :modal-penetrable="true"
      modal-class="home-map__pond-detail-overlay"
      :append-to-body="true"
      :close-on-click-modal="false"
      :lock-scroll="false"
      class="home-map__detail-dialog home-map__pond-detail-dialog"
    >
      <template #header>
        <div class="home-map__pond-detail-dialog-title">坑塘信息</div>
      </template>
      <div v-loading="pondDetailLoading" class="home-map__pond-detail-body">
        <div class="home-map__pond-detail-brief">
          <div class="home-map__pond-detail-name">{{ pondDetailTitle }}</div>
          <div v-if="pondDetailCode" class="home-map__pond-detail-code">{{ pondDetailCode }}</div>
          <div v-if="pondDetailTags.length" class="home-map__pond-detail-tags">
            <span v-for="tag in pondDetailTags" :key="tag.text" class="home-map__pond-detail-tag">{{ tag.text }}</span>
          </div>
        </div>
        <div class="home-map__pond-detail-stats">
          <div class="home-map__pond-detail-stat">
            <span class="home-map__pond-detail-stat-val">{{ formatNumber(pondDetail?.areaSqm) }}</span>
            <span class="home-map__pond-detail-stat-unit">㎡</span>
          </div>
          <div class="home-map__pond-detail-stat">
            <span class="home-map__pond-detail-stat-val">{{ formatNumber(pondDetail?.areaMu) }}</span>
            <span class="home-map__pond-detail-stat-unit">亩</span>
          </div>
        </div>
        <div class="home-map__river-info-grid">
          <div class="home-map__river-info-row">
            <div class="home-map__river-info-label">行政区划</div>
            <div class="home-map__river-info-value">{{ formatPondAreaLabel(pondDetail) }}</div>
          </div>
          <div class="home-map__river-info-row">
            <div class="home-map__river-info-label">坐落位置</div>
            <div class="home-map__river-info-value">{{ formatTextOrDash(pondDetail?.locationDesc) }}</div>
          </div>
          <div class="home-map__river-info-row">
            <div class="home-map__river-info-label">土地权属</div>
            <div class="home-map__river-info-value">{{ formatTextOrDash(pondDetail?.ownershipType) }}</div>
          </div>
          <div class="home-map__river-info-row">
            <div class="home-map__river-info-label">权属单位</div>
            <div class="home-map__river-info-value">{{ formatTextOrDash(pondDetail?.ownerUnit) }}</div>
          </div>
          <div class="home-map__river-info-row">
            <div class="home-map__river-info-label">使用状态</div>
            <div class="home-map__river-info-value">{{ formatTextOrDash(pondDetail?.usageStatus) }}</div>
          </div>
          <div class="home-map__river-info-row">
            <div class="home-map__river-info-label">资源类型</div>
            <div class="home-map__river-info-value">{{ formatTextOrDash(pondDetail?.resourceType) }}</div>
          </div>
        </div>
        <div class="home-map__pond-detail-boundary">
          <div
            v-for="item in pondBoundaryItems"
            :key="item.key"
            class="home-map__pond-detail-boundary-item"
          >
            <div class="home-map__pond-detail-boundary-badge">{{ item.dir }}</div>
            <div class="home-map__pond-detail-boundary-content">
              <div class="home-map__pond-detail-boundary-label">{{ item.label }}</div>
              <div class="home-map__pond-detail-boundary-val">{{ item.value }}</div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog
      v-model="riverSectionDetailVisible"
      width="520px"
      :destroy-on-close="true"
      align-center
      class="home-map__detail-dialog"
    >
      <template #header>
        <div class="home-map__river-dialog-title">
          {{ riverSectionDetail?.sectionName || '河段详情' }}
        </div>
      </template>
      <div v-loading="riverSectionDetailLoading" class="home-map__river-dialog-body">
        <div class="home-map__river-section">
          <div class="home-map__river-section-title">基础信息</div>
          <div class="home-map__river-info-grid">
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">河段名称</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverSectionDetail?.sectionName) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">起点</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverSectionDetail?.startPoint) }}</div>
            </div>
            <div class="home-map__river-info-row">
              <div class="home-map__river-info-label">终点</div>
              <div class="home-map__river-info-value">{{ formatTextOrDash(riverSectionDetail?.endPoint) }}</div>
            </div>
          </div>
        </div>

        <div class="home-map__river-section">
          <div class="home-map__river-section-title">河长信息</div>
          <el-table :data="riverSectionHeadList" size="small" stripe>
            <el-table-column prop="headName" label="河长姓名" width="90" />
            <el-table-column prop="referenceTypeLabel" label="关联设施" width="90" />
            <el-table-column prop="referenceName" label="关联设施名称" min-width="140" show-overflow-tooltip />
            <el-table-column prop="headLevelLabel" label="级别" width="90" />
            <el-table-column prop="headPosition" label="职务" min-width="160" show-overflow-tooltip />
            <el-table-column prop="headContact" label="联系电话" width="110" />
          </el-table>
        </div>
      </div>
    </el-dialog>

    <div class="home-map__feedback-entry" @click="openFeedbackDialog">
      <img :src="feedbackIconUrl" alt="公众反馈" />
    </div>

    <div v-if="mapLoading" class="home-map__loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span class="home-map__loading-text">地图加载中...</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { ArrowDown, ArrowRight, Close, Fold, Expand, Loading } from '@element-plus/icons-vue'
import { getArea, getAreaTree, type AreaNodeRespVO, type AreaRespVO } from '@/api/system/area'
import { getTiandituKey } from '@/components/Gis/tiandituKey'
import { createTiandituImageryBaseLayer, createTiandituVectorBaseLayer } from '@/components/Gis/tiandituLayers'
import TiandituGeoJsonEditor from '@/components/Gis/TiandituGeoJsonEditor.vue'
import TiandituGeoJsonPreview from '@/components/Gis/TiandituGeoJsonPreview.vue'
import { Echart } from '@/components/Echart'
import type { EChartsOption } from 'echarts'
import { dateUtil, formatToDateTime } from '@/utils/dateUtil'
import {
  createCustomizeWaterFacility,
  getWaterFacilityDict,
  getWaterFacilityDictForLabel,
  getWaterFacilityDetail,
  updateWaterFacility,
  updateWaterFacilityGeometry,
  type WaterFacilityDetailRespVO
} from '@/api/gis/waterFacility'
import {
  getRiverDict,
  getRiverChannelDetail,
  getRiverChannelDetailByFacility,
  getRiverSectionDetailByFacility,
  getRiverManagement,
  type RiverChannelDetailRespVO,
  type RiverManagementSectionDetailVO,
  type RiverSectionDetailRespVO
} from '@/api/gis/riverChannel'
import {
  getWaterFacilityAreaCount,
  getRiverAreaOverview,
  getRiverAreaOverviewByArea,
  getWaterFacilityListByArea,
  getWaterFacilityListByName,
  getEmbankmentAreaOverviewByArea,
  getFloodMaterialWarehouseAreaOverviewByArea,
  getIrrigationDistrictAreaOverviewByArea,
  getWaterPondAreaOverviewByArea,
  getPumpStationAreaOverviewByArea,
  getReservoirAreaOverviewByArea,
  getSignboardAreaOverviewByArea,
  getSignboardReferenceDetail,
  type EmbankmentAreaOverviewItemRespVO,
  type EmbankmentAreaOverviewRespVO,
  type FloodMaterialWarehouseAreaOverviewItemRespVO,
  type FloodMaterialWarehouseAreaOverviewRespVO,
  type IrrigationDistrictAreaOverviewItemRespVO,
  type IrrigationDistrictAreaOverviewRespVO,
  type WaterPondAreaOverviewItemRespVO,
  type WaterPondAreaOverviewRespVO,
  type WaterPondMapOverviewReqVO,
  type PumpStationAreaOverviewItemRespVO,
  type PumpStationAreaOverviewRespVO,
  type ReservoirAreaOverviewItemRespVO,
  type ReservoirAreaOverviewRespVO,
  type RiverAreaOverviewItemRespVO,
  type RiverAreaOverviewRespVO,
  type SignboardAreaOverviewItemRespVO,
  type SignboardAreaOverviewRespVO,
  type SignboardReferenceDetailRespVO,
  type WaterFacilityMapItemRespVO
} from '@/api/home/homeMap'
import { createBufferQuery, type GisBufferQueryFacilityRespVO, type GisBufferQueryRespVO } from '@/api/gis/bufferQuery'
import {
  getReservoirDetail,
  getReservoirDetailByFacility,
  getReservoirManagement,
  getReservoirPage,
  type ReservoirHeadItemReqVO,
  type ReservoirPageRespVO,
  type ReservoirSaveReqVO
} from '@/api/gis/reservoir'
import {
  getPumpStationDetail,
  getPumpStationDetailByFacility,
  getPumpStationPage,
  type PumpStationPageRespVO,
  type PumpStationSaveReqVO
} from '@/api/gis/pumpStation'
import { getEmbankmentDetail, getEmbankmentPage, type EmbankmentPageRespVO, type EmbankmentSaveReqVO } from '@/api/gis/embankment'
import {
  getFloodMaterialWarehouseDetail,
  getFloodMaterialWarehousePage,
  type FloodMaterialWarehousePageRespVO,
  type FloodMaterialWarehouseSaveReqVO
} from '@/api/gis/floodMaterialWarehouse'
import {
  getIrrigationDistrictDetail,
  getIrrigationDistrictDetailByFacility,
  getIrrigationDistrictPage,
  type IrrigationDistrictPageRespVO,
  type IrrigationDistrictSaveReqVO
} from '@/api/gis/irrigationDistrict'
import { getWaterPondFilterOptions, getWaterPondDetail, getWaterPondPage, type WaterPondPageRespVO, type WaterPondSaveReqVO } from '@/api/gis/waterPond'
import { getSignboardDetail, getSignboardPage, type SignboardPageRespVO, type SignboardSaveReqVO } from '@/api/gis/signboard'
import {
  getProblemFeedbackDetail,
  getProblemFeedbackScreenList,
  getProblemFeedbackSpatialAnalysis,
  type ProblemFeedbackDetailRespVO,
  type ProblemFeedbackListReqVO,
  type ProblemFeedbackPageRespVO,
  type ProblemFeedbackSpatialAnalysisRespVO
} from '@/api/yz/problemFeedback'

defineOptions({ name: 'Home' })

type DictOption = { label: string; value: string }
type FlatArea = { id: number; name: string }
type CustomizeAttrRow = { key: string; value: string }
type TreeSelectNode = { label: string; value: string; children?: TreeSelectNode[] }
type HomeAreaTreeNode = { id: number; name: string; children?: HomeAreaTreeNode[] }
type HeadDisplayItem = {
  headName: string
  headLevelLabel: string
  headPosition: string
  headContact: string
  referenceTypeLabel: string
  referenceName: string
}

const mapRef = ref<HTMLDivElement>()
const mapLoading = ref(true)
const mapBaseLayerType = ref<'imagery' | 'vector'>('imagery')

const facilityTypeOptions = ref<DictOption[]>([])
const facilityTypeLabelOptions = ref<DictOption[]>([])
const selectedFacilityType = ref<string>('')
const areaTree = ref<AreaNodeRespVO[]>([])
const areaCountMap = ref<Record<number, number>>({})
const areaKeyword = ref<string>('')
const facilityNameKeyword = ref<string>('')
const facilitySearchLoading = ref(false)
const facilityListCollapsed = ref(false)
const areaListCollapsed = ref(false)
const areaTreeRef = ref<any>()
const panelHiddenByUser = ref(false)
const bufferFacilityListHidden = computed(() => bufferPicking.value || panelHiddenByUser.value)

const drawerVisible = ref(false)
const selectedArea = ref<FlatArea | null>(null)
const facilityList = ref<WaterFacilityMapItemRespVO[]>([])

const facilityIconNameMap: Record<string, string> = {
  pump_station: 'pump_station',
  dike: 'dike',
  flood_prevention_material: 'flood_material',
  signboard: 'signboard',
  public_notice: 'signboard',
  publicnotice: 'signboard',
  irrigation: 'irrigation',
  river: 'river',
  river_channel: 'river',
  riverchannel: 'river',
  reservoir: 'reservoir',
  customize: 'customize'
}

const facilityIconLabelMap: Record<string, string> = {
  泵站: 'pump_station',
  堤防: 'dike',
  防汛物资: 'flood_material',
  公示牌: 'signboard',
  灌区: 'irrigation',
  河道: 'river',
  水库: 'reservoir',
  自定义: 'customize'
}

const facilityDefaultIconUrl = `${import.meta.env.BASE_URL}icon/facility_default@2x.png`

const resolveFacilityIconName = (item: DictOption) => {
  const value = String(item?.value || '').trim().toLowerCase()
  if (value && facilityIconNameMap[value]) return facilityIconNameMap[value]
  const label = String(item?.label || '').trim()
  return facilityIconLabelMap[label] || ''
}

const buildFacilityIconUrl = (item: DictOption, isActive: boolean) => {
  const name = resolveFacilityIconName(item)
  if (!name) return ''
  const status = isActive ? 'selected' : 'default'
  return `${import.meta.env.BASE_URL}icon/${name}_${status}@2x.png`
}

const handleFacilityIconError = (event: Event) => {
  const target = event.target as HTMLImageElement | null
  if (!target) return
  if (target.dataset.fallbackApplied === '1') return
  target.dataset.fallbackApplied = '1'
  target.src = facilityDefaultIconUrl
}

const hideHomePanel = () => {
  panelHiddenByUser.value = true
}

const showHomePanel = () => {
  panelHiddenByUser.value = false
}
const drawerMode = ref<'area' | 'search'>('area')
const riverOverview = ref<RiverAreaOverviewRespVO | null>(null)
const riverSearchList = ref<RiverAreaOverviewItemRespVO[]>([])
const reservoirOverview = ref<ReservoirAreaOverviewRespVO | null>(null)
const signboardOverview = ref<SignboardAreaOverviewRespVO | null>(null)
const pumpStationOverview = ref<PumpStationAreaOverviewRespVO | null>(null)
const embankmentOverview = ref<EmbankmentAreaOverviewRespVO | null>(null)
const floodMaterialWarehouseOverview = ref<FloodMaterialWarehouseAreaOverviewRespVO | null>(null)
const irrigationDistrictOverview = ref<IrrigationDistrictAreaOverviewRespVO | null>(null)
const pondOverview = ref<WaterPondAreaOverviewRespVO | null>(null)
const pondFilterQuery = ref<WaterPondMapOverviewReqVO>({})
const pondOwnershipTypeOptions = ref<string[]>([])
const pondResourceTypeOptions = ref<string[]>([])
const pondUsageStatusOptions = ref<string[]>([])
const pondList = ref<WaterPondPageRespVO[]>([])
const pondListTotal = ref(0)
const pondListPageNo = ref(1)
const pondListPageSize = 15
const pondListLoading = ref(false)
const pondDetailVisible = ref(false)
const pondDetailLoading = ref(false)
const pondDetail = ref<WaterPondSaveReqVO | null>(null)
const riverLevelOptions = ref<DictOption[]>([])
const basinOptions = ref<DictOption[]>([])
const riverTypeOptions = ref<DictOption[]>([])
const ecologyOptions = ref<DictOption[]>([])
const headLevelOptions = ref<DictOption[]>([])
const reservoirScaleOptions = ref<DictOption[]>([])
const reservoirNatureOptions = ref<DictOption[]>([])
const reservoirManagementUnitOptions = ref<DictOption[]>([])
const signboardMaintenanceUnitOptions = ref<DictOption[]>([])
const signboardManagementUnitOptions = ref<DictOption[]>([])
const signboardOwnershipUnitOptions = ref<DictOption[]>([])
const pumpStationTypeOptions = ref<DictOption[]>([])
const engineeringGradeOptions = ref<DictOption[]>([])
const embankmentFormOptions = ref<DictOption[]>([])
const embankmentTypeOptions = ref<DictOption[]>([])
const embankmentLevelOptions = ref<DictOption[]>([])
const riverBankSideOptions = ref<DictOption[]>([])

const riverDetailVisible = ref(false)
const riverDetailLoading = ref(false)
const riverDetail = ref<RiverChannelDetailRespVO | null>(null)
const riverHeadList = ref<HeadDisplayItem[]>([])
const riverSectionDetailVisible = ref(false)
const riverSectionDetailLoading = ref(false)
const riverSectionDetail = ref<RiverSectionDetailRespVO | null>(null)
const riverSectionHeadList = ref<HeadDisplayItem[]>([])

const reservoirDetailVisible = ref(false)
const reservoirDetailLoading = ref(false)
const reservoirDetail = ref<ReservoirSaveReqVO | null>(null)
const reservoirHeadList = ref<HeadDisplayItem[]>([])

const signboardDetailVisible = ref(false)
const signboardDetailLoading = ref(false)
const signboardDetail = ref<any | null>(null)

const pumpStationDetailVisible = ref(false)
const pumpStationDetailLoading = ref(false)
const pumpStationDetail = ref<PumpStationSaveReqVO | null>(null)

const embankmentDetailVisible = ref(false)
const embankmentDetailLoading = ref(false)
const embankmentDetail = ref<EmbankmentSaveReqVO | null>(null)

const floodMaterialWarehouseDetailVisible = ref(false)
const floodMaterialWarehouseDetailLoading = ref(false)
const floodMaterialWarehouseDetail = ref<FloodMaterialWarehouseSaveReqVO | null>(null)

const irrigationDistrictDetailVisible = ref(false)
const irrigationDistrictDetailLoading = ref(false)
const irrigationDistrictDetail = ref<IrrigationDistrictSaveReqVO | null>(null)

// region 缓冲区查询
const bufferDialogVisible = ref(false)
const bufferDialogRestore = ref(false)
const bufferResultVisible = ref(false)
const bufferQueryLoading = ref(false)
const bufferRadius = ref<string>('')
const bufferFacilityTypes = ref<string[]>([])
const bufferSelectAll = ref(false)
const bufferCenterPoint = ref<{ lat: number; lng: number } | null>(null)
const bufferPicking = ref(false)
const bufferResult = ref<GisBufferQueryRespVO | null>(null)
const bufferGeoJsonData = ref<any>(null)
const bufferExtraFacilities = ref<GisBufferQueryFacilityRespVO[]>([])
const bufferFacilityPageNo = ref(1)
const bufferFacilityPageSize = ref(10)
const bufferObjectFilter = ref<string>('')

const bufferFacilityDetailVisible = ref(false)
const bufferFacilityDetailLoading = ref(false)
const bufferFacilityDetail = ref<WaterFacilityDetailRespVO | null>(null)
const bufferFacilityDialogStyle = {
  background: 'transparent',
  boxShadow: 'none',
  padding: '0'
}
const bufferFacilityAdminRegionText = computed(() => {
  const detail = bufferFacilityDetail.value as any
  if (!detail) return '-'
  return formatAdminRegionNames(
    detail.adminRegionCode || detail.adminRegion || detail.divisionCode || detail.township || detail.town
  )
})

const yizhengBoundaryGeoJson = ref<any>(null)
// endregion

// region 公众反馈
const feedbackDialogVisible = ref(false)
const feedbackTypeOptions = ref<DictOption[]>([])
const feedbackStatusOptions = ref<DictOption[]>([])
const feedbackTypeValues = ref<string[]>([])
const feedbackStatusValues = ref<string[]>([])
const feedbackTypeAllChecked = ref(false)
const feedbackStatusAllChecked = ref(false)
const feedbackTypeAllIndeterminate = ref(false)
const feedbackStatusAllIndeterminate = ref(false)
const feedbackTypeAllValues = computed(() => feedbackTypeOptions.value.map((item) => String(item.value)))
const feedbackStatusAllValues = computed(() => feedbackStatusOptions.value.map((item) => String(item.value)))
const feedbackIconUrl = `${import.meta.env.BASE_URL}issue/problem.png`
const feedbackIssueIconUrl = `${import.meta.env.BASE_URL}issue/issue.png`
const feedbackList = ref<ProblemFeedbackPageRespVO[]>([])
const feedbackListLoading = ref(false)
// 避免筛选频繁切换导致接口响应乱序
const feedbackQueryToken = ref(0)
const feedbackClusterVisible = ref(false)
const feedbackClusterList = ref<ProblemFeedbackPageRespVO[]>([])
const feedbackMarkerCount = ref(0)
const feedbackDetailVisible = ref(false)
const feedbackDetailLoading = ref(false)
const feedbackDetail = ref<ProblemFeedbackDetailRespVO | null>(null)
const feedbackFacilityTypeLabel = ref('-')
const feedbackFacilityName = ref('-')
// endregion

// region 问题空间分析
const spatialAnalysisDialogVisible = ref(false)
const spatialAnalysisResultVisible = ref(false)
const spatialAnalysisLoading = ref(false)
const spatialAnalysisRadius = ref<number | null>(null)
const spatialAnalysisPoint = ref<{ longitude: number; latitude: number } | null>(null)
const spatialAnalysisResult = ref<ProblemFeedbackSpatialAnalysisRespVO | null>(null)
const spatialAnalysisChartKey = ref(0)
const analysisChiefPageNo = ref(1)
const analysisChiefPageSize = ref(10)
const feedbackDetailPoint = ref<{ longitude: number; latitude: number } | null>(null)
const feedbackClusterPoint = ref<{ longitude: number; latitude: number } | null>(null)
// endregion

//region 自定义图层（facilityType=customize）
const customizeDialogVisible = ref(false)
const customizeMode = ref<'create' | 'edit' | 'view'>('create')
const customizeSubmitting = ref(false)
const customizeForm = reactive<{
  id: string
  facilityName: string
  adminRegionCode: string
  manageUnit: string
  geometryGeoJson: string
}>({
  id: '',
  facilityName: '',
  adminRegionCode: '',
  manageUnit: '',
  geometryGeoJson: ''
})

const customizeAttrRows = ref<CustomizeAttrRow[]>([])

const customizeReadonly = computed(() => customizeMode.value === 'view')
const customizeDialogTitle = computed(() => {
  if (customizeMode.value === 'create') return '新增自定义图层'
  if (customizeMode.value === 'edit') return '编辑自定义图层'
  return '自定义图层详情'
})

const customizeAreaTreeProps = {
  label: 'label',
  value: 'value',
  children: 'children'
}

const filterCustomizeAreaNode = (keyword: string, data: any) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  return String(data?.label || '').includes(k)
}

const customizeAreaNameMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  const walk = (nodes?: AreaNodeRespVO[]) => {
    if (!nodes || nodes.length === 0) return
    for (const node of nodes) {
      if (!node) continue
      map[String(node.id)] = String(node.name || '')
      if (node.children && node.children.length > 0) walk(node.children)
    }
  }
  walk(areaTree.value)
  return map
})

const systemAreaNameMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  const walk = (nodes?: AreaNodeRespVO[]) => {
    if (!nodes || nodes.length === 0) return
    for (const node of nodes) {
      if (!node) continue
      map[String(node.id)] = String(node.name || '')
      if (node.children && node.children.length > 0) walk(node.children)
    }
  }
  walk(areaTree.value)
  return map
})

const customizeAreaTreeData = computed<TreeSelectNode[]>(() => {
  const build = (nodes?: AreaNodeRespVO[]): TreeSelectNode[] => {
    if (!nodes || nodes.length === 0) return []
    return nodes
      .filter((n) => !!n)
      .map((node) => ({
        label: String(node.name || ''),
        value: String(node.id),
        children: build(node.children)
      }))
  }
  return build(areaTree.value)
})

const resetCustomizeForm = () => {
  customizeForm.id = ''
  customizeForm.facilityName = ''
  customizeForm.adminRegionCode = ''
  customizeForm.manageUnit = ''
  customizeForm.geometryGeoJson = ''
  customizeAttrRows.value = []
}

const addCustomizeAttrRow = () => {
  customizeAttrRows.value = [...customizeAttrRows.value, { key: '', value: '' }]
}

const removeCustomizeAttrRow = (index: number) => {
  const list = [...customizeAttrRows.value]
  list.splice(index, 1)
  customizeAttrRows.value = list
}

const buildCustomizeAttributes = () => {
  const rows = customizeAttrRows.value || []
  const result: Record<string, any> = {}
  for (const row of rows) {
    const k = String(row?.key || '').trim()
    if (!k) continue
    result[k] = String(row?.value ?? '').trim()
  }
  return result
}

const validateCustomize = () => {
  const name = String(customizeForm.facilityName || '').trim()
  if (!name) {
    ElMessage.warning('请输入图层名称')
    return false
  }
  const areaCode = String(customizeForm.adminRegionCode || '').trim()
  if (!areaCode) {
    ElMessage.warning('请选择行政区划')
    return false
  }
  const geo = String(customizeForm.geometryGeoJson || '').trim()
  if (!geo) {
    ElMessage.warning('请绘制点/线/面后再保存')
    return false
  }
  const keySet = new Set<string>()
  for (const row of customizeAttrRows.value || []) {
    const k = String(row?.key || '').trim()
    if (!k) continue
    if (keySet.has(k)) {
      ElMessage.warning(`属性键重复：${k}`)
      return false
    }
    keySet.add(k)
  }
  return true
}

const openCustomizeCreateDialog = () => {
  resetCustomizeForm()
  customizeMode.value = 'create'
  customizeDialogVisible.value = true
  // 默认给一行，降低录入成本
  addCustomizeAttrRow()
}

const fillCustomizeFormFromDetail = (id: string, detail: WaterFacilityDetailRespVO) => {
  customizeForm.id = id
  customizeForm.facilityName = String(detail?.facilityName || '')
  customizeForm.adminRegionCode = String(detail?.adminRegionCode || '')
  customizeForm.manageUnit = String(detail?.manageUnit || '')
  customizeForm.geometryGeoJson = String(detail?.geometryGeoJson || '')

  const attrs = (detail?.attributes || {}) as Record<string, any>
  const rows: CustomizeAttrRow[] = Object.keys(attrs).map((k) => ({
    key: k,
    value: attrs[k] == null ? '' : String(attrs[k])
  }))
  customizeAttrRows.value = rows.length ? rows : [{ key: '', value: '' }]
}

const openCustomizeDialog = async (
  row: WaterFacilityMapItemRespVO,
  mode: 'view' | 'edit',
  detailFromHighlight?: any
) => {
  const id = normalizeId(row?.id)
  if (!id) return

  resetCustomizeForm()
  customizeMode.value = mode
  customizeDialogVisible.value = true

  const detail = (detailFromHighlight || (await getWaterFacilityDetail(id))) as any
  if (!detail) {
    ElMessage.error('获取自定义图层详情失败')
    return
  }
  fillCustomizeFormFromDetail(id, detail as WaterFacilityDetailRespVO)
  // 展示时同步高亮（避免用户找不到图层）
  clearHighlight()
  renderHighlightFromGeoJson(String(detail?.geometryGeoJson || ''), String(detail?.facilityName || ''))
}

const switchCustomizeToEdit = () => {
  customizeMode.value = 'edit'
}

const submitCustomize = async () => {
  if (!validateCustomize()) return
  customizeSubmitting.value = true
  try {
    const adminRegionCode = String(customizeForm.adminRegionCode || '').trim()
    const payload = {
      facilityName: String(customizeForm.facilityName || '').trim(),
      adminRegionCode,
      adminRegion: customizeAreaNameMap.value[adminRegionCode] || '',
      manageUnit: String(customizeForm.manageUnit || '').trim(),
      attributes: buildCustomizeAttributes(),
      geometryGeoJson: String(customizeForm.geometryGeoJson || '').trim()
    }

    if (customizeMode.value === 'create') {
      const id = await createCustomizeWaterFacility(payload as any)
      ElMessage.success('新增成功')
      customizeDialogVisible.value = false
      // 新增后若已选择行政区域，则刷新列表与地图点位
      if (selectedArea.value && selectedFacilityType.value.trim().toLowerCase() === 'customize') {
        await openAreaDrawer(selectedArea.value)
      }
      if (id) {
        await handleFacilityClick(String(id), payload.facilityName)
      }
      return
    }

    const id = normalizeId(customizeForm.id)
    if (!id) return
    await Promise.all([
      updateWaterFacility({
        id,
        facilityName: payload.facilityName,
        facilityType: 'customize',
        adminRegion: payload.adminRegion,
        adminRegionCode: payload.adminRegionCode,
        manageUnit: payload.manageUnit,
        attributes: payload.attributes
      } as any),
      updateWaterFacilityGeometry(id, { geometryGeoJson: payload.geometryGeoJson, srid: 4490 })
    ])
    ElMessage.success('保存成功')
    customizeDialogVisible.value = false
    if (selectedArea.value && selectedFacilityType.value.trim().toLowerCase() === 'customize') {
      await openAreaDrawer(selectedArea.value)
    }
  } finally {
    customizeSubmitting.value = false
  }
}
//endregion

const selectedFacilityLabel = computed(() => {
  const hit = facilityTypeOptions.value.find((it) => it.value === selectedFacilityType.value)
  return hit?.label || ''
})

const drawerTitleMain = computed(() => {
  if (drawerMode.value === 'search') return '搜索结果'
  return selectedArea.value?.name || '详情'
})

const drawerCount = computed(() => {
  if (showRiverSearchOverview.value) return riverSearchList.value.length
  if (showReservoirSearchList.value) return reservoirOverview.value?.list?.length ?? 0
  if (showSignboardSearchList.value) return signboardOverview.value?.list?.length ?? 0
  if (showPumpStationSearchList.value) return pumpStationOverview.value?.list?.length ?? 0
  if (showEmbankmentSearchList.value) return embankmentOverview.value?.list?.length ?? 0
  if (showFloodMaterialWarehouseSearchList.value) return floodMaterialWarehouseOverview.value?.list?.length ?? 0
  if (showIrrigationSearchList.value) return irrigationDistrictOverview.value?.list?.length ?? 0
  if (showPondSearchList.value) return pondListTotal.value
  if (drawerMode.value === 'search') return facilityList.value.length
  if (showRiverOverview.value) return Number(riverOverview.value?.totalCount ?? 0)
  if (showPumpStationOverview.value) return Number(pumpStationOverview.value?.totalCount ?? 0)
  if (showReservoirOverview.value) return Number(reservoirOverview.value?.totalCount ?? 0)
  if (showSignboardOverview.value) return Number(signboardOverview.value?.totalCount ?? 0)
  if (showEmbankmentOverview.value) return Number(embankmentOverview.value?.totalCount ?? 0)
  if (showFloodMaterialWarehouseOverview.value)
    return Number(floodMaterialWarehouseOverview.value?.totalCount ?? 0)
  if (showIrrigationOverview.value) return Number(irrigationDistrictOverview.value?.totalCount ?? 0)
  if (showPondOverview.value) return Number(pondOverview.value?.totalCount ?? pondListTotal.value)
  return facilityList.value.length
})

const isRiverSelected = computed(() => {
  const val = selectedFacilityType.value.trim().toLowerCase()
  return val === 'river' || val === 'river_channel' || val === 'riverchannel'
})

const isReservoirSelected = computed(() => {
  return selectedFacilityType.value.trim().toLowerCase() === 'reservoir'
})

const isSignboardSelected = computed(() => {
  const val = selectedFacilityType.value.trim().toLowerCase()
  return val === 'signboard' || val === 'public_notice' || val === 'publicnotice'
})

const isPumpStationSelected = computed(() => {
  return selectedFacilityType.value.trim().toLowerCase() === 'pump_station'
})

const isEmbankmentSelected = computed(() => {
  return selectedFacilityType.value.trim().toLowerCase() === 'dike'
})

const isFloodMaterialSelected = computed(() => {
  return selectedFacilityType.value.trim().toLowerCase() === 'flood_prevention_material'
})

const isIrrigationSelected = computed(() => {
  return selectedFacilityType.value.trim().toLowerCase() === 'irrigation'
})

const isPondSelected = computed(() => {
  return selectedFacilityType.value.trim().toLowerCase() === 'pond'
})

const isCustomizeSelected = computed(() => {
  return selectedFacilityType.value.trim().toLowerCase() === 'customize'
})

const showRiverOverview = computed(() => {
  return isRiverSelected.value && drawerMode.value === 'area'
})

const showRiverSearchOverview = computed(() => {
  return isRiverSelected.value && drawerMode.value === 'search'
})

const showReservoirSearchList = computed(() => {
  return isReservoirSelected.value && drawerMode.value === 'search'
})

const showReservoirOverview = computed(() => {
  return isReservoirSelected.value && drawerMode.value === 'area'
})

const showSignboardSearchList = computed(() => {
  return isSignboardSelected.value && drawerMode.value === 'search'
})

const showSignboardOverview = computed(() => {
  return isSignboardSelected.value && drawerMode.value === 'area'
})

const showPumpStationSearchList = computed(() => {
  return isPumpStationSelected.value && drawerMode.value === 'search'
})

const showPumpStationOverview = computed(() => {
  return isPumpStationSelected.value && drawerMode.value === 'area'
})

const showEmbankmentSearchList = computed(() => {
  return isEmbankmentSelected.value && drawerMode.value === 'search'
})

const showEmbankmentOverview = computed(() => {
  return isEmbankmentSelected.value && drawerMode.value === 'area'
})

const showFloodMaterialWarehouseSearchList = computed(() => {
  return isFloodMaterialSelected.value && drawerMode.value === 'search'
})

const showFloodMaterialWarehouseOverview = computed(() => {
  return isFloodMaterialSelected.value && drawerMode.value === 'area'
})

const showIrrigationSearchList = computed(() => {
  return isIrrigationSelected.value && drawerMode.value === 'search'
})

const showIrrigationOverview = computed(() => {
  return isIrrigationSelected.value && drawerMode.value === 'area'
})

const showPondSearchList = computed(() => {
  return isPondSelected.value && drawerMode.value === 'search'
})

const showPondOverview = computed(() => {
  return isPondSelected.value && drawerMode.value === 'area'
})

const reservoirScaleLabelMap = computed<Record<string, string>>(() => buildDictMap(reservoirScaleOptions.value))
const reservoirNatureLabelMap = computed<Record<string, string>>(() => buildDictMap(reservoirNatureOptions.value))
const reservoirManagementUnitLabelMap = computed<Record<string, string>>(() =>
  buildDictMap(reservoirManagementUnitOptions.value)
)
const signboardMaintenanceUnitLabelMap = computed<Record<string, string>>(() =>
  buildDictMap(signboardMaintenanceUnitOptions.value)
)
const signboardManagementUnitLabelMap = computed<Record<string, string>>(() =>
  buildDictMap(signboardManagementUnitOptions.value)
)
const signboardOwnershipUnitLabelMap = computed<Record<string, string>>(() =>
  buildDictMap(signboardOwnershipUnitOptions.value)
)
const pumpStationTypeLabelMap = computed<Record<string, string>>(() => buildDictMap(pumpStationTypeOptions.value))
const engineeringGradeLabelMap = computed<Record<string, string>>(() => buildDictMap(engineeringGradeOptions.value))
const embankmentFormLabelMap = computed<Record<string, string>>(() => buildDictMap(embankmentFormOptions.value))
const embankmentTypeLabelMap = computed<Record<string, string>>(() => buildDictMap(embankmentTypeOptions.value))
const embankmentLevelLabelMap = computed<Record<string, string>>(() => buildDictMap(embankmentLevelOptions.value))
const riverBankSideLabelMap = computed<Record<string, string>>(() => buildDictMap(riverBankSideOptions.value))
const basinOptionsMap = computed<Record<string, string>>(() => buildDictMap(basinOptions.value))

const irrigationDistrictTypeOptions = ref<DictOption[]>([])
const irrigationDistrictTypeLabelMap = computed<Record<string, string>>(() => buildDictMap(irrigationDistrictTypeOptions.value))
const facilityTypeLabelMap = computed<Record<string, string>>(() => {
  const map: Record<string, string> = {}
  for (const item of facilityTypeLabelOptions.value || []) {
    if (!item?.value) continue
    const key = String(item.value || '').trim()
    if (!key) continue
    const label = String(item.label || '').trim()
    map[key] = label
    map[key.toLowerCase()] = label
  }
  return map
})

const bufferFacilityOptions = computed(() => {
  const list = facilityTypeOptions.value || []
  return list.filter((item) => String(item?.value || '').toLowerCase() !== 'customize')
})

const bufferCenterPointText = computed(() => {
  if (!bufferCenterPoint.value) return ''
  const { lat, lng } = bufferCenterPoint.value
  return `${lng.toFixed(6)}, ${lat.toFixed(6)}`
})

const isBufferSpecialFacilityType = (type?: string) => {
  return isSignboardFacilityType(type) || isFloodMaterialFacilityType(type)
}

const isFacilityPointInGeoJson = (item: GisBufferQueryFacilityRespVO, geoJson: any) => {
  const lng = Number(item?.longitude)
  const lat = Number(item?.latitude)
  if (!Number.isFinite(lng) || !Number.isFinite(lat)) return false
  return isPointInGeoJson([lng, lat], geoJson)
}

const bufferFacilityRawList = computed<GisBufferQueryFacilityRespVO[]>(() => {
  const rawList = (bufferResult.value?.facilities || []) as GisBufferQueryFacilityRespVO[]
  const extraList = bufferExtraFacilities.value || []
  if (rawList.length === 0 && extraList.length === 0) return []
  const geoJson = bufferGeoJsonData.value
  const merged = rawList.concat(extraList)
  const result: GisBufferQueryFacilityRespVO[] = []
  const seen = new Set<string>()
  for (const item of merged) {
    if (!item) continue
    const rawType = String(item.facilityType || '').trim()
    const id = String((item as any)?.facilityId ?? '').trim()
    const coordKey = `${Number(item.longitude) || ''}_${Number(item.latitude) || ''}_${String(item.facilityName || '')}`
    const dedupeKey = id || coordKey
    const key = `${rawType.toLowerCase()}__${dedupeKey}`
    if (seen.has(key)) continue
    if (geoJson && isBufferSpecialFacilityType(rawType)) {
      if (!isFacilityPointInGeoJson(item, geoJson)) continue
    }
    result.push(item)
    seen.add(key)
  }
  return result
})

const bufferFacilityList = computed<GisBufferQueryFacilityRespVO[]>(() => {
  const list = bufferFacilityRawList.value
  const filterKey = String(bufferObjectFilter.value || '').trim().toLowerCase()
  if (!filterKey) return list
  return list.filter((item) => String(item?.facilityType || '').trim().toLowerCase() === filterKey)
})

const bufferFacilityTotal = computed(() => bufferFacilityList.value.length)

const bufferFacilityPageList = computed<GisBufferQueryFacilityRespVO[]>(() => {
  const start = (bufferFacilityPageNo.value - 1) * bufferFacilityPageSize.value
  return bufferFacilityList.value.slice(start, start + bufferFacilityPageSize.value)
})

const bufferAdminAreas = computed(() => {
  return bufferResult.value?.adminAreas || []
})

const bufferAdminAreaRows = computed(() => {
  const list = bufferAdminAreas.value || []
  const rows: Array<typeof list> = []
  for (let i = 0; i < list.length; i += 3) {
    rows.push(list.slice(i, i + 3))
  }
  return rows
})

const bufferFacilityCount = computed(() => {
  return bufferFacilityList.value.length
})

const bufferStatsTimeText = computed(() => {
  const raw = bufferResult.value?.statsTime
  if (raw == null || raw === '') return '-'
  const text = String(raw).trim()
  if (!text) return '-'
  const numeric = Number(text)
  if (Number.isFinite(numeric)) {
    return formatToDateTime(numeric, 'YYYY-MM-DD HH:mm:ss')
  }
  const parsed = dateUtil(text)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : text
})

const bufferAreaText = computed(() => {
  const area = Number(bufferResult.value?.bufferAreaM2)
  if (!Number.isFinite(area) || area <= 0) return '-'
  if (area >= 1_000_000) {
    return `${(area / 1_000_000).toFixed(2)} km²`
  }
  return `${area.toFixed(0)} ㎡`
})

const bufferObjectInfoList = computed(() => {
  const list = bufferFacilityRawList.value
  if (!list.length) return []
  const countMap: Record<string, number> = {}
  for (const item of list) {
    const rawKey = String(item?.facilityType || '').trim()
    if (!rawKey) continue
    const key = rawKey.toLowerCase()
    countMap[key] = (countMap[key] || 0) + 1
  }
  const options = Object.entries(countMap).map(([key, count]) => {
    const label = facilityTypeLabelMap.value[key] || facilityTypeLabelMap.value[key.toLowerCase()] || key
    return { value: key, label, count }
  })
  return [{ value: '', label: '全部', count: list.length }, ...options]
})

const bufferFacilityAttrRows = computed(() => {
  const attrs = (bufferFacilityDetail.value?.attributes || {}) as Record<string, any>
  return Object.keys(attrs).map((key) => ({
    key,
    value: attrs[key] == null ? '-' : String(attrs[key])
  }))
})

const handleBufferObjectFilter = async (item?: { value?: string }) => {
  const key = String(item?.value || '').trim().toLowerCase()
  bufferObjectFilter.value = bufferObjectFilter.value === key ? '' : key
  bufferFacilityPageNo.value = 1
  await renderBufferFacilitiesOnMap(bufferFacilityList.value)
}

const homeAreaTreeHasType = computed(() => {
  const walk = (nodes?: AreaNodeRespVO[]): boolean => {
    if (!nodes || nodes.length === 0) return false
    for (const node of nodes) {
      if (!node) continue
      if (typeof (node as any).type === 'number') return true
      if (walk(node.children)) return true
    }
    return false
  }
  return walk(areaTree.value)
})

const areaTreeListData = computed<AreaNodeRespVO[]>(() => {
  const roots = areaTree.value || []
  if (!roots.length) return []
  if (!homeAreaTreeHasType.value) return roots

  // 首页优先从 type=5 节点开始展示，并保留其下级节点，避免把省/市层级树全部铺开导致列表过长
  const result: AreaNodeRespVO[] = []
  const walk = (nodes?: AreaNodeRespVO[]) => {
    if (!nodes || nodes.length === 0) return
    for (const node of nodes) {
      if (!node) continue
      if ((node as any).type === 5) {
        result.push(node)
      } else if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }
  walk(roots)
  return result.length ? result : roots
})

// 行政区域树默认折叠：不自动展开任何节点（仅当用户手动展开时才展示下级）
const defaultExpandedAreaIds = ref<number[]>([])

const homeAreaTreeProps = {
  label: 'name',
  children: 'children'
}

const filterHomeAreaNode = (keyword: string, data: HomeAreaTreeNode) => {
  const k = String(keyword || '').trim()
  if (!k) return true
  return String(data?.name || '').includes(k)
}

watch(areaKeyword, (val) => {
  areaTreeRef.value?.filter?.(val)
})

watch(
  () => bufferFacilityList.value.length,
  () => {
    bufferFacilityPageNo.value = 1
  }
)

const areaDescendantIdMap = computed<Record<number, number[]>>(() => {
  const map: Record<number, number[]> = {}
  const dfs = (node: AreaNodeRespVO): number[] => {
    const ids: number[] = []
    if (!node || typeof node.id !== 'number') return ids
    ids.push(node.id)
    if (Array.isArray(node.children) && node.children.length > 0) {
      for (const child of node.children) {
        if (!child) continue
        ids.push(...dfs(child))
      }
    }
    map[node.id] = ids
    return ids
  }
  for (const root of areaTree.value || []) {
    if (!root) continue
    dfs(root)
  }
  return map
})

const selectedAreaCount = computed(() => {
  if (!selectedArea.value) return 0
  return getAreaCount(selectedArea.value.id)
})

const getAreaCount = (areaId: number) => {
  const ownCount = areaCountMap.value?.[areaId] ?? 0
  // 父级已有统计时不叠加子级，避免父子重复计数
  if (ownCount > 0) return ownCount
  const ids = areaDescendantIdMap.value?.[areaId] || [areaId]
  let sum = 0
  for (const id of ids) {
    if (id === areaId) continue
    sum += areaCountMap.value?.[id] ?? 0
  }
  return sum
}

const riverLevelStats = computed(() => {
  const map = riverOverview.value?.riverLevelCountMap || {}
  return riverLevelOptions.value.map((opt) => ({
    value: opt.value,
    label: opt.label,
    count: map?.[opt.value] ?? 0
  }))
})

const reservoirScaleStats = computed(() => {
  const map = reservoirOverview.value?.reservoirScaleCountMap || {}
  return reservoirScaleOptions.value.map((opt) => ({
    value: opt.value,
    label: opt.label,
    count: map?.[opt.value] ?? 0
  }))
})

const pumpStationTypeStats = computed(() => {
  const map = pumpStationOverview.value?.pumpStationTypeCountMap || {}
  return pumpStationTypeOptions.value.map((opt) => ({
    value: opt.value,
    label: opt.label,
    count: map?.[opt.value] ?? 0
  }))
})

const formatKm2 = (val: any) => {
  const num = Number(val)
  if (!Number.isFinite(num)) return '0'
  return num.toFixed(2).replace(/\.00$/, '')
}

const formatKm = (val: any) => {
  const num = Number(val)
  if (!Number.isFinite(num)) return '-'
  return num.toFixed(1).replace(/\.0$/, '')
}

// 库容字段以“万m³”为口径展示（与水库管理页保持一致），不做额外单位换算
const formatWanM3 = (val: any) => {
  const num = Number(val)
  if (!Number.isFinite(num)) return '-'
  return num.toFixed(2).replace(/\.00$/, '').replace(/(\.\d)0$/, '$1')
}

const formatRiverLevelLabel = (lv: { value: string; label?: string }) => {
  const val = String(lv?.value || '').trim()
  const raw = String(lv?.label || '').trim()

  const digitFromValue = val.match(/\d+/)?.[0]
  if (digitFromValue) {
    return `${digitFromValue}级河道`
  }
  const digitFromLabel = raw.match(/\d+/)?.[0]
  if (digitFromLabel) {
    return `${digitFromLabel}级河道`
  }

  if (raw) {
    if (raw.includes('河道')) return raw
    if (raw.includes('级')) return `${raw}河道`
    return raw
  }

  if (!val) return ''
  if (val.includes('河道')) return val
  if (val.includes('级')) return `${val}河道`
  return `${val}级河道`
}

const toggleFacilityList = () => {
  facilityListCollapsed.value = !facilityListCollapsed.value
}

const toggleAreaList = () => {
  areaListCollapsed.value = !areaListCollapsed.value
}

const handleAreaNodeClick = async (data: HomeAreaTreeNode) => {
  if (!selectedFacilityType.value) return
  if (!data || typeof data.id !== 'number') return
  await openAreaDrawer({ id: data.id, name: data.name || '' })
}

//region 天地图（Leaflet）
const DEFAULT_CENTER: [number, number] = [32.272, 119.184]
let Leaflet: any = null
let mapInstance: any = null
let markerLayer: any = null
let feedbackMarkerLayer: any = null
let highlightLayer: any = null
let areaHighlightLayer: any = null
let highlightLabelLayer: any = null
let areaLabelLayer: any = null
let pondGeometryLayerMap = new Map<string, any>()
let activePondLayerKey = ''
let measureLayerGroup: any = null
let measureShapeLayer: any = null
let measureLabelLayer: any = null
let measurePoints: any[] = []
let measureFinished = false
let bufferLayer: any = null
let bufferCenterLayer: any = null
let yizhengBoundaryLayer: any = null
let baseLayerGroup: any = null
let tiandituKeyCache = ''

const applyMapBaseLayer = (type: 'imagery' | 'vector') => {
  if (!Leaflet || !mapInstance || !tiandituKeyCache) return
  if (baseLayerGroup) {
    mapInstance.removeLayer(baseLayerGroup)
    baseLayerGroup = null
  }
  baseLayerGroup =
    type === 'imagery'
      ? createTiandituImageryBaseLayer(Leaflet, tiandituKeyCache)
      : createTiandituVectorBaseLayer(Leaflet, tiandituKeyCache)
  baseLayerGroup.addTo(mapInstance)
}

const switchMapBaseLayer = (type: 'imagery' | 'vector') => {
  if (mapBaseLayerType.value === type) return
  mapBaseLayerType.value = type
  applyMapBaseLayer(type)
}

const loadLeafletAssets = async () => {
  if ((window as any).L) return
  await Promise.all([
    new Promise((resolve, reject) => {
      const existing = document.getElementById('leaflet-style')
      if (existing) return resolve(true)
      const link = document.createElement('link')
      link.id = 'leaflet-style'
      link.rel = 'stylesheet'
      link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
      link.onload = () => resolve(true)
      link.onerror = reject
      document.head.appendChild(link)
    }),
    new Promise((resolve, reject) => {
      const existing = document.getElementById('leaflet-script')
      if (existing) return resolve(true)
      const script = document.createElement('script')
      script.id = 'leaflet-script'
      script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
      script.onload = () => resolve(true)
      script.onerror = reject
      document.body.appendChild(script)
    })
  ])
}

const initMap = async () => {
  if (!mapRef.value) return
  Leaflet = (window as any).L
  if (!Leaflet) throw new Error('Leaflet 资源加载失败')

  tiandituKeyCache = await getTiandituKey()

  mapInstance = Leaflet.map(mapRef.value, {
    center: DEFAULT_CENTER,
    zoom: 12,
    zoomControl: true
  })

  mapBaseLayerType.value = 'imagery'
  applyMapBaseLayer('imagery')

  markerLayer = Leaflet.layerGroup().addTo(mapInstance)
  feedbackMarkerLayer = Leaflet.layerGroup().addTo(mapInstance)
  mapInstance.on('click', () => {
  })
}

const renderYizhengBoundary = () => {
  if (!Leaflet || !mapInstance) return
  clearYizhengBoundary()
  if (!yizhengBoundaryGeoJson.value) return
  yizhengBoundaryLayer = Leaflet.geoJSON(yizhengBoundaryGeoJson.value, {
    style: {
      color: '#f97316',
      weight: 3,
      dashArray: '6 6',
      opacity: 0.9,
      fillOpacity: 0
    }
  }).addTo(mapInstance)
  yizhengBoundaryLayer.bringToFront?.()
}

const clearYizhengBoundary = () => {
  if (yizhengBoundaryLayer && mapInstance) {
    mapInstance.removeLayer(yizhengBoundaryLayer)
    yizhengBoundaryLayer = null
  }
}

//region 测距/测面
const measureMode = ref<'' | 'distance' | 'area'>('')
const measureResultText = ref('')
const measureTipText = computed(() => {
  if (!measureMode.value) return ''
  const action = measureMode.value === 'distance' ? '测距' : '测面'
  const result = measureResultText.value ? `，${measureResultText.value}` : ''
  return `当前：${action}（单击添加节点，双击结束）${result}`
})

const canClearMeasure = computed(() => {
  if (mapLoading.value) return false
  return Boolean(measureMode.value) || Boolean(bufferResult.value) || bufferResultVisible.value || feedbackMarkerCount.value > 0
})

const toRad = (deg: number) => (deg * Math.PI) / 180

// 两点球面距离（米）
const haversineDistance = (a: any, b: any) => {
  const lat1 = Number(a?.lat)
  const lon1 = Number(a?.lng)
  const lat2 = Number(b?.lat)
  const lon2 = Number(b?.lng)
  if (![lat1, lon1, lat2, lon2].every((n) => Number.isFinite(n))) return 0
  const R = 6378137
  const dLat = toRad(lat2 - lat1)
  const dLon = toRad(lon2 - lon1)
  const rLat1 = toRad(lat1)
  const rLat2 = toRad(lat2)
  const h =
    Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(rLat1) * Math.cos(rLat2) * Math.sin(dLon / 2) * Math.sin(dLon / 2)
  return 2 * R * Math.asin(Math.min(1, Math.sqrt(h)))
}

// 折线总长度（米）
const calcLineDistance = (points: any[]) => {
  if (!points || points.length < 2) return 0
  let sum = 0
  for (let i = 1; i < points.length; i++) {
    sum += haversineDistance(points[i - 1], points[i])
  }
  return sum
}

// 球面多边形面积（平方米），适用于经纬度坐标（EPSG:4490 与 WGS84 同一量级）
const calcPolygonArea = (points: any[]) => {
  if (!points || points.length < 3) return 0
  const R = 6378137
  let sum = 0
  for (let i = 0; i < points.length; i++) {
    const p1 = points[i]
    const p2 = points[(i + 1) % points.length]
    const lon1 = Number(p1?.lng)
    const lat1 = Number(p1?.lat)
    const lon2 = Number(p2?.lng)
    const lat2 = Number(p2?.lat)
    if (![lon1, lat1, lon2, lat2].every((n) => Number.isFinite(n))) continue
    sum += toRad(lon2 - lon1) * (2 + Math.sin(toRad(lat1)) + Math.sin(toRad(lat2)))
  }
  return Math.abs((sum * R * R) / 2)
}

const formatKmValue = (meters: number) => {
  const km = meters / 1000
  if (!Number.isFinite(km)) return '0'
  return km.toFixed(2).replace(/\.00$/, '')
}

const formatKm2Value = (meters2: number) => {
  const km2 = meters2 / 1_000_000
  if (!Number.isFinite(km2)) return '0'
  return km2.toFixed(3).replace(/\.000$/, '')
}

const ensureMeasureLayer = () => {
  if (!Leaflet || !mapInstance) return
  if (!measureLayerGroup) {
    measureLayerGroup = Leaflet.layerGroup().addTo(mapInstance)
  }
}

const resetMeasureDrawing = () => {
  ensureMeasureLayer()
  if (!Leaflet || !mapInstance || !measureLayerGroup) return
  measureLayerGroup.clearLayers()
  if (measureLabelLayer) {
    mapInstance.removeLayer(measureLabelLayer)
    measureLabelLayer = null
  }
  measurePoints = []
  measureFinished = false
  measureResultText.value = ''

  const commonStyle = { color: '#fa8c16', weight: 3, fillOpacity: 0.12 }
  if (measureMode.value === 'distance') {
    measureShapeLayer = Leaflet.polyline([], commonStyle).addTo(measureLayerGroup)
  } else if (measureMode.value === 'area') {
    measureShapeLayer = Leaflet.polygon([], { ...commonStyle, fillColor: '#fa8c16' }).addTo(measureLayerGroup)
  } else {
    measureShapeLayer = null
  }
}

const updateMeasureResult = () => {
  if (!Leaflet || !mapInstance) return
  if (!measureMode.value) return

  if (measureMode.value === 'distance') {
    const meters = calcLineDistance(measurePoints)
    measureResultText.value = `总距离：${formatKmValue(meters)} km`
  } else {
    const meters2 = calcPolygonArea(measurePoints)
    measureResultText.value = `总面积：${formatKm2Value(meters2)} km²`
  }

  const last = measurePoints[measurePoints.length - 1]
  if (!last || !measureResultText.value) return
  if (measureLabelLayer) {
    mapInstance.removeLayer(measureLabelLayer)
    measureLabelLayer = null
  }
  measureLabelLayer = Leaflet.tooltip({
    permanent: true,
    direction: 'top',
    opacity: 0.95,
    className: 'home-map__measure-label',
    offset: [0, -10]
  })
    .setLatLng(last)
    .setContent(measureResultText.value)
    .addTo(mapInstance)
}

const onMeasureClick = (e: any) => {
  if (!Leaflet || !mapInstance || !measureMode.value) return
  if (measureFinished) return
  const latlng = e?.latlng
  if (!latlng) return

  measurePoints.push(latlng)
  ensureMeasureLayer()
  if (measureLayerGroup) {
    Leaflet.circleMarker(latlng, {
      radius: 5,
      color: '#fa8c16',
      weight: 2,
      fillColor: '#fa8c16',
      fillOpacity: 0.55
    }).addTo(measureLayerGroup)
  }

  if (measureShapeLayer) {
    measureShapeLayer.setLatLngs(measureMode.value === 'area' ? [measurePoints] : measurePoints)
  }

  if (
    (measureMode.value === 'distance' && measurePoints.length >= 2) ||
    (measureMode.value === 'area' && measurePoints.length >= 3)
  ) {
    updateMeasureResult()
  }
}

const endMeasure = () => {
  if (!mapInstance) return
  if (measureFinished) return
  measureFinished = true
  mapInstance.off('click', onMeasureClick)
  mapInstance.off('dblclick', onMeasureDblClick)
  mapInstance.doubleClickZoom?.enable?.()
  updateMeasureResult()
}

const onMeasureDblClick = (e: any) => {
  if (e?.originalEvent && Leaflet?.DomEvent?.stop) {
    Leaflet.DomEvent.stop(e.originalEvent)
  }
  if (!measureMode.value) return
  endMeasure()
}

const startMeasure = (mode: 'distance' | 'area') => {
  if (!Leaflet || !mapInstance) return
  measureMode.value = mode
  resetMeasureDrawing()
  mapInstance.off('click', onMeasureClick)
  mapInstance.off('dblclick', onMeasureDblClick)
  mapInstance.on('click', onMeasureClick)
  mapInstance.on('dblclick', onMeasureDblClick)
  mapInstance.doubleClickZoom?.disable?.()
}

const stopMeasure = () => {
  if (!mapInstance) return
  mapInstance.off('click', onMeasureClick)
  mapInstance.off('dblclick', onMeasureDblClick)
  mapInstance.doubleClickZoom?.enable?.()
  measureMode.value = ''
  measureResultText.value = ''
  measurePoints = []
  measureFinished = false
  if (measureLayerGroup) {
    measureLayerGroup.clearLayers()
  }
  if (measureLabelLayer && mapInstance) {
    mapInstance.removeLayer(measureLabelLayer)
    measureLabelLayer = null
  }
  measureShapeLayer = null
}

const toggleMeasure = (mode: 'distance' | 'area') => {
  if (measureMode.value === mode) {
    stopMeasure()
    return
  }
  startMeasure(mode)
}

const clearMeasure = () => {
  if (measureMode.value) {
    resetMeasureDrawing()
  }
  clearBufferResult()
  resetFeedbackFilters()
}
//endregion

//region 缓冲区查询
const clearBufferResult = () => {
  clearBufferLayer()
  clearBufferCenterMarker()
  if (bufferResultVisible.value || bufferResult.value) {
    clearMarkers()
  }
  bufferResultVisible.value = false
  bufferResult.value = null
  bufferGeoJsonData.value = null
  bufferExtraFacilities.value = []
  bufferObjectFilter.value = ''
  bufferFacilityPageNo.value = 1
}

const openBufferQueryDialog = () => {
  stopMeasure()
  if (bufferPicking.value) return
  resetBufferQueryForm()
  enableBufferPick(true)
}

const openFeedbackDialog = async () => {
  feedbackDialogVisible.value = true
  await applyFeedbackFilters()
}

const closeBufferDialog = () => {
  bufferDialogVisible.value = false
  bufferDialogRestore.value = false
  disableBufferPick(false)
}

const handleBufferSelectAllChanged = (val: boolean) => {
  if (val) {
    const list = bufferFacilityOptions.value || []
    bufferFacilityTypes.value = list
      .map((item) => String(item?.value || '').trim())
      .filter((item) => item.length > 0)
    return
  }
  bufferFacilityTypes.value = []
}

const resetBufferQueryForm = () => {
  bufferRadius.value = ''
  bufferSelectAll.value = false
  bufferFacilityTypes.value = []
  bufferCenterPoint.value = null
  clearBufferCenterMarker()
}

const enableBufferPick = (forceRestore = false) => {
  if (!mapInstance) return
  stopMeasure()
  bufferDialogRestore.value = forceRestore || bufferDialogVisible.value
  bufferDialogVisible.value = false
  bufferPicking.value = true
  mapInstance.off('click', onBufferPick)
  mapInstance.on('click', onBufferPick)
  mapInstance.doubleClickZoom?.disable?.()
  const container = mapInstance.getContainer?.()
  if (container) {
    container.style.cursor = 'crosshair'
  }
  ElMessage.info('请在地图上单击选择中心点')
}

const disableBufferPick = (restoreDialog: boolean) => {
  if (!mapInstance) return
  mapInstance.off('click', onBufferPick)
  mapInstance.doubleClickZoom?.enable?.()
  bufferPicking.value = false
  const container = mapInstance.getContainer?.()
  if (container) {
    container.style.cursor = ''
  }
  if (restoreDialog && bufferDialogRestore.value) {
    bufferDialogVisible.value = true
  }
  bufferDialogRestore.value = false
}

const onBufferPick = (e: any) => {
  if (!Leaflet || !mapInstance || !bufferPicking.value) return
  const latlng = e?.latlng
  if (!latlng) return
  const lngLat: [number, number] = [Number(latlng.lng), Number(latlng.lat)]
  if (!isPointInYizheng(lngLat)) {
    ElMessage.warning('请在仪征范围内选择中心点')
    return
  }
  bufferCenterPoint.value = { lat: Number(latlng.lat), lng: Number(latlng.lng) }
  renderBufferCenterMarker([latlng.lat, latlng.lng])
  disableBufferPick(true)
}

const parseGeoJsonText = (geoJsonText: string) => {
  const text = String(geoJsonText || '').trim()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch {
    return null
  }
}

const getBufferAdminAreaIds = () => {
  const list = bufferResult.value?.adminAreas || []
  const ids = new Set<number>()
  for (const item of list) {
    const id = Number((item as any)?.id)
    if (!Number.isFinite(id)) continue
    ids.add(id)
  }
  return Array.from(ids)
}

const getBufferSpecialFacilityFlags = () => {
  if (bufferSelectAll.value) {
    return { needSignboard: true, needFloodMaterial: true }
  }
  const list = bufferFacilityTypes.value || []
  return {
    needSignboard: list.some((item) => isSignboardFacilityType(item)),
    needFloodMaterial: list.some((item) => isFloodMaterialFacilityType(item))
  }
}

const loadBufferSpecialFacilities = async () => {
  bufferExtraFacilities.value = []
  const { needSignboard, needFloodMaterial } = getBufferSpecialFacilityFlags()
  if (!needSignboard && !needFloodMaterial) return
  const areaIds = getBufferAdminAreaIds()
  if (!areaIds.length) return
  // 公示牌、防汛物资不关联基础设施，需按经纬度再次判断
  const signboardMap = new Map<string, SignboardAreaOverviewItemRespVO>()
  const warehouseMap = new Map<string, FloodMaterialWarehouseAreaOverviewItemRespVO>()
  try {
    const tasks: Promise<void>[] = []
    if (needSignboard) {
      tasks.push(
        Promise.all(areaIds.map((areaId) => getSignboardAreaOverviewByArea(areaId))).then((responses) => {
          for (const resp of responses) {
            const list = (resp?.list || []) as SignboardAreaOverviewItemRespVO[]
            for (const item of list) {
              const id = normalizeId((item as any)?.signboardId)
              if (!id || signboardMap.has(id)) continue
              signboardMap.set(id, item)
            }
          }
        })
      )
    }
    if (needFloodMaterial) {
      tasks.push(
        Promise.all(areaIds.map((areaId) => getFloodMaterialWarehouseAreaOverviewByArea(areaId))).then((responses) => {
          for (const resp of responses) {
            const list = (resp?.list || []) as FloodMaterialWarehouseAreaOverviewItemRespVO[]
            for (const item of list) {
              const id = normalizeId((item as any)?.warehouseId)
              if (!id || warehouseMap.has(id)) continue
              warehouseMap.set(id, item)
            }
          }
        })
      )
    }
    await Promise.all(tasks)
  } catch {
    // 已由全局 axios 拦截器提示
  }
  const geoJson = bufferGeoJsonData.value
  const extra: GisBufferQueryFacilityRespVO[] = []
  if (needSignboard) {
    for (const item of signboardMap.values()) {
      const lng = Number((item as any)?.longitude)
      const lat = Number((item as any)?.latitude)
      if (!Number.isFinite(lng) || !Number.isFinite(lat)) continue
      if (geoJson && !isPointInGeoJson([lng, lat], geoJson)) continue
      extra.push({
        facilityId: (item as any)?.signboardId,
        facilityType: 'signboard',
        facilityName: String((item as any)?.signboardName || ''),
        longitude: lng,
        latitude: lat
      })
    }
  }
  if (needFloodMaterial) {
    for (const item of warehouseMap.values()) {
      const lng = Number((item as any)?.longitude)
      const lat = Number((item as any)?.latitude)
      if (!Number.isFinite(lng) || !Number.isFinite(lat)) continue
      if (geoJson && !isPointInGeoJson([lng, lat], geoJson)) continue
      extra.push({
        facilityId: (item as any)?.warehouseId,
        facilityType: 'flood_prevention_material',
        facilityName: String((item as any)?.warehouseName || ''),
        longitude: lng,
        latitude: lat
      })
    }
  }
  bufferExtraFacilities.value = extra
}

const submitBufferQuery = async () => {
  const radius = Number(bufferRadius.value)
  if (!Number.isFinite(radius) || radius <= 0) {
    ElMessage.warning('请输入有效的缓冲区半径')
    return
  }
  if (!bufferCenterPoint.value) {
    ElMessage.warning('请先在地图上选择中心点')
    return
  }
  if (!bufferSelectAll.value && bufferFacilityTypes.value.length === 0) {
    ElMessage.warning('请至少选择一个设施类型')
    return
  }
  const point: [number, number] = [bufferCenterPoint.value.lng, bufferCenterPoint.value.lat]
  if (!isPointInYizheng(point)) {
    ElMessage.warning('请在仪征范围内选择中心点')
    return
  }

  bufferQueryLoading.value = true
  try {
    const resp = await createBufferQuery({
      centerLongitude: bufferCenterPoint.value.lng,
      centerLatitude: bufferCenterPoint.value.lat,
      radiusMeters: radius,
      facilityTypes: bufferSelectAll.value ? [] : bufferFacilityTypes.value,
      selectAll: bufferSelectAll.value
    })
    bufferResult.value = resp as GisBufferQueryRespVO
    bufferGeoJsonData.value = parseGeoJsonText(String(resp?.bufferGeoJson || ''))
    bufferObjectFilter.value = ''
    bufferFacilityPageNo.value = 1
    bufferResultVisible.value = true
    drawerVisible.value = false
    closeBufferDialog()
    await loadBufferSpecialFacilities()
    renderBufferOnMap(String(resp?.bufferGeoJson || ''))
    renderBufferFacilitiesOnMap(bufferFacilityList.value)
  } finally {
    bufferQueryLoading.value = false
  }
}

const handleBufferFacilityRowClick = async (row: GisBufferQueryFacilityRespVO) => {
  await openBufferFacilityDetail(row)
}

const isRiverFacilityType = (type?: string) => {
  const val = String(type || '').trim().toLowerCase()
  return val === 'river' || val === 'river_channel' || val === 'riverchannel'
}

const isReservoirFacilityType = (type?: string) => {
  const val = String(type || '').trim().toLowerCase()
  return val === 'reservoir' || val === 'water_reservoir' || val === 'waterreservoir'
}

const isRiverSectionFacilityType = (type?: string) => {
  const val = String(type || '').trim().toLowerCase()
  return val === 'river_section' || val === 'riversection' || val === 'section'
}

const isIrrigationFacilityType = (type?: string) => {
  const val = String(type || '').trim().toLowerCase()
  return val === 'irrigation' || val === 'irrigation_district' || val === 'irrigationdistrict'
}

const isPumpStationFacilityType = (type?: string) => {
  const val = String(type || '').trim().toLowerCase()
  return val === 'pump_station' || val === 'pumpstation'
}

const isSignboardFacilityType = (type?: string) => {
  const val = String(type || '').trim().toLowerCase()
  return val === 'signboard' || val === 'public_notice' || val === 'publicnotice'
}

const isFloodMaterialFacilityType = (type?: string) => {
  const val = String(type || '').trim().toLowerCase()
  return val === 'flood_prevention_material'
}

const openBufferFacilityDetail = async (row: GisBufferQueryFacilityRespVO) => {
  const id = normalizeId((row as any)?.facilityId)
  if (!id) return
  if (isRiverFacilityType(row?.facilityType)) {
    const detail = await getRiverChannelDetailByFacility(id)
    const channelId = normalizeId((detail as any)?.id)
    if (!channelId) return
    await openRiverDetailByChannel(channelId, id, detail?.riverName, detail as any)
    return
  }
  if (isRiverSectionFacilityType(row?.facilityType)) {
    const detail = await getRiverSectionDetailByFacility(id)
    const sectionId = normalizeId((detail as any)?.id)
    if (!sectionId) return
    await openRiverSectionDetailByFacility(sectionId, id, detail?.sectionName, detail as any)
    return
  }
  if (isReservoirFacilityType(row?.facilityType)) {
    const detail = await getReservoirDetailByFacility(id)
    const reservoirId = normalizeId((detail as any)?.id)
    if (!reservoirId) return
    await openReservoirDetailByFacility(reservoirId, id, detail?.reservoirName, detail as any)
    return
  }
  if (isIrrigationFacilityType(row?.facilityType)) {
    const detail = await getIrrigationDistrictDetailByFacility(id)
    const irrigationId = normalizeId((detail as any)?.id)
    if (!irrigationId) return
    await openIrrigationDistrictDetailByFacility(irrigationId, id, detail?.irrigationDistrictName, detail as any)
    return
  }
  if (isSignboardFacilityType(row?.facilityType)) {
    await openSignboardDetailById(id, row?.facilityName)
    return
  }
  if (isFloodMaterialFacilityType(row?.facilityType)) {
    await openFloodMaterialWarehouseDetail({
      warehouseId: id,
      warehouseName: row?.facilityName
    } as any)
    return
  }
  if (isPumpStationFacilityType(row?.facilityType)) {
    await openPumpStationDetailByFacility(id, row?.facilityName)
    return
  }
  bufferFacilityDetailVisible.value = true
  bufferFacilityDetailLoading.value = true
  bufferFacilityDetail.value = null
  try {
    const detail = await handleFacilityClick(id, row?.facilityName)
    bufferFacilityDetail.value = detail || null
  } finally {
    bufferFacilityDetailLoading.value = false
  }
}

const bufferFacilityTypeLabel = (type: string) => {
  const val = String(type || '').trim()
  return facilityTypeLabelMap.value[val] || facilityTypeLabelMap.value[val.toLowerCase()] || val || '-'
}

const renderBufferCenterMarker = (latlng: [number, number]) => {
  if (!Leaflet || !mapInstance) return
  clearBufferCenterMarker()
  bufferCenterLayer = Leaflet.circleMarker(latlng, {
    radius: 6,
    color: '#1677ff',
    weight: 2,
    fillColor: '#1677ff',
    fillOpacity: 0.6
  }).addTo(mapInstance)
}

const clearBufferCenterMarker = () => {
  if (bufferCenterLayer && mapInstance) {
    mapInstance.removeLayer(bufferCenterLayer)
    bufferCenterLayer = null
  }
}

const renderBufferOnMap = (geoJsonText: string) => {
  if (!Leaflet || !mapInstance) return
  clearBufferLayer()
  const parsed = bufferGeoJsonData.value || parseGeoJsonText(geoJsonText)
  if (!bufferGeoJsonData.value) {
    bufferGeoJsonData.value = parsed
  }
  if (!parsed) return
  bufferLayer = Leaflet.geoJSON(parsed, {
    style: {
      color: '#1677ff',
      weight: 2,
      fillColor: '#1677ff',
      fillOpacity: 0.18
    }
  }).addTo(mapInstance)
  const bounds = bufferLayer.getBounds ? bufferLayer.getBounds() : null
  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 15 })
  }
}

const clearBufferLayer = () => {
  if (bufferLayer && mapInstance) {
    mapInstance.removeLayer(bufferLayer)
    bufferLayer = null
  }
}

const renderBufferFacilitiesOnMap = async (list: GisBufferQueryFacilityRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return
  const latLngs: any[] = []
  for (const item of list) {
    const lat = Number(item?.latitude)
    const lng = Number(item?.longitude)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) continue
    latLngs.push([lat, lng])
    const marker = Leaflet.circleMarker([lat, lng], {
      radius: 6,
      color: '#13c2c2',
      weight: 2,
      fillColor: '#13c2c2',
      fillOpacity: 0.4
    }).addTo(markerLayer)
    const name = String(item?.facilityName || '').trim()
    const openDetail = async () => {
      await openBufferFacilityDetail(item)
    }
    bindPermanentClickableTooltip(marker, name, openDetail)
    marker.on('click', () => {
      runMapNameClickHandler(openDetail)
    })
  }
  if (latLngs.length > 0) {
    const bounds = Leaflet.latLngBounds(latLngs)
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const isPointInYizheng = (lngLat: [number, number]) => {
  if (!yizhengBoundaryGeoJson.value) return true
  return isPointInGeoJson(lngLat, yizhengBoundaryGeoJson.value)
}

const isPointInGeoJson = (lngLat: [number, number], geoJson: any): boolean => {
  if (!geoJson) return false
  const type = geoJson?.type
  if (type === 'FeatureCollection') {
    const list = geoJson?.features || []
    return list.some((feature: any) => isPointInGeoJson(lngLat, feature))
  }
  if (type === 'Feature') {
    return isPointInGeoJson(lngLat, geoJson?.geometry)
  }
  if (type === 'GeometryCollection') {
    const list = geoJson?.geometries || []
    return list.some((geometry: any) => isPointInGeoJson(lngLat, geometry))
  }
  if (type === 'Polygon') {
    return isPointInPolygon(lngLat, geoJson?.coordinates)
  }
  if (type === 'MultiPolygon') {
    const polys = geoJson?.coordinates || []
    return polys.some((polygon: any) => isPointInPolygon(lngLat, polygon))
  }
  return false
}

const isPointInPolygon = (lngLat: [number, number], polygon: number[][][]) => {
  if (!Array.isArray(polygon) || polygon.length === 0) return false
  const [outer, ...holes] = polygon
  if (!isPointInRing(lngLat, outer)) return false
  return !holes.some((ring) => isPointInRing(lngLat, ring))
}

const isPointInRing = (lngLat: [number, number], ring: number[][]) => {
  if (!Array.isArray(ring) || ring.length < 3) return false
  const [x, y] = lngLat
  let inside = false
  for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
    const xi = ring[i][0]
    const yi = ring[i][1]
    const xj = ring[j][0]
    const yj = ring[j][1]
    const intersect = yi > y !== yj > y && x < ((xj - xi) * (y - yi)) / (yj - yi) + xi
    if (intersect) inside = !inside
  }
  return inside
}
//endregion

const clearMarkers = () => {
  if (!markerLayer) return
  markerLayer.clearLayers()
  pondGeometryLayerMap.clear()
  activePondLayerKey = ''
}

const clearFeedbackMarkers = () => {
  if (!feedbackMarkerLayer) return
  feedbackMarkerLayer.clearLayers()
  feedbackMarkerCount.value = 0
}

const clearHighlight = () => {
  if (highlightLabelLayer && mapInstance) {
    mapInstance.removeLayer(highlightLabelLayer)
    highlightLabelLayer = null
  }
  if (highlightLayer && mapInstance) {
    mapInstance.removeLayer(highlightLayer)
    highlightLayer = null
  }
}

const clearAreaHighlight = () => {
  if (areaLabelLayer && mapInstance) {
    mapInstance.removeLayer(areaLabelLayer)
    areaLabelLayer = null
  }
  if (areaHighlightLayer && mapInstance) {
    mapInstance.removeLayer(areaHighlightLayer)
    areaHighlightLayer = null
  }
}

const createPermanentLabel = (
  latLng: [number, number],
  name: string,
  direction: 'top' | 'center' = 'top'
) => {
  const text = String(name ?? '').trim()
  if (!text || !Leaflet || !mapInstance) return null
  const tooltip = Leaflet.tooltip({
    permanent: true,
    direction,
    opacity: 0.95,
    className: 'home-map__gis-label',
    offset: direction === 'top' ? [0, -8] : [0, 0]
  })
    .setLatLng(latLng)
    .setContent(text)
    .addTo(mapInstance)
  return tooltip
}

const normalizeFeedbackStatusParam = (val: string) => {
  const numeric = Number(val)
  return Number.isFinite(numeric) ? numeric : val
}

const formatFeedbackTime = (val?: string) => {
  if (!val) return '-'
  const text = String(val).trim()
  if (!text) return '-'
  const numeric = Number(text)
  if (Number.isFinite(numeric)) {
    return formatToDateTime(numeric, 'YYYY-MM-DD HH:mm:ss')
  }
  const parsed = dateUtil(text)
  return parsed.isValid() ? parsed.format('YYYY-MM-DD HH:mm:ss') : text
}

const resolveFeedbackTimeValue = (val?: string) => {
  if (!val) return 0
  const text = String(val).trim()
  if (!text) return 0
  const numeric = Number(text)
  if (Number.isFinite(numeric)) {
    return numeric
  }
  const parsed = dateUtil(text)
  return parsed.isValid() ? parsed.valueOf() : 0
}

const resolveFeedbackTypeLabel = (val?: string) => {
  if (!val) return '-'
  return feedbackTypeLabelMap.value[val] || val
}

const resolveFeedbackStatusLabel = (val?: string | number) => {
  if (val === null || val === undefined || val === '') return '-'
  const key = String(val)
  return feedbackStatusLabelMap.value[key] || key
}

const resolveFeedbackPerson = (val?: string) => {
  const text = String(val ?? '').trim()
  if (!text) return '匿名'
  return text
}

const resolveFeedbackFacilityTypeLabel = (val?: string) => {
  const key = String(val || '').trim()
  if (key === 'river') return '河道'
  if (key === 'river_section') return '河段'
  if (key === 'reservoir') return '水库'
  return key || '-'
}

const resolveFeedbackFacilityInfo = async (detail: ProblemFeedbackDetailRespVO | null) => {
  feedbackFacilityTypeLabel.value = '-'
  feedbackFacilityName.value = '-'
  if (!detail) {
    return
  }
  const referenceType = String(detail?.referenceType || '').trim()
  const referenceId = detail?.referenceId
  const fallbackName =
    detail?.facilityName ||
    detail?.referenceName ||
    detail?.riverName ||
    detail?.riverChannelName ||
    detail?.riverSectionName
  feedbackFacilityTypeLabel.value = resolveFeedbackFacilityTypeLabel(referenceType)
  try {
    if (referenceType === 'river' && referenceId) {
      const riverDetail = (await getRiverChannelDetail(referenceId)) as any
      feedbackFacilityName.value = riverDetail?.riverName || fallbackName || '-'
      return
    }
    if (referenceType === 'reservoir' && referenceId) {
      const reservoirDetail = (await getReservoirDetail(referenceId)) as any
      feedbackFacilityName.value = reservoirDetail?.reservoirName || fallbackName || '-'
      return
    }
  } catch {
    // 失败时继续走兜底展示
  }
  feedbackFacilityName.value = fallbackName || '-'
}

const openFeedbackDetail = async (id: string | number, point?: { longitude: number; latitude: number } | null) => {
  feedbackDetailPoint.value = point || null
  feedbackDetailVisible.value = true
  feedbackDetailLoading.value = true
  try {
    const detail = (await getProblemFeedbackDetail(String(id))) as any
    feedbackDetail.value = detail || null
    await resolveFeedbackFacilityInfo(feedbackDetail.value)
  } catch {
    feedbackDetail.value = null
    feedbackFacilityTypeLabel.value = '-'
    feedbackFacilityName.value = '-'
  } finally {
    feedbackDetailLoading.value = false
  }
}

const openFeedbackClusterList = (items: ProblemFeedbackPageRespVO[], point?: { longitude: number; latitude: number } | null) => {
  feedbackClusterPoint.value = point || null
  const sorted = [...items].sort(
    (a, b) => resolveFeedbackTimeValue(b?.createTime) - resolveFeedbackTimeValue(a?.createTime)
  )
  feedbackClusterList.value = sorted
  feedbackClusterVisible.value = true
}

const handleFeedbackClusterRowClick = (row: ProblemFeedbackPageRespVO) => {
  const feedbackId = (row as any)?.id
  if (feedbackId === null || feedbackId === undefined || `${feedbackId}` === '') return
  feedbackClusterVisible.value = false
  openFeedbackDetail(String(feedbackId), feedbackClusterPoint.value)
}

const renderFeedbackMarkers = (list: ProblemFeedbackPageRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearFeedbackMarkers()
  if (!list || list.length === 0) return
  const icon = Leaflet.icon({
    iconUrl: feedbackIssueIconUrl,
    iconSize: [32, 32],
    iconAnchor: [16, 32]
  })
  const buildClusterIcon = (total: number) => {
    const text = total > 99 ? '99+' : String(total)
    return Leaflet.divIcon({
      className: 'home-map__feedback-cluster-wrapper',
      html: `<div class="home-map__feedback-cluster">
  <img class="home-map__feedback-cluster-icon" src="${feedbackIssueIconUrl}" alt="" />
  <span class="home-map__feedback-cluster-count">${text}</span>
</div>`,
      iconSize: [36, 36],
      iconAnchor: [18, 18]
    })
  }
  const grouped = new Map<string, { lat: number; lng: number; items: ProblemFeedbackPageRespVO[] }>()
  // 同坐标问题合并显示数量
  for (const item of list) {
    const feedbackId = (item as any)?.id
    if (feedbackId === null || feedbackId === undefined || `${feedbackId}` === '') {
      continue
    }
    const lat = Number((item as any)?.problemLatitude ?? (item as any)?.latitude)
    const lng = Number((item as any)?.problemLongitude ?? (item as any)?.longitude)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) continue
    const key = `${lat.toFixed(6)}_${lng.toFixed(6)}`
    const group = grouped.get(key)
    if (group) {
      group.items.push(item)
    } else {
      grouped.set(key, { lat, lng, items: [item] })
    }
  }
  let totalCount = 0
  for (const group of grouped.values()) {
    const size = group.items.length
    totalCount += size
    const marker = Leaflet.marker([group.lat, group.lng], {
      icon: size === 1 ? icon : buildClusterIcon(size)
    }).addTo(feedbackMarkerLayer)
    marker.on('click', (event: any) => {
      openIssueMenu(group.items, group.lat, group.lng)
    })
  }
  feedbackMarkerCount.value = totalCount
}

const buildFeedbackQueryParams = () => {
  const types = feedbackTypeValues.value.map((val) => String(val)).filter((val) => val)
  const statuses = feedbackStatusValues.value.map((val) => String(val)).filter((val) => val)
  if (!types.length && !statuses.length) return []
  const typeList = types.length ? types : ['']
  const statusList = statuses.length ? statuses : ['']
  const params: ProblemFeedbackListReqVO[] = []
  for (const type of typeList) {
    for (const status of statusList) {
      const payload: ProblemFeedbackListReqVO = {}
      if (type) {
        payload.feedbackType = type
      }
      if (status) {
        payload.status = normalizeFeedbackStatusParam(status)
      }
      params.push(payload)
    }
  }
  return params
}

const mergeFeedbackList = (responses: ProblemFeedbackPageRespVO[][]) => {
  const merged = new Map<string, ProblemFeedbackPageRespVO>()
  for (const list of responses) {
    const items = Array.isArray(list) ? list : []
    for (const item of items) {
      const id = (item as any)?.id
      if (id === null || id === undefined || `${id}` === '') continue
      const key = String(id)
      if (!merged.has(key)) {
        merged.set(key, item)
      }
    }
  }
  return Array.from(merged.values())
}

const applyFeedbackFilters = async () => {
  const queryToken = ++feedbackQueryToken.value
  if (!feedbackFilterActive.value) {
    feedbackList.value = []
    feedbackListLoading.value = false
    feedbackClusterVisible.value = false
    feedbackClusterList.value = []
    clearFeedbackMarkers()
    return
  }
  const paramsList = buildFeedbackQueryParams()
  if (!paramsList.length) {
    feedbackList.value = []
    feedbackListLoading.value = false
    feedbackClusterVisible.value = false
    feedbackClusterList.value = []
    clearFeedbackMarkers()
    return
  }
  feedbackListLoading.value = true
  try {
    const responses = await Promise.all(paramsList.map((params) => getProblemFeedbackScreenList(params)))
    if (queryToken !== feedbackQueryToken.value) return
    const merged = mergeFeedbackList(responses as ProblemFeedbackPageRespVO[][])
    feedbackList.value = merged
    renderFeedbackMarkers(merged)
  } catch {
    if (queryToken !== feedbackQueryToken.value) return
    feedbackList.value = []
    feedbackClusterVisible.value = false
    feedbackClusterList.value = []
    clearFeedbackMarkers()
  } finally {
    if (queryToken === feedbackQueryToken.value) {
      feedbackListLoading.value = false
    }
  }
}

const resetFeedbackFilters = () => {
  feedbackTypeValues.value = []
  feedbackStatusValues.value = []
  feedbackList.value = []
  feedbackClusterVisible.value = false
  feedbackClusterList.value = []
  clearFeedbackMarkers()
}

const updateFeedbackTypeAllState = () => {
  const allValues = feedbackTypeAllValues.value
  if (!allValues.length) {
    feedbackTypeAllChecked.value = false
    feedbackTypeAllIndeterminate.value = false
    return
  }
  const selected = new Set(feedbackTypeValues.value.map((val) => String(val)))
  const selectedCount = allValues.filter((val) => selected.has(String(val))).length
  feedbackTypeAllChecked.value = selectedCount === allValues.length
  feedbackTypeAllIndeterminate.value = selectedCount > 0 && selectedCount < allValues.length
}

const updateFeedbackStatusAllState = () => {
  const allValues = feedbackStatusAllValues.value
  if (!allValues.length) {
    feedbackStatusAllChecked.value = false
    feedbackStatusAllIndeterminate.value = false
    return
  }
  const selected = new Set(feedbackStatusValues.value.map((val) => String(val)))
  const selectedCount = allValues.filter((val) => selected.has(String(val))).length
  feedbackStatusAllChecked.value = selectedCount === allValues.length
  feedbackStatusAllIndeterminate.value = selectedCount > 0 && selectedCount < allValues.length
}

const handleFeedbackTypeAllChange = (checked: boolean | string | number) => {
  const isChecked = Boolean(checked)
  feedbackTypeValues.value = isChecked ? [...feedbackTypeAllValues.value] : []
}

const handleFeedbackStatusAllChange = (checked: boolean | string | number) => {
  const isChecked = Boolean(checked)
  feedbackStatusValues.value = isChecked ? [...feedbackStatusAllValues.value] : []
}

const feedbackDetailView = computed(() => {
  const detail = feedbackDetail.value
  return {
    feedbackTypeText: detail?.feedbackTypeLabel || resolveFeedbackTypeLabel(detail?.feedbackType),
    statusText: detail?.statusLabel || resolveFeedbackStatusLabel(detail?.status),
    contentText: formatTextOrDash(detail?.feedbackContent),
    timeText: formatFeedbackTime(detail?.createTime),
    specificText: formatTextOrDash(detail?.issueSpecificLocation || detail?.specificLocation),
    personText: resolveFeedbackPerson(detail?.feedbackPerson)
  }
})

const spatialAnalysisProblemStats = computed(() =>
  (spatialAnalysisResult.value?.problemTypeStats || []).filter((it) => Number(it?.count ?? 0) > 0)
)

const spatialAnalysisHasProblemTypeStats = computed(() => spatialAnalysisProblemStats.value.length > 0)

const spatialAnalysisPieOptions = computed<EChartsOption>(() => {
  const list = spatialAnalysisProblemStats.value
  const data = list.map((it) => ({
    name: String(it?.feedbackTypeLabel || it?.feedbackType || '').trim() || '其他',
    value: Number(it?.count ?? 0)
  }))
  return {
    animation: false,
    tooltip: {
      trigger: 'item',
      formatter: '{b}<br/>数量：{c}次<br/>占比：{d}%'
    },
    legend: {
      left: 'center',
      bottom: 0,
      width: '96%',
      type: 'plain',
      itemGap: 8,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        center: ['50%', '45%'],
        avoidLabelOverlap: true,
        labelLayout: { hideOverlap: false },
        label: {
          show: true,
          fontSize: 12,
          lineHeight: 16,
          formatter: (params: any) => {
            const name = String(params?.name ?? '').trim()
            const value = Number(params?.value ?? 0)
            const percentRaw = Number(params?.percent ?? 0)
            const percent = Number.isFinite(percentRaw) ? percentRaw.toFixed(1).replace(/\.0$/, '') : '0'
            return `${name}\n${percent}% (${Number.isFinite(value) ? value : 0}次)`
          }
        },
        labelLine: { show: true, length: 10, length2: 8 },
        emphasis: {
          scale: true,
          scaleSize: 8,
          label: { show: true, formatter: '{b}\n{d}% ({c}次)', fontSize: 12 }
        },
        data
      }
    ]
  }
})

const spatialAnalysisAssetStats = computed(() => {
  const stats = spatialAnalysisResult.value?.assetStats
  return {
    riverCount: stats?.riverCount ?? 0,
    reservoirCount: stats?.reservoirCount ?? 0,
    embankmentCount: stats?.embankmentCount ?? 0,
    pumpStationCount: stats?.pumpStationCount ?? 0,
    irrigationDistrictCount: stats?.irrigationDistrictCount ?? 0,
    floodMaterialCount: stats?.floodMaterialCount ?? 0
  }
})

const spatialAnalysisChiefRows = computed(() => {
  const list = spatialAnalysisResult.value?.managementList || []
  const headLevelMap = buildDictMap(headLevelOptions.value)
  return list.map((item) => ({
    headName: item?.headName || '-',
    headLevelLabel: item?.headLevelLabel || headLevelMap[String(item?.headLevel ?? '')] || item?.headLevel || '-',
    referenceTypeLabel: item?.referenceTypeLabel || resolveFeedbackFacilityTypeLabel(item?.referenceType),
    referenceName: item?.referenceName || '-',
    adminRegion: formatAdminRegionNames(item?.administrativeRegion)
  }))
})

const analysisChiefTotal = computed(() => spatialAnalysisChiefRows.value.length)
const analysisChiefPageList = computed(() => {
  const start = (analysisChiefPageNo.value - 1) * analysisChiefPageSize.value
  return spatialAnalysisChiefRows.value.slice(start, start + analysisChiefPageSize.value)
})

const resolveAnalysisPoint = (item?: any) => {
  const lat = Number(item?.problemLatitude ?? item?.latitude)
  const lng = Number(item?.problemLongitude ?? item?.longitude)
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) return null
  return { latitude: lat, longitude: lng }
}

const feedbackDetailAnalysisPoint = computed(() => resolveAnalysisPoint(feedbackDetail.value) || feedbackDetailPoint.value)
const feedbackClusterAnalysisPoint = computed(() => feedbackClusterPoint.value)

const handleOpenFeedbackDetailAnalysis = () => {
  openSpatialAnalysis(feedbackDetailAnalysisPoint.value)
}

const handleOpenFeedbackClusterAnalysis = () => {
  openSpatialAnalysis(feedbackClusterAnalysisPoint.value)
}

const openSpatialAnalysis = (point: { longitude: number; latitude: number } | null) => {
  if (!point) {
    ElMessage.warning('当前问题缺少坐标，无法进行空间分析')
    return
  }
  spatialAnalysisPoint.value = point
  spatialAnalysisRadius.value = null
  spatialAnalysisResult.value = null
  spatialAnalysisResultVisible.value = false
  spatialAnalysisDialogVisible.value = true
}

const openIssueMenu = (
  items: ProblemFeedbackPageRespVO[],
  lat: number,
  lng: number
) => {
  if (!items || items.length === 0) return
  const point = { longitude: lng, latitude: lat }
  if (items.length === 1) {
    const feedbackId = (items[0] as any)?.id
    if (feedbackId !== null && feedbackId !== undefined && `${feedbackId}` !== '') {
      openFeedbackDetail(String(feedbackId), point)
    }
    return
  }
  openFeedbackClusterList(items, point)
}

const submitSpatialAnalysis = async () => {
  const point = spatialAnalysisPoint.value
  if (!point) {
    ElMessage.warning('请先选择有效的坐标')
    return
  }
  const radius = Number(spatialAnalysisRadius.value)
  if (!Number.isFinite(radius) || radius <= 0) {
    ElMessage.warning('请输入有效的分析范围（米）')
    return
  }
  spatialAnalysisLoading.value = true
  try {
    spatialAnalysisResult.value = (await getProblemFeedbackSpatialAnalysis({
      longitude: point.longitude,
      latitude: point.latitude,
      radiusM: radius
    })) as ProblemFeedbackSpatialAnalysisRespVO
    spatialAnalysisDialogVisible.value = false
    spatialAnalysisResultVisible.value = true
    await nextTick()
    spatialAnalysisChartKey.value = Date.now()
  } catch {
    ElMessage.error('空间分析失败，请稍后重试')
  } finally {
    spatialAnalysisLoading.value = false
  }
}

const handleSpatialAnalysisOpened = async () => {
  await nextTick()
  spatialAnalysisChartKey.value = Date.now()
}

watch(
  () => spatialAnalysisResult.value,
  () => {
    analysisChiefPageNo.value = 1
    spatialAnalysisChartKey.value = Date.now()
  }
)

watch(
  () => spatialAnalysisResultVisible.value,
  (visible) => {
    if (visible) {
      spatialAnalysisChartKey.value = Date.now()
    }
  }
)

watch(
  () => [feedbackTypeValues.value, feedbackStatusValues.value],
  () => {
    updateFeedbackTypeAllState()
    updateFeedbackStatusAllState()
    applyFeedbackFilters()
  },
  { deep: true }
)

watch(
  () => yizhengBoundaryGeoJson.value,
  () => {
    if (!mapInstance) return
    renderYizhengBoundary()
  },
  { deep: true }
)

watch(
  () => feedbackTypeOptions.value,
  () => {
    if (feedbackTypeAllChecked.value) {
      feedbackTypeValues.value = [...feedbackTypeAllValues.value]
    }
    updateFeedbackTypeAllState()
  },
  { deep: true }
)

watch(
  () => feedbackStatusOptions.value,
  () => {
    if (feedbackStatusAllChecked.value) {
      feedbackStatusValues.value = [...feedbackStatusAllValues.value]
    }
    updateFeedbackStatusAllState()
  },
  { deep: true }
)

const findParentAreaIdFromTree = (targetId: number): number | null => {
  let parentId: number | null = null
  const walk = (nodes?: AreaNodeRespVO[], parent?: AreaNodeRespVO): boolean => {
    for (const node of nodes || []) {
      if (!node || typeof node.id !== 'number') continue
      if (node.id === targetId) {
        parentId = typeof parent?.id === 'number' ? parent.id : null
        return true
      }
      if (walk(node.children, node)) return true
    }
    return false
  }
  walk(areaTree.value)
  return parentId
}

const renderAreaHighlight = async (areaId: number) => {
  if (!Leaflet || !mapInstance) return
  clearAreaHighlight()
  const detail: any = (await getArea(areaId)) as AreaRespVO
  const highlightLayers: any[] = []
  let bounds: any = null

  const mergeBounds = (layer: any) => {
    const b = layer?.getBounds?.()
    if (b?.isValid?.()) {
      bounds = bounds ? bounds.extend(b) : b
    }
  }

  const createGeoJsonLayer = (geoJsonText: string, style: Record<string, unknown>) => {
    const text = String(geoJsonText || '').trim()
    if (!text) return null
    let parsed: any
    try {
      parsed = JSON.parse(text)
    } catch {
      return null
    }
    const layer = Leaflet.geoJSON(parsed, { style })
    mergeBounds(layer)
    return layer
  }

  const isVillage = Number(detail?.type) === 6 || String(detail?.name || '').trim().endsWith('村')
  let parentId = Number(detail?.parentId)
  if (!Number.isFinite(parentId) || parentId <= 0) {
    const treeParentId = findParentAreaIdFromTree(areaId)
    if (treeParentId != null) parentId = treeParentId
  }
  if (isVillage && Number.isFinite(parentId) && parentId > 0) {
    try {
      const parent: any = (await getArea(parentId)) as AreaRespVO
      const parentLayer = createGeoJsonLayer(String(parent?.gemoGeoJson || ''), {
        color: '#fa8c16',
        weight: 2,
        dashArray: '8 5',
        fillColor: '#fa8c16',
        fillOpacity: 0.08
      })
      if (parentLayer) highlightLayers.push(parentLayer)
    } catch {
      // 镇级边界加载失败不影响村级展示
    }
  }

  const villageLayer = createGeoJsonLayer(String(detail?.gemoGeoJson || ''), {
    color: '#1677ff',
    weight: 3,
    fillColor: '#1677ff',
    fillOpacity: 0.14
  })
  if (villageLayer) highlightLayers.push(villageLayer)

  if (!highlightLayers.length) return

  areaHighlightLayer = Leaflet.layerGroup(highlightLayers).addTo(mapInstance)
  if (bounds?.isValid?.()) {
    const center = bounds.getCenter?.()
    if (center) {
      areaLabelLayer = createPermanentLabel([center.lat, center.lng], String(detail?.name || ''), 'center')
    }
    mapInstance.fitBounds(bounds, { padding: [50, 50], maxZoom: 14 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const POND_MAP_STYLE = {
  color: '#20b2aa',
  weight: 2,
  fillColor: '#20b2aa',
  fillOpacity: 0.34
}

const POND_MAP_HOVER_STYLE = {
  color: '#0d9488',
  weight: 3,
  fillColor: '#14b8a6',
  fillOpacity: 0.48
}

const POND_MAP_ACTIVE_STYLE = {
  color: '#ea580c',
  weight: 3,
  fillColor: '#f97316',
  fillOpacity: 0.55
}

const getPondLayerKey = (item: WaterPondAreaOverviewItemRespVO) => {
  const pondId = String(item?.pondId ?? '').trim()
  if (pondId && pondId !== '0' && pondId.toLowerCase() !== 'nan') return pondId
  return String(item?.resourceCode || item?.resourceName || '').trim()
}

const resetPondGeometryStyles = () => {
  pondGeometryLayerMap.forEach((layer) => {
    layer?.setStyle?.(POND_MAP_STYLE)
  })
  activePondLayerKey = ''
}

const fitPondLayerBounds = (layer: any) => {
  if (!mapInstance || !layer) return
  const bounds = layer.getBounds?.()
  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [48, 48], maxZoom: 17 })
  }
}

const setActivePondGeometry = (key: string) => {
  resetPondGeometryStyles()
  if (!key) return
  const layer = pondGeometryLayerMap.get(key)
  if (!layer) return
  layer.setStyle?.(POND_MAP_ACTIVE_STYLE)
  if (layer.bringToFront) layer.bringToFront()
  activePondLayerKey = key
  fitPondLayerBounds(layer)
}

const pondDetailTitle = computed(() => {
  const d = pondDetail.value
  if (!d) return '坑塘详情'
  return String(d.resourceName || d.resourceCode || d.locationDesc || '坑塘详情').trim() || '坑塘详情'
})

const pondDetailCode = computed(() => {
  const code = String(pondDetail.value?.resourceCode || '').trim()
  return code || ''
})

const pondDetailTags = computed(() => {
  const d = pondDetail.value
  if (!d) return [] as { text: string; type: string }[]
  const tags: { text: string; type: string }[] = []
  if (d.ownershipType) tags.push({ text: d.ownershipType, type: 'ownership' })
  if (d.resourceType) tags.push({ text: d.resourceType, type: 'type' })
  if (d.usageStatus) tags.push({ text: d.usageStatus, type: 'status' })
  return tags
})

const pondBoundaryItems = computed(() => {
  const d = pondDetail.value
  return [
    { dir: '东', label: '东至', key: 'east', value: formatTextOrDash(d?.eastTo) },
    { dir: '南', label: '南至', key: 'south', value: formatTextOrDash(d?.southTo) },
    { dir: '西', label: '西至', key: 'west', value: formatTextOrDash(d?.westTo) },
    { dir: '北', label: '北至', key: 'north', value: formatTextOrDash(d?.northTo) }
  ]
})

const formatPondAreaLabel = (detail?: WaterPondSaveReqVO | null) => {
  if (!detail) return '-'
  const village = String(detail.villageName || '').trim()
  const code = String(detail.villageCode || '').trim()
  if (village && code) return `${village}（${code}）`
  return village || code || '-'
}

const runMapNameClickHandler = (handler: () => void | Promise<void>) => {
  try {
    void Promise.resolve(handler()).catch((error) => {
      console.error('[HomeMap] 点击设施名称打开详情失败', error)
    })
  } catch (error) {
    console.error('[HomeMap] 点击设施名称打开详情失败', error)
  }
}

const bindPermanentClickableTooltip = (marker: any, name: string, onClick: () => void | Promise<void>) => {
  const label = String(name || '').trim()
  if (!label || !marker?.bindTooltip) return
  marker.bindTooltip(label, {
    permanent: true,
    direction: 'top',
    offset: [0, -8],
    className: 'home-map__gis-label',
    interactive: true
  })
  const tooltip = marker.getTooltip?.()
  if (!tooltip || (tooltip as any).__yzClickBound) return
  ;(tooltip as any).__yzClickBound = true
  tooltip.on('click', (event: any) => {
    event?.originalEvent?.preventDefault?.()
    event?.originalEvent?.stopPropagation?.()
    runMapNameClickHandler(onClick)
  })
}

const renderFacilitiesOnMap = async (list: WaterFacilityMapItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  const latLngs: any[] = []
  for (const item of list) {
    const lat = typeof item.latitude === 'number' ? item.latitude : null
    const lng = typeof item.longitude === 'number' ? item.longitude : null
    if (lat == null || lng == null) continue
    latLngs.push([lat, lng])
    const marker = Leaflet.circleMarker([lat, lng], {
      radius: 6,
      color: '#13c2c2',
      weight: 2,
      fillColor: '#13c2c2',
      fillOpacity: 0.4
    }).addTo(markerLayer)
    const openDetail = async () => {
      const detail = await handleFacilityClick(item.id, item.facilityName)
      if (isCustomizeSelected.value) {
        await openCustomizeDialog(item as any, 'view', detail)
      }
    }
    bindPermanentClickableTooltip(marker, item.facilityName || '', openDetail)
    marker.on('click', () => {
      runMapNameClickHandler(openDetail)
    })
  }

  if (latLngs.length > 0) {
    const bounds = Leaflet.latLngBounds(latLngs)
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const renderSignboardsOnMap = async (list: SignboardAreaOverviewItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  const latLngs: any[] = []
  for (const item of list) {
    const lat = Number((item as any)?.latitude)
    const lng = Number((item as any)?.longitude)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) continue
    latLngs.push([lat, lng])

    const marker = Leaflet.circleMarker([lat, lng], {
      radius: 6,
      color: '#fa8c16',
      weight: 2,
      fillColor: '#fa8c16',
      fillOpacity: 0.35
    }).addTo(markerLayer)

    const name = String((item as any)?.signboardName || '').trim()
    const openDetail = () => {
      openSignboardDetail(item)
    }
    bindPermanentClickableTooltip(marker, name, openDetail)
    marker.on('click', () => {
      runMapNameClickHandler(openDetail)
    })
  }

  if (latLngs.length > 0) {
    const bounds = Leaflet.latLngBounds(latLngs)
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const renderPumpStationsOnMap = async (list: PumpStationAreaOverviewItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  const latLngs: any[] = []
  for (const item of list) {
    const lat = Number((item as any)?.latitude)
    const lng = Number((item as any)?.longitude)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) continue
    latLngs.push([lat, lng])

    const marker = Leaflet.circleMarker([lat, lng], {
      radius: 6,
      color: '#1677ff',
      weight: 2,
      fillColor: '#1677ff',
      fillOpacity: 0.35
    }).addTo(markerLayer)

    const name = String((item as any)?.pumpStationName || '').trim()
    const openDetail = async () => {
      await openPumpStationDetail(item)
    }
    bindPermanentClickableTooltip(marker, name, openDetail)
    marker.on('click', () => {
      runMapNameClickHandler(openDetail)
    })
  }

  if (latLngs.length > 0) {
    const bounds = Leaflet.latLngBounds(latLngs)
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const renderEmbankmentsOnMap = async (list: EmbankmentAreaOverviewItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  const latLngs: any[] = []
  for (const item of list) {
    const lat = Number((item as any)?.latitude)
    const lng = Number((item as any)?.longitude)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) continue
    latLngs.push([lat, lng])

    const marker = Leaflet.circleMarker([lat, lng], {
      radius: 6,
      color: '#52c41a',
      weight: 2,
      fillColor: '#52c41a',
      fillOpacity: 0.35
    }).addTo(markerLayer)

    const name = String((item as any)?.embankmentName || '').trim()
    const openDetail = async () => {
      await openEmbankmentDetail(item)
    }
    bindPermanentClickableTooltip(marker, name, openDetail)
    marker.on('click', () => {
      runMapNameClickHandler(openDetail)
    })
  }

  if (latLngs.length > 0) {
    const bounds = Leaflet.latLngBounds(latLngs)
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const renderFloodMaterialWarehousesOnMap = async (list: FloodMaterialWarehouseAreaOverviewItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  const latLngs: any[] = []
  for (const item of list) {
    const lat = Number((item as any)?.latitude)
    const lng = Number((item as any)?.longitude)
    if (!Number.isFinite(lat) || !Number.isFinite(lng)) continue
    latLngs.push([lat, lng])

    const marker = Leaflet.circleMarker([lat, lng], {
      radius: 6,
      color: '#722ed1',
      weight: 2,
      fillColor: '#722ed1',
      fillOpacity: 0.35
    }).addTo(markerLayer)

    const name = String((item as any)?.warehouseName || '').trim()
    const openDetail = async () => {
      await openFloodMaterialWarehouseDetail(item)
    }
    bindPermanentClickableTooltip(marker, name, openDetail)
    marker.on('click', () => {
      runMapNameClickHandler(openDetail)
    })
  }

  if (latLngs.length > 0) {
    const bounds = Leaflet.latLngBounds(latLngs)
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const renderIrrigationGeometriesOnMap = async (list: IrrigationDistrictAreaOverviewItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  let bounds: any = null
  for (const item of list) {
    const geoJsonText = String((item as any)?.geometryGeoJson || '').trim()
    if (!geoJsonText) continue
    let parsed: any
    try {
      parsed = JSON.parse(geoJsonText)
    } catch {
      continue
    }

    const layer = Leaflet.geoJSON(parsed, {
      style: {
        color: '#13c2c2',
        weight: 2,
        fillColor: '#13c2c2',
        fillOpacity: 0.18
      }
    }).addTo(markerLayer)

    const name = String((item as any)?.irrigationDistrictName || '').trim()
    if (name && layer?.bindTooltip) {
      layer.bindTooltip(name, {
        permanent: false,
        direction: 'top',
        className: 'home-map__gis-label'
      })
    }

    layer.on('click', async () => {
      await openIrrigationDistrictDetail(item)
    })

    const b = layer.getBounds ? layer.getBounds() : null
    if (b && b.isValid && b.isValid()) {
      bounds = bounds ? bounds.extend(b) : b
    }
  }

  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 15 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const renderPondGeometriesOnMap = async (list: WaterPondAreaOverviewItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  let bounds: any = null
  for (const item of list) {
    const geoJsonText = String(item?.geometryGeoJson || '').trim()
    if (!geoJsonText) continue
    let parsed: any
    try {
      parsed = JSON.parse(geoJsonText)
    } catch {
      continue
    }

    const layerKey = getPondLayerKey(item)
    const layer = Leaflet.geoJSON(parsed, {
      style: POND_MAP_STYLE
    }).addTo(markerLayer)
    if (layerKey) {
      pondGeometryLayerMap.set(layerKey, layer)
    }

    const name = String(item?.resourceName || item?.resourceCode || '').trim()
    if (name && layer?.bindTooltip) {
      layer.bindTooltip(name, {
        permanent: false,
        direction: 'top',
        className: 'home-map__gis-label'
      })
    }

    layer.on('mouseover', () => {
      if (layerKey !== activePondLayerKey) {
        layer.setStyle?.(POND_MAP_HOVER_STYLE)
      }
    })
    layer.on('mouseout', () => {
      layer.setStyle?.(layerKey === activePondLayerKey ? POND_MAP_ACTIVE_STYLE : POND_MAP_STYLE)
    })
    layer.on('click', async (e: any) => {
      e?.originalEvent?.stopPropagation?.()
      await openPondDetail(item)
    })

    const b = layer.getBounds ? layer.getBounds() : null
    if (b && b.isValid && b.isValid()) {
      bounds = bounds ? bounds.extend(b) : b
    }
  }

  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const loadPondFilterOptions = async () => {
  try {
    const data = await getWaterPondFilterOptions()
    pondOwnershipTypeOptions.value = data?.ownershipTypeList || []
    pondResourceTypeOptions.value = data?.resourceTypeList || []
    pondUsageStatusOptions.value = data?.usageStatusList || []
  } catch {
    pondOwnershipTypeOptions.value = []
    pondResourceTypeOptions.value = []
    pondUsageStatusOptions.value = []
  }
}

const buildPondOverviewParams = (areaId?: number): WaterPondMapOverviewReqVO => {
  const params: WaterPondMapOverviewReqVO = { ...pondFilterQuery.value }
  if (areaId != null) {
    params.areaId = areaId
  }
  return params
}

const buildPondPageParams = () => {
  const params: Record<string, any> = {
    pageNo: pondListPageNo.value,
    pageSize: pondListPageSize,
    ownershipType: pondFilterQuery.value.ownershipType,
    resourceType: pondFilterQuery.value.resourceType,
    usageStatus: pondFilterQuery.value.usageStatus,
    resourceNature: pondFilterQuery.value.resourceNature,
    occupationStatus: pondFilterQuery.value.occupationStatus
  }
  if (drawerMode.value === 'search') {
    const keyword = facilityNameKeyword.value.trim()
    if (keyword) params.resourceName = keyword
  } else if (selectedArea.value?.id != null) {
    params.villageCode = String(selectedArea.value.id)
  }
  return params
}

const fetchPondDrawerList = async () => {
  pondListLoading.value = true
  try {
    const data = (await getWaterPondPage(buildPondPageParams() as any)) as {
      list?: WaterPondPageRespVO[]
      total?: number
    }
    pondList.value = data?.list || []
    pondListTotal.value = Number(data?.total ?? 0)
  } catch {
    pondList.value = []
    pondListTotal.value = 0
  } finally {
    pondListLoading.value = false
  }
}

const resetPondListState = () => {
  pondList.value = []
  pondListTotal.value = 0
  pondListPageNo.value = 1
  pondListLoading.value = false
}

const fetchPondAreaOverview = async (areaId: number) => {
  const data = (await getWaterPondAreaOverviewByArea(buildPondOverviewParams(areaId))) as WaterPondAreaOverviewRespVO
  const list = (data?.list || []) as WaterPondAreaOverviewItemRespVO[]
  await renderPondGeometriesOnMap(list)
  // 地图层已持有几何，列表改分页接口，避免把全量 GeoJSON 挂在响应式数据上
  pondOverview.value = {
    totalCount: data?.totalCount ?? list.length,
    totalAreaSqm: data?.totalAreaSqm,
    totalAreaMu: data?.totalAreaMu,
    list: []
  }
  pondListPageNo.value = 1
  await fetchPondDrawerList()
}

const applyPondFilter = async () => {
  if (!selectedArea.value) return
  await fetchPondAreaOverview(selectedArea.value.id)
}

const resetPondFilter = async () => {
  pondFilterQuery.value = {}
  if (!selectedArea.value) return
  await fetchPondAreaOverview(selectedArea.value.id)
}

const fetchPondSearchOverview = async (keyword: string) => {
  const data = (await getWaterPondAreaOverviewByArea({
    resourceName: keyword
  })) as WaterPondAreaOverviewRespVO
  const list = (data?.list || []) as WaterPondAreaOverviewItemRespVO[]
  await renderPondGeometriesOnMap(list)
  pondOverview.value = {
    totalCount: data?.totalCount ?? list.length,
    totalAreaSqm: data?.totalAreaSqm,
    totalAreaMu: data?.totalAreaMu,
    list: []
  }
  pondListPageNo.value = 1
  await fetchPondDrawerList()
  return Number(data?.totalCount ?? list.length)
}

const toPondOverviewItem = (row: WaterPondPageRespVO | WaterPondAreaOverviewItemRespVO): WaterPondAreaOverviewItemRespVO => {
  const pageRow = row as WaterPondPageRespVO
  const overviewRow = row as WaterPondAreaOverviewItemRespVO
  return {
    pondId: overviewRow.pondId ?? pageRow.id,
    facilityBaseId: overviewRow.facilityBaseId ?? pageRow.facilityId,
    resourceName: row.resourceName,
    resourceCode: row.resourceCode,
    ownershipType: row.ownershipType,
    resourceType: row.resourceType,
    usageStatus: row.usageStatus,
    areaSqm: row.areaSqm,
    areaMu: row.areaMu,
    longitude: overviewRow.longitude ?? pageRow.centerLon,
    latitude: overviewRow.latitude ?? pageRow.centerLat,
    geometryGeoJson: overviewRow.geometryGeoJson
  }
}

const openPondDetail = async (row: WaterPondPageRespVO | WaterPondAreaOverviewItemRespVO) => {
  const item = toPondOverviewItem(row)
  const pondId = normalizeId(item?.pondId)
  const layerKey = getPondLayerKey(item)

  drawerVisible.value = false
  clearHighlight()
  setActivePondGeometry(layerKey)

  pondDetailVisible.value = true
  pondDetailLoading.value = true
  pondDetail.value = null

  try {
    if (pondId) {
      pondDetail.value = (await getWaterPondDetail(pondId)) as WaterPondSaveReqVO
      return
    }
    pondDetail.value = {
      resourceName: item?.resourceName,
      resourceCode: item?.resourceCode,
      ownershipType: item?.ownershipType,
      resourceType: item?.resourceType,
      usageStatus: item?.usageStatus,
      areaSqm: item?.areaSqm,
      areaMu: item?.areaMu,
      geometryGeoJson: item?.geometryGeoJson
    }
  } catch {
    pondDetail.value = {
      resourceName: item?.resourceName,
      resourceCode: item?.resourceCode,
      ownershipType: item?.ownershipType,
      resourceType: item?.resourceType,
      usageStatus: item?.usageStatus,
      areaSqm: item?.areaSqm,
      areaMu: item?.areaMu
    }
  } finally {
    pondDetailLoading.value = false
  }
}

const closePondDetail = () => {
  pondDetailVisible.value = false
  pondDetailLoading.value = false
  pondDetail.value = null
  clearHighlight()
  resetPondGeometryStyles()
}

watch(pondDetailVisible, (visible) => {
  if (!visible) {
    pondDetailLoading.value = false
    pondDetail.value = null
    clearHighlight()
    resetPondGeometryStyles()
  }
})

watch(drawerVisible, (visible) => {
  if (visible) closePondDetail()
})

watch(bufferResultVisible, (visible) => {
  if (visible) closePondDetail()
})

const handlePondRowClick = async (row: WaterPondPageRespVO | WaterPondAreaOverviewItemRespVO) => {
  await openPondDetail(row)
}

const normalizeId = (value: unknown): string => {
  const id = String(value ?? '').trim()
  if (!id || id === '0' || id.toLowerCase() === 'nan') return ''
  return id
}

const renderHighlightFromGeoJson = (geoJsonText: string, displayName: string) => {
  if (!Leaflet || !mapInstance) return
  const geoJson = String(geoJsonText || '').trim()
  if (!geoJson) return
  clearHighlight()
  let parsed: any
  try {
    parsed = JSON.parse(geoJson)
  } catch {
    return
  }
  highlightLayer = Leaflet.geoJSON(parsed, {
    style: { color: '#ff4d4f', weight: 4 },
    pointToLayer: (_f: any, latlng: any) =>
      Leaflet.circleMarker(latlng, {
        radius: 9,
        color: '#ff4d4f',
        weight: 4,
        fillColor: '#ff4d4f',
        fillOpacity: 0.35
      })
  }).addTo(mapInstance)
  const bounds = highlightLayer.getBounds ? highlightLayer.getBounds() : null
  if (bounds && bounds.isValid && bounds.isValid()) {
    const center = bounds.getCenter?.()
    if (center && displayName) {
      highlightLabelLayer = createPermanentLabel([center.lat, center.lng], displayName, 'top')
    }
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 16 })
  }
}

const handleFacilityClick = async (id: string | number, facilityName?: string) => {
  const normalizedId = normalizeId(id)
  if (!normalizedId) return
  clearHighlight()
  try {
    const detail: any = await getWaterFacilityDetail(normalizedId)
    const geoJsonText = detail?.geometryGeoJson
    if (!geoJsonText) return null
    const displayName = String(
      facilityName || detail?.facilityName || detail?.name || detail?.riverName || detail?.reservoirName || ''
    ).trim()
    renderHighlightFromGeoJson(geoJsonText, displayName)
    return detail as WaterFacilityDetailRespVO
  } catch {
    // 已由全局 axios 拦截器提示
  }
  return null
}
//endregion

const loadFacilityTypes = async () => {
  const list: any[] = (await getWaterFacilityDict('zd_sslb')) as any
  const options = list
    .filter(
      (it: any) =>
        it &&
        typeof it.value === 'string' &&
        typeof it.label === 'string' &&
        (it.status === undefined || it.status === 0)
    )
    .map((it: any) => ({ label: it.label, value: it.value }))
  // ????????????????????????
  if (!options.some((it: any) => String(it.value).toLowerCase() === 'customize')) {
    options.push({ label: '???', value: 'customize' })
  }
  facilityTypeOptions.value = options
}

const loadFacilityTypeLabels = async () => {
  const list: any[] = (await getWaterFacilityDictForLabel('zd_sslb')) as any
  const options = list
    .filter((it: any) => it && typeof it.value === 'string' && typeof it.label === 'string')
    .map((it: any) => ({ label: it.label, value: it.value }))
  facilityTypeLabelOptions.value = options
}

const loadRiverLevels = async () => {
  const list: any[] = (await getRiverDict('zd_hljb')) as any
  riverLevelOptions.value = (Array.isArray(list) ? list : [])
    .filter(
      (it: any) =>
        it &&
        typeof it.value === 'string' &&
        typeof it.label === 'string' &&
        (it.status === undefined || it.status === 0)
    )
    .map((it: any) => ({ label: it.label, value: it.value }))
}

const loadRiverDetailDicts = async () => {
  const [basinList, riverTypeList, ecoList, headLevelList] = await Promise.all([
    getRiverDict('zd_szly'),
    getRiverDict('zd_hdlx'),
    getRiverDict('zd_stlx'),
    getRiverDict('zd_hzjb')
  ])
  const normalize = (raw: any): DictOption[] =>
    (Array.isArray(raw) ? raw : [])
      .filter(
        (it: any) =>
          it &&
          typeof it.value === 'string' &&
          typeof it.label === 'string' &&
          (it.status === undefined || it.status === 0)
      )
      .map((it: any) => ({ label: it.label, value: it.value }))
  basinOptions.value = normalize(basinList)
  riverTypeOptions.value = normalize(riverTypeList)
  ecologyOptions.value = normalize(ecoList)
  headLevelOptions.value = normalize(headLevelList)
}

const loadReservoirDicts = async () => {
  const [scaleList, natureList, managementUnitList] = await Promise.all([
    getRiverDict('zd_skgm'),
    getRiverDict('zd_skxz'),
    getRiverDict('zd_gldw')
  ])
  const normalize = (raw: any): DictOption[] =>
    (Array.isArray(raw) ? raw : [])
      .filter(
        (it: any) =>
          it &&
          typeof it.value === 'string' &&
          typeof it.label === 'string' &&
          (it.status === undefined || it.status === 0)
      )
      .map((it: any) => ({ label: it.label, value: it.value }))
  reservoirScaleOptions.value = normalize(scaleList)
  reservoirNatureOptions.value = normalize(natureList)
  reservoirManagementUnitOptions.value = normalize(managementUnitList)
}

const loadPumpStationDicts = async () => {
  const [typeList, gradeList] = await Promise.all([getRiverDict('zd_bzlx'), getRiverDict('zd_gcdb')])
  const normalize = (raw: any): DictOption[] =>
    (Array.isArray(raw) ? raw : [])
      .filter(
        (it: any) =>
          it &&
          typeof it.value === 'string' &&
          typeof it.label === 'string' &&
          (it.status === undefined || it.status === 0)
      )
      .map((it: any) => ({ label: it.label, value: it.value }))
  pumpStationTypeOptions.value = normalize(typeList)
  engineeringGradeOptions.value = normalize(gradeList)
}

const loadEmbankmentDicts = async () => {
  const [bankSideList, typeList, formList, levelList] = await Promise.all([
    getRiverDict('zd_hlab'),
    getRiverDict('zd_dflx'),
    getRiverDict('zd_dfxs'),
    getRiverDict('zd_dfjb')
  ])
  const normalize = (raw: any): DictOption[] =>
    (Array.isArray(raw) ? raw : [])
      .filter(
        (it: any) =>
          it &&
          typeof it.value === 'string' &&
          typeof it.label === 'string' &&
          (it.status === undefined || it.status === 0)
      )
      .map((it: any) => ({ label: it.label, value: it.value }))
  riverBankSideOptions.value = normalize(bankSideList)
  embankmentTypeOptions.value = normalize(typeList)
  embankmentFormOptions.value = normalize(formList)
  embankmentLevelOptions.value = normalize(levelList)
}

const loadIrrigationDistrictDicts = async () => {
  const list = await getRiverDict('zd_gqlx')
  const normalize = (raw: any): DictOption[] =>
    (Array.isArray(raw) ? raw : [])
      .filter(
        (it: any) =>
          it &&
          typeof it.value === 'string' &&
          typeof it.label === 'string' &&
          (it.status === undefined || it.status === 0)
      )
      .map((it: any) => ({ label: it.label, value: it.value }))
  irrigationDistrictTypeOptions.value = normalize(list)
}

const loadSignboardDicts = async () => {
  const [whdw, gldw, qsdw] = await Promise.all([
    getRiverDict('zd_whdw'),
    getRiverDict('zd_gldw'),
    getRiverDict('zd_qsdw')
  ])
  const normalize = (raw: any): DictOption[] =>
    (Array.isArray(raw) ? raw : [])
      .filter(
        (it: any) =>
          it &&
          typeof it.value === 'string' &&
          typeof it.label === 'string' &&
          (it.status === undefined || it.status === 0)
      )
      .map((it: any) => ({ label: it.label, value: it.value }))
  signboardMaintenanceUnitOptions.value = normalize(whdw)
  signboardManagementUnitOptions.value = normalize(gldw)
  signboardOwnershipUnitOptions.value = normalize(qsdw)
}

const loadFeedbackDicts = async () => {
  const [feedbackTypeList, feedbackStatusList] = await Promise.all([
    getRiverDict('zd_fklx'),
    getRiverDict('zd_wtjd')
  ])
  const normalize = (raw: any): DictOption[] =>
    (Array.isArray(raw) ? raw : [])
      .filter(
        (it: any) =>
          it &&
          typeof it.value === 'string' &&
          typeof it.label === 'string' &&
          (it.status === undefined || it.status === 0)
      )
      .map((it: any) => ({ label: it.label, value: it.value }))
  feedbackTypeOptions.value = normalize(feedbackTypeList)
  feedbackStatusOptions.value = normalize(feedbackStatusList)
}

const buildDictMap = (list: DictOption[]) => {
  const map: Record<string, string> = {}
  for (const item of list) {
    if (!item?.value) continue
    map[item.value] = item.label
  }
  return map
}

const feedbackTypeLabelMap = computed(() => buildDictMap(feedbackTypeOptions.value))
const feedbackStatusLabelMap = computed(() => buildDictMap(feedbackStatusOptions.value))
const feedbackFilterActive = computed(
  () => feedbackTypeValues.value.length > 0 || feedbackStatusValues.value.length > 0
)

const findAreaIdByName = (keyword: string) => {
  const k = String(keyword || '').trim()
  if (!k) return null
  let hit: number | null = null
  const walk = (nodes?: AreaNodeRespVO[]) => {
    if (!nodes || hit != null) return
    for (const node of nodes) {
      if (!node) continue
      if (String(node.name || '').includes(k)) {
        hit = node.id
        return
      }
      if (node.children && node.children.length > 0) {
        walk(node.children)
      }
    }
  }
  walk(areaTree.value)
  return hit
}

const loadYizhengBoundary = async () => {
  let detail: AreaRespVO | null = null
  try {
    detail = (await getArea(321081)) as any
  } catch {
    detail = null
  }
  if (!detail?.gemo && !detail?.gemoGeoJson) {
    const fallbackId = findAreaIdByName('仪征')
    if (fallbackId) {
      try {
        detail = (await getArea(fallbackId)) as any
      } catch {
        detail = null
      }
    }
  }
  const gemoText = String(detail?.gemo || '').trim()
  const geoJsonText = String(detail?.gemoGeoJson || '').trim()
  const tryParseGeoJson = (text: string) => {
    if (!text) return null
    try {
      return JSON.parse(text)
    } catch {
      return null
    }
  }
  const parsed = tryParseGeoJson(gemoText) || tryParseGeoJson(geoJsonText)
  yizhengBoundaryGeoJson.value = parsed
}

const loadAreas = async () => {
  const list: any[] = (await getAreaTree()) as any
  areaTree.value = Array.isArray(list) ? (list as any) : []
  await loadYizhengBoundary()
}

const loadAreaCounts = async () => {
  if (!selectedFacilityType.value) {
    areaCountMap.value = {}
    return
  }
  const list: any[] = (await getWaterFacilityAreaCount(selectedFacilityType.value)) as any
  const map: Record<number, number> = {}
  for (const item of list) {
    const areaId = Number(item?.areaId)
    const count = Number(item?.count)
    if (!Number.isFinite(areaId) || !Number.isFinite(count)) continue
    map[areaId] = count
  }
  areaCountMap.value = map
}

const selectFacilityType = async (value: string) => {
  selectedFacilityType.value = value
  await handleFacilityTypeChanged()
}

const handleFacilityTypeChanged = async () => {
  areaKeyword.value = ''
  facilityNameKeyword.value = ''
  drawerMode.value = 'area'
  drawerVisible.value = false
  selectedArea.value = null
  facilityList.value = []
  riverOverview.value = null
  riverSearchList.value = []
  reservoirOverview.value = null
  signboardOverview.value = null
  pumpStationOverview.value = null
  embankmentOverview.value = null
  floodMaterialWarehouseOverview.value = null
  irrigationDistrictOverview.value = null
  pondOverview.value = null
  pondFilterQuery.value = {}
  resetPondListState()
  signboardDetailVisible.value = false
  signboardDetailLoading.value = false
  signboardDetail.value = null
  pumpStationDetailVisible.value = false
  pumpStationDetail.value = null
  embankmentDetailVisible.value = false
  embankmentDetail.value = null
  floodMaterialWarehouseDetailVisible.value = false
  floodMaterialWarehouseDetail.value = null
  irrigationDistrictDetailVisible.value = false
  irrigationDistrictDetail.value = null
  clearMarkers()
  clearHighlight()
  clearAreaHighlight()

  if (isPondSelected.value) {
    await loadPondFilterOptions()
  }

  await loadAreaCounts()
}

const openAreaDrawer = async (area: FlatArea) => {
  if (!selectedFacilityType.value) return
  drawerMode.value = 'area'
  selectedArea.value = area
  drawerVisible.value = true
  riverOverview.value = null
  riverSearchList.value = []
  reservoirOverview.value = null
  signboardOverview.value = null
  pumpStationOverview.value = null
  embankmentOverview.value = null
  floodMaterialWarehouseOverview.value = null
  irrigationDistrictOverview.value = null
  pondOverview.value = null
  resetPondListState()
  signboardDetailVisible.value = false
  signboardDetailLoading.value = false
  signboardDetail.value = null
  pumpStationDetailVisible.value = false
  pumpStationDetail.value = null
  embankmentDetailVisible.value = false
  embankmentDetail.value = null
  floodMaterialWarehouseDetailVisible.value = false
  floodMaterialWarehouseDetail.value = null
  irrigationDistrictDetailVisible.value = false
  irrigationDistrictDetail.value = null
  closePondDetail()
  try {
    await renderAreaHighlight(area.id)
    if (isRiverSelected.value) {
      await fetchRiverOverview(area.id)
      const riverList = ((riverOverview.value as any)?.list || []) as any[]
      const markers: WaterFacilityMapItemRespVO[] = riverList.map((it: any) => ({
        id: normalizeId(it?.facilityBaseId),
        facilityCode: '',
        facilityName: it?.riverName || '',
        facilityType: 'river',
        adminRegionCode: String(area.id),
        longitude: it?.longitude,
        latitude: it?.latitude,
        geomType: it?.riverLevelLabel || '河道'
      }))
      facilityList.value = markers
      await renderFacilitiesOnMap(markers)
      return
    }
    if (isReservoirSelected.value) {
      const data: any = await getReservoirAreaOverviewByArea(area.id)
      reservoirOverview.value = data as ReservoirAreaOverviewRespVO
      const list = (reservoirOverview.value?.list || []) as ReservoirAreaOverviewItemRespVO[]
      await renderReservoirGeometriesOnMap(list)
      return
    }
    if (isPumpStationSelected.value) {
      const data: any = await getPumpStationAreaOverviewByArea(area.id)
      pumpStationOverview.value = data as PumpStationAreaOverviewRespVO
      const list = (pumpStationOverview.value?.list || []) as PumpStationAreaOverviewItemRespVO[]
      await renderPumpStationsOnMap(list)
      return
    }
    if (isEmbankmentSelected.value) {
      const data: any = await getEmbankmentAreaOverviewByArea(area.id)
      embankmentOverview.value = data as EmbankmentAreaOverviewRespVO
      const list = (embankmentOverview.value?.list || []) as EmbankmentAreaOverviewItemRespVO[]
      await renderEmbankmentsOnMap(list)
      return
    }
    if (isFloodMaterialSelected.value) {
      const data: any = await getFloodMaterialWarehouseAreaOverviewByArea(area.id)
      floodMaterialWarehouseOverview.value = data as FloodMaterialWarehouseAreaOverviewRespVO
      const list = (floodMaterialWarehouseOverview.value?.list || []) as FloodMaterialWarehouseAreaOverviewItemRespVO[]
      await renderFloodMaterialWarehousesOnMap(list)
      return
    }
    if (isIrrigationSelected.value) {
      const data: any = await getIrrigationDistrictAreaOverviewByArea(area.id)
      irrigationDistrictOverview.value = data as IrrigationDistrictAreaOverviewRespVO
      const list = (irrigationDistrictOverview.value?.list || []) as IrrigationDistrictAreaOverviewItemRespVO[]
      await renderIrrigationGeometriesOnMap(list)
      return
    }
    if (isPondSelected.value) {
      await fetchPondAreaOverview(area.id)
      return
    }
    if (isSignboardSelected.value) {
      const data: any = await getSignboardAreaOverviewByArea(area.id)
      signboardOverview.value = data as SignboardAreaOverviewRespVO
      const list = (signboardOverview.value?.list || []) as SignboardAreaOverviewItemRespVO[]
      await renderSignboardsOnMap(list)
      return
    }
    const list: any[] = (await getWaterFacilityListByArea(selectedFacilityType.value, area.id)) as any
    facilityList.value = Array.isArray(list) ? (list as any) : []
    await renderFacilitiesOnMap(facilityList.value)
  } catch {
    // 已由全局 axios 拦截器提示
  }
}

const clearFacilityNameSearch = () => {
  facilityNameKeyword.value = ''
  if (drawerMode.value === 'search') {
    drawerVisible.value = false
    facilityList.value = []
    riverSearchList.value = []
    drawerMode.value = 'area'
  }
}

const searchPageSize = 100

const resolveSearchTotal = (total: number | undefined, listLength: number) => {
  const normalized = Number(total)
  if (Number.isFinite(normalized) && normalized > 0) return normalized
  return listLength
}

const fetchSearchMarkers = async (keyword: string) => {
  const list: any[] = (await getWaterFacilityListByName(selectedFacilityType.value, keyword, searchPageSize)) as any
  facilityList.value = Array.isArray(list) ? (list as any) : []
  await renderFacilitiesOnMap(facilityList.value)
}

const fetchRiverSearchList = async (keyword: string) => {
  riverSearchList.value = []
  facilityList.value = []
  const data: any = await getRiverAreaOverview()
  const list = ((data as any)?.list || []) as RiverAreaOverviewItemRespVO[]
  const filterKey = keyword.trim().toLowerCase()
  const filtered = list.filter((item) => String(item?.riverName || '').toLowerCase().includes(filterKey))
  riverSearchList.value = filtered
  const markers: WaterFacilityMapItemRespVO[] = filtered.map((it) => ({
    id: normalizeId(it?.facilityBaseId),
    facilityCode: '',
    facilityName: it?.riverName || '',
    facilityType: 'river',
    adminRegionCode: '',
    longitude: it?.longitude,
    latitude: it?.latitude,
    geomType: it?.riverLevelLabel || '河道'
  }))
  facilityList.value = markers
  await renderFacilitiesOnMap(markers)
  return filtered.length
}

const fetchReservoirSearchOverview = async (keyword: string) => {
  const resp = await getReservoirPage({ pageNo: 1, pageSize: searchPageSize, reservoirName: keyword })
  const list = (resp?.list || []) as ReservoirPageRespVO[]
  const items: ReservoirAreaOverviewItemRespVO[] = list.map((row) => ({
    reservoirId: (row as any)?.id,
    facilityBaseId: (row as any)?.facilityId,
    reservoirName: row?.reservoirName || row?.reservoirCode || '',
    reservoirScale: row?.reservoirScale,
    totalCapacity: row?.totalCapacity
  }))
  const scaleCountMap: Record<string, number> = {}
  let totalCapacity = 0
  for (const item of items) {
    const key = String(item?.reservoirScale || '').trim()
    if (key) {
      scaleCountMap[key] = (scaleCountMap[key] || 0) + 1
    }
    const capacity = Number(item?.totalCapacity)
    if (Number.isFinite(capacity)) {
      totalCapacity += capacity
    }
  }
  reservoirOverview.value = {
    totalCount: resolveSearchTotal(resp?.total, items.length),
    totalCapacity,
    reservoirScaleCountMap: scaleCountMap,
    list: items
  }
  return items.length
}

const fetchPumpStationSearchOverview = async (keyword: string) => {
  const resp = await getPumpStationPage({ pageNo: 1, pageSize: searchPageSize, pumpStationName: keyword })
  const list = (resp?.list || []) as PumpStationPageRespVO[]
  const items: PumpStationAreaOverviewItemRespVO[] = list.map((row) => ({
    pumpStationId: (row as any)?.id,
    facilityBaseId: (row as any)?.facilityId,
    pumpStationName: row?.pumpStationName || '',
    pumpStationType: (row as any)?.pumpStationType || row?.pumpStationTypeLabel || '',
    engineeringGrade: (row as any)?.engineeringGrade || row?.engineeringGradeLabel || ''
  }))
  const labelValueMap: Record<string, string> = {}
  for (const opt of pumpStationTypeOptions.value || []) {
    const label = String(opt?.label || '').trim()
    const value = String(opt?.value || '').trim()
    if (label && value) {
      labelValueMap[label] = value
    }
  }
  const typeCountMap: Record<string, number> = {}
  for (const row of list) {
    const label = String((row as any)?.pumpStationTypeLabel || (row as any)?.pumpStationType || '').trim()
    const key = labelValueMap[label] || label
    if (!key) continue
    typeCountMap[key] = (typeCountMap[key] || 0) + 1
  }
  pumpStationOverview.value = {
    totalCount: resolveSearchTotal(resp?.total, items.length),
    pumpStationTypeCountMap: typeCountMap,
    list: items
  }
  return items.length
}

const fetchEmbankmentSearchOverview = async (keyword: string) => {
  const resp = await getEmbankmentPage({ pageNo: 1, pageSize: searchPageSize, embankmentName: keyword })
  const list = (resp?.list || []) as EmbankmentPageRespVO[]
  const items: EmbankmentAreaOverviewItemRespVO[] = list.map((row) => ({
    embankmentId: (row as any)?.id,
    facilityBaseId: (row as any)?.facilityId,
    embankmentName: row?.embankmentName || '',
    embankmentForm: (row as any)?.embankmentForm || row?.embankmentFormLabel || ''
  }))
  embankmentOverview.value = {
    totalCount: resolveSearchTotal(resp?.total, items.length),
    list: items
  }
  return items.length
}

const fetchFloodMaterialWarehouseSearchOverview = async (keyword: string) => {
  const resp = await getFloodMaterialWarehousePage({ pageNo: 1, pageSize: searchPageSize, warehouseName: keyword })
  const list = (resp?.list || []) as FloodMaterialWarehousePageRespVO[]
  const items: FloodMaterialWarehouseAreaOverviewItemRespVO[] = list.map((row) => ({
    warehouseId: (row as any)?.id,
    warehouseName: row?.warehouseName || '',
    belongUnit: row?.belongUnit || ''
  }))
  floodMaterialWarehouseOverview.value = {
    totalCount: resolveSearchTotal(resp?.total, items.length),
    list: items
  }
  return items.length
}

const fetchIrrigationSearchOverview = async (keyword: string) => {
  const resp = await getIrrigationDistrictPage({ pageNo: 1, pageSize: searchPageSize, irrigationDistrictName: keyword })
  const list = (resp?.list || []) as IrrigationDistrictPageRespVO[]
  const items: IrrigationDistrictAreaOverviewItemRespVO[] = list.map((row) => ({
    irrigationDistrictId: (row as any)?.id,
    facilityBaseId: (row as any)?.facilityId,
    irrigationDistrictName: row?.irrigationDistrictName || '',
    actualIrrigableArea: row?.actualIrrigableArea,
    basicFarmlandAreaKm2: row?.basicFarmlandAreaKm2
  }))
  irrigationDistrictOverview.value = {
    totalCount: resolveSearchTotal(resp?.total, items.length),
    list: items
  }
  return items.length
}

const fetchSignboardSearchOverview = async (keyword: string) => {
  const resp = await getSignboardPage({ pageNo: 1, pageSize: searchPageSize, signboardName: keyword })
  const list = (resp?.list || []) as SignboardPageRespVO[]
  const detailList = await Promise.all(
    list.map(async (row) => {
      const id = normalizeId((row as any)?.id)
      if (!id) return null
      try {
        return (await getSignboardDetail(id)) as SignboardSaveReqVO
      } catch {
        return null
      }
    })
  )
  let riverCount = 0
  let reservoirCount = 0
  const items: SignboardAreaOverviewItemRespVO[] = list.map((row, index) => {
    const detail = detailList[index] as any
    const refType = String(detail?.referenceType || '').trim()
    const refTypeLabel = signboardReferenceTypeLabelMap[refType] || refType
    const refName =
      detail?.referenceName ||
      detail?.waterReservoirName ||
      detail?.riverSectionName ||
      detail?.riverChannelName ||
      ''
    if (refType === 'reservoir') {
      reservoirCount += 1
    } else if (refType) {
      riverCount += 1
    }
    return {
      signboardId: (row as any)?.id,
      signboardName: row?.signboardName || '',
      referenceType: refType,
      referenceTypeLabel: refTypeLabel,
      referenceName: refName,
      referenceId: detail?.referenceId,
      specificLocation: detail?.specificLocation || row?.specificLocation,
      maintenanceUnit: detail?.maintenanceUnit || row?.maintenanceUnit,
      longitude: detail?.longitude,
      latitude: detail?.latitude
    }
  })
  signboardOverview.value = {
    riverCount,
    reservoirCount,
    totalCount: resolveSearchTotal(resp?.total, items.length),
    list: items
  }
  return items.length
}

const handleFacilityNameSearch = async () => {
  if (!selectedFacilityType.value) return
  const keyword = facilityNameKeyword.value.trim()
  if (!keyword) {
    ElMessage.warning('请输入水利设施名称')
    return
  }

  facilitySearchLoading.value = true
  try {
    drawerMode.value = 'search'
    selectedArea.value = null
    drawerVisible.value = true
    riverOverview.value = null
    reservoirOverview.value = null
    signboardOverview.value = null
    pumpStationOverview.value = null
    embankmentOverview.value = null
    floodMaterialWarehouseOverview.value = null
    irrigationDistrictOverview.value = null
    pondOverview.value = null
    resetPondListState()
    riverSearchList.value = []
    clearAreaHighlight()
    closePondDetail()

    if (isRiverSelected.value) {
      const total = await fetchRiverSearchList(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的河道')
      }
      return
    }

    if (isPondSelected.value) {
      const total = await fetchPondSearchOverview(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的坑塘')
      }
      return
    }

    await fetchSearchMarkers(keyword)

    if (isReservoirSelected.value) {
      const total = await fetchReservoirSearchOverview(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的水库')
      }
      return
    }
    if (isIrrigationSelected.value) {
      const total = await fetchIrrigationSearchOverview(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的灌区')
      }
      return
    }
    if (isSignboardSelected.value) {
      const total = await fetchSignboardSearchOverview(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的公示牌')
      }
      return
    }
    if (isPumpStationSelected.value) {
      const total = await fetchPumpStationSearchOverview(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的泵站')
      }
      return
    }
    if (isEmbankmentSelected.value) {
      const total = await fetchEmbankmentSearchOverview(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的堤防')
      }
      return
    }
    if (isFloodMaterialSelected.value) {
      const total = await fetchFloodMaterialWarehouseSearchOverview(keyword)
      if (total === 0) {
        ElMessage.info('未找到匹配的防汛物资')
      }
      return
    }

    if (facilityList.value.length === 0) {
      ElMessage.info('未找到匹配的水利设施')
    }
  } catch {
    // 已由全局 axios 拦截器提示
  } finally {
    facilitySearchLoading.value = false
  }
}

const handleFacilityRowClick = async (row: WaterFacilityMapItemRespVO) => {
  const id = normalizeId(row?.id)
  if (!id) return
  const detail = await handleFacilityClick(id, row?.facilityName)
  if (isCustomizeSelected.value) {
    await openCustomizeDialog(row, 'view', detail)
  }
}

const fetchRiverOverview = async (areaId: number) => {
  const data: any = await getRiverAreaOverviewByArea(areaId)
  riverOverview.value = data as RiverAreaOverviewRespVO
}

const renderReservoirGeometriesOnMap = async (list: ReservoirAreaOverviewItemRespVO[]) => {
  if (!Leaflet || !mapInstance) return
  clearMarkers()
  clearHighlight()
  if (!list || list.length === 0) return

  let bounds: any = null
  for (const item of list) {
    const geoJsonText = String(item.geometryGeoJson || '').trim()
    if (!geoJsonText) continue
    let parsed: any
    try {
      parsed = JSON.parse(geoJsonText)
    } catch {
      continue
    }

    const layer = Leaflet.geoJSON(parsed, {
      style: {
        color: '#1677ff',
        weight: 2,
        fillColor: '#1677ff',
        fillOpacity: 0.15
      }
    }).addTo(markerLayer)

    const name = String(item.reservoirName || '').trim()
    if (name && layer?.bindTooltip) {
      layer.bindTooltip(name, {
        permanent: false,
        direction: 'top',
        className: 'home-map__gis-label'
      })
    }

    layer.on('click', async () => {
      await openReservoirDetail(item)
    })

    const b = layer.getBounds ? layer.getBounds() : null
    if (b && b.isValid && b.isValid()) {
      bounds = bounds ? bounds.extend(b) : b
    }
  }

  if (bounds && bounds.isValid && bounds.isValid()) {
    mapInstance.fitBounds(bounds, { padding: [40, 40], maxZoom: 15 })
  }
  await nextTick()
  mapInstance.invalidateSize()
}

const openReservoirDetailByFacility = async (
  reservoirId: string | number,
  facilityBaseId?: string | number,
  reservoirName?: string,
  presetDetail?: ReservoirSaveReqVO | null
) => {
  const normalizedId = normalizeId(reservoirId)
  if (!normalizedId) return
  const facilityId = normalizeId(facilityBaseId)
  if (facilityId) {
    await handleFacilityClick(facilityId, reservoirName || presetDetail?.reservoirName)
  }

  reservoirDetailVisible.value = true
  reservoirDetailLoading.value = true
  reservoirDetail.value = null
  reservoirHeadList.value = []
  try {
    const [detail, management] = await Promise.all([
      presetDetail ? Promise.resolve(presetDetail) : getReservoirDetail(normalizedId),
      getReservoirManagement(normalizedId)
    ])
    reservoirDetail.value = detail as any
    if (!facilityId) {
      const detailFacilityId = normalizeId((detail as any)?.facilityId)
      if (detailFacilityId) {
        await handleFacilityClick(detailFacilityId, reservoirName || (detail as any)?.reservoirName)
      }
    }

    const headLevelMap = buildDictMap(headLevelOptions.value)
    const reservoirNameText = String((detail as any)?.reservoirName || reservoirName || '').trim()
    const heads = ((management as any as ReservoirHeadItemReqVO[]) || []).map((h: any) => {
      const levelVal = String(h?.headLevel ?? '')
      return {
        headName: h?.headName || '',
        headLevelLabel: headLevelMap[levelVal] || levelVal,
        headPosition: h?.headPosition || '',
        headContact: h?.headContact || '',
        referenceTypeLabel: '水库',
        referenceName: reservoirNameText || '-'
      }
    })
    reservoirHeadList.value = heads
  } finally {
    reservoirDetailLoading.value = false
  }
}

const openReservoirDetail = async (row: ReservoirAreaOverviewItemRespVO) => {
  const reservoirId = normalizeId(row?.reservoirId)
  if (!reservoirId) return
  const facilityBaseId = normalizeId(row?.facilityBaseId)
  await openReservoirDetailByFacility(reservoirId, facilityBaseId, row?.reservoirName)
}

const handleSignboardRowClick = (row: SignboardAreaOverviewItemRespVO) => {
  openSignboardDetail(row)
}

const highlightSignboardLocation = (lat?: number, lng?: number, name?: string) => {
  if (!Leaflet || !mapInstance || !Number.isFinite(lat) || !Number.isFinite(lng)) {
    return
  }
  clearHighlight()
  highlightLayer = Leaflet.circleMarker([lat as number, lng as number], {
    radius: 10,
    color: '#ff4d4f',
    weight: 3,
    fillColor: '#ff4d4f',
    fillOpacity: 0.25
  }).addTo(mapInstance)
  const label = String(name || '').trim()
  if (label) {
    highlightLabelLayer = createPermanentLabel([lat as number, lng as number], label, 'top')
  }
  mapInstance.setView([lat as number, lng as number], 16)
}

const resolveSignboardReferenceInfo = async (detail: SignboardSaveReqVO | null) => {
  if (!detail) return {}
  const reservoirId = normalizeId((detail as any)?.waterReservoirId)
  const sectionId = normalizeId((detail as any)?.riverSectionId)
  const channelId = normalizeId((detail as any)?.riverChannelId)
  let referenceType = ''
  let referenceId: string | number | null = null
  if (reservoirId) {
    referenceType = 'reservoir'
    referenceId = reservoirId
  } else if (sectionId) {
    referenceType = 'river_section'
    referenceId = sectionId
  } else if (channelId) {
    referenceType = 'river'
    referenceId = channelId
  }
  if (!referenceType || !referenceId) {
    return {}
  }
  try {
    const ref = (await getSignboardReferenceDetail(referenceType, referenceId)) as SignboardReferenceDetailRespVO
    return {
      referenceType,
      referenceName: ref?.name || (referenceType === 'reservoir' ? (detail as any)?.waterReservoirName : '')
    }
  } catch {
    return {
      referenceType,
      referenceName: referenceType === 'reservoir' ? (detail as any)?.waterReservoirName : ''
    }
  }
}

const openSignboardDetailById = async (signboardId: string | number, fallbackName?: string) => {
  const id = normalizeId(signboardId)
  if (!id) return
  signboardDetailVisible.value = true
  signboardDetailLoading.value = true
  signboardDetail.value = null
  try {
    const detail = (await getSignboardDetail(id)) as SignboardSaveReqVO
    const referenceInfo = await resolveSignboardReferenceInfo(detail)
    signboardDetail.value = { ...detail, ...referenceInfo }
    const lat = Number((detail as any)?.latitude)
    const lng = Number((detail as any)?.longitude)
    const name = String((detail as any)?.signboardName || fallbackName || '').trim()
    highlightSignboardLocation(lat, lng, name)
  } finally {
    signboardDetailLoading.value = false
  }
}

const openSignboardDetail = (row: SignboardAreaOverviewItemRespVO) => {
  signboardDetail.value = row || null
  signboardDetailVisible.value = true
  signboardDetailLoading.value = false

  const lat = Number((row as any)?.latitude)
  const lng = Number((row as any)?.longitude)
  const name = String((row as any)?.signboardName || '').trim()
  highlightSignboardLocation(lat, lng, name)
}

const openPumpStationDetailByFacility = async (facilityBaseId: string | number, facilityName?: string) => {
  const facilityId = normalizeId(facilityBaseId)
  if (!facilityId) return
  await handleFacilityClick(facilityId, facilityName)

  pumpStationDetailVisible.value = true
  pumpStationDetailLoading.value = true
  pumpStationDetail.value = null
  try {
    const detail: any = await getPumpStationDetailByFacility(facilityId)
    pumpStationDetail.value = detail as any
  } finally {
    pumpStationDetailLoading.value = false
  }
}

const handlePumpStationRowClick = async (row: PumpStationAreaOverviewItemRespVO) => {
  await openPumpStationDetail(row)
}

const openPumpStationDetail = async (row: PumpStationAreaOverviewItemRespVO) => {
  const pumpStationId = normalizeId((row as any)?.pumpStationId)
  const facilityBaseId = normalizeId((row as any)?.facilityBaseId)
  if (!pumpStationId) return

  if (facilityBaseId) {
    await handleFacilityClick(facilityBaseId, (row as any)?.pumpStationName)
  }

  pumpStationDetailVisible.value = true
  pumpStationDetailLoading.value = true
  pumpStationDetail.value = null
  try {
    const detail: any = await getPumpStationDetail(pumpStationId)
    pumpStationDetail.value = detail as any

    const lat = Number((row as any)?.latitude ?? (detail as any)?.latitude)
    const lng = Number((row as any)?.longitude ?? (detail as any)?.longitude)
    if (Leaflet && mapInstance && Number.isFinite(lat) && Number.isFinite(lng)) {
      mapInstance.setView([lat, lng], 16)
    }
  } finally {
    pumpStationDetailLoading.value = false
  }
}

const handleEmbankmentRowClick = async (row: EmbankmentAreaOverviewItemRespVO) => {
  await openEmbankmentDetail(row)
}

const openEmbankmentDetail = async (row: EmbankmentAreaOverviewItemRespVO) => {
  const embankmentId = normalizeId((row as any)?.embankmentId)
  const facilityBaseId = normalizeId((row as any)?.facilityBaseId)
  if (!embankmentId) return

  if (facilityBaseId) {
    await handleFacilityClick(facilityBaseId, (row as any)?.embankmentName)
  }

  embankmentDetailVisible.value = true
  embankmentDetailLoading.value = true
  embankmentDetail.value = null
  try {
    const detail: any = await getEmbankmentDetail(embankmentId)
    embankmentDetail.value = detail as any

    const lat = Number((row as any)?.latitude ?? (detail as any)?.latitude)
    const lng = Number((row as any)?.longitude ?? (detail as any)?.longitude)
    if (Leaflet && mapInstance && Number.isFinite(lat) && Number.isFinite(lng)) {
      mapInstance.setView([lat, lng], 16)
    }
  } finally {
    embankmentDetailLoading.value = false
  }
}

const handleFloodMaterialWarehouseRowClick = async (row: FloodMaterialWarehouseAreaOverviewItemRespVO) => {
  await openFloodMaterialWarehouseDetail(row)
}

const openFloodMaterialWarehouseDetail = async (row: FloodMaterialWarehouseAreaOverviewItemRespVO) => {
  const warehouseId = normalizeId((row as any)?.warehouseId)
  if (!warehouseId) return

  floodMaterialWarehouseDetailVisible.value = true
  floodMaterialWarehouseDetailLoading.value = true
  floodMaterialWarehouseDetail.value = null
  try {
    const detail: any = await getFloodMaterialWarehouseDetail(warehouseId)
    floodMaterialWarehouseDetail.value = detail as any

    const lat = Number((row as any)?.latitude ?? (detail as any)?.latitude)
    const lng = Number((row as any)?.longitude ?? (detail as any)?.longitude)
    if (!Leaflet || !mapInstance || !Number.isFinite(lat) || !Number.isFinite(lng)) {
      return
    }
    clearHighlight()
    highlightLayer = Leaflet.circleMarker([lat, lng], {
      radius: 10,
      color: '#ff4d4f',
      weight: 3,
      fillColor: '#ff4d4f',
      fillOpacity: 0.25
    }).addTo(mapInstance)
    const name = String((row as any)?.warehouseName || (detail as any)?.warehouseName || '').trim()
    if (name) {
      highlightLabelLayer = createPermanentLabel([lat, lng], name, 'top')
    }
    mapInstance.setView([lat, lng], 16)
  } finally {
    floodMaterialWarehouseDetailLoading.value = false
  }
}

const handleIrrigationDistrictRowClick = async (row: IrrigationDistrictAreaOverviewItemRespVO) => {
  await openIrrigationDistrictDetail(row)
}

const openIrrigationDistrictDetailByFacility = async (
  irrigationId: string | number,
  facilityBaseId?: string | number,
  irrigationName?: string,
  presetDetail?: IrrigationDistrictSaveReqVO | null
) => {
  const normalizedId = normalizeId(irrigationId)
  if (!normalizedId) return
  const facilityId = normalizeId(facilityBaseId)
  if (facilityId) {
    await handleFacilityClick(facilityId, irrigationName || presetDetail?.irrigationDistrictName)
  }

  irrigationDistrictDetailVisible.value = true
  irrigationDistrictDetailLoading.value = true
  irrigationDistrictDetail.value = null
  try {
    const detail: any = await (presetDetail ? Promise.resolve(presetDetail) : getIrrigationDistrictDetail(normalizedId))
    irrigationDistrictDetail.value = detail as any
    if (!facilityId) {
      const detailFacilityId = normalizeId((detail as any)?.facilityId)
      if (detailFacilityId) {
        await handleFacilityClick(detailFacilityId, irrigationName || detail?.irrigationDistrictName)
      }
    }
  } finally {
    irrigationDistrictDetailLoading.value = false
  }
}

const openIrrigationDistrictDetail = async (row: IrrigationDistrictAreaOverviewItemRespVO) => {
  const irrigationId = normalizeId((row as any)?.irrigationDistrictId)
  if (!irrigationId) return
  const facilityBaseId = normalizeId((row as any)?.facilityBaseId)
  await openIrrigationDistrictDetailByFacility(irrigationId, facilityBaseId, (row as any)?.irrigationDistrictName)
}

const openRiverSectionDetailByFacility = async (
  sectionId: string | number,
  facilityBaseId?: string | number,
  sectionName?: string,
  presetDetail?: RiverSectionDetailRespVO | null
) => {
  const normalizedId = normalizeId(sectionId)
  if (!normalizedId) return
  const facilityId = normalizeId(facilityBaseId)
  if (facilityId) {
    await handleFacilityClick(facilityId, sectionName || presetDetail?.sectionName)
  }

  riverSectionDetailVisible.value = true
  riverSectionDetailLoading.value = true
  riverSectionDetail.value = null
  riverSectionHeadList.value = []
  try {
    const detail =
      presetDetail || (facilityId ? ((await getRiverSectionDetailByFacility(facilityId)) as any) : null)
    if (!detail) return
    riverSectionDetail.value = detail as any

    const channelId = normalizeId((detail as any)?.riverChannelId)
    if (!channelId) return
    const management = (await getRiverManagement(channelId)) as any
    const headLevelMap = buildDictMap(headLevelOptions.value)
    const sections = (management as any as RiverManagementSectionDetailVO[]) || []
    const matched = sections.find((sec) => String(sec?.sectionId || '') === normalizedId)
    const sectionName = String((detail as any)?.sectionName || (matched as any)?.sectionName || '').trim()
    const heads = ((matched as any)?.heads || []).map((h: any) => {
      const levelVal = String(h?.headLevel ?? '')
      return {
        headName: h?.headName || '',
        headLevelLabel: headLevelMap[levelVal] || levelVal,
        headPosition: h?.headPosition || '',
        headContact: h?.headContact || '',
        referenceTypeLabel: '河段',
        referenceName: sectionName || '-'
      }
    })
    riverSectionHeadList.value = heads
  } finally {
    riverSectionDetailLoading.value = false
  }
}

const handleRiverRowClick = async (row: any) => {
  const id = normalizeId(row?.facilityBaseId)
  if (!id) return
  await handleFacilityClick(id, row?.riverName)
}

const openRiverDetailByChannel = async (
  riverId: string | number,
  facilityBaseId?: string | number,
  riverName?: string,
  presetDetail?: RiverChannelDetailRespVO | null
) => {
  const normalizedId = normalizeId(riverId)
  if (!normalizedId) return
  const facilityId = normalizeId(facilityBaseId)
  if (facilityId) await handleFacilityClick(facilityId, riverName)
  riverDetailVisible.value = true
  riverDetailLoading.value = true
  riverDetail.value = null
  riverHeadList.value = []
  try {
    const [detail, management] = await Promise.all([
      presetDetail ? Promise.resolve(presetDetail) : getRiverChannelDetail(normalizedId),
      getRiverManagement(normalizedId)
    ])
    riverDetail.value = detail as any
    const headLevelMap = buildDictMap(headLevelOptions.value)
    const sections = (management as any as RiverManagementSectionDetailVO[]) || []
    const riverNameText = String((detail as any)?.riverName || riverName || '').trim()
    const heads: HeadDisplayItem[] = []
    for (const sec of sections) {
      const list = (sec as any)?.heads || []
      const sectionId = normalizeId((sec as any)?.sectionId)
      const sectionName = String((sec as any)?.sectionName || '').trim()
      const isSection = Boolean(sectionId)
      for (const h of list) {
        const levelVal = String(h?.headLevel ?? '')
        const headSectionName = String(h?.sectionName || '').trim()
        const resolvedSectionName = sectionName || headSectionName
        heads.push({
          headName: h?.headName || '',
          headLevelLabel: headLevelMap[levelVal] || levelVal,
          headPosition: h?.headPosition || '',
          headContact: h?.headContact || '',
          referenceTypeLabel: isSection ? '河段' : '河道',
          referenceName: isSection ? resolvedSectionName || riverNameText || '-' : riverNameText || resolvedSectionName || '-'
        })
      }
    }
    riverHeadList.value = heads
  } finally {
    riverDetailLoading.value = false
  }
}

const openRiverDetail = async (row: any) => {
  const riverId = normalizeId(row?.riverId)
  if (!riverId) return
  const facilityBaseId = normalizeId(row?.facilityBaseId)
  await openRiverDetailByChannel(riverId, facilityBaseId, row?.riverName)
}

const riverDetailView = computed(() => {
  const detail = riverDetail.value as any
  if (!detail) return null
  const basinMap = buildDictMap(basinOptions.value)
  const riverTypeMap = buildDictMap(riverTypeOptions.value)
  const ecoMap = buildDictMap(ecologyOptions.value)
  const levelMap = buildDictMap(riverLevelOptions.value)
  const riverTypeArr: string[] = Array.isArray(detail.riverType) ? detail.riverType : []
  return {
    riverName: detail.riverName || '',
    lengthKm: formatKm(detail.lengthKm),
    startPoint: detail.startPoint || '',
    endPoint: detail.endPoint || '',
    catchmentKm2: formatKm2(detail.catchmentKm2),
    basinLabel: basinMap[String(detail.basinType || '')] || String(detail.basinType || ''),
    riverTypeLabel: riverTypeArr.map((v) => riverTypeMap[v] || v).filter(Boolean).join('、'),
    ecologyLabel: ecoMap[String(detail.ecologyType || '')] || String(detail.ecologyType || ''),
    riverLevelLabel: levelMap[String(detail.riverLevel || '')] || String(detail.riverLevel || ''),
    remarks: detail.remarks || '',
    adminRegion: formatAdminRegionNames(detail.town)
  }
})

const riverSectionList = computed<RiverSectionDetailRespVO[]>(() => {
  const detail = riverDetail.value as any
  const list = (detail?.sections || []) as RiverSectionDetailRespVO[]
  return Array.isArray(list) ? list : []
})

const riverSectionNamesText = computed(() => {
  const names = riverSectionList.value
    .map((item) => String(item?.sectionName || '').trim())
    .filter((name) => name)
  return names.join('、')
})

const signboardReferenceTypeLabelMap: Record<string, string> = {
  river: '河道',
  river_section: '河段',
  reservoir: '水库'
}

const formatSignboardUnits = (values: any, labelMap: Record<string, string>) => {
  const list = Array.isArray(values) ? values : values ? [values] : []
  if (list.length === 0) return ''
  const labels = list
    .map((item) => {
      const key = String(item ?? '').trim()
      return labelMap[key] || key
    })
    .filter((item) => item)
  return labels.join('、')
}

const signboardDetailView = computed(() => {
  const detail = signboardDetail.value as any
  if (!detail) return null
  const refType = String(detail.referenceType || '').trim()
  const refTypeLabel = detail.referenceTypeLabel || signboardReferenceTypeLabelMap[refType] || refType
  const refName =
    detail.referenceName ||
    detail.waterReservoirName ||
    detail.riverChannelName ||
    detail.riverSectionName ||
    ''
  const referenceText =
    refTypeLabel && refName ? `${refTypeLabel}：${refName}` : refName || refTypeLabel || ''
  return {
    signboardName: detail.signboardName,
    signboardTypeLabel: detail.signboardTypeLabel || detail.signboardType,
    referenceTypeLabel: refTypeLabel,
    referenceName: refName,
    referenceText,
    specificLocation: detail.specificLocation,
    adminRegion: formatAdminRegionNames(detail.adminRegion),
    maintenanceUnit: formatSignboardUnits(detail.maintenanceUnit, signboardMaintenanceUnitLabelMap.value),
    responsiblePerson: detail.responsiblePerson,
    managementUnit: formatSignboardUnits(detail.managementUnit, signboardManagementUnitLabelMap.value),
    ownershipUnit: formatSignboardUnits(detail.ownershipUnit, signboardOwnershipUnitLabelMap.value),
    longitude: detail.longitude,
    latitude: detail.latitude
  }
})

const reservoirDetailView = computed(() => {
  const detail = reservoirDetail.value as any
  if (!detail) return null
  const scaleMap = reservoirScaleLabelMap.value
  const natureMap = reservoirNatureLabelMap.value
  const managementUnitMap = reservoirManagementUnitLabelMap.value
  const scaleVal = String(detail.reservoirScale || '').trim()
  const natureVal = String(detail.reservoirNature || '').trim()
  const rawManagementUnit = Array.isArray(detail.managementUnit) ? detail.managementUnit : detail.managementUnit ? [detail.managementUnit] : []
  const managementUnitLabel = rawManagementUnit
    .map((val: any) => {
      const key = String(val || '').trim()
      return managementUnitMap[key] || key
    })
    .filter(Boolean)
    .join('、')
  const adminRegionText = formatAdminRegionNames(detail.townshipName || detail.township)
  return {
    reservoirName: detail.reservoirName || '',
    reservoirScaleLabel: scaleMap[scaleVal] || scaleVal,
    managementUnitLabel,
    reservoirNatureLabel: natureMap[natureVal] || natureVal,
    totalCapacity: detail.totalCapacity,
    activeCapacity: detail.activeCapacity,
    deadLevel: detail.deadLevel,
    adminRegion: adminRegionText
  }
})

// 兼容 text[] 与 text 形式的行政区划字段
const normalizeAdminRegionCodes = (raw: any): string[] => {
  if (!raw) return []
  if (Array.isArray(raw)) {
    return raw.map((item) => String(item ?? '').trim()).filter(Boolean)
  }
  const text = String(raw ?? '').trim()
  if (!text) return []
  let cleaned = text
  if (cleaned.startsWith('{') && cleaned.endsWith('}')) {
    cleaned = cleaned.slice(1, -1)
  }
  const parts = cleaned
    .split(',')
    .map((item) => item.replace(/^"|"$/g, '').trim())
    .filter(Boolean)
  return parts.length ? parts : [text]
}

const formatAdminRegionNames = (raw: any) => {
  const list = normalizeAdminRegionCodes(raw)
  if (!list.length) return '-'
  const map = systemAreaNameMap.value
  const names = list.map((code) => map[code] || code).filter(Boolean)
  return names.join('、') || '-'
}

const formatTextOrDash = (val: any) => {
  const text = String(val ?? '').trim()
  return text ? text : '-'
}

const formatManagementUnitLabels = (units: any) => {
  const list = Array.isArray(units) ? units : units ? [units] : []
  if (list.length === 0) return ''
  const map = reservoirManagementUnitLabelMap.value
  const labels = list
    .map((item) => {
      const key = String(item ?? '').trim()
      return map[key] || key
    })
    .filter((item) => item)
  return labels.join('、')
}

const formatWithUnit = (val: any, unit: string) => {
  const text = String(val ?? '').trim()
  if (!text || text === '-') return '-'
  return `${text}${unit}`
}

const formatNumber = (val: any) => {
  const n = Number(val)
  if (!Number.isFinite(n)) return '-'
  return n.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
}

const formatCompactNumber = (val: any) => {
  const n = Number(val)
  if (!Number.isFinite(n)) return '0'
  if (n >= 10000) {
    return `${(n / 10000).toLocaleString('zh-CN', { maximumFractionDigits: 2 })}万`
  }
  return n.toLocaleString('zh-CN', { maximumFractionDigits: n >= 100 ? 0 : 2 })
}

const formatYesNo = (val: any) => {
  const n = Number(val)
  if (n === 1) return '是'
  if (n === 0) return '否'
  return '-'
}

onMounted(async () => {
  try {
    await Promise.all([
      loadLeafletAssets(),
      loadFacilityTypes(),
      loadFacilityTypeLabels(),
      loadRiverLevels(),
      loadRiverDetailDicts(),
      loadReservoirDicts(),
      loadPumpStationDicts(),
      loadEmbankmentDicts(),
      loadIrrigationDistrictDicts(),
      loadSignboardDicts(),
      loadFeedbackDicts(),
      loadAreas()
    ])
    await initMap()
    renderYizhengBoundary()
    await applyFeedbackFilters()
    mapLoading.value = false
  } catch (e: any) {
    mapLoading.value = false
    ElMessage.error(e?.message || '地图初始化失败')
  }
})

onBeforeUnmount(() => {
  clearMarkers()
  clearHighlight()
  clearAreaHighlight()
  clearFeedbackMarkers()
  clearBufferLayer()
  clearBufferCenterMarker()
  clearYizhengBoundary()
  disableBufferPick(false)
  stopMeasure()
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})
</script>

<style scoped>
.home-map {
  position: relative;
  width: 100%;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}

.home-map__map {
  width: 100%;
  height: 100%;
}

.home-map__panel {
  position: absolute;
  top: 14px;
  left: 14px;
  z-index: 1000;
  width: 428px;
  background: linear-gradient(180deg, rgba(250, 253, 255, 0.96) 0%, rgba(241, 248, 255, 0.96) 100%);
  border: 1px solid #d2e3f7;
  border-radius: 14px;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.22);
  backdrop-filter: blur(6px);
  overflow: hidden;
}

.home-map__panel-reveal {
  position: absolute;
  top: 14px;
  left: 14px;
  z-index: 1000;
}

.home-map__panel-reveal-btn.el-button {
  height: 34px;
  border-radius: 8px;
  --el-button-bg-color: rgba(255, 255, 255, 0.96);
  --el-button-border-color: #cfe0f5;
  --el-button-text-color: #27527e;
  --el-button-hover-bg-color: #f3f8ff;
  --el-button-hover-border-color: #b8cfe9;
}

.home-map__measure-tool {
  position: absolute;
  top: 14px;
  right: 14px;
  z-index: 1001;
  padding: 10px;
  background: rgba(255, 255, 255, 0.96);
  border: 1px solid #e6f0ff;
  border-radius: 6px;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12);
  max-width: none;
}

.home-map__measure-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.home-map__measure-dropdown-icon {
  margin-left: 4px;
}

.home-map__measure-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #606266;
  line-height: 18px;
}

:global(.home-map__measure-label) {
  background: rgba(250, 140, 22, 0.92);
  color: #fff;
  border: 0;
  border-radius: 4px;
  padding: 4px 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.18);
  font-size: 12px;
  font-weight: 600;
}

.home-map__buffer-dialog :deep(.el-dialog) {
  width: min(392px, calc(100vw - 24px)) !important;
  border-radius: 14px;
  border: 1px solid #d6e3f5;
  background: linear-gradient(180deg, #f9fcff 0%, #f3f8ff 100%);
  box-shadow: 0 20px 46px rgba(15, 23, 42, 0.24);
  overflow: hidden;
}

.home-map__buffer-dialog :deep(.el-dialog__header) {
  padding: 0;
  margin: 0;
}

.home-map__buffer-dialog :deep(.el-dialog__body) {
  padding: 0;
}

.home-map__buffer-dialog :deep(.el-dialog__footer) {
  padding: 0;
}

.home-map__buffer-dialog :deep(.el-overlay) {
  pointer-events: none;
}

.home-map__buffer-dialog :deep(.el-overlay .el-dialog) {
  pointer-events: auto;
}

.home-map__buffer-dialog :deep(.el-dialog__wrapper) {
  pointer-events: none;
}

.home-map__buffer-dialog :deep(.el-dialog__wrapper .el-dialog) {
  pointer-events: auto;
}

.home-map__buffer-dialog-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(90deg, #0f4a89 0%, #1e63aa 100%);
  color: #fff;
  padding: 10px 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.16);
}

.home-map__buffer-dialog-title {
  font-weight: 700;
  letter-spacing: 0.4px;
  font-size: 15px;
}

.home-map__buffer-close.el-button {
  --el-button-text-color: #fff;
  --el-button-hover-text-color: #fff;
  --el-button-active-text-color: #fff;
  padding: 4px;
  border-radius: 6px;
  transition: background-color 0.2s ease;
}

.home-map__buffer-close.el-button:hover {
  background: rgba(255, 255, 255, 0.18);
}

.home-map__buffer-submit.el-button {
  --el-button-bg-color: #1d4f8f;
  --el-button-border-color: #1d4f8f;
  --el-button-hover-bg-color: #164279;
  --el-button-hover-border-color: #164279;
  --el-button-active-bg-color: #123867;
  --el-button-active-border-color: #123867;
  --el-button-text-color: #fff;
  --el-button-hover-text-color: #fff;
  --el-button-active-text-color: #fff;
  min-width: 102px;
}

.home-map__buffer-body {
  padding: 14px 16px 10px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-map__buffer-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.home-map__buffer-row--top {
  align-items: flex-start;
}

.home-map__buffer-label {
  min-width: 74px;
  color: #234a71;
  font-weight: 600;
}

.home-map__buffer-input {
  flex: 1;
}

.home-map__buffer-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #d7e4f4 inset;
  transition: box-shadow 0.2s ease;
}

.home-map__buffer-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #2f78d8 inset;
}

.home-map__buffer-input :deep(.el-input-group__append) {
  background: #f4f8ff;
  color: #3d5f82;
  border-color: #d7e4f4;
}

.home-map__buffer-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1;
  background: rgba(240, 246, 255, 0.7);
  border: 1px solid #dce9f8;
  border-radius: 10px;
  padding: 8px 10px;
}

.home-map__buffer-options :deep(.el-checkbox) {
  margin-right: 0;
  min-height: 22px;
}

.home-map__buffer-options :deep(.el-checkbox-group) {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 6px 10px;
}

.home-map__buffer-options :deep(.el-checkbox__label) {
  color: #264769;
  font-size: 12px;
}

.home-map__buffer-center {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-height: 34px;
  padding: 0 10px;
  border: 1px dashed #c2d7f0;
  border-radius: 8px;
  background: rgba(248, 252, 255, 0.9);
}

.home-map__buffer-center-placeholder {
  color: #8a97a8;
}

.home-map__buffer-tip {
  font-size: 12px;
  line-height: 18px;
  color: #4a6281;
  background: #eff5ff;
  border: 1px solid #d8e6f6;
  border-left: 3px solid #6f96c2;
  border-radius: 8px;
  padding: 8px 10px;
}

.home-map__buffer-footer {
  display: flex;
  justify-content: space-between;
  padding: 10px 16px 14px;
  border-top: 1px solid #e2ecf8;
  background: rgba(248, 252, 255, 0.85);
}

.home-map__buffer-result {
  padding: 2px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.home-map__buffer-kpi {
  display: flex;
  flex-direction: column;
  gap: 0;
  background: linear-gradient(180deg, #edf4ff 0%, #e6f1ff 100%);
  padding: 10px 12px;
  border-radius: 10px;
  border: 1px solid #d6e4f6;
}

.home-map__buffer-kpi-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  padding: 6px 0;
  border-top: 1px dashed rgba(111, 150, 194, 0.32);
}

.home-map__buffer-kpi-row:first-child {
  border-top: none;
  padding-top: 2px;
}

.home-map__buffer-kpi-label {
  color: #375575;
}

.home-map__buffer-kpi-value {
  color: #173a62;
  font-weight: 700;
}

.home-map__buffer-section {
  border: 1px solid #dbe7f6;
  border-radius: 10px;
  background: #fff;
  padding: 10px;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.06);
}

.home-map__buffer-section-title {
  font-weight: 700;
  color: #224d78;
  border-radius: 8px;
  padding: 7px 10px;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  background: linear-gradient(90deg, #eaf2ff 0%, #f4f8ff 100%);
  border: 1px solid #d8e5f6;
}

.home-map__buffer-section-title::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: #2f78d8;
  margin-right: 6px;
}

.home-map__buffer-area-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.home-map__buffer-area-table {
  border: 1px solid #dbe7f6;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.home-map__buffer-area-table table {
  width: 100%;
  border-collapse: collapse;
}

.home-map__buffer-area-table td {
  width: 33.3333%;
  padding: 8px 10px;
  border-bottom: 1px solid #edf3fb;
  border-right: 1px solid #edf3fb;
  font-size: 12px;
  color: #2e4f70;
}

.home-map__buffer-area-table tr:last-child td {
  border-bottom: none;
}

.home-map__buffer-area-table td:last-child {
  border-right: none;
}

.home-map__buffer-area-table tr:nth-child(even) td {
  background: #f9fbff;
}

.home-map__buffer-object-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 10px;
}

.home-map__buffer-object-item {
  background: linear-gradient(180deg, #f3f8ff 0%, #edf5ff 100%);
  border-radius: 8px;
  padding: 10px 6px;
  text-align: center;
  cursor: pointer;
  border: 1px solid #d9e6f7;
  transition: all 0.2s ease;
}

.home-map__buffer-object-item:hover {
  border-color: #8fb7e4;
  background: linear-gradient(180deg, #ecf4ff 0%, #e3efff 100%);
  box-shadow: 0 6px 14px rgba(59, 130, 246, 0.18);
}

.home-map__buffer-object-item.is-active {
  background: linear-gradient(180deg, #dcecff 0%, #d3e7ff 100%);
  border-color: #3b82f6;
}

.home-map__buffer-object-count {
  font-size: 16px;
  font-weight: 700;
  color: #1f62b4;
}

.home-map__buffer-object-label {
  margin-top: 4px;
  font-size: 12px;
  color: #234a71;
}

.home-map__buffer-empty {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  min-height: 48px;
  color: #8b97a8;
  font-size: 12px;
  background: #f8fbff;
  border: 1px dashed #d8e5f6;
  border-radius: 8px;
}

.home-map__buffer-section :deep(.el-table) {
  border: 1px solid #dbe7f6;
  border-radius: 8px;
  overflow: hidden;
}

.home-map__buffer-section :deep(.el-table th.el-table__cell) {
  background: linear-gradient(180deg, #eef5ff 0%, #f7fbff 100%);
  color: #1d466f;
  font-weight: 600;
}

.home-map__buffer-section :deep(.el-table td.el-table__cell) {
  border-bottom-color: #edf3fb;
}

.home-map__buffer-section :deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.home-map__buffer-section :deep(.el-table__row:hover > td.el-table__cell) {
  background: #eef5ff !important;
}

.home-map__buffer-section :deep(.el-pagination) {
  justify-content: flex-end;
}

.home-map__buffer-pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.home-map__buffer-detail-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-map__feedback-entry {
  position: absolute;
  right: 20px;
  bottom: 20px;
  z-index: 1001;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: #ffffff;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.home-map__feedback-entry img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  display: block;
}

:global(.home-map__feedback-cluster-wrapper) {
  background: transparent;
  border: none;
}

:global(.home-map__feedback-cluster) {
  position: relative;
  width: 36px;
  height: 36px;
}

:global(.home-map__feedback-cluster-icon) {
  width: 36px;
  height: 36px;
  display: block;
}

:global(.home-map__feedback-cluster-count) {
  position: absolute;
  top: -6px;
  right: -6px;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: #f56c6c;
  color: #fff;
  border-radius: 9px;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  box-sizing: border-box;
  border: 1px solid #fff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.18);
}

.home-map__feedback-dialog :deep(.el-dialog) {
  background: #eaf2fc;
  border: 1px solid #eaf2fc;
  border-radius: 0;
  margin: 0;
  position: absolute;
  right: 20px;
  bottom: 84px;
  transform: none;
  pointer-events: auto;
  z-index: 1002;
}

.home-map__feedback-dialog :deep(.el-overlay) {
  pointer-events: none;
}

.home-map__feedback-dialog :deep(.el-overlay .el-dialog) {
  pointer-events: auto;
}

.home-map__feedback-dialog :deep(.el-dialog__wrapper) {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.home-map__feedback-dialog :deep(.el-dialog__header) {
  padding: 0;
  background: #0b3a73;
  border-bottom: 1px solid #e6eef9;
  border-radius: 0;
  pointer-events: auto;
  width: 100%;
  box-sizing: border-box;
}

.home-map__feedback-dialog :deep(.el-dialog__body) {
  padding: 12px 16px 16px;
  background: #eaf2fc;
  pointer-events: auto;
}

.home-map__feedback-dialog-title {
  font-weight: 700;
  color: #111827;
}

.home-map__feedback-dialog .home-map__feedback-dialog-title {
  display: flex;
  align-items: center;
  width: 100%;
  flex: 1;
  padding: 12px 16px;
  background: #0b3a73;
  border-radius: 0;
  color: #fff;
  box-sizing: border-box;
}

.home-map__feedback-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.home-map__feedback-section-title {
  font-weight: 700;
  color: #0b3a73;
  margin-bottom: 8px;
}

.home-map__feedback-section + .home-map__feedback-section {
  padding-top: 12px;
  border-top: 1px solid #e6eef9;
}

.home-map__feedback-checkbox-row {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 8px 16px;
}

.home-map__feedback-checkbox-row :deep(.el-checkbox) {
  margin-right: 0;
  width: 100%;
}

.home-map__feedback-checkbox-all {
  font-weight: 600;
}

.home-map__feedback-checkbox-group {
  display: contents;
}

.home-map__feedback-checkbox-group :deep(.el-checkbox) {
  margin-right: 0;
}

.home-map__feedback-detail-dialog :deep(.el-dialog) {
  background: #eaf2fc;
  border-radius: 10px;
}

.home-map__feedback-detail-dialog :deep(.el-dialog__header) {
  padding: 12px 16px;
  background: #eaf2fc;
  border-bottom: 1px solid #e6eef9;
  border-radius: 10px 10px 0 0;
}

.home-map__feedback-detail-dialog :deep(.el-dialog__body) {
  padding: 12px 16px 16px;
  background: #eaf2fc;
}

.home-map__feedback-detail-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: #eaf2fc;
  border-radius: 8px;
  padding: 12px;
}

.home-map__feedback-list-dialog :deep(.el-dialog) {
  background: #eaf2fc;
  border-radius: 10px;
}

.home-map__feedback-list-dialog :deep(.el-dialog__header) {
  padding: 12px 16px;
  background: #eaf2fc;
  border-bottom: 1px solid #e6eef9;
  border-radius: 10px 10px 0 0;
}

.home-map__feedback-list-dialog :deep(.el-dialog__body) {
  padding: 12px 16px 16px;
  background: #eaf2fc;
}

.home-map__feedback-list-body {
  background: #eaf2fc;
  border-radius: 8px;
  padding: 10px;
}

.home-map__feedback-empty {
  padding: 8px 0 2px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.home-map__feedback-detail-row {
  display: grid;
  grid-template-columns: 90px 1fr;
  gap: 8px;
  align-items: flex-start;
  background: #fff;
  border: 1px solid #e6eef9;
  border-radius: 6px;
  padding: 8px 10px;
}

.home-map__feedback-detail-label {
  color: #374151;
  font-weight: 600;
}

.home-map__feedback-detail-value {
  color: #111827;
  word-break: break-all;
}

.home-map__feedback-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 6px;
}

.home-map__analysis-dialog :deep(.el-dialog),
.home-map__analysis-result-dialog :deep(.el-dialog) {
  background: #eaf2fc;
  border-radius: 10px;
}

.home-map__analysis-dialog :deep(.el-dialog__header),
.home-map__analysis-result-dialog :deep(.el-dialog__header) {
  padding: 12px 16px;
  background: #eaf2fc;
  border-bottom: 1px solid #e6eef9;
  border-radius: 10px 10px 0 0;
}

.home-map__analysis-dialog :deep(.el-dialog__body),
.home-map__analysis-result-dialog :deep(.el-dialog__body) {
  padding: 12px 16px 16px;
  background: #eaf2fc;
}

.home-map__analysis-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.home-map__analysis-form-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.home-map__analysis-label {
  width: 72px;
  font-weight: 600;
  color: #303133;
}

.home-map__analysis-unit {
  color: #606266;
}

.home-map__analysis-tip {
  font-size: 12px;
  color: #909399;
}

.home-map__analysis-footer {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.home-map__analysis-result-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
  background: #eaf2fc;
  border-radius: 8px;
  padding: 12px;
}

.home-map__analysis-summary {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  background: #fff;
  border: 1px solid #e6eef9;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 12px;
  color: #374151;
}

.home-map__analysis-section {
  background: #fff;
  border: 1px solid #e6eef9;
  border-radius: 8px;
  padding: 10px 12px;
}

.home-map__analysis-section-title {
  font-weight: 700;
  color: #0b3a73;
  margin-bottom: 8px;
}

.home-map__analysis-chart {
  width: 100%;
  max-width: 420px;
  flex: 0 1 420px;
}

.home-map__analysis-chart-grid {
  display: flex;
  justify-content: center;
  gap: 12px;
}

.home-map__analysis-asset-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.home-map__analysis-asset-item {
  background: #f5f9ff;
  border: 1px solid #e0ecff;
  border-radius: 6px;
  padding: 8px;
  text-align: center;
}

.home-map__analysis-asset-count {
  font-size: 16px;
  font-weight: 700;
  color: #2f78d8;
}

.home-map__analysis-asset-label {
  margin-top: 2px;
  font-size: 12px;
  color: #606266;
}

.home-map__analysis-empty {
  text-align: center;
  color: #909399;
  font-size: 12px;
  padding: 6px 0;
}

.home-map__analysis-pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.home-map__buffer-attr-title {
  font-weight: 700;
  color: #111827;
  font-size: 13px;
}

.home-map__panel-toolbar {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 12px 10px;
  border-bottom: 1px solid #dce8f7;
  background: linear-gradient(180deg, #edf5ff 0%, #f7fbff 100%);
}

.home-map__panel-toolbar-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.home-map__panel-hide-btn.el-button {
  margin-left: auto;
  padding: 0 4px;
  color: #3e648b;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.home-map__panel-hide-btn.el-button:hover {
  color: #1f4f80;
}

.home-map__panel-toolbar-row + .home-map__panel-toolbar-row {
  flex-wrap: wrap;
}

.home-map__panel-select {
  width: 186px;
}

.home-map__panel-input {
  flex: 1;
  min-width: 0;
}

.home-map__panel-toolbar-btn {
  flex: none;
  height: 32px;
  padding: 0 12px;
  border-radius: 8px;
  border-color: #cfe0f5;
  color: #2a537c;
}

.home-map__panel :deep(.el-select__wrapper),
.home-map__panel :deep(.el-input__wrapper) {
  min-height: 34px;
  border-radius: 8px;
  background: #ffffff;
  border: 1px solid #d4e2f6;
  box-shadow: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.home-map__panel :deep(.el-select__wrapper:hover),
.home-map__panel :deep(.el-input__wrapper:hover) {
  border-color: #9bbce0;
}

.home-map__panel :deep(.el-select__wrapper.is-focused),
.home-map__panel :deep(.el-input.is-focus .el-input__wrapper) {
  border-color: #3d79b8;
  box-shadow: 0 0 0 2px rgba(61, 121, 184, 0.14);
}

.home-map__panel :deep(.el-input-group__append .el-button) {
  color: #1f4f80;
  border-left: 1px solid #d6e3f5;
  transition: color 0.2s ease, background-color 0.2s ease;
}

.home-map__panel :deep(.el-input-group__append .el-button:hover) {
  color: #15395f;
  background: #eaf3ff;
}

.home-map__customize-section {
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px dashed #e6f0ff;
}

.home-map__customize-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.home-map__customize-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.home-map__customize-attr-tip,
.home-map__customize-geo-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 18px;
}

.home-map__customize-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.home-map__panel-head {
  display: grid;
  grid-template-columns: 1fr;
  background: linear-gradient(90deg, #1f5f97 0%, #1d4c7f 100%);
  color: #f8fbff;
  font-weight: 600;
  font-size: 14px;
  border-top: 1px solid rgba(255, 255, 255, 0.28);
}

.home-map__panel-col-title {
  padding: 10px 14px;
  text-align: left;
}

.home-map__panel-col-title--left {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  cursor: pointer;
  user-select: none;
}

.home-map__panel-toggle-icon {
  font-size: 16px;
  transition: transform 0.2s ease;
}

.home-map__panel-body {
  display: grid;
  grid-template-columns: 1.04fr 1.3fr;
  min-height: 380px;
  background: rgba(247, 251, 255, 0.9);
}

.home-map__panel-body.is-all-collapsed {
  display: none;
}

.home-map__panel-body.is-facility-collapsed {
  grid-template-columns: 1fr;
}

.home-map__panel-body.is-area-collapsed {
  grid-template-columns: 1fr;
}

.home-map__facility-list {
  border-right: 1px solid #dce8f7;
  padding: 10px 8px;
  max-height: 462px;
  overflow: auto;
}

.home-map__panel-body.is-area-collapsed .home-map__facility-list {
  border-right: none;
}

.home-map__facility-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 11px;
  margin-bottom: 7px;
  border-radius: 10px;
  border: 1px solid transparent;
  background: #ffffff;
  box-shadow: 0 2px 7px rgba(15, 23, 42, 0.05);
  cursor: pointer;
  user-select: none;
  color: #1f3f63;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}

.home-map__facility-item:hover {
  background: #f5faff;
  border-color: #d0e3f8;
  box-shadow: 0 5px 12px rgba(15, 23, 42, 0.08);
}

.home-map__facility-item.is-active {
  background: linear-gradient(180deg, #e5f0ff 0%, #deebff 100%);
  border-color: #b6d0ee;
  color: #1b4875;
  font-weight: 600;
  box-shadow: 0 6px 14px rgba(32, 90, 145, 0.16);
}

.home-map__facility-name-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.home-map__facility-icon {
  width: 22px;
  height: 22px;
  flex-shrink: 0;
}

.home-map__facility-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  min-width: 0;
}

.home-map__arrow {
  color: #7f98b2;
  transition: color 0.2s ease, transform 0.2s ease;
}

.home-map__facility-item:hover .home-map__arrow,
.home-map__facility-item.is-active .home-map__arrow {
  color: #2d649a;
  transform: translateX(1px);
}

.home-map__area-list {
  padding: 0 0 8px;
  max-height: 460px;
  overflow: auto;
  background: linear-gradient(180deg, rgba(248, 252, 255, 0.92) 0%, rgba(244, 249, 255, 0.92) 100%);
}

.home-map__area-list-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 6px;
  padding: 10px 12px;
  background: linear-gradient(180deg, #f2f8ff 0%, #f8fbff 100%);
  color: #1f4a76;
  font-weight: 600;
  cursor: pointer;
  user-select: none;
  position: sticky;
  top: 0;
  z-index: 2;
  border-bottom: 1px solid #dce8f7;
}

.home-map__drawer :deep(.el-drawer) {
  width: min(430px, calc(100vw - 28px)) !important;
  height: calc(100vh - 28px) !important;
  background: linear-gradient(180deg, #f9fcff 0%, #f3f7fd 44%, #eff5ff 100%);
  bottom: auto;
  top: 14px;
  border-radius: 16px 0 0 16px;
  border: 1px solid #d2e2f5;
  box-shadow: 0 22px 52px rgba(15, 23, 42, 0.22);
  overflow: hidden;
}

.home-map__drawer :deep(.el-drawer__header) {
  margin-bottom: 0;
  padding: 12px 16px 10px;
  background: linear-gradient(180deg, #e8f2ff 0%, #f6faff 100%);
  border-bottom: 1px solid #dce8f7;
}

.home-map__drawer :deep(.el-drawer__headerbtn) {
  margin-top: 2px;
  color: #42678d;
}

.home-map__drawer :deep(.el-drawer__headerbtn:hover) {
  color: #1f4d7f;
}

.home-map__drawer :deep(.el-drawer__body) {
  overflow: auto;
  padding: 12px;
  background: rgba(248, 252, 255, 0.9);
}

.home-map__drawer :deep(.el-table) {
  border: 1px solid #dce8f8;
  border-radius: 10px;
  overflow: hidden;
  background: #ffffff;
}

.home-map__drawer :deep(.el-table th.el-table__cell) {
  background: linear-gradient(180deg, #edf4ff 0%, #f7fbff 100%);
  color: #1c456d;
  font-weight: 600;
}

.home-map__drawer :deep(.el-table td.el-table__cell) {
  border-bottom-color: #edf3fb;
}

.home-map__drawer :deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.22s ease;
}

.home-map__drawer :deep(.el-table__row:hover > td.el-table__cell) {
  background: #edf5ff !important;
}

.home-map__drawer :deep(.el-table__row.current-row > td.el-table__cell) {
  background: #e4f0ff !important;
}

.home-map__area-tree {
  padding: 6px 8px 0;
}

.home-map__area-node {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  gap: 10px;
}

:deep(.home-map__area-tree .el-tree-node__content) {
  height: 38px;
  margin: 2px 0;
  padding: 0 8px;
  border-radius: 8px;
  transition: background-color 0.2s ease;
}

:deep(.home-map__area-tree .el-tree-node__content:hover) {
  background: #edf4ff;
}

:deep(.home-map__area-tree .el-tree-node.is-current > .el-tree-node__content) {
  background: #e1eeff;
}

:deep(.home-map__area-tree .el-tree-node__expand-icon) {
  color: #6f8dab;
}

.home-map__area-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  cursor: pointer;
  user-select: none;
  border-bottom: 1px solid #f3f4f6;
}

.home-map__area-item:hover {
  background: #f8fafc;
}

.home-map__area-name {
  color: #1f3f63;
}

.home-map__area-count {
  color: #4b6d92;
  font-size: 12px;
  background: #e9f2ff;
  border: 1px solid #d6e6fb;
  border-radius: 999px;
  padding: 1px 7px;
  line-height: 18px;
}

.home-map__placeholder {
  padding: 24px 12px;
  color: #57799f;
  text-align: center;
  font-size: 12px;
  line-height: 18px;
}

.home-map__drawer-title {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.home-map__drawer-title-main {
  font-weight: 700;
  font-size: 17px;
  color: #1a4a78;
  letter-spacing: 0.3px;
}

.home-map__drawer-title-sub {
  color: #617f9f;
  font-size: 12px;
}

.home-map__buffer-header {
  display: flex;
  flex-direction: column;
  width: 100%;
  padding: 10px 12px 8px;
}

.home-map__buffer-header-title {
  background: linear-gradient(90deg, #0f4a89 0%, #1e63aa 100%);
  color: #fff;
  padding: 9px 12px;
  font-weight: 700;
  font-size: 15px;
  border-radius: 10px;
  letter-spacing: 0.4px;
  box-shadow: 0 6px 16px rgba(20, 71, 122, 0.24);
}

.home-map__drawer-body {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.home-map__drawer-tip {
  color: #4f6e8f;
  font-size: 12px;
  line-height: 19px;
  padding: 9px 11px;
  background: #f2f7ff;
  border: 1px dashed #cddff6;
  border-left: 3px solid #7ca7d6;
  border-radius: 8px;
}

.home-map__river-overview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-map__river-title {
  font-weight: 700;
  color: #111827;
}

.home-map__river-kpi {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 350px;
  height: 110px;
  opacity: 1;
  background: #eaf2fc;
  padding: 10px 12px;
  margin: 0 auto;
  box-sizing: border-box;
  border-radius: 6px;
}

.home-map__river-kpi-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
}

.home-map__river-kpi-label {
  color: #374151;
}

.home-map__river-kpi-value {
  color: #111827;
  font-weight: 600;
}

.home-map__river-level-grid {
  display: grid;
  grid-template-columns: repeat(3, 110px);
  gap: 10px 8px;
  padding: 6px 0;
  width: 350px;
  margin: 0 auto;
  box-sizing: border-box;
  justify-content: space-between;
}

.home-map__river-level-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.home-map__river-level-circle {
  width: 110px;
  height: 70px;
  opacity: 1;
  background: #eaf2fc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #111827;
  border-radius: 6px;
  gap: 4px;
}

.home-map__river-level-box-count {
  font-size: 16px;
  font-weight: 700;
  line-height: 18px;
}

.home-map__river-level-box-label {
  font-size: 12px;
  color: #374151;
  line-height: 16px;
}

.home-map__reservoir-overview {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-map__reservoir-title {
  font-weight: 700;
  color: #111827;
}

.home-map__reservoir-kpi {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 350px;
  height: 110px;
  opacity: 1;
  background: #eaf2fc;
  padding: 10px 12px;
  margin: 0 auto;
  box-sizing: border-box;
  border-radius: 6px;
}

.home-map__reservoir-kpi-row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  font-size: 13px;
}

.home-map__reservoir-kpi-label {
  color: #374151;
}

.home-map__reservoir-kpi-value {
  color: #111827;
  font-weight: 600;
}

.home-map__reservoir-scale-grid {
  display: grid;
  grid-template-columns: repeat(3, 110px);
  gap: 10px 8px;
  padding: 6px 0;
  width: 350px;
  margin: 0 auto;
  box-sizing: border-box;
  justify-content: space-between;
}

.home-map__reservoir-scale-item {
  width: 110px;
  height: 70px;
  opacity: 1;
  background: #eaf2fc;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border-radius: 6px;
  box-sizing: border-box;
}

.home-map__reservoir-scale-count {
  font-size: 16px;
  font-weight: 700;
  color: #111827;
  line-height: 18px;
}

.home-map__reservoir-scale-label {
  font-size: 12px;
  color: #374151;
  line-height: 16px;
  text-align: center;
}

.home-map__river-pagination {
  display: flex;
  justify-content: center;
  padding-top: 8px;
}

.home-map__river-dialog-title {
  font-weight: 700;
  font-size: 16px;
  color: #111827;
}

.home-map__detail-dialog :deep(.el-dialog) {
  width: min(620px, calc(100vw - 36px)) !important;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e5edf7;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.16);
  overflow: hidden;
}

.home-map__detail-dialog :deep(.el-dialog__header) {
  padding: 14px 20px;
  background: #ffffff;
  border-bottom: 1px solid #eaf1f9;
  border-radius: 12px 12px 0 0;
}

.home-map__detail-dialog :deep(.el-dialog__headerbtn) {
  top: 16px;
  right: 16px;
  color: #42678d;
}

.home-map__detail-dialog :deep(.el-dialog__headerbtn:hover) {
  color: #1f4d7f;
}

.home-map__detail-dialog :deep(.el-dialog__body) {
  background: #ffffff;
  padding: 14px 20px 20px;
}

.home-map__river-dialog-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: transparent;
  border: none;
  border-radius: 0;
  padding: 0;
}

.home-map__river-section {
  display: flex;
  flex-direction: column;
  gap: 9px;
  background: transparent;
  border-radius: 0;
  border: none;
  box-shadow: none;
  padding: 0;
}

.home-map__river-section-title {
  font-weight: 700;
  color: #224a72;
}

.home-map__river-info-grid {
  border: 1px solid #e7eef7;
  border-radius: 8px;
  overflow: hidden;
  background: #fff;
}

.home-map__river-info-row {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 12px;
  padding: 10px 12px;
  border-top: 1px solid #edf3fb;
  font-size: 13px;
  background: #ffffff;
}

.home-map__river-info-row:nth-child(even) {
  background: #f9fbff;
}

.home-map__river-info-row:first-child {
  border-top: none;
}

.home-map__river-info-row--full {
  grid-template-columns: 104px 1fr;
}

.home-map__river-info-label {
  color: #385d82;
  font-weight: 600;
}

.home-map__river-info-value {
  color: #163355;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .home-map__panel {
    width: min(428px, calc(100vw - 10px));
    top: 5px;
    left: 5px;
    border-radius: 12px;
  }

  .home-map__panel-reveal {
    top: 5px;
    left: 5px;
  }

  .home-map__panel-toolbar {
    padding: 10px;
  }

  .home-map__panel-toolbar-row {
    flex-wrap: wrap;
  }

  .home-map__panel-select,
  .home-map__panel-input,
  .home-map__panel-toolbar-btn {
    width: 100%;
  }

  .home-map__panel-body {
    grid-template-columns: 1fr;
    min-height: auto;
  }

  .home-map__facility-list {
    max-height: 220px;
    border-right: none;
    border-bottom: 1px solid #dce8f7;
  }

  .home-map__area-list {
    max-height: 236px;
  }

  .home-map__buffer-dialog :deep(.el-dialog) {
    width: calc(100vw - 14px) !important;
  }

  .home-map__buffer-body {
    padding: 12px 12px 8px;
    gap: 10px;
  }

  .home-map__buffer-row {
    flex-direction: column;
    align-items: stretch;
    gap: 6px;
  }

  .home-map__buffer-row--top {
    align-items: stretch;
  }

  .home-map__buffer-label {
    min-width: auto;
  }

  .home-map__buffer-options :deep(.el-checkbox-group) {
    grid-template-columns: 1fr;
  }

  .home-map__buffer-center {
    min-height: 36px;
    flex-wrap: wrap;
  }

  .home-map__buffer-footer {
    padding: 10px 12px 12px;
  }

  .home-map__buffer-result {
    gap: 8px;
  }

  .home-map__buffer-kpi-row {
    font-size: 12px;
  }

  .home-map__buffer-section {
    padding: 9px;
  }

  .home-map__buffer-object-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 8px;
  }

  .home-map__drawer :deep(.el-drawer) {
    width: calc(100vw - 10px) !important;
    height: calc(100vh - 10px) !important;
    top: 5px;
    border-radius: 12px 0 0 12px;
  }

  .home-map__drawer :deep(.el-drawer__header) {
    padding: 10px 12px 8px;
  }

  .home-map__drawer :deep(.el-drawer__body) {
    padding: 10px;
  }

  .home-map__detail-dialog :deep(.el-dialog) {
    width: calc(100vw - 20px) !important;
  }

  .home-map__detail-dialog :deep(.el-dialog__header) {
    padding: 12px 14px 10px;
  }

  .home-map__detail-dialog :deep(.el-dialog__body) {
    padding: 12px 14px 14px;
  }

  .home-map__river-dialog-body {
    padding: 10px 11px;
  }

  .home-map__river-section {
    padding: 10px 11px;
  }

  .home-map__river-info-row {
    grid-template-columns: 92px 1fr;
    gap: 8px;
    padding: 8px 9px;
  }

  .home-map__river-info-row--full {
    grid-template-columns: 92px 1fr;
  }
}

.home-map__loading {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: rgba(255, 255, 255, 0.86);
  z-index: 1200;
}

.home-map__loading-text {
  color: #374151;
}

:deep(.leaflet-tooltip.home-map__gis-label) {
  background: rgba(11, 58, 115, 0.92);
  color: #fff;
  border: none;
  border-radius: 4px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.18);
  padding: 3px 8px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
}

:deep(.leaflet-tooltip.home-map__gis-label::before) {
  border-top-color: rgba(11, 58, 115, 0.92);
}

:deep(.leaflet-container path),
:deep(.leaflet-interactive:focus),
:deep(.leaflet-container path:focus) {
  outline: none !important;
}

</style>

<style>
/* 终极样式修复：使用全局样式并增加特异性 */
:root {
  --opt-primary: #1a73e8;
  --opt-bg-light: #f8faff;
  --opt-border: #e0e6ed;
  --opt-text-main: #2c3e50;
  --opt-text-sub: #5f6368;
  --opt-card-bg: #ffffff;
}

/* 穿透 Element Plus 弹窗限制 */
body .el-overlay .el-dialog.optimized-detail-dialog {
  background: transparent !important;
  box-shadow: none !important;
  border: none !important;
  border-radius: 12px !important;
  overflow: hidden !important;
}

body .el-overlay .el-dialog.optimized-detail-dialog .optimized-dialog-header {
  display: none !important;
}

body .el-overlay .el-dialog.optimized-detail-dialog .optimized-dialog-body {
  padding: 0 !important;
}

.optimized-modal-container {
  width: 500px !important;
  background: #fff !important;
  border-radius: 12px !important;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15) !important;
  overflow: hidden !important;
  position: relative !important;
}

.optimized-modal-header {
  padding: 18px 24px !important;
  border-bottom: 1px solid var(--opt-border) !important;
  display: flex !important;
  justify-content: space-between !important;
  align-items: center !important;
  background: #fff !important;
}

.optimized-modal-title {
  font-size: 18px !important;
  font-weight: 600 !important;
  color: var(--opt-text-main) !important;
  margin: 0 !important;
}

.optimized-close-btn {
  color: #999 !important;
  font-size: 20px !important;
  cursor: pointer !important;
  width: 32px !important;
  height: 32px !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  border-radius: 50% !important;
  transition: all 0.2s !important;
}

.optimized-close-btn:hover {
  background-color: #f0f2f5 !important;
  color: #333 !important;
}

.optimized-modal-content {
  padding: 24px !important;
  background-color: var(--opt-bg-light) !important;
}

.optimized-section-card {
  background: var(--opt-card-bg) !important;
  border-radius: 10px !important;
  padding: 18px !important;
  margin-bottom: 20px !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03) !important;
}

.optimized-section-card:last-child {
  margin-bottom: 0 !important;
}

.optimized-section-title {
  font-size: 15px !important;
  font-weight: 600 !important;
  color: var(--opt-primary) !important;
  margin-bottom: 14px !important;
  padding-left: 12px !important;
  border-left: 4px solid var(--opt-primary) !important;
  line-height: 1 !important;
}

.optimized-table-wrapper {
  border: 1px solid #edf2f7 !important;
  border-radius: 8px !important;
  overflow: hidden !important;
}

.optimized-info-table {
  width: 100% !important;
  border-collapse: collapse !important;
  background: #fff !important;
}

.optimized-info-table tr {
  border-bottom: 1px solid #edf2f7 !important;
}

.optimized-info-table tr:last-child {
  border-bottom: none !important;
}

.optimized-info-table td {
  padding: 14px 12px !important;
  font-size: 14px !important;
}

.optimized-label-cell {
  color: var(--opt-text-sub) !important;
  width: 120px !important;
  background-color: #f9fafb !important;
  font-weight: 400 !important;
}

.optimized-value-cell {
  color: var(--opt-text-main) !important;
  font-weight: 500 !important;
  background-color: #fff !important;
}

/* 斑马纹效果 */
.optimized-info-table tr:nth-child(even) .optimized-value-cell {
  background-color: #fafbfc !important;
}

/* 缓冲区结果抽屉头部与内容间距控制（弹窗为 Teleport） */
.home-map__buffer-result-drawer .el-drawer {
  border-left: 1px solid #d6e3f5 !important;
  border-radius: 16px 0 0 16px !important;
  overflow: hidden !important;
  box-shadow: -18px 0 38px rgba(15, 23, 42, 0.22) !important;
  background: linear-gradient(180deg, #f9fcff 0%, #f2f7ff 100%) !important;
}

.home-map__buffer-result-drawer .el-drawer__header {
  background: linear-gradient(180deg, #e9f2ff 0%, #f5f9ff 100%) !important;
  padding: 0 !important;
  margin: 0 !important;
  border-bottom: 1px solid #d8e5f6 !important;
  min-height: 48px !important;
}

.home-map__buffer-result-drawer .el-drawer__body {
  padding: 10px 12px 12px !important;
  background: transparent !important;
}

.home-map__buffer-result-drawer .el-drawer__close-btn {
  color: #3e6288 !important;
  border-radius: 6px !important;
  transition: all 0.2s ease !important;
}

.home-map__buffer-result-drawer .el-drawer__close-btn:hover {
  color: #1f4d7f !important;
  background: rgba(64, 114, 164, 0.12) !important;
}

.home-map__pond-drawer {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.home-map__pond-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 8px;
}

.home-map__pond-stat-card {
  position: relative;
  overflow: hidden;
  min-height: 88px;
  padding: 10px 10px 12px;
  border-radius: 12px;
  color: #fff;
  box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
}

.home-map__pond-stat-card::after {
  content: '';
  position: absolute;
  right: -18px;
  top: -18px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.14);
}

.home-map__pond-stat-card--count {
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 100%);
}

.home-map__pond-stat-card--area {
  background: linear-gradient(135deg, #0f766e 0%, #14b8a6 100%);
}

.home-map__pond-stat-card--mu {
  background: linear-gradient(135deg, #0369a1 0%, #38bdf8 100%);
}

.home-map__pond-stat-label {
  position: relative;
  z-index: 1;
  font-size: 11px;
  line-height: 1.2;
  opacity: 0.92;
}

.home-map__pond-stat-value {
  position: relative;
  z-index: 1;
  margin-top: 8px;
  font-size: 20px;
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: 0.2px;
  word-break: break-all;
}

.home-map__pond-stat-unit {
  margin-left: 2px;
  font-size: 11px;
  font-weight: 500;
  opacity: 0.9;
}

.home-map__pond-section {
  border: 1px solid #dce8f8;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 6px 16px rgba(15, 23, 42, 0.05);
  overflow: hidden;
}

.home-map__pond-section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 10px 12px;
  background: linear-gradient(180deg, #f5f9ff 0%, #ffffff 100%);
  border-bottom: 1px solid #e8f0fb;
}

.home-map__pond-section-title {
  font-size: 14px;
  font-weight: 700;
  color: #1e3a8a;
  padding-left: 10px;
  border-left: 3px solid #2563eb;
  line-height: 1.2;
}

.home-map__pond-section-badge {
  min-width: 24px;
  height: 24px;
  padding: 0 8px;
  border-radius: 999px;
  background: #e8f1ff;
  color: #1d4ed8;
  font-size: 12px;
  font-weight: 700;
  line-height: 24px;
  text-align: center;
}

.home-map__pond-filter-form {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
}

.home-map__pond-filter-field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.home-map__pond-filter-field label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.home-map__pond-filter-field :deep(.el-select) {
  width: 100%;
}

.home-map__pond-filter-actions {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  margin-top: 2px;
}

.home-map__pond-filter-actions .el-button {
  width: 100%;
  margin: 0;
}

.home-map__pond-section--list .home-map__pond-list {
  max-height: min(42vh, 360px);
}

.home-map__pond-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 10px 12px 12px;
  overflow: auto;
}

.home-map__pond-item {
  padding: 10px 12px;
  border: 1px solid #e6eef8;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.home-map__pond-item:hover {
  border-color: #93c5fd;
  box-shadow: 0 8px 18px rgba(37, 99, 235, 0.12);
  transform: translateY(-1px);
}

.home-map__pond-item-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
}

.home-map__pond-item-main {
  min-width: 0;
  flex: 1;
}

.home-map__pond-item-area {
  flex-shrink: 0;
  padding: 4px 8px;
  border-radius: 8px;
  background: #f1f5f9;
  color: #334155;
  font-size: 12px;
  font-weight: 600;
  line-height: 1.2;
  white-space: nowrap;
}

.home-map__pond-item-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e3a8a;
  line-height: 1.35;
  word-break: break-all;
}

.home-map__pond-item-code {
  margin-top: 4px;
  font-size: 11px;
  color: #94a3b8;
  line-height: 1.3;
  word-break: break-all;
}

.home-map__pond-item-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}

.home-map__pond-tag {
  display: inline-flex;
  align-items: center;
  max-width: 100%;
  padding: 2px 8px;
  border-radius: 999px;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 11px;
  line-height: 18px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.home-map__pond-tag--type {
  background: #ecfeff;
  color: #0f766e;
}

.home-map__pond-tag--status {
  background: #fff7ed;
  color: #c2410c;
}

.home-map__pond-pagination {
  display: flex;
  justify-content: center;
  padding: 8px 0 4px;
  flex-shrink: 0;
}

/* 坑塘详情弹框：class 直接挂在 .el-dialog 上，必须用同级选择器，不能写后代 .el-dialog */
.el-dialog.home-map__pond-detail-dialog {
  width: 400px !important;
  margin: 0 !important;
  position: fixed !important;
  top: 55% !important;
  right: 190px !important;
  left: auto !important;
  transform: translateY(-50%) !important;
  border: 1px solid rgba(32, 178, 170, 0.22) !important;
  border-radius: 12px !important;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.16) !important;
  pointer-events: auto !important;
  z-index: 1002 !important;
}

/* modal=false 时仍有全屏 fixed 容器，必须穿透，否则地图点不了 */
.home-map__pond-detail-overlay {
  background: transparent !important;
  pointer-events: none !important;
}

.home-map__pond-detail-overlay .el-overlay-dialog {
  pointer-events: none !important;
  overflow: visible !important;
}

.el-dialog.home-map__pond-detail-dialog .el-dialog__header {
  padding: 12px 16px !important;
  margin: 0 !important;
  background: #ffffff !important;
  border-bottom: 1px solid #eaf1f9 !important;
}

.el-dialog.home-map__pond-detail-dialog .el-dialog__headerbtn {
  top: 12px !important;
  right: 12px !important;
  color: #42678d !important;
}

.el-dialog.home-map__pond-detail-dialog .el-dialog__headerbtn:hover {
  color: #1f4d7f !important;
}

.el-dialog.home-map__pond-detail-dialog .el-dialog__body {
  padding: 12px 16px 16px !important;
}

.home-map__pond-detail-dialog-title {
  font-size: 16px;
  font-weight: 700;
  color: #0f766e;
}

.home-map__pond-detail-brief {
  padding: 10px 12px;
  border-radius: 8px;
  background: linear-gradient(135deg, #f0fdfa 0%, #ecfeff 100%);
  border: 1px solid #ccfbf1;
}

.home-map__pond-detail-name {
  font-size: 15px;
  font-weight: 700;
  color: #134e4a;
  line-height: 1.35;
  word-break: break-all;
}

.home-map__pond-detail-code {
  margin-top: 4px;
  font-size: 11px;
  color: #64748b;
  font-family: ui-monospace, monospace;
  word-break: break-all;
}

.home-map__pond-detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 8px;
}

.home-map__pond-detail-tag {
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 11px;
  background: #fff;
  color: #0f766e;
  border: 1px solid #99f6e4;
}

.home-map__pond-detail-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.home-map__pond-detail-stats {
  display: flex;
  gap: 8px;
}

.home-map__pond-detail-stat {
  flex: 1;
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
  padding: 8px;
  border-radius: 8px;
  background: #f8fafc;
  border: 1px solid #e8eef5;
}

.home-map__pond-detail-stat-val {
  font-size: 16px;
  font-weight: 700;
  color: #0f766e;
}

.home-map__pond-detail-stat-unit {
  font-size: 11px;
  color: #14b8a6;
}

.home-map__pond-detail-boundary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.home-map__pond-detail-boundary-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #e8eef5;
  background: #fff;
  min-height: 52px;
}

.home-map__pond-detail-boundary-badge {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  margin-top: 1px;
  border-radius: 4px;
  background: #0f766e;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.home-map__pond-detail-boundary-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.home-map__pond-detail-boundary-label {
  font-size: 11px;
  color: #64748b;
  line-height: 1.2;
}

.home-map__pond-detail-boundary-val {
  font-size: 12px;
  color: #334155;
  font-weight: 500;
  line-height: 1.35;
  word-break: break-all;
}

</style>
