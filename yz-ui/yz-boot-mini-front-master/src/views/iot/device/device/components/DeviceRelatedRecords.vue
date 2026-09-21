<template>
  <div class="device-records">
    <el-tabs v-if="!mode" v-model="activeTab" class="records-tabs">
      <el-tab-pane label="故障记录" name="fault">
        <div v-if="activeTab === 'fault'" class="records-panel">
          <div class="records-header">
            <div class="records-title">故障记录</div>
            <div class="records-count">共 {{ total.fault }} 条</div>
          </div>
          <el-table
            v-loading="loading.fault"
            :data="faultList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="故障时间" prop="faultTime" min-width="160">
              <template #default="scope">
                {{ scope.row.faultTime ? formatDate(scope.row.faultTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="故障现象" prop="faultSymptom" min-width="180" />
            <el-table-column label="处理状态" prop="status" width="120">
              <template #default="scope">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_FAULT_STATUS" :value="scope.row.status" />
              </template>
            </el-table-column>
            <el-table-column label="处理人" prop="repairName" width="120" />
            <el-table-column label="完成时间" prop="finishTime" min-width="160">
              <template #default="scope">
                {{ scope.row.finishTime ? formatDate(scope.row.finishTime) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.fault"
              v-model:page="pageInfo.fault.pageNo"
              v-model:limit="pageInfo.fault.pageSize"
              @pagination="getFaultList"
            />
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="养护记录" name="maintenance">
        <div v-if="activeTab === 'maintenance'" class="records-panel">
          <div class="records-header">
            <div class="records-title">养护记录</div>
            <div class="records-count">共 {{ total.maintenance }} 条</div>
          </div>
          <el-table
            v-loading="loading.maintenance"
            :data="maintenanceList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="养护类型" prop="maintainType" min-width="140">
              <template #default="scope">
                {{ getMaintenanceTypeLabel(scope.row.maintainType) }}
              </template>
            </el-table-column>
            <el-table-column label="养护项目" prop="maintainItems" min-width="180" />
            <el-table-column label="养护状态" prop="status" width="120">
              <template #default="scope">
                {{ getMaintenanceStatusLabel(scope.row.status) }}
              </template>
            </el-table-column>
            <el-table-column label="养护人" prop="maintainerName" width="120" />
            <el-table-column label="养护时间" min-width="160">
              <template #default="scope">
                {{ formatPlanTime(scope.row) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.maintenance"
              v-model:page="pageInfo.maintenance.pageNo"
              v-model:limit="pageInfo.maintenance.pageSize"
              @pagination="getMaintenanceList"
            />
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="技术文档" name="doc">
        <div v-if="activeTab === 'doc'" class="records-panel">
          <div class="records-header">
            <div class="records-title">技术文档</div>
            <div class="records-count">共 {{ total.doc }} 条</div>
          </div>
          <el-table
            v-loading="loading.doc"
            :data="docList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="资料名称" prop="docName" min-width="180" />
            <el-table-column label="上传文件" min-width="220">
              <template #default="scope">
                <el-popover
                  v-if="getFileName(scope.row.fileUrl) !== '-'"
                  placement="top"
                  trigger="click"
                  :width="320"
                >
                  <div class="doc-file-info">
                    <div>文件名称：{{ getFileName(scope.row.fileUrl) }}</div>
                    <div>文件类型：{{ getFileType(scope.row.fileFormat, scope.row.fileUrl) }}</div>
                  </div>
                  <template #reference>
                    <el-link class="doc-file-name" type="primary" :underline="false">
                      {{ getFileName(scope.row.fileUrl) }}
                    </el-link>
                  </template>
                </el-popover>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="类型" prop="docType" width="120">
              <template #default="scope">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_DOC_TYPE" :value="scope.row.docType" />
              </template>
            </el-table-column>
            <el-table-column label="文件类型" prop="fileFormat" width="120">
              <template #default="scope">
                {{ getFileType(scope.row.fileFormat, scope.row.fileUrl) }}
              </template>
            </el-table-column>
            <el-table-column label="上传时间" prop="createTime" min-width="160">
              <template #default="scope">
                {{ scope.row.createTime ? formatDate(scope.row.createTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-link v-if="scope.row.fileUrl" :href="scope.row.fileUrl" target="_blank" download type="primary">
                  下载
                </el-link>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.doc"
              v-model:page="pageInfo.doc.pageNo"
              v-model:limit="pageInfo.doc.pageSize"
              @pagination="getDocList"
            />
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="库存备件" name="spare">
        <div v-if="activeTab === 'spare'" class="records-panel">
          <div class="records-header">
            <div class="records-title">库存备件</div>
            <div class="records-count">共 {{ total.spare + total.consumption }} 条</div>
          </div>
          <el-tabs v-model="spareTab" class="records-sub-tabs">
            <el-tab-pane label="库存备件" name="stock">
              <el-table
                v-loading="loading.spare"
                :data="spareList"
                :stripe="true"
                :show-overflow-tooltip="true"
                size="small"
                class="records-table"
                empty-text="暂无数据"
              >
                <el-table-column label="备件名称" prop="spareName" min-width="180" />
                <el-table-column label="备件分类" prop="spareType" min-width="120">
                  <template #default="scope">
                    <dict-tag :type="DICT_TYPE.IOT_SPARE_TYPE" :value="scope.row.spareType" />
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" min-width="200">
                  <template #default="scope">
                    {{ [scope.row.spareSpec, scope.row.spareModel].filter(Boolean).join(' ') || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="库存数量" prop="stockQty" min-width="120" />
                <el-table-column label="最低库存阈值" prop="minStock" min-width="140" />
                <el-table-column label="库管员" prop="keeperName" min-width="140" />
              </el-table>
              <div class="records-pagination">
                <Pagination
                  :total="total.spare"
                  v-model:page="pageInfo.spare.pageNo"
                  v-model:limit="pageInfo.spare.pageSize"
                  @pagination="getSpareList"
                />
              </div>
            </el-tab-pane>
            <el-tab-pane label="历史消耗" name="history">
              <el-table
                v-loading="loading.consumption"
                :data="consumptionList"
                :stripe="true"
                :show-overflow-tooltip="true"
                size="small"
                class="records-table"
                empty-text="暂无数据"
              >
                <el-table-column label="设备名称" prop="deviceName" min-width="160" />
                <el-table-column label="备件分类" prop="spareType" width="120">
                  <template #default="scope">
                    <dict-tag :type="DICT_TYPE.IOT_SPARE_TYPE" :value="scope.row.spareType" />
                  </template>
                </el-table-column>
                <el-table-column label="用途" prop="usageType" width="140">
                  <template #default="scope">
                    {{ getUsageTypeLabel(scope.row.usageType) }}
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" prop="spareSpec" min-width="180">
                  <template #default="scope">
                    {{ [scope.row.spareSpec, scope.row.spareModel].filter(Boolean).join(' ') || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="消耗数量" prop="ioQty" width="120" />
                <el-table-column label="消耗时间" prop="ioTime" min-width="160">
                  <template #default="scope">
                    {{ scope.row.ioTime ? formatDate(scope.row.ioTime) : '-' }}
                  </template>
                </el-table-column>
              </el-table>
              <div class="records-pagination">
                <Pagination
                  :total="total.consumption"
                  v-model:page="pageInfo.consumption.pageNo"
                  v-model:limit="pageInfo.consumption.pageSize"
                  @pagination="getConsumptionList"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </el-tab-pane>
      <el-tab-pane label="设备评级" name="rating">
        <div v-if="activeTab === 'rating'" class="records-panel">
          <div class="records-header">
            <div class="records-title">设备评级</div>
            <div class="records-count">共 {{ total.rating }} 条</div>
          </div>
          <el-table
            v-loading="loading.rating"
            :data="ratingList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="评级时间" prop="ratingTime" min-width="160">
              <template #default="scope">
                {{ scope.row.ratingTime ? formatDate(scope.row.ratingTime, 'YYYY-MM-DD') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="评级等级" prop="ratingResult" min-width="120">
              <template #default="scope">
                {{ getRatingLabel(scope.row.ratingResult) }}
              </template>
            </el-table-column>
            <el-table-column label="评级人员" prop="ratingUserName" min-width="120" />
            <el-table-column label="评级说明" prop="ratingBasis" min-width="200" />
            <el-table-column label="附件" min-width="140">
              <template #default="scope">
                <el-popover
                  v-if="normalizeAttachments(scope.row.attachments).length"
                  placement="top"
                  trigger="click"
                  :width="260"
                >
                  <div class="doc-file-info">
                    <el-link
                      v-for="(file, index) in normalizeAttachments(scope.row.attachments)"
                      :key="file + index"
                      :href="file"
                      target="_blank"
                      type="primary"
                      :underline="false"
                    >
                      {{ getFileName(file) }}
                    </el-link>
                  </div>
                  <template #reference>
                    <el-link type="primary" :underline="false">查看</el-link>
                  </template>
                </el-popover>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.rating"
              v-model:page="pageInfo.rating.pageNo"
              v-model:limit="pageInfo.rating.pageSize"
              @pagination="getRatingList"
            />
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="事故登记" name="accident">
        <div v-if="activeTab === 'accident'" class="records-panel">
          <div class="records-header">
            <div class="records-title">事故登记</div>
            <div class="records-count">共 {{ total.accident }} 条</div>
          </div>
          <el-table
            v-loading="loading.accident"
            :data="accidentList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="事故时间" prop="accidentTime" min-width="160">
              <template #default="scope">
                {{ scope.row.accidentTime ? formatDate(scope.row.accidentTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="事故类型" prop="accidentType" min-width="120">
              <template #default="scope">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_ACCIDENT_TYPE" :value="scope.row.accidentType" />
              </template>
            </el-table-column>
            <el-table-column label="事故地点" prop="accidentLocation" min-width="160" />
            <el-table-column label="事故描述" prop="accidentDesc" min-width="200" />
            <el-table-column label="处理结果" prop="handleResult" min-width="160" />
            <el-table-column label="责任人" prop="responsibleName" min-width="120" />
            <el-table-column label="登记人" prop="creator" min-width="120">
              <template #default="scope">
                {{ scope.row.creatorName || scope.row.creator || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="事故图片" min-width="140">
              <template #default="scope">
                <el-popover
                  v-if="normalizeAttachments(scope.row.attachments).length"
                  placement="top"
                  trigger="click"
                  :width="260"
                >
                  <div class="doc-file-info">
                    <el-link
                      v-for="(file, index) in normalizeAttachments(scope.row.attachments)"
                      :key="file + index"
                      :href="file"
                      target="_blank"
                      type="primary"
                      :underline="false"
                    >
                      {{ getFileName(file) }}
                    </el-link>
                  </div>
                  <template #reference>
                    <el-link type="primary" :underline="false">查看</el-link>
                  </template>
                </el-popover>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.accident"
              v-model:page="pageInfo.accident.pageNo"
              v-model:limit="pageInfo.accident.pageSize"
              @pagination="getAccidentList"
            />
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <template v-else>
      <template v-if="mode === 'fault'">
        <div class="records-panel">
          <div class="records-header">
            <div class="records-title">故障记录</div>
            <div class="records-count">共 {{ total.fault }} 条</div>
          </div>
          <el-table
            v-loading="loading.fault"
            :data="faultList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="故障时间" prop="faultTime" min-width="160">
              <template #default="scope">
                {{ scope.row.faultTime ? formatDate(scope.row.faultTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="故障现象" prop="faultSymptom" min-width="180" />
            <el-table-column label="处理状态" prop="status" width="120">
              <template #default="scope">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_FAULT_STATUS" :value="scope.row.status" />
              </template>
            </el-table-column>
            <el-table-column label="处理人" prop="repairName" width="120" />
            <el-table-column label="完成时间" prop="finishTime" min-width="160">
              <template #default="scope">
                {{ scope.row.finishTime ? formatDate(scope.row.finishTime) : '-' }}
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.fault"
              v-model:page="pageInfo.fault.pageNo"
              v-model:limit="pageInfo.fault.pageSize"
              @pagination="getFaultList"
            />
          </div>
        </div>
      </template>

      <template v-else-if="mode === 'maintenance'">
        <div class="records-panel">
          <div class="records-header">
            <div class="records-title">养护记录</div>
            <div class="records-count">共 {{ total.maintenance }} 条</div>
          </div>
          <el-table
            v-loading="loading.maintenance"
            :data="maintenanceList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="养护类型" prop="maintainType" min-width="140">
              <template #default="scope">
                {{ getMaintenanceTypeLabel(scope.row.maintainType) }}
              </template>
            </el-table-column>
            <el-table-column label="养护项目" prop="maintainItems" min-width="180" />
            <el-table-column label="养护状态" prop="status" width="120">
              <template #default="scope">
                {{ getMaintenanceStatusLabel(scope.row.status) }}
              </template>
            </el-table-column>
            <el-table-column label="养护人" prop="maintainerName" width="120" />
            <el-table-column label="养护时间" min-width="160">
              <template #default="scope">
                {{ formatPlanTime(scope.row) }}
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.maintenance"
              v-model:page="pageInfo.maintenance.pageNo"
              v-model:limit="pageInfo.maintenance.pageSize"
              @pagination="getMaintenanceList"
            />
          </div>
        </div>
      </template>

      <template v-else-if="mode === 'doc'">
        <div class="records-panel">
          <div class="records-header">
            <div class="records-title">技术文档</div>
            <div class="records-count">共 {{ total.doc }} 条</div>
          </div>
          <el-table
            v-loading="loading.doc"
            :data="docList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="资料名称" prop="docName" min-width="180" />
            <el-table-column label="上传文件" min-width="220">
              <template #default="scope">
                <el-popover
                  v-if="getFileName(scope.row.fileUrl) !== '-'"
                  placement="top"
                  trigger="click"
                  :width="320"
                >
                  <div class="doc-file-info">
                    <div>文件名称：{{ getFileName(scope.row.fileUrl) }}</div>
                    <div>文件类型：{{ getFileType(scope.row.fileFormat, scope.row.fileUrl) }}</div>
                  </div>
                  <template #reference>
                    <el-link class="doc-file-name" type="primary" :underline="false">
                      {{ getFileName(scope.row.fileUrl) }}
                    </el-link>
                  </template>
                </el-popover>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="类型" prop="docType" width="120">
              <template #default="scope">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_DOC_TYPE" :value="scope.row.docType" />
              </template>
            </el-table-column>
            <el-table-column label="文件类型" prop="fileFormat" width="120">
              <template #default="scope">
                {{ getFileType(scope.row.fileFormat, scope.row.fileUrl) }}
              </template>
            </el-table-column>
            <el-table-column label="上传时间" prop="createTime" min-width="160">
              <template #default="scope">
                {{ scope.row.createTime ? formatDate(scope.row.createTime) : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120">
              <template #default="scope">
                <el-link v-if="scope.row.fileUrl" :href="scope.row.fileUrl" target="_blank" download type="primary">
                  下载
                </el-link>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.doc"
              v-model:page="pageInfo.doc.pageNo"
              v-model:limit="pageInfo.doc.pageSize"
              @pagination="getDocList"
            />
          </div>
        </div>
      </template>

      <template v-else-if="mode === 'rating'">
        <div class="records-panel">
          <div class="records-header">
            <div class="records-title">设备评级</div>
            <div class="records-count">共 {{ total.rating }} 条</div>
          </div>
          <el-table
            v-loading="loading.rating"
            :data="ratingList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="评级时间" prop="ratingTime" min-width="160">
              <template #default="scope">
                {{ scope.row.ratingTime ? formatDate(scope.row.ratingTime, 'YYYY-MM-DD') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="评级等级" prop="ratingResult" min-width="120">
              <template #default="scope">
                {{ getRatingLabel(scope.row.ratingResult) }}
              </template>
            </el-table-column>
            <el-table-column label="评级人员" prop="ratingUserName" min-width="120" />
            <el-table-column label="评级说明" prop="ratingBasis" min-width="200" />
            <el-table-column label="附件" min-width="140">
              <template #default="scope">
                <el-popover
                  v-if="normalizeAttachments(scope.row.attachments).length"
                  placement="top"
                  trigger="click"
                  :width="260"
                >
                  <div class="doc-file-info">
                    <el-link
                      v-for="(file, index) in normalizeAttachments(scope.row.attachments)"
                      :key="file + index"
                      :href="file"
                      target="_blank"
                      type="primary"
                      :underline="false"
                    >
                      {{ getFileName(file) }}
                    </el-link>
                  </div>
                  <template #reference>
                    <el-link type="primary" :underline="false">查看</el-link>
                  </template>
                </el-popover>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.rating"
              v-model:page="pageInfo.rating.pageNo"
              v-model:limit="pageInfo.rating.pageSize"
              @pagination="getRatingList"
            />
          </div>
        </div>
      </template>

      <template v-else-if="mode === 'accident'">
        <div class="records-panel">
          <div class="records-header">
            <div class="records-title">事故登记</div>
            <div class="records-count">共 {{ total.accident }} 条</div>
          </div>
          <el-table
            v-loading="loading.accident"
            :data="accidentList"
            :stripe="true"
            :show-overflow-tooltip="true"
            size="small"
            class="records-table"
            empty-text="暂无数据"
          >
            <el-table-column label="事故时间" prop="accidentTime" min-width="160">
              <template #default="scope">
                {{ scope.row.accidentTime ? formatDate(scope.row.accidentTime, 'YYYY-MM-DD HH:mm:ss') : '-' }}
              </template>
            </el-table-column>
            <el-table-column label="事故类型" prop="accidentType" min-width="120">
              <template #default="scope">
                <dict-tag :type="DICT_TYPE.IOT_DEVICE_ACCIDENT_TYPE" :value="scope.row.accidentType" />
              </template>
            </el-table-column>
            <el-table-column label="事故地点" prop="accidentLocation" min-width="160" />
            <el-table-column label="事故描述" prop="accidentDesc" min-width="200" />
            <el-table-column label="处理结果" prop="handleResult" min-width="160" />
            <el-table-column label="责任人" prop="responsibleName" min-width="120" />
            <el-table-column label="登记人" prop="creator" min-width="120">
              <template #default="scope">
                {{ scope.row.creatorName || scope.row.creator || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="事故图片" min-width="140">
              <template #default="scope">
                <el-popover
                  v-if="normalizeAttachments(scope.row.attachments).length"
                  placement="top"
                  trigger="click"
                  :width="260"
                >
                  <div class="doc-file-info">
                    <el-link
                      v-for="(file, index) in normalizeAttachments(scope.row.attachments)"
                      :key="file + index"
                      :href="file"
                      target="_blank"
                      type="primary"
                      :underline="false"
                    >
                      {{ getFileName(file) }}
                    </el-link>
                  </div>
                  <template #reference>
                    <el-link type="primary" :underline="false">查看</el-link>
                  </template>
                </el-popover>
                <span v-else>-</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="records-pagination">
            <Pagination
              :total="total.accident"
              v-model:page="pageInfo.accident.pageNo"
              v-model:limit="pageInfo.accident.pageSize"
              @pagination="getAccidentList"
            />
          </div>
        </div>
      </template>

      <template v-else>
        <div class="records-panel">
          <div class="records-header">
            <div class="records-title">库存备件</div>
            <div class="records-count">共 {{ total.spare + total.consumption }} 条</div>
          </div>
          <el-tabs v-model="spareTab" class="records-sub-tabs">
            <el-tab-pane label="库存备件" name="stock">
              <el-table
                v-loading="loading.spare"
                :data="spareList"
                :stripe="true"
                :show-overflow-tooltip="true"
                size="small"
                class="records-table"
                empty-text="暂无数据"
              >
                <el-table-column label="备件名称" prop="spareName" min-width="180" />
                <el-table-column label="备件分类" prop="spareType" min-width="120">
                  <template #default="scope">
                    <dict-tag :type="DICT_TYPE.IOT_SPARE_TYPE" :value="scope.row.spareType" />
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" min-width="200">
                  <template #default="scope">
                    {{ [scope.row.spareSpec, scope.row.spareModel].filter(Boolean).join(' ') || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="库存数量" prop="stockQty" min-width="120" />
                <el-table-column label="最低库存阈值" prop="minStock" min-width="140" />
                <el-table-column label="库管员" prop="keeperName" min-width="140" />
              </el-table>
              <div class="records-pagination">
                <Pagination
                  :total="total.spare"
                  v-model:page="pageInfo.spare.pageNo"
                  v-model:limit="pageInfo.spare.pageSize"
                  @pagination="getSpareList"
                />
              </div>
            </el-tab-pane>
            <el-tab-pane label="历史消耗" name="history">
              <el-table
                v-loading="loading.consumption"
                :data="consumptionList"
                :stripe="true"
                :show-overflow-tooltip="true"
                size="small"
                class="records-table"
                empty-text="暂无数据"
              >
                <el-table-column label="设备名称" prop="deviceName" min-width="160" />
                <el-table-column label="备件分类" prop="spareType" width="120">
                  <template #default="scope">
                    <dict-tag :type="DICT_TYPE.IOT_SPARE_TYPE" :value="scope.row.spareType" />
                  </template>
                </el-table-column>
                <el-table-column label="用途" prop="usageType" width="140">
                  <template #default="scope">
                    {{ getUsageTypeLabel(scope.row.usageType) }}
                  </template>
                </el-table-column>
                <el-table-column label="规格型号" prop="spareSpec" min-width="180">
                  <template #default="scope">
                    {{ [scope.row.spareSpec, scope.row.spareModel].filter(Boolean).join(' ') || '-' }}
                  </template>
                </el-table-column>
                <el-table-column label="消耗数量" prop="ioQty" width="120" />
                <el-table-column label="消耗时间" prop="ioTime" min-width="160">
                  <template #default="scope">
                    {{ scope.row.ioTime ? formatDate(scope.row.ioTime) : '-' }}
                  </template>
                </el-table-column>
              </el-table>
              <div class="records-pagination">
                <Pagination
                  :total="total.consumption"
                  v-model:page="pageInfo.consumption.pageNo"
                  v-model:limit="pageInfo.consumption.pageSize"
                  @pagination="getConsumptionList"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </template>
    </template>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { FaultRepairApi, type FaultRepairVO } from '@/api/iot/fault-repair'
import { MaintenancePlanApi, type MaintenancePlanVO } from '@/api/iot/maintenance-plan'
import { DeviceDocApi, type DeviceDocVO } from '@/api/iot/device-doc'
import { SpareApi, type SpareVO } from '@/api/iot/spare'
import { SpareIoApi, type SpareIoVO } from '@/api/iot/spare-io'
import { DeviceApi } from '@/api/iot/device/device'
import { DeviceRatingApi, type DeviceRatingVO } from '@/api/iot/device-rating'
import { DeviceAccidentApi, type DeviceAccidentVO } from '@/api/iot/device-accident'
import { DICT_TYPE, getDictLabel } from '@/utils/dict'
import { formatDate } from '@/utils/formatTime'

defineOptions({ name: 'DeviceRelatedRecords' })

type ModeType = 'fault' | 'maintenance' | 'doc' | 'spare' | 'rating' | 'accident'

const props = defineProps<{
  deviceId?: number | string
  deviceName?: string
  deviceType?: number | string
  lastMaintainTime?: number | string
  maintainCycleDays?: number
  mode?: ModeType
}>()

const activeTab = ref<ModeType>('fault')
const spareTab = ref<'stock' | 'history'>('stock')
const PAGE_SIZE = 10
const loading = reactive({
  fault: false,
  maintenance: false,
  doc: false,
  spare: false,
  consumption: false,
  rating: false,
  accident: false
})

const faultList = ref<FaultRepairVO[]>([])
const maintenanceList = ref<MaintenancePlanVO[]>([])
const docList = ref<DeviceDocVO[]>([])
const spareList = ref<SpareVO[]>([])
const ratingList = ref<DeviceRatingVO[]>([])
const accidentList = ref<DeviceAccidentVO[]>([])
const consumptionList = ref<
  Array<{
    id?: number
    usageType?: string
    ioQty?: number
    deviceName?: string
    spareType?: string
    spareSpec?: string
    spareModel?: string
    ioTime?: string | number
  }>
>([])

const total = reactive({
  fault: 0,
  maintenance: 0,
  doc: 0,
  spare: 0,
  consumption: 0,
  rating: 0,
  accident: 0
})

const pageInfo = reactive({
  fault: { pageNo: 1, pageSize: PAGE_SIZE },
  maintenance: { pageNo: 1, pageSize: PAGE_SIZE },
  doc: { pageNo: 1, pageSize: PAGE_SIZE },
  spare: { pageNo: 1, pageSize: PAGE_SIZE },
  consumption: { pageNo: 1, pageSize: PAGE_SIZE },
  rating: { pageNo: 1, pageSize: PAGE_SIZE },
  accident: { pageNo: 1, pageSize: PAGE_SIZE }
})

const syncedMaintenanceDeviceId = ref<string>()

const normalizeDeviceType = computed(() => {
  if (props.deviceType === undefined || props.deviceType === null) {
    return undefined
  }
  const value = Number(props.deviceType)
  return Number.isNaN(value) ? undefined : value
})

const normalizeDeviceId = computed(() => {
  if (props.deviceId === undefined || props.deviceId === null || props.deviceId === '') {
    return undefined
  }
  return String(props.deviceId)
})

const getFaultList = async () => {
  if (!normalizeDeviceId.value) {
    faultList.value = []
    total.fault = 0
    return
  }
  loading.fault = true
  try {
    const data = await FaultRepairApi.getFaultRepairPage({
      pageNo: pageInfo.fault.pageNo,
      pageSize: pageInfo.fault.pageSize,
      deviceId: normalizeDeviceId.value
    })
    const list = data.list || []
    faultList.value = [...list].sort((a, b) => {
      const aTime = a.faultTime ? dayjs(a.faultTime).valueOf() : 0
      const bTime = b.faultTime ? dayjs(b.faultTime).valueOf() : 0
      return bTime - aTime
    })
    total.fault = data.total || 0
  } finally {
    loading.fault = false
  }
}

const calcPlanTime = (item: MaintenancePlanVO) => {
  if (item.planDate) {
    return dayjs(item.planDate).valueOf()
  }
  if (props.lastMaintainTime && props.maintainCycleDays) {
    return dayjs(props.lastMaintainTime).add(props.maintainCycleDays, 'day').valueOf()
  }
  return 0
}

const formatPlanTime = (item: MaintenancePlanVO) => {
  if (item.planDate) {
    return formatDate(item.planDate)
  }
  if (props.lastMaintainTime && props.maintainCycleDays) {
    return formatDate(dayjs(props.lastMaintainTime).add(props.maintainCycleDays, 'day').valueOf())
  }
  return '-'
}

const getMaintenanceList = async () => {
  if (!normalizeDeviceId.value) {
    maintenanceList.value = []
    total.maintenance = 0
    return
  }
  if (syncedMaintenanceDeviceId.value !== normalizeDeviceId.value) {
    try {
      await MaintenancePlanApi.syncAutoMaintenancePlanByDevice(normalizeDeviceId.value)
    } catch {}
    syncedMaintenanceDeviceId.value = normalizeDeviceId.value
  }
  loading.maintenance = true
  try {
    const data = await MaintenancePlanApi.getMaintenancePlanPage({
      pageNo: pageInfo.maintenance.pageNo,
      pageSize: pageInfo.maintenance.pageSize,
      deviceId: normalizeDeviceId.value
    })
    const list = data.list || []
    maintenanceList.value = [...list].sort((a, b) => calcPlanTime(b) - calcPlanTime(a))
    total.maintenance = data.total || 0
  } finally {
    loading.maintenance = false
  }
}

const getDocList = async () => {
  if (!normalizeDeviceId.value) {
    docList.value = []
    total.doc = 0
    return
  }
  loading.doc = true
  try {
    const data = await DeviceDocApi.getDeviceDocPage({
      pageNo: pageInfo.doc.pageNo,
      pageSize: pageInfo.doc.pageSize,
      deviceId: normalizeDeviceId.value,
      deviceType: normalizeDeviceType.value
    })
    const list = data.list || []
    docList.value = [...list].sort((a, b) => {
      const aTime = a.createTime ? dayjs(a.createTime).valueOf() : 0
      const bTime = b.createTime ? dayjs(b.createTime).valueOf() : 0
      return bTime - aTime
    })
    total.doc = data.total || 0
  } finally {
    loading.doc = false
  }
}

const getSpareList = async () => {
  if (!normalizeDeviceId.value && !normalizeDeviceType.value) {
    spareList.value = []
    total.spare = 0
    return
  }
  loading.spare = true
  try {
    const data = await SpareApi.getSparePage({
      pageNo: pageInfo.spare.pageNo,
      pageSize: pageInfo.spare.pageSize,
      deviceId: normalizeDeviceId.value,
      deviceType: normalizeDeviceId.value ? undefined : normalizeDeviceType.value
    })
    spareList.value = data.list || []
    total.spare = data.total || 0
  } finally {
    loading.spare = false
  }
}

const spareDeviceCache = ref<
  Record<
    string,
    {
      deviceId?: number | string
      deviceName?: string
      spareType?: string
      spareSpec?: string
      spareModel?: string
    }
  >
>({})

const resolveSpareSnapshot = async (spareId?: number) => {
  if (!spareId && spareId !== 0) {
    return { deviceName: '-', spareType: undefined, spareSpec: undefined, spareModel: undefined }
  }
  const key = String(spareId)
  if (spareDeviceCache.value[key]?.deviceName) {
    return spareDeviceCache.value[key]
  }
  const spare = await SpareApi.getSpare(spareId)
  const deviceId = spare?.deviceId
  const spareType = spare?.spareType
  const spareSpec = spare?.spareSpec
  const spareModel = spare?.spareModel
  if (!deviceId) {
    spareDeviceCache.value[key] = { deviceId: undefined, deviceName: '-', spareType, spareSpec, spareModel }
    return spareDeviceCache.value[key]
  }
  const device = await DeviceApi.getDevice(deviceId)
  const deviceName = device?.nickname || device?.deviceName || device?.serialNumber || '-'
  spareDeviceCache.value[key] = { deviceId, deviceName, spareType, spareSpec, spareModel }
  return spareDeviceCache.value[key]
}

const getUsageTypeLabel = (value?: string) => {
  if (!value) return '-'
  return getDictLabel(DICT_TYPE.IOT_SPARE_USAGE_TYPE, value) || value
}

const getFileName = (url?: string) => {
  if (!url) return '-'
  const cleanUrl = url.split('?')[0]
  const rawName = cleanUrl.substring(cleanUrl.lastIndexOf('/') + 1)
  if (!rawName) return '-'
  try {
    return decodeURIComponent(rawName)
  } catch {
    return rawName
  }
}

const getFileType = (fileFormat?: string, fileUrl?: string) => {
  if (fileFormat) return fileFormat
  if (!fileUrl) return '-'
  const cleanUrl = fileUrl.split('?')[0]
  const extIndex = cleanUrl.lastIndexOf('.')
  if (extIndex < 0 || extIndex === cleanUrl.length - 1) return '-'
  return cleanUrl.substring(extIndex + 1).toLowerCase()
}

const MaintenanceTypeLabelMap: Record<string, string> = {
  routine: '日常常规养护',
  periodic: '周期定检养护',
  special: '专项场景养护',
  fault_linked: '故障联动养护'
}

const getMaintenanceTypeLabel = (value?: string) => {
  if (!value) return '-'
  return (
    getDictLabel(DICT_TYPE.IOT_MAINTENANCE_TYPE, value) ||
    MaintenanceTypeLabelMap[value] ||
    value
  )
}

const getConsumptionList = async () => {
  if (!normalizeDeviceId.value && !normalizeDeviceType.value) {
    consumptionList.value = []
    total.consumption = 0
    return
  }
  loading.consumption = true
  try {
    const data = await SpareIoApi.getSpareIoPage({
      pageNo: pageInfo.consumption.pageNo,
      pageSize: pageInfo.consumption.pageSize,
      ioType: 'OUT',
      auditStatus: 'approved',
      deviceId: normalizeDeviceId.value,
      deviceType: normalizeDeviceId.value ? undefined : normalizeDeviceType.value
    })
    const list = (data.list || []) as SpareIoVO[]
    const rows = await Promise.all(
      list.map(async (item) => {
        const snapshot = await resolveSpareSnapshot(item.spareId)
        return {
          id: item.id,
          usageType: item.usageType,
          ioQty: item.ioQty,
          deviceName: snapshot.deviceName,
          spareType: snapshot.spareType,
          spareSpec: snapshot.spareSpec,
          spareModel: snapshot.spareModel,
          ioTime: item.ioTime
        }
      })
    )
    consumptionList.value = rows
    total.consumption = data.total || 0
  } finally {
    loading.consumption = false
  }
}

const getRatingLabel = (value?: string) => {
  if (!value) return '-'
  return getDictLabel(DICT_TYPE.IOT_DEVICE_RATING, value) || value
}

const normalizeAttachments = (
  attachments?: string[] | string | Array<{ url?: string }>
) => {
  if (!attachments) return []
  if (Array.isArray(attachments)) {
    return attachments
      .map((item) => (typeof item === 'string' ? item : item?.url))
      .filter((item): item is string => Boolean(item))
  }
  return attachments.split(',').filter(Boolean)
}

const getRatingList = async () => {
  if (!normalizeDeviceId.value) {
    ratingList.value = []
    total.rating = 0
    return
  }
  loading.rating = true
  try {
    const data = await DeviceRatingApi.getDeviceRatingPage({
      pageNo: pageInfo.rating.pageNo,
      pageSize: pageInfo.rating.pageSize,
      deviceId: normalizeDeviceId.value
    })
    ratingList.value = data.list || []
    total.rating = data.total || 0
  } finally {
    loading.rating = false
  }
}

const getAccidentList = async () => {
  if (!normalizeDeviceId.value) {
    accidentList.value = []
    total.accident = 0
    return
  }
  loading.accident = true
  try {
    const data = await DeviceAccidentApi.getDeviceAccidentPage({
      pageNo: pageInfo.accident.pageNo,
      pageSize: pageInfo.accident.pageSize,
      deviceId: normalizeDeviceId.value
    })
    accidentList.value = data.list || []
    total.accident = data.total || 0
  } finally {
    loading.accident = false
  }
}

const resetPageInfo = () => {
  pageInfo.fault.pageNo = 1
  pageInfo.maintenance.pageNo = 1
  pageInfo.doc.pageNo = 1
  pageInfo.spare.pageNo = 1
  pageInfo.consumption.pageNo = 1
  pageInfo.rating.pageNo = 1
  pageInfo.accident.pageNo = 1
}

const loadByMode = async () => {
  if (props.mode === 'fault') {
    await getFaultList()
    return
  }
  if (props.mode === 'maintenance') {
    await getMaintenanceList()
    return
  }
  if (props.mode === 'doc') {
    await getDocList()
    return
  }
  if (props.mode === 'spare') {
    await Promise.all([getSpareList(), getConsumptionList()])
    return
  }
  if (props.mode === 'rating') {
    await getRatingList()
    return
  }
  if (props.mode === 'accident') {
    await getAccidentList()
    return
  }
  await Promise.all([
    getFaultList(),
    getMaintenanceList(),
    getDocList(),
    getSpareList(),
    getConsumptionList(),
    getRatingList(),
    getAccidentList()
  ])
}

watch(
  () => [props.deviceId, props.deviceType, props.lastMaintainTime, props.maintainCycleDays, props.mode],
  () => {
    resetPageInfo()
    loadByMode()
  },
  { immediate: true }
)

const MaintenanceStatusLabelMap: Record<string, string> = {
  pending: '未完成',
  completed: '已完成'
}

const getMaintenanceStatusLabel = (value?: string) => {
  if (!value) return '-'
  return MaintenanceStatusLabelMap[value] || value
}
</script>

<style scoped>
.device-records :deep(.el-tabs__header) {
  margin-bottom: 12px;
}

.device-records :deep(.el-tabs__content) {
  padding-top: 4px;
}

.records-tabs :deep(.el-tabs__nav-wrap) {
  overflow: visible;
}

.records-tabs :deep(.el-tabs__nav) {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 8px 10px;
  width: 100%;
}

.records-tabs :deep(.el-tabs__item) {
  height: 32px;
  line-height: 32px;
  padding: 0 12px;
  border-radius: 16px;
  border: 1px solid var(--el-border-color-light);
  background: var(--el-fill-color-lighter);
  color: var(--el-text-color-regular);
  margin-right: 0;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  white-space: nowrap;
  transition: all 0.2s ease;
}

.records-tabs :deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.records-tabs :deep(.el-tabs__item:hover) {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.records-tabs :deep(.el-tabs__active-bar) {
  display: none;
}

.records-panel {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 12px;
  padding: 12px;
  background: var(--el-bg-color);
  box-shadow: 0 6px 18px rgba(15, 23, 42, 0.04);
}

.records-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.records-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.records-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  padding: 2px 10px;
  border-radius: 999px;
  background: var(--el-fill-color-light);
}

.records-sub-tabs :deep(.el-tabs__header) {
  margin-bottom: 10px;
}

.records-sub-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.records-sub-tabs :deep(.el-tabs__nav) {
  background: var(--el-fill-color-light);
  border-radius: 999px;
  padding: 4px;
}

.records-sub-tabs :deep(.el-tabs__item) {
  border-radius: 999px;
  padding: 0 18px;
  height: 30px;
  line-height: 30px;
  color: var(--el-text-color-regular);
}

.records-sub-tabs :deep(.el-tabs__item.is-active) {
  background: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
  font-weight: 600;
}

.device-records :deep(.el-table) {
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
  background: var(--el-bg-color);
}

.device-records :deep(.el-table__header th) {
  background: var(--el-fill-color-light);
  font-weight: 600;
}

.device-records :deep(.el-table__cell) {
  padding: 10px 8px;
}

.records-table :deep(.el-table__header table),
.records-table :deep(.el-table__body table) {
  width: 100%;
}

.device-records :deep(.el-table__empty-text) {
  color: var(--el-text-color-secondary);
}

.device-records :deep(.el-pagination) {
  justify-content: flex-end;
  margin-top: 8px;
}

.records-pagination {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
}

.doc-file-name {
  display: inline-block;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  vertical-align: middle;
}

.doc-file-info {
  line-height: 1.6;
}

</style>
