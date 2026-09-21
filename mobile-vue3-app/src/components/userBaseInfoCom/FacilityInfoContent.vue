<!-- 关联设施内容组件 -->
<template>
    <div class="facility-info-content">
        <!-- 设施列表 -->
        <div class="section">
            <span class="contentTit">关联设施</span>
            <!-- 有数据时显示设施列表 -->
            <div v-if="facilityList && facilityList.length > 0" class="facility-list">
                <!-- 设施卡片 -->
                <div class="facility-card" v-for="(facility, index) in facilityList" :key="index">
                    <div class="facility-info">
                        <div class="info-item">
                            <span class="label">设施名称:</span>
                            <span class="value">{{ facility.name }}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">设施类型:</span>
                            <span class="value">{{ facility.type }}</span>
                        </div>
                        <div class="info-item">
                            <span class="label">设施位置:</span>
                            <span class="value">{{ facility.location }}</span>
                        </div>
                    </div>
                </div>
            </div>
            <!-- 无数据时显示缺省图 -->
            <div v-else class="facility-empty">
                <div class="empty-placeholder">
                    <img :src="nodataImg" alt="暂无数据" class="nodata-img" />
                    <div class="nodata-text">——暂无数据——</div>
                </div>
            </div>
        </div>

        <!-- 底部提示 -->
        <div v-if="facilityList && facilityList.length > 0" class="footer-tip">——已全部展示——</div>
    </div>
</template>

<script>
import nodataImg from '@/assets/img/nodataImg.png';

export default {
    name: 'FacilityInfoContent',
    data() {
        return {
            // 设施信息列表
            facilityList: [],
            // 缺省图片
            nodataImg
        }
    }
}
</script>

<style lang="scss" scoped>
.facility-info-content {
    padding: 24px;
    background-color: #f5f5f5;
    min-height: 100%;
    display: flex;
    flex-direction: column;

    .contentTit {
        font-size: 32px;
        font-weight: 500;
        color: #3D3D3D;
        margin-bottom: 24px;
        display: block;
    }

    .section {
        margin-bottom: 32px;
    }

    .facility-list {
        display: flex;
        flex-direction: column;
        gap: 24px;
    }

    .facility-card {
        background-color: #ffffff;
        border-radius: 8px;
        padding: 24px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    }

    .facility-info {
        display: flex;
        flex-direction: column;
        gap: 16px;
    }

    .info-item {
        display: flex;
        align-items: center;
        font-size: 28px;

        .label {
            color: #666666;
            margin-right: 16px;
            min-width: 160px;
        }

        .value {
            color: #3D3D3D;
            font-weight: 400;
            flex: 1;
        }
    }

    .footer-tip {
        text-align: center;
        color: #999999;
        font-size: 24px;
        margin-top: auto;
        padding-top: 32px;
        padding-bottom: 32px;
    }

    // 缺省图样式
    .facility-empty {
        width: 100%;
        display: flex;
        justify-content: center;
        align-items: center;
        min-height: 400px;

        .empty-placeholder {
            width: 100%;
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            background-color: #f8f8f8;
            border-radius: 8px;
            padding: 40px 0;

            .nodata-img {
                width: 100px;
                height: auto;
                margin-bottom: 16px;
            }

            .nodata-text {
                font-size: 24px;
                color: #999999;
                text-align: center;
            }
        }
    }
}
</style>

