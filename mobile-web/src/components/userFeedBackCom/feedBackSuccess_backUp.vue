<!-- 反馈提交成功中转页面 -->
<template>
    <div class="feedBack-success-container">
        <PageHeader title="仪征河湖管理平台" @close="handleClose" />
        <div class="content-layer">
            <!-- 页面标题组件 -->

            <div class="success-content1">
                <!-- 1. 反馈成功标题 -->
                <div class="success-title">
                    <img src="@/assets/img/uploadSuccessIcon.png" alt="">
                    <span>反馈成功</span>
                </div>
                
                <!-- 2. 二维码容器 -->
                <div class="qrcode-box">
                    <canvas ref="qrcodeCanvas" class="qrcode-canvas"></canvas>
                    <img :src="loginLogo" class="qrcode-center-icon" alt="中心图标" />
                </div>
                
                <!-- 3. 下载二维码按钮 -->
                <button class="download-qrcode-btn" @click="downloadQRCode">下载二维码</button>
                
                <!-- 4. 说明文字 -->
                <div class="qrcode-label">下载二维码以便查看反馈</div>
            </div>

            <div class="feedBackList">
                <div class="feedBackItem" v-for="item in feedBackList" :key="item.id">
                    <div class="itemTitle">{{ formatTime(item.createTime) }}创建的问题</div>

                    <div class="feedBackBox">
                        <!-- 问题类型 -->
                        <div class="feedBackItem-row">
                            <span class="itemLabel">问题类型:</span>
                            <span class="itemVal">{{ item.feedbackTypeLabel }}</span>
                        </div>
                        <!-- 问题描述 -->
                        <div class="feedBackItem-row">
                            <span class="itemLabel">问题描述:</span>
                            <span class="itemVal">{{ item.feedbackContent }}</span>
                        </div>
                        <!-- 反馈人员 -->
                        <div class="feedBackItem-row" v-if="item.realName">
                            <span class="itemLabel">反馈人员:</span>
                            <span class="itemVal">{{ item.name || '--' }}</span>
                        </div>
                        <!-- 反馈文件 -->
                        <div class="feedBackItem-file" v-if="item.uploadedFiles && item.uploadedFiles.length > 0">
                            <span class="itemLabel">反馈文件:</span>
                            <div class="fileList">
                                <div class="fileItem" v-for="(fileUrl, index) in item.uploadedFiles" :key="index"
                                    @click="previewFile(fileUrl)">
                                    <img v-if="isImage(fileUrl)" :src="fileUrl" alt="反馈图片" />
                                    <div v-else class="video-icon">
                                        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
                                            <path d="M8 5v14l11-7z" fill="#fff" />
                                        </svg>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>

                    <div class="feedBackBox" style="border-bottom: none; margin-bottom: 0px; padding-bottom: 0px;">
                        <div class="feedBackItem-row">
                            <span class="itemLabel">水利设施:</span>
                            <span class="itemVal">{{ getReferenceTypeLabel(item.referenceType) || '--' }}</span>
                        </div>
                        <div class="feedBackItem-row">
                            <span class="itemLabel">{{ getReferenceTypeNameLabel(item.referenceType) }}:</span>
                            <span class="itemVal">{{ item.referenceName || '--' }}</span>
                        </div>
                        <div class="feedBackItem-row" v-if="item.referenceType === 'river' && item.riverSectionName">
                            <span class="itemLabel">河段名称:</span>
                            <span class="itemVal">{{ item.riverSectionName }}</span>
                        </div>
                        <div class="feedBackItem-row">
                            <span class="itemLabel">问题定位:</span>
                            <span class="itemVal" style="display: flex; align-items: center">
                                <img src="@/assets/img/positionIcon.png" alt="问题定位" style="width: 15px; height: 15px;">
                                <span style="flex: 1;">{{ item.issueSpecificLocation }}</span>
                            </span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 去反馈列表按钮 -->
            <div class="go-to-list-btn-wrapper">
                <button class="go-to-list-btn" @click="goToFeedBackList">
                    <img src="@/assets/img/backArrow.png" alt="前往反馈列表" style="width: 25px; height: 25px;"/>
                    <span>反馈列表</span>
                </button>
            </div>

        </div>
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import QRCode from 'qrcode';
import loginLogo from '@/assets/img/loginLogo.png';

export default {
    name: 'FeedBackSuccess',
    components: {
        PageHeader
    },
    mounted() {
        this.getFeedBackList();
        // 使用 nextTick 确保 DOM 渲染完成后再生成二维码
        this.$nextTick(() => {
            this.generateQRCode();
        });
    },
    data() {
        return {
            feedBackList: [],
            loginLogo: loginLogo // 将导入的图片添加到 data 中
        };
    },
    methods: {
        // 关闭页面
        handleClose() {
            this.$router.push({ name: 'UserFeedBack' });
        },

        // 前往反馈列表
        goToFeedBackList() {
            this.$router.push({ name: 'FeedBackList' });
        },

        //查询全部反馈列表
        getFeedBackList() {
            const params = {
                pageNo: 1,
                pageSize: 1,
            };
            this.$http.get('/app-api/problem/feedback/my-page', { params }).then(res => {
                console.log(res.data.list[0]);
                if (res.data && res.data.list) {
                    // 第一条
                    this.feedBackList = [res.data.list[0]];
                }
            }).catch(error => {
                console.error('获取反馈列表失败:', error);
            });
        },

        // 格式化时间戳
        formatTime(timestamp) {
     
            return this.$tool.dateFormat(timestamp, 'yyyy-MM-dd hh:mm:ss');
        },

        // 判断是否为图片
        isImage(url) {
            if (!url) return false;
            const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
            const lowerUrl = url.toLowerCase();
            return imageExtensions.some(ext => lowerUrl.includes(ext));
        },

        // 预览文件
        previewFile(fileUrl) {
            if (this.isImage(fileUrl)) {
                // 预览图片
                window.open(fileUrl, '_blank');
            } else {
                // 预览视频
                window.open(fileUrl, '_blank');
            }
        },

        // 获取 referenceType 对应的中文字段
        getReferenceTypeLabel(referenceType) {
            const typeMap = {
                'river': '河道',
                'river_section': '河段',
                'reservoir': '水库'
            };
            return typeMap[referenceType] || '--';
        },

        // 获取 referenceType 拼接 '名称' 的标签
        getReferenceTypeNameLabel(referenceType) {
            const typeMap = {
                'river': '河道名称',
                'river_section': '河段名称',
                'reservoir': '水库名称'
            };
            return typeMap[referenceType] || '名称';
        },

        // 生成二维码
        generateQRCode() {
            try {
                // 使用 setTimeout 确保 DOM 完全渲染
                setTimeout(() => {
                    const canvas = this.$refs.qrcodeCanvas;
                    if (canvas) {
                        // 获取 baseUrl 并拼接路径
                        const baseUrl = window.getConfig('api.baseUrl') || '';
                        const qrCodeUrl = `${baseUrl}/h5/adminLogin`;
                        console.log('二维码URL:', qrCodeUrl);
                        
                        // 生成指向管理后台登录页面的二维码
                        QRCode.toCanvas(canvas, qrCodeUrl, {
                            width: 90, // 二维码尺寸
                            margin: 0, // 增大边距，增加中间空白区域（从2增加到6）
                            errorCorrectionLevel: 'H', // 高容错级别，支持中心图标
                            color: {
                                dark: '#000000',
                                light: '#FFFFFF' // 先生成白色背景
                            }
                        }).then(() => {
                            console.log('二维码生成成功');
                            // 清除白色背景，使其透明
                            const ctx = canvas.getContext('2d');
                            const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
                            const data = imageData.data;
                            // 将白色像素改为透明
                            for (let i = 0; i < data.length; i += 4) {
                                // 检查是否为白色像素（RGB 都接近 255）
                                if (data[i] > 250 && data[i + 1] > 250 && data[i + 2] > 250) {
                                    data[i + 3] = 0; // 设置 alpha 为 0（透明）
                                }
                            }
                            ctx.putImageData(imageData, 0, 0);
                        }).catch(error => {
                            console.error('生成二维码失败:', error);
                        });
                    } else {
                        console.warn('二维码 canvas 元素未找到');
                    }
                }, 100);
            } catch (error) {
                console.error('生成二维码失败:', error);
            }
        },


        // 下载二维码
        downloadQRCode() {
            try {
                const canvas = this.$refs.qrcodeCanvas;
                if (canvas) {
                    // 将 canvas 转换为图片并下载
                    const url = canvas.toDataURL('image/png');
                    const link = document.createElement('a');
                    link.download = '二维码.png';
                    link.href = url;
                    link.click();
                }
            } catch (error) {
                console.error('下载二维码失败:', error);
            }
        },
    }
}
</script>

<style lang="scss" scoped>
.feedBack-success-container {
    position: relative;
    width: 100%;
    min-height: 100vh;
    overflow: hidden;


    .content-layer {
        display: flex;
        justify-content: center;
        align-items: center;
        flex-direction: column;
        padding: 24px 0px;


        .success-content1 {
            width: 702px;
            padding: 24px;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: flex-start;
            background-color: #ffffff;
            margin-bottom: 16px;

            .success-title {
                color: #3d3d3d;
                font-size: 40px;
                font-weight: 500;
                line-height: 40px;
                text-align: center;
                margin-bottom: 24px;
                img{
                    width: 20px;
                    height: 20px;
                }
            }

            .qrcode-box {
                width: 410px;
                height: 250px;
                background-color: #C5E2FF;
                background-image: url('@/assets/img/qrcodeBox.png');
                background-size: 100% 100%;
                background-repeat: no-repeat;
                background-position: center;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 24px;
                border-radius: 8px;
                position: relative;

                .qrcode-canvas {
                    margin-top: 25px;
                    width: 100px;
                    height: 100px;
                }

                .qrcode-center-icon {
                    position: absolute;
                    width: 45px; // 增大图标尺寸，确保可见
                    height: 45px;
                    top: 50%;
                    left: 50%;
                    transform: translate(-50%, -50%);
                    object-fit: contain;
                    // 添加白色背景圆角矩形
                    border-radius: 4px;
                    padding: 2px;
                    box-sizing: content-box;
                    z-index: 10; // 确保图标在二维码上方
                }
            }

            .download-qrcode-btn {
                width: 200px;
                height: 64px;
                background: #349DFF;
                border-radius: 8px;
                border: none;
                color: #ffffff;
                font-size: 28px;
                font-weight: 500;
                cursor: pointer;
                transition: all 0.3s ease;
                margin-bottom: 16px;

                &:active {
                    opacity: 0.8;
                    transform: scale(0.98);
                }
            }

            .qrcode-label {
                font-size: 24px;
                color: #999999;
                text-align: center;
            }
        }

        .feedBackList {
            width: 702px;
            opacity: 1;
            background: #ffffff;
            padding: 24px;

            .feedBackItem {
                .itemTitle {
                    color: #3d3d3d;
                    font-size: 32px;
                    font-weight: 500;
                    line-height: 32px;
                    padding-bottom: 16px;
                    border-bottom: 1px solid rgba(238, 238, 238, 0.5);
                    margin-bottom: 16px;
                }

                .feedBackBox {
                    display: flex;
                    flex-direction: column;
                    gap: 16px;
                    padding-bottom: 16px;
                    border-bottom: 1px solid rgba(238, 238, 238, 0.5);
                    margin-bottom: 24px;

                    .feedBackItem-row {
                        display: flex;
                        align-items: center;
                        justify-content: space-between;
                        color: #3d3d3d;
                        font-size: 28px;
                        font-weight: 500;
                        line-height: 28px;
                        // margin-bottom: 16px;

                        .itemVal {
                            font-weight: 400;
                        }
                    }

                    .feedBackItem-file {
                        margin-top: -3px;

                        .itemLabel {
                            color: #3d3d3d;
                            font-size: 28px;
                            font-weight: 500;
                            line-height: 28px;
                        }

                        .fileList {
                            display: flex;
                            gap: 20px;
                            margin-top: 16px;

                            .fileItem {
                                width: 80px;
                                height: 80px;
                                background-color: #f5f5f5;
                                border-radius: 8px;
                                overflow: hidden;
                                cursor: pointer;
                                position: relative;

                                img {
                                    width: 100%;
                                    height: 100%;
                                    object-fit: cover;
                                }

                                .video-icon {
                                    width: 100%;
                                    height: 100%;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    background-color: rgba(0, 0, 0, 0.5);

                                    svg {
                                        width: 32px;
                                        height: 32px;
                                    }
                                }

                                &:active {
                                    opacity: 0.8;
                                }
                            }
                        }
                    }
                }

            }
        }


        .go-to-list-btn-wrapper {
            width: 702px;
            padding: 24px;
            display: flex;
            justify-content: center;
            align-items: center;

            .go-to-list-btn {
                width: 100%;
                height: 96px;
                background: #349DFF;
                border-radius: 8px;
                border: none;
                color: #ffffff;
                font-size: 32px;
                font-weight: 500;
                display: flex;
                align-items: center;
                justify-content: center;
                gap: 12px;
                cursor: pointer;
                transition: all 0.3s ease;

                svg {
                    flex-shrink: 0;
                }

                span {
                    line-height: 1;
                }

                &:active {
                    opacity: 0.8;
                    transform: scale(0.98);
                }
            }
        }
    }
}
</style>
