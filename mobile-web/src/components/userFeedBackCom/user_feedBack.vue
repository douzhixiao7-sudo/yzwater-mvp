<!-- 用户端反馈页面 -->
<template>
    <div class="user-feedBack-container">
        <div class="content-layer" :class="{ 'has-bottom-nav': hasBottomNav }">
            <!-- 页面标题组件 -->
            <!-- <PageHeader :title="pageTitle" @close="handleClose" /> -->

            <div class="paddingContent">
                <!-- 反馈类型 -->
                <div class="feedBackType">
                    <ModuleTitle title="反馈类型(必填)" required />
                    <div class="feedBackType-content">
                        <div class="typeList">
                            <div class="typeItem" v-for="(item, index) in typeList" :key="item.id"
                                :class="{ 'activeTypeItem': index === currentIndex }"
                                @click="handleTypeClick(index, item)">
                                <div class="image-wrapper">
                                    <img :src="getTypeImage(item.id, index === currentIndex)" :alt="item.label"
                                        class="type-image" :class="{ 'image-active': index === currentIndex }" />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- 反馈内容 -->
                <div class="feedBackContent">
                    <ModuleTitle title="反馈内容(必填)" required />
                    <div class="content-textarea-wrapper">
                        <textarea v-model="formData.content" class="content-textarea" placeholder="请输入您的意见和建议"
                            maxlength="200" @input="updateCharCount"></textarea>
                        <div class="char-count">{{ charCount }}/200</div>
                    </div>
                </div>
                <!-- 反馈附件-->
                <div class="feedBackResource">
                    <ModuleTitle title="图片/视频(必填)" required info="(最多传6个文件，视频时长不超过30秒)" />
                    <div class="upload-area">
                        <MediaUpload v-model="fileList" :max-files="6" @upload-success="handleUploadSuccess"
                            @upload-delete="handleUploadDelete" @upload-status-change="handleUploadStatusChange" />
                    </div>
                </div>
                <!-- 问题点位 -->
                <div class="feedBackPosition">
                    <ModuleTitle title="问题点位(必填)" required />
                    <div class="upload-position-wrapper">
                        <div class="upload-position">
                            <img src="@/assets/img/position2.png" alt="" class="position-icon">
                            <div class="location-content">
                                <div class="location-main-text">{{ locationText }}</div>
                                <div class="location-sub-text" v-if="riverReservoirName">{{ riverReservoirName }}</div>
                            </div>
                            <div class="location-actions">
                                <!-- <span class="clickLabel get-location-btn" @click="handleGetCurrentLocation" title="获取位置"></span> -->
                                <span class="clickLabel" @click="goToMap">去定位 ></span>
                            </div>
                        </div>
                    </div>
                </div>
                <!-- 反馈表单 -->
                <div class="feedBackForm">
                    <ModuleTitle :title="contactTitle" :required="isContactRequired" />
                    <!-- 联系方式输入 -->
                    <div class="contact-inputs">
                        <div class="name-input-wrapper" :class="{ 'has-error': nameError }">
                            <input v-model="formData.name" type="text" class="form-input"
                                :class="{ 'input-error': nameError }" placeholder="请输入您的姓名" @input="handleNameInput" />
                            <div v-if="nameError" class="error-tip">{{ nameError }}</div>
                        </div>
                        <!-- 电话号码输入框始终显示，但只在必填时自动填充 -->
                        <div class="phone-input-wrapper" :class="{ 'has-error': phoneNumberError }">
                            <input v-model="formData.phoneNumber" type="tel" class="form-input"
                                :class="{ 'input-error': phoneNumberError }" placeholder="请输入您的手机号码" maxlength="11"
                                @input="handlePhoneInput" />
                            <div v-if="phoneNumberError" class="error-tip">{{ phoneNumberError }}</div>
                        </div>
                    </div>

                </div>
            </div>
        </div>

        <!-- 提交按钮 - 固定定位，悬浮在底部 -->
        <div class="submit-btn-wrapper">
            <span class="submit-btn" :class="{ 'submit-btn-active': isFormComplete }" @click="submitForm">
                提交反馈
            </span>
        </div>

        <!-- 历史记录悬浮按钮 -->
        <div class="floating-history-btn" @click="goToMyHistory">
        </div>
    </div>
</template>

<script>
import PageHeader from '@/components/commonCom/PageHeader.vue';
import ModuleTitle from '@/components/userFeedBackCom/moduleTitle.vue';
import MediaUpload from '@/components/commonCom/MediaUpload.vue';
import { ElTooltip, ElMessage } from 'element-plus';
import tool from '@/utils/tools';
import http from '@/utils/request';
import { getCurrentLocationByAmap } from '@/utils/amapLocation';
// 导入类型图片
import type1No from '@/assets/img/type1-no.png';
import type1Act from '@/assets/img/type1-act.png';
import type2No from '@/assets/img/type2-no.png';
import type2Act from '@/assets/img/type2-act.png';
import type3No from '@/assets/img/type3-no.png';
import type3Act from '@/assets/img/type3-act.png';

export default {
    name: 'UserFeedBack',
    components: {
        PageHeader,
        ModuleTitle,
        MediaUpload,
        ElTooltip
    },
    data() {
        return {
            //反馈类型列表
            typeList: [
                { id: 1, label: '漂浮物', value: 1 },
                { id: 4, label: '设施损坏', value: 4 },
                { id: 6, label: '其他', value: 6 },
            ],
            //当前点击的类型索引
            currentIndex: -1,
            // 表单数据
            formData: {
                content: '',
                contactType: 'realname', // 默认选择实名
                name: '',
                phoneNumber: '', // 手机号码
                resource: [] // 资源数据，存放 uploadResults
            },
            // 字符计数
            charCount: 0,
            // 文件列表
            fileList: [],
            // 上传结果存储（只包含 fileUrl, fileName, fileType）
            uploadResults: [],
            // 是否有文件正在上传
            hasUploading: false,
            // 位置信息
            locationText: '正在获取位置...', // 默认位置文本

            //保存经纬度等信息
            saveLnglatInfo: null,

            // 原始经纬度和位置信息（用于判断是否重新定位）
            originalLocation: {
                longitude: null,
                latitude: null,
                specificLocation: null
            },

            // 重新定位后的经纬度和位置信息
            relocatedLocation: {
                longitude: null,
                latitude: null,
                specificLocation: null
            },

            // 是否重新定位过
            isRelocated: false,

            // 手机号码错误提示
            phoneNumberError: '',

            // 姓名错误提示
            nameError: '',

            // 是否正在恢复表单数据（用于防止 watch 触发）
            isRestoringFormData: false,
        }
    },
    computed: {
        // 判断是否选择了"其他"类型
        isOtherType() {
            if (this.currentIndex === -1) return false;
            const selectedType = this.typeList[this.currentIndex];
            if (!selectedType) return false;
            // 支持数字ID（兼容旧数据）和字符串ID（新数据），以及通过 label 判断
            return selectedType.label === '其他' ||
                selectedType.value === 6 ||
                selectedType.id === 6 ||
                selectedType.value === '6' ||
                selectedType.id === '6';
        },

        // 联系方式是否必填
        isContactRequired() {
            return this.isOtherType;
        },

        // 联系方式标题
        contactTitle() {
            return this.isContactRequired ? '联系方式(必填)' : '联系方式';
        },

        // 判断表单是否完整
        isFormComplete() {
            // 如果有资源正在上传或检查中，表单不可提交
            if (this.hasUploading) return false;
            // 反馈类型已选择
            if (this.currentIndex === -1) return false;
            // 反馈内容已填写
            if (!this.formData.content.trim()) return false;
            // 资源已上传
            if (this.formData.resource.length === 0) return false;
            // 验证位置信息（必须选择位置）
            if (!this.saveLnglatInfo || !this.saveLnglatInfo.referenceId || !this.saveLnglatInfo.referenceType) {
                return false;
            }
            // 如果选择了"其他"类型，联系方式为必填
            if (this.isOtherType) {
                if (!this.formData.name.trim() || !this.formData.phoneNumber.trim()) {
                    return false;
                }
                // 姓名格式验证
                if (!this.isValidName(this.formData.name)) {
                    return false;
                }
                // 手机号码格式验证
                if (!this.isValidPhoneNumber(this.formData.phoneNumber)) {
                    return false;
                }
                // 手机号码须与登录账号一致
                if (!this.isPhoneMatchLoginUser(this.formData.phoneNumber)) {
                    return false;
                }
            } else {
                // 非「其他」类型：若填写了手机号，也须与登录账号一致
                const formPhone = this.formData.phoneNumber.trim();
                if (formPhone && !this.isPhoneMatchLoginUser(formPhone)) {
                    return false;
                }
            }
            return true;
        },


        // 河道/水库名称（用于显示在问题点位模块）
        riverReservoirName() {
            // 优先显示：河道名称 + 河段名称
            if (this.saveLnglatInfo?.riverChannelName) {
                let name = this.saveLnglatInfo.riverChannelName;
                if (this.saveLnglatInfo?.riverSectionName) {
                    name += ' | ' + this.saveLnglatInfo.riverSectionName;
                }
                return name;
            }
            // 如果没有河道名称，显示水库名称
            if (this.saveLnglatInfo?.waterReservoirs?.[0]?.reservoirName) {
                return this.saveLnglatInfo.waterReservoirs[0].reservoirName;
            }
            return '';
        },

        // 判断是否有底部导航栏（安卓设备且路由 meta.showBackButton 为 true）
        hasBottomNav() {
            const isAndroid = tool.isAndroid();
            const hasMeta = this.$route && this.$route.meta && this.$route.meta.showBackButton === true;
            return isAndroid && hasMeta;
        }
    },
    mounted() {
        // 设置固定页面标题
        // document.title = '仪征河长制';
        // 页面加载后确保滚动到顶部
        this.scrollToTop();
        //获取反馈类型
        this.getFeedBackType();
        // 获取经纬度（会优先从 Vuex 读取位置信息）
        this.lngLatInfo();
        // 从 localStorage 读取手机号码并回显
        this.loadUserMobile();
        // 从 Vuex 恢复表单数据
        this.restoreFormData();
        // 从 Vuex 恢复文件列表
        this.restoreFileListFromVuex();
        // 自动获取位置（如果Vuex中没有位置信息）
        this.autoGetLocation();
    },
    activated() {
        // 当从地图页面返回时，重新检查 Vuex 中的位置信息
        const relocatedLocationFromStore = this.$store.getters.getRelocatedLocation;
        if (relocatedLocationFromStore && relocatedLocationFromStore.specificLocation) {
            // 应用重新定位的位置信息
            this.applyRelocatedLocation(relocatedLocationFromStore);

            // 更新 saveLnglatInfo（如果已存在）
            if (this.saveLnglatInfo) {
                this.saveLnglatInfo.latitude = relocatedLocationFromStore.latitude;
                this.saveLnglatInfo.longitude = relocatedLocationFromStore.longitude;
                this.saveLnglatInfo.specificLocation = relocatedLocationFromStore.specificLocation;
                // 更新河道/水库信息（无论是否存在都更新，允许清空）
                this.saveLnglatInfo.riverChannelName = relocatedLocationFromStore.riverChannelName || null;
                this.saveLnglatInfo.riverSectionName = relocatedLocationFromStore.riverSectionName || null;
                this.saveLnglatInfo.waterReservoirs = relocatedLocationFromStore.waterReservoirs || null;
                // 更新关联信息（referenceId 和 referenceType）
                this.saveLnglatInfo.referenceId = relocatedLocationFromStore.referenceId || null;
                this.saveLnglatInfo.referenceType = relocatedLocationFromStore.referenceType || null;
            } else {
                // 如果 saveLnglatInfo 不存在，创建它
                this.saveLnglatInfo = {
                    longitude: relocatedLocationFromStore.longitude,
                    latitude: relocatedLocationFromStore.latitude,
                    specificLocation: relocatedLocationFromStore.specificLocation,
                    riverChannelName: relocatedLocationFromStore.riverChannelName || null,
                    riverSectionName: relocatedLocationFromStore.riverSectionName || null,
                    waterReservoirs: relocatedLocationFromStore.waterReservoirs || null,
                    // 关联信息（referenceId 和 referenceType）
                    referenceId: relocatedLocationFromStore.referenceId || null,
                    referenceType: relocatedLocationFromStore.referenceType || null
                };
            }

            console.log('activated - 更新后的 saveLnglatInfo:', this.saveLnglatInfo);
        }
        // 从 Vuex 恢复表单数据
        this.restoreFormData();
        // 从 Vuex 恢复文件列表
        this.restoreFileListFromVuex();
    },
    watch: {
        // 监听表单数据变化，自动保存到 Vuex
        currentIndex: {
            handler(newIndex) {
                if (!this.isRestoringFormData) {
                    this.saveFormDataToVuex();
                }
                // 不管是否必填，都自动填充手机号码（如果为空）
                if (newIndex !== -1) {
                    const savedMobile = localStorage.getItem('userMobile');
                    if (savedMobile && !this.formData.phoneNumber) {
                        this.formData.phoneNumber = savedMobile;
                        this.validatePhoneNumber();
                    }
                    // 清空错误提示（切换类型时）
                    this.phoneNumberError = '';
                }
            }
        },
        'formData.content': {
            handler() {
                if (!this.isRestoringFormData) {
                    this.saveFormDataToVuex();
                }
            }
        },
        'formData.contactType': {
            handler() {
                if (!this.isRestoringFormData) {
                    this.saveFormDataToVuex();
                }
            }
        },
        'formData.name': {
            handler() {
                if (!this.isRestoringFormData) {
                    this.saveFormDataToVuex();
                }
            }
        },
        'formData.phoneNumber': {
            handler() {
                if (!this.isRestoringFormData) {
                    this.saveFormDataToVuex();
                }
            }
        },
        uploadResults: {
            handler() {
                if (!this.isRestoringFormData) {
                    this.saveFormDataToVuex();
                }
            },
            deep: true
        }
    },
    methods: {
        // 获取类型图片
        getTypeImage(typeId, isActive) {
            // 根据类型ID和激活状态返回对应的图片
            // 支持数字ID（兼容旧数据）和字符串ID（新数据）
            // 数字ID：1-漂浮物, 4-设施损坏, 6-其他
            // 字符串ID：根据 label 或 value 判断

            // 如果是数字，直接判断
            if (typeId === 1 || typeId === '1') {
                return isActive ? type1Act : type1No;
            } else if (typeId === 4 || typeId === '4') {
                return isActive ? type2Act : type2No;
            } else if (typeId === 6 || typeId === '6') {
                return isActive ? type3Act : type3No;
            }

            // 如果是字符串，根据当前 typeList 中的位置判断
            // 因为 typeList 的顺序是固定的：漂浮物、设施损坏、其他
            const currentItem = this.typeList.find(item => item.id === typeId || item.value === typeId);
            if (currentItem) {
                const index = this.typeList.indexOf(currentItem);
                if (index === 0) {
                    // 第一个：漂浮物
                    return isActive ? type1Act : type1No;
                } else if (index === 1) {
                    // 第二个：设施损坏
                    return isActive ? type2Act : type2No;
                } else if (index === 2) {
                    // 第三个：其他
                    return isActive ? type3Act : type3No;
                }
            }

            // 默认返回 type1-no（漂浮物）
            return type1No;
        },

        //获取反馈类型
        getFeedBackType() {
            this.$http.get('/admin-api/system/dict-data/list-by-type?dictType=zd_fklx').then(res => {
                if (res && res.data && Array.isArray(res.data) && res.data.length > 0) {
                    // 根据 label 匹配需要的三个类型：漂浮物、设施损坏、其他
                    const allowedLabels = ['漂浮物', '设施损坏', '其他'];

                    const filteredData = res.data.filter(item => {
                        return allowedLabels.includes(item.label);
                    });

                    if (filteredData.length > 0) {
                        // 按照指定顺序排序：漂浮物、设施损坏、其他
                        const orderMap = {
                            '漂浮物': 0,
                            '设施损坏': 1,
                            '其他': 2
                        };
                        filteredData.sort((a, b) => {
                            const aOrder = orderMap[a.label] ?? 999;
                            const bOrder = orderMap[b.label] ?? 999;
                            return aOrder - bOrder;
                        });

                        // 直接使用接口返回的 value（字符串），不进行数字转换
                        this.typeList = filteredData.map(item => ({
                            id: item.value, // 使用原始 value（字符串），如 "floating_objects"
                            label: item.label,
                            value: item.value // 使用原始 value（字符串），如 "floating_objects"
                        }));
                    }
                }
            }).catch(error => {
                // 接口失败时使用默认数据，不显示错误提示
            });
        },


        //获取 经纬度
        async lngLatInfo() {
            // 优先从 Vuex 读取重新定位的位置信息
            const relocatedLocationFromStore = this.$store.getters.getRelocatedLocation;

            console.log('lngLatInfo - 从 Vuex 读取的位置信息:', relocatedLocationFromStore);

            // 如果 Vuex 中有位置信息，应用它
            if (relocatedLocationFromStore && relocatedLocationFromStore.specificLocation) {
                this.applyRelocatedLocation(relocatedLocationFromStore);

                // 保存位置信息到 saveLnglatInfo（包含河道/水库信息和关联信息）
                this.saveLnglatInfo = {
                    longitude: relocatedLocationFromStore.longitude,
                    latitude: relocatedLocationFromStore.latitude,
                    specificLocation: relocatedLocationFromStore.specificLocation,
                    // 河道/水库信息
                    riverChannelName: relocatedLocationFromStore.riverChannelName || null,
                    riverSectionName: relocatedLocationFromStore.riverSectionName || null,
                    waterReservoirs: relocatedLocationFromStore.waterReservoirs || null,
                    // 关联信息（referenceId 和 referenceType）
                    referenceId: relocatedLocationFromStore.referenceId || null,
                    referenceType: relocatedLocationFromStore.referenceType || null
                };

                console.log('lngLatInfo - 填充后的 saveLnglatInfo:', this.saveLnglatInfo);

                // 保存原始位置信息
                this.originalLocation = {
                    longitude: relocatedLocationFromStore.longitude,
                    latitude: relocatedLocationFromStore.latitude,
                    specificLocation: relocatedLocationFromStore.specificLocation
                };
            }
        },

        // 应用重新定位的位置信息（辅助方法）
        applyRelocatedLocation(relocatedLocationFromStore) {
            // 如果 Vuex 中有位置信息，优先使用
            this.locationText = relocatedLocationFromStore.specificLocation;
            this.relocatedLocation = {
                longitude: relocatedLocationFromStore.longitude,
                latitude: relocatedLocationFromStore.latitude,
                specificLocation: relocatedLocationFromStore.specificLocation
            };
            this.isRelocated = true;
        },

        // 保存表单数据到 Vuex
        saveFormDataToVuex() {
            const formData = {
                currentIndex: this.currentIndex,
                formData: {
                    content: this.formData.content,
                    contactType: this.formData.contactType,
                    name: this.formData.name,
                    phoneNumber: this.formData.phoneNumber
                },
                uploadResults: JSON.parse(JSON.stringify(this.uploadResults)) // 深拷贝
            };
            this.$store.dispatch('updateFeedbackFormData', formData);
        },

        // 从 Vuex 恢复表单数据
        restoreFormData() {
            const savedFormData = this.$store.getters.getFeedbackFormData;
            if (savedFormData) {
                // 设置标志，防止 watch 触发
                this.isRestoringFormData = true;

                // 恢复反馈类型索引
                if (savedFormData.currentIndex !== undefined && savedFormData.currentIndex !== -1) {
                    this.currentIndex = savedFormData.currentIndex;
                }

                // 恢复表单内容
                if (savedFormData.formData) {
                    if (savedFormData.formData.content !== undefined) {
                        this.formData.content = savedFormData.formData.content;
                        this.charCount = savedFormData.formData.content.length;
                    }
                    if (savedFormData.formData.contactType !== undefined) {
                        this.formData.contactType = savedFormData.formData.contactType;
                    }
                    if (savedFormData.formData.name !== undefined) {
                        this.formData.name = savedFormData.formData.name;
                    }
                    if (savedFormData.formData.phoneNumber !== undefined) {
                        this.formData.phoneNumber = savedFormData.formData.phoneNumber;
                    }
                }

                // 恢复上传的文件列表
                if (savedFormData.uploadResults && Array.isArray(savedFormData.uploadResults)) {
                    this.uploadResults = JSON.parse(JSON.stringify(savedFormData.uploadResults)); // 深拷贝
                    this.formData.resource = [...this.uploadResults];
                }

                // 使用 nextTick 确保所有数据更新完成后再重置标志
                this.$nextTick(() => {
                    this.isRestoringFormData = false;
                });
            }
        },

        // 提交表单
        async submitForm() {
            // 检查是否有文件正在上传
            if (this.hasUploading) {
                this.showMessage('您有资源正在上传', 'error');
                return;
            }

            // 验证表单（会给出具体的错误提示）
            if (!this.validateForm()) {
                return;
            }

            // 验证位置信息是否存在
            if (!this.saveLnglatInfo || (!this.saveLnglatInfo.longitude || !this.saveLnglatInfo.latitude)) {
                this.showMessage('位置信息不存在，请先获取问题点位', 'error');
                return;
            }

            // 验证是否选择了河道/水库（referenceId 和 referenceType 是必填的）
            if (!this.saveLnglatInfo.referenceId || !this.saveLnglatInfo.referenceType) {
                this.showMessage('请先选择河道/水库', 'error');
                return;
            }

            // 构建提交数据（按照API要求）
            const selectedType = this.typeList[this.currentIndex];
            const feedbackTypeValue = selectedType?.value || selectedType?.id;

            // 根据是否重新定位，选择使用哪个数据源
            let problemLongitude, problemLatitude, issueSpecificLocation;

            if (this.isRelocated && this.relocatedLocation.longitude && this.relocatedLocation.latitude) {
                // 如果重新定位过，使用重新定位后的数据
                problemLongitude = this.relocatedLocation.longitude;
                problemLatitude = this.relocatedLocation.latitude;
                issueSpecificLocation = this.relocatedLocation.specificLocation;
            } else if (this.originalLocation.longitude && this.originalLocation.latitude) {
                // 如果没有重新定位，使用原始数据
                problemLongitude = this.originalLocation.longitude;
                problemLatitude = this.originalLocation.latitude;
                issueSpecificLocation = this.originalLocation.specificLocation;
            } else if (this.saveLnglatInfo) {
                // 兜底：如果原始数据不存在，使用 saveLnglatInfo 中的数据
                problemLongitude = this.saveLnglatInfo.longitude;
                problemLatitude = this.saveLnglatInfo.latitude;
                issueSpecificLocation = this.saveLnglatInfo.specificLocation;
            }

            // 判断是否实名反馈
            const isRealName = this.isRealNameFeedback();
            const realNameValue = isRealName ? 1 : 0; // 0-匿名，1-实名

            // 构建提交数据（按照新API要求）
            const submitData = {
                // 必填参数
                feedbackType: feedbackTypeValue, // string，字典值
                feedbackContent: this.formData.content.trim(), // string
                realName: realNameValue, // 0 或 1
                referenceId: this.saveLnglatInfo.referenceId, // integer(int64)
                referenceType: this.saveLnglatInfo.referenceType, // string: river/river_section/reservoir

                // 可选参数
                uploadedFiles: this.uploadResults.map(item => item.fileUrl).filter(url => url), // array[string]

                // 位置信息（可选）
                longitude: problemLongitude, // number
                latitude: problemLatitude, // number
                issueSpecificLocation: issueSpecificLocation || undefined // string
            };

            // 手机号码：须与登录账号一致
            const phoneNumber = this.getSubmitPhoneNumber();
            if (!phoneNumber || !this.isPhoneMatchLoginUser(phoneNumber)) {
                this.validatePhoneMatchesLogin(this.formData.phoneNumber);
                return;
            }
            submitData.phoneNumber = phoneNumber;

            // 如果选择了"其他"类型，必须添加姓名
            if (this.isOtherType) {
                // 选择了"其他"类型，必须提交姓名
                if (this.formData.name && this.formData.name.trim()) {
                    submitData.name = this.formData.name.trim();
                }
            } else if (isRealName) {
                // 非"其他"类型，只在实名反馈时添加姓名
                submitData.name = this.formData.name.trim();
            }


            // 调用提交接口
            try {
                const response = await this.$http.post('/app-api/problem/feedback', submitData);

                // 检查响应中的 code 字段（根据 request.js 拦截器，返回的是 response.data）
                const responseCode = response?.code !== undefined ? response.code : (response?.data?.code);

                // 如果 code 为 500，禁止提交并显示提示
                if (responseCode === 500 || responseCode === '500') {
                    this.showMessage('请检查表单!', 'error');
                    return; // 禁止提交，不执行后续操作
                }

                // 显示成功提示
                this.showMessage('提交中...', 'success');
                // 清除 Vuex 中的表单数据和文件列表
                this.$store.dispatch('clearFeedbackFormData');
                this.$store.dispatch('clearFeedbackFileList');
                // 延迟1.5秒后跳转到成功中转页面
                setTimeout(() => {
                    this.$router.push({ name: 'FeedBackSuccess' });
                }, 1000);
            } catch (error) {
                // 检查错误响应中的 code 字段
                const errorCode = error.response?.data?.code;

                // 如果 code 为 500，显示特定提示
                if (errorCode === 500 || errorCode === '500') {
                    this.showMessage('请检查表单!', 'error');
                    return; // 禁止提交，不执行后续操作
                }

                // 其他错误，显示通用错误信息
                const errorMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '提交失败，请重试'
                this.showMessage(errorMsg, 'error');
                // 不执行跳转操作
                return
            }
        },


        // 滚动到顶部
        scrollToTop() {
            window.scrollTo(0, 0)
            document.documentElement.scrollTop = 0
            document.body.scrollTop = 0
        },

        //选择反馈类型
        handleTypeClick(index, item) {
            this.currentIndex = index;
        },
        // 更新字符计数
        updateCharCount() {
            this.charCount = this.formData.content.length;
        },

        // 处理上传成功事件（从组件接收上传结果）
        handleUploadSuccess(uploadResult) {
            // uploadResult 包含: { fileUrl, fileName, fileType }
            this.uploadResults.push(uploadResult);
            // 同步更新表单数据中的 resource 字段
            this.formData.resource = [...this.uploadResults];
        },

        // 处理上传状态变化
        handleUploadStatusChange(hasUploading) {
            this.hasUploading = hasUploading;
        },

        // 处理删除事件（从组件接收删除信息，更新数组）
        handleUploadDelete(deleteInfo) {
            // deleteInfo 包含: { fileUrl, fileName, fileType, fileId }
            // 优先使用 fileUrl 匹配（如果已上传）
            let index = -1;
            if (deleteInfo.fileUrl) {
                index = this.uploadResults.findIndex(item => item.fileUrl === deleteInfo.fileUrl);
            }

            // 如果 fileUrl 匹配失败，使用 fileName + fileType 匹配
            if (index === -1) {
                index = this.uploadResults.findIndex(item =>
                    item.fileName === deleteInfo.fileName &&
                    item.fileType === deleteInfo.fileType
                );
            }

            if (index !== -1) {
                this.uploadResults.splice(index, 1);
                // 同步更新表单数据中的 resource 字段
                this.formData.resource = [...this.uploadResults];
            } else {
                // 如果 uploadResults 中找不到，尝试直接从 formData.resource 中删除
                const resourceIndex = this.formData.resource.findIndex(item => {
                    if (deleteInfo.fileUrl) {
                        return item.fileUrl === deleteInfo.fileUrl;
                    }
                    return item.fileName === deleteInfo.fileName && item.fileType === deleteInfo.fileType;
                });

                if (resourceIndex !== -1) {
                    this.formData.resource.splice(resourceIndex, 1);
                    this.uploadResults = [...this.formData.resource];
                }
            }
        },

        // 表单验证
        validateForm() {
            // 验证反馈类型（required）
            if (this.currentIndex === -1) {
                this.showMessage('反馈类型不能为空', 'error');
                return false;
            }

            // 验证反馈内容（required）
            if (!this.formData.content || !this.formData.content.trim()) {
                this.showMessage('反馈内容不能为空', 'error');
                return false;
            }

            // 验证上传资源（required）
            if (!this.formData.resource || this.formData.resource.length === 0) {
                this.showMessage('图片/视频不能为空，请至少上传一个文件', 'error');
                return false;
            }

            // 如果选择了"其他"类型，联系方式为必填
            if (this.isOtherType) {
                if (!this.formData.name || !this.formData.name.trim()) {
                    this.showMessage('姓名不能为空', 'error');
                    return false;
                }
                // 验证姓名格式
                if (!this.isValidName(this.formData.name)) {
                    if (this.nameError) {
                        this.showMessage(this.nameError, 'error');
                    } else {
                        this.showMessage('请输入有效的中文姓名（至少2个字符，不能包含英文和数字）', 'error');
                    }
                    return false;
                }
                if (!this.formData.phoneNumber || !this.formData.phoneNumber.trim()) {
                    this.showMessage('手机号码不能为空', 'error');
                    return false;
                }
                // 验证手机号码格式
                if (!this.isValidPhoneNumber(this.formData.phoneNumber)) {
                    this.showMessage('请输入正确的手机号码', 'error');
                    return false;
                }
                // 验证手机号码须与登录账号一致
                if (!this.validatePhoneMatchesLogin(this.formData.phoneNumber)) {
                    return false;
                }
            } else {
                // 非「其他」类型：若填写了手机号，也须与登录账号一致
                const formPhone = this.formData.phoneNumber.trim();
                if (formPhone && !this.validatePhoneMatchesLogin(formPhone)) {
                    return false;
                }
            }

            // 提交前统一校验：最终使用的手机号须与登录账号一致
            const submitPhone = this.getSubmitPhoneNumber();
            if (!submitPhone || !this.validatePhoneMatchesLogin(submitPhone)) {
                return false;
            }

            return true;
        },


        // 显示消息提示
        showMessage(message, type = 'info') {
            const messageEl = document.createElement('div');
            messageEl.className = `simple-message simple-message-${type}`;
            messageEl.textContent = message;
            document.body.appendChild(messageEl);

            setTimeout(() => {
                messageEl.classList.add('show');
            }, 10);

            setTimeout(() => {
                messageEl.classList.remove('show');
                setTimeout(() => {
                    if (document.body.contains(messageEl)) {
                        document.body.removeChild(messageEl);
                    }
                }, 300);
            }, 2000);
        },

        // 前往地图页面（使用路由跳转）
        goToMap() {
            // 检查是否有资源正在上传
            if (this.hasUploading) {
                this.showMessage('您有资源正在上传,请稍后', 'error');
                return;
            }

            // 保存 fileList 到 Vuex（只保存可序列化的信息）
            this.saveFileListToVuex();

            // 准备传递给地图页面的默认数据
            const query = {};

            // 优先使用 saveLnglatInfo 中的位置信息
            if (this.saveLnglatInfo) {
                if (this.saveLnglatInfo.latitude && this.saveLnglatInfo.longitude) {
                    query.lat = this.saveLnglatInfo.latitude;
                    query.lng = this.saveLnglatInfo.longitude;
                }
                // 传递位置信息（specificLocation）
                if (this.saveLnglatInfo.specificLocation) {
                    query.address = this.saveLnglatInfo.specificLocation;
                }
            } else {
                // 如果 saveLnglatInfo 不存在，尝试从 Vuex 读取
                const relocatedLocationFromStore = this.$store.getters.getRelocatedLocation;
                if (relocatedLocationFromStore) {
                    if (relocatedLocationFromStore.latitude && relocatedLocationFromStore.longitude) {
                        query.lat = relocatedLocationFromStore.latitude;
                        query.lng = relocatedLocationFromStore.longitude;
                    }
                    if (relocatedLocationFromStore.specificLocation) {
                        query.address = relocatedLocationFromStore.specificLocation;
                    }
                }
            }

            console.log('goToMap - 传递的 query 参数:', query);

            // 使用路由跳转到地图页面
            this.$router.push({
                name: 'FeedBackMap',
                query: query
            });
        },

        // 前往我的反馈历史记录
        goToMyHistory() {
            this.$router.push({ name: 'FeedBackList' });
        },

        // 保存 fileList 到 Vuex（只保存可序列化的信息）
        saveFileListToVuex() {
            // 将 fileList 转换为可序列化的格式
            const serializableFileList = this.fileList.map(file => {
                // 只保存可序列化的字段
                return {
                    id: file.id,
                    name: file.name,
                    type: file.type,
                    url: file.url, // base64 或 blob URL（如果是 base64 可以保存）
                    serverUrl: file.serverUrl, // 服务器 URL（最重要）
                    poster: file.poster, // 视频封面（base64）
                    duration: file.duration,
                    uploading: file.uploading || false,
                    checking: file.checking || false,
                    uploadError: file.uploadError || false,
                    posterGenerated: file.posterGenerated || false,
                    needPoster: file.needPoster || false,
                    // 不保存 raw File 对象和 objectUrl（无法序列化）
                    // 如果 url 是 base64，保留它；如果是 blob URL，需要通过 serverUrl 恢复
                    isBase64: file.url && file.url.startsWith('data:')
                };
            });

            this.$store.dispatch('updateFeedbackFileList', serializableFileList);
        },

        // 从 Vuex 恢复 fileList
        restoreFileListFromVuex() {
            const savedFileList = this.$store.getters.getFeedbackFileList;
            if (savedFileList && Array.isArray(savedFileList) && savedFileList.length > 0) {
                // 如果当前 fileList 为空，才恢复
                if (this.fileList.length === 0) {
                    // 将保存的文件信息恢复为 fileList 格式
                    const restoredFileList = savedFileList.map(fileInfo => {
                        const fileItem = {
                            id: fileInfo.id,
                            name: fileInfo.name,
                            type: fileInfo.type,
                            url: fileInfo.serverUrl || fileInfo.url, // 优先使用 serverUrl
                            serverUrl: fileInfo.serverUrl,
                            poster: fileInfo.poster,
                            duration: fileInfo.duration || 0,
                            uploading: false, // 恢复时不应该是上传中状态
                            checking: false, // 恢复时不应该是检查中状态
                            uploadError: fileInfo.uploadError || false,
                            posterGenerated: fileInfo.posterGenerated || false,
                            needPoster: fileInfo.needPoster || false,
                            // 如果没有 raw File 对象，设置为 null（已上传的文件不需要）
                            raw: null
                        };

                        // 如果有 base64 URL（图片），使用它作为预览
                        if (fileInfo.isBase64 && fileInfo.url) {
                            fileItem.url = fileInfo.url;
                        }

                        return fileItem;
                    });

                    // 恢复 fileList
                    this.fileList = restoredFileList;

                    // 同步更新 uploadResults（从已上传的文件中提取）
                    this.uploadResults = restoredFileList
                        .filter(file => file.serverUrl) // 只包含已上传的文件
                        .map(file => ({
                            fileUrl: file.serverUrl,
                            fileName: file.name,
                            fileType: file.type
                        }));

                    // 同步更新 formData.resource
                    this.formData.resource = [...this.uploadResults];
                }
            }
        },


        // 从 localStorage 加载用户手机号码
        loadUserMobile() {
            // 不管是否必填，都自动填充手机号码（如果为空）
            const savedMobile = localStorage.getItem('userMobile');
            if (savedMobile && !this.formData.phoneNumber) {
                this.formData.phoneNumber = savedMobile;
                // 加载后验证格式
                this.validatePhoneNumber();
            }
        },

        // 处理姓名输入
        handleNameInput(event) {
            let value = event.target.value;
            // 不立即过滤，保留输入以便显示错误提示
            // 只移除明显不是姓名字符的特殊符号（保留中文、英文、数字，用于验证）
            // 移除一些特殊符号，但保留常见的中文标点
            value = value.replace(/[^\u4e00-\u9fa5a-zA-Z0-9·•]/g, '');

            this.formData.name = value;
            // 实时验证格式（会显示错误提示）
            this.validateName();
        },

        // 验证姓名格式
        validateName() {
            const name = this.formData.name.trim();
            if (!name) {
                this.nameError = '';
                return false;
            }

            // 检查是否包含英文
            if (/[a-zA-Z]/.test(name)) {
                this.nameError = '姓名不能包含英文';
                return false;
            }

            // 检查是否包含数字
            if (/[0-9]/.test(name)) {
                this.nameError = '姓名不能包含数字';
                return false;
            }

            // 检查长度（至少2个字符）
            if (name.length < 2) {
                this.nameError = '姓名至少需要2个字符';
                return false;
            }

            // 验证是否为有效的中文姓名（至少包含一个中文字符）
            if (!/[\u4e00-\u9fa5]/.test(name)) {
                this.nameError = '请输入有效的中文姓名';
                return false;
            }

            this.nameError = '';
            return true;
        },

        // 检查姓名格式是否有效（用于表单验证）
        isValidName(name) {
            if (!name || !name.trim()) {
                return false;
            }
            const trimmedName = name.trim();
            // 不能包含英文
            if (/[a-zA-Z]/.test(trimmedName)) {
                return false;
            }
            // 不能包含数字
            if (/[0-9]/.test(trimmedName)) {
                return false;
            }
            // 至少2个字符
            if (trimmedName.length < 2) {
                return false;
            }
            // 必须包含中文字符
            if (!/[\u4e00-\u9fa5]/.test(trimmedName)) {
                return false;
            }
            return true;
        },

        // 获取登录用户手机号
        getLoginMobile() {
            return (localStorage.getItem('userMobile') || '').trim();
        },

        // 获取提交时使用的手机号（表单优先，否则取登录手机号）
        getSubmitPhoneNumber() {
            const formPhone = this.formData.phoneNumber?.trim() || '';
            if (formPhone) {
                return formPhone;
            }
            return this.getLoginMobile();
        },

        // 判断手机号是否与登录账号一致
        isPhoneMatchLoginUser(phone) {
            const loginMobile = this.getLoginMobile();
            if (!loginMobile) {
                return false;
            }
            return (phone || '').trim() === loginMobile;
        },

        // 校验手机号与登录账号一致，失败时设置错误提示
        validatePhoneMatchesLogin(phone) {
            const loginMobile = this.getLoginMobile();
            if (!loginMobile) {
                this.phoneNumberError = '未获取到登录手机号，请重新登录';
                this.showMessage(this.phoneNumberError, 'error');
                return false;
            }

            const formPhone = (phone || '').trim();
            const effectivePhone = formPhone || loginMobile;

            if (effectivePhone !== loginMobile) {
                this.phoneNumberError = '手机号码须与登录账号一致';
                this.showMessage(this.phoneNumberError, 'error');
                return false;
            }

            this.phoneNumberError = '';
            return true;
        },

        // 处理手机号码输入
        handlePhoneInput(event) {
            // 只允许输入数字
            let value = event.target.value.replace(/\D/g, '');
            // 限制最大长度为11位
            if (value.length > 11) {
                value = value.slice(0, 11);
            }
            this.formData.phoneNumber = value;
            // 实时验证格式
            this.validatePhoneNumber();
        },

        // 验证手机号码格式
        validatePhoneNumber() {
            const phone = this.formData.phoneNumber.trim();
            if (!phone) {
                this.phoneNumberError = '';
                return false;
            }

            // 中国手机号正则：1开头，第二位是3-9，总共11位
            const phoneRegex = /^1[3-9]\d{9}$/;
            if (!phoneRegex.test(phone)) {
                if (phone.length !== 11) {
                    this.phoneNumberError = '手机号码必须为11位数字';
                } else {
                    this.phoneNumberError = '请输入正确的手机号码格式';
                }
                return false;
            }

            if (!this.isPhoneMatchLoginUser(phone)) {
                this.phoneNumberError = '手机号码须与登录账号一致';
                return false;
            }

            this.phoneNumberError = '';
            return true;
        },

        // 检查手机号码格式是否有效（用于表单验证）
        isValidPhoneNumber(phone) {
            if (!phone || !phone.trim()) {
                return false;
            }
            const phoneRegex = /^1[3-9]\d{9}$/;
            return phoneRegex.test(phone.trim());
        },

        // 判断是否实名反馈
        isRealNameFeedback() {
            const name = this.formData.name?.trim() || '';

            // 只要姓名通过格式校验，就算实名反馈
            return this.isValidName(name);
        },

        // 关闭页面
        handleClose() {
            // 清理所有 ObjectURL
            this.fileList.forEach(file => {
                if (file.objectUrl) {
                    URL.revokeObjectURL(file.objectUrl);
                } else if (file.url && file.url.startsWith('blob:')) {
                    URL.revokeObjectURL(file.url);
                }
            });
            this.$router.go(-1);
        },

        // 自动获取位置（静默模式，失败不提示）
        async autoGetLocation() {
            // 如果Vuex中已有位置信息，不需要自动获取
            const relocatedLocationFromStore = this.$store.getters.getRelocatedLocation;
            if (relocatedLocationFromStore && relocatedLocationFromStore.specificLocation) {
                return;
            }

            // 延迟一下，确保页面完全加载
            await new Promise(resolve => setTimeout(resolve, 1000));

            // 静默获取位置（第一次失败不提示，自动重试）
            await this.handleGetCurrentLocation(true);
        },

        // 获取当前位置（使用高德API）
        async handleGetCurrentLocation(silentMode = false) {
            try {
                // 更新位置文本为"正在获取位置..."
                this.locationText = '正在获取位置...';

                // 非静默模式才显示加载提示
                if (!silentMode) {
                    this.showMessage('正在获取位置...', 'info');
                }

                // 使用高德API获取位置
                const locationData = await getCurrentLocationByAmap();

                // 更新位置信息
                this.locationText = locationData.address;
                this.saveLnglatInfo = {
                    longitude: locationData.longitude,
                    latitude: locationData.latitude,
                    specificLocation: locationData.address,
                    riverChannelName: null,
                    riverSectionName: null,
                    waterReservoirs: null,
                    referenceId: null,
                    referenceType: null
                };
                this.originalLocation = {
                    longitude: locationData.longitude,
                    latitude: locationData.latitude,
                    specificLocation: locationData.address
                };

                // 非静默模式才显示成功提示
                if (!silentMode) {
                    this.showMessage('位置获取成功', 'success');
                }

            } catch (error) {
                // 静默模式下，第一次失败自动重试一次
                if (silentMode) {
                    try {
                        // 等待一下再重试
                        await new Promise(resolve => setTimeout(resolve, 1000));
                        // 重试一次
                        await this.handleGetCurrentLocation(false); // 第二次失败会显示错误
                        return;
                    } catch (retryError) {
                        // 重试也失败
                        this.locationText = '定位失败,请稍后再试';
                        return;
                    }
                }

                // 非静默模式显示错误
                this.locationText = '定位失败,请稍后再试';
                this.showMessage('定位失败,请稍后再试', 'error');
            }
        },


    }
}
</script>

<style lang="scss" scoped>
.user-feedBack-container {
    position: relative;
    width: 100%;
    min-height: 100vh;
    overflow: hidden;


    // 页面内容层
    .content-layer {
        width: 100%;
        height: 100vh;
        height: 100dvh; // 动态视口高度
        overflow-y: auto; // 允许滚动
        overflow-x: hidden;
        -webkit-overflow-scrolling: touch; // iOS 平滑滚动

        .paddingContent {
            padding: 24px;
            // 添加底部留白，确保内容不被固定按钮遮挡（按钮高度96px + 上下padding 48px + 安全区域）
            padding-bottom: calc(144px + env(safe-area-inset-bottom, 0px));

            .feedBackType,
            .feedBackContent,
            .feedBackResource,
            .feedBackPosition,
            .feedBackForm {
                padding: 0px 24px;
                width: 702px;
                opacity: 1;
                background: #ffffff;
                margin-bottom: 16px;
            }

            .feedBackType {
                position: relative;
                height: 264px;

                .feedBackType-content {
                    display: flex;
                    align-items: flex-start;
                    gap: 24px;
                    width: 100%;
                }

                .typeList {
                    display: flex;
                    flex-wrap: nowrap;
                    gap: 24px;
                    flex: 1;
                    justify-content: flex-start;

                    .typeItem {
                        width: 202px;
                        height: 136px;
                        cursor: pointer;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        border: none;
                        background: transparent;
                        padding: 0;
                        position: relative;

                        .image-wrapper {
                            width: 100%;
                            height: 100%;
                            position: relative;
                            display: flex;
                            align-items: center;
                            justify-content: center;
                        }

                        .type-image {
                            width: 202px;
                            height: 136px;
                            object-fit: contain;
                            display: block;
                            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
                            opacity: 1;
                            transform: scale(1);
                            filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));
                        }

                        .type-image.image-active {
                            transform: scale(1.05);
                            filter: drop-shadow(0 4px 12px rgba(52, 157, 255, 0.3));
                        }

                        &:active {
                            .type-image {
                                transform: scale(0.95);
                            }

                            .type-image.image-active {
                                transform: scale(1.0);
                            }
                        }

                        // 悬停效果（可选）
                        &:hover {
                            .type-image {
                                transform: scale(1.02);
                            }

                            .type-image.image-active {
                                transform: scale(1.06);
                            }
                        }
                    }
                }

                .activeTypeItem {
                    .type-image {
                        animation: imagePulse 0.4s ease-out;
                    }
                }

                @keyframes imagePulse {
                    0% {
                        transform: scale(1);
                    }

                    50% {
                        transform: scale(1.08);
                    }

                    100% {
                        transform: scale(1.05);
                    }
                }
            }

            .feedBackContent {
                height: 272px;
                display: flex;
                flex-direction: column;
                box-sizing: border-box;
                overflow: hidden;

                .content-textarea-wrapper {
                    position: relative;
                    flex: 1;
                    width: 100%;
                    max-height: 148px; // 272px - 80px(标题) - 24px(标题margin-bottom)
                    min-height: 0;
                    background: #f8f8f8;
                    border-radius: 8px;
                    padding: 16px;
                    border: 1px solid #e8e8e8;
                    box-sizing: border-box;
                    display: flex;
                    flex-direction: column;
                    overflow: hidden;
                }

                .content-textarea {
                    flex: 1;
                    width: 100%;
                    min-height: 0;
                    background: transparent;
                    border: none;
                    outline: none;
                    font-size: 28px;
                    color: #666666;
                    line-height: 1.6;
                    resize: none;
                    font-family: inherit;
                    box-sizing: border-box;
                    overflow-y: auto;
                    padding: 0;
                    margin: 0;

                    &::placeholder {
                        color: #999;
                    }
                }

                .char-count {
                    position: absolute;
                    bottom: 16px;
                    right: 16px;
                    font-size: 24px;
                    color: #999;
                    line-height: 1;
                    pointer-events: none;
                }
            }

            .feedBackForm {
                min-height: 272px;
                height: auto;
                padding-bottom: 24px;
                // 让容器自适应内容高度，当有错误提示时自动撑开
            }

            .feedBackResource {
                height: 208px;
                // padding-top: 24px;
                padding-bottom: 24px;

                .upload-area {
                    width: 100%;
                    padding-bottom: 28px;
                    margin-bottom: 16px;
                }
            }

            .feedBackPosition {
                height: 220px;
                padding-bottom: 24px;

                .upload-position-wrapper {
                    background: #f8f8f8;
                    padding: 0px 5px;
                    border-radius: 8px;
                }

                .upload-position {
                    width: 654px;
                    height: 100px;
                    padding: 0px 20px;
                    box-sizing: border-box;
                    opacity: 1;
                    background-image: url('@/assets/img/feebackTypeBck.png');
                    background-size: cover;
                    background-position: center;
                    background-repeat: no-repeat;
                    border-radius: 8px;
                    position: relative;
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    gap: 12px;

                    .position-icon {
                        width: 36px;
                        height: 36px;
                        flex-shrink: 0;
                        object-fit: contain;
                        margin-right: 10px;
                        ;
                    }

                    .location-content {
                        flex: 1;
                        min-width: 0;
                        display: flex;
                        flex-direction: column;
                        justify-content: center;

                    }

                    .location-main-text {
                        font-size: 28px;
                        color: #3D3D3D;
                        font-weight: 500;
                        line-height: 1.4;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                    }

                    .location-sub-text {
                        height: auto;
                        min-height: 32px;
                        opacity: 1;
                        background: #daedff;
                        color: #349dff;
                        text-align: left;
                        font-size: 28px;
                        font-weight: 400;
                        line-height: 1.4;
                        padding: 5px;
                        border-radius: 4px;
                        display: inline-block;
                        width: fit-content;
                        max-width: 100%;
                        font-weight: 500;
                        overflow: hidden;
                        text-overflow: ellipsis;
                        white-space: nowrap;
                        box-sizing: border-box;
                        margin-top: 4px;
                    }

                    .location-actions {
                        display: flex;
                        align-items: center;
                        gap: 16px;
                        flex-shrink: 0;
                    }

                    .clickLabel {
                        flex-shrink: 0;
                        color: #349DFF;
                        font-size: 24px;
                        font-weight: 500;
                        cursor: pointer;
                        padding-left: 12px;
                        white-space: nowrap;
                        transition: color 0.3s ease;
                        // 下划线
                        text-decoration: underline;
                        text-underline-offset: 8px;

                        &:active {
                            color: #349DFF;
                        }
                    }

                    .get-location-btn {
                        padding-left: 0;
                        width: 40px;
                        height: 40px;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        background-image: url('@/assets/img/position2.png');
                        background-size: contain;
                        background-repeat: no-repeat;
                        background-position: center;
                        cursor: pointer;

                        &::before {
                            content: '';
                            display: none;
                        }

                        &:active {
                            opacity: 0.7;
                        }
                    }
                }
            }

            .anonymousRealName {
                display: flex;
                align-items: center;
                justify-content: flex-start;
                margin-top: 16px;

                &-item {
                    display: flex;
                    align-items: center;
                    justify-content: flex-start;
                    width: 50%;
                    cursor: pointer;
                    position: relative;

                    .radio-input {
                        position: absolute;
                        opacity: 0;
                        width: 0;
                        height: 0;
                        margin: 0;
                    }

                    .radio-custom {
                        position: relative;
                        width: 32px;
                        height: 32px;
                        border-radius: 50%;
                        border: 2px solid #d0d0d0;
                        background-color: #fff;
                        margin-right: 12px;
                        transition: all 0.3s ease;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        flex-shrink: 0;

                        &::after {
                            content: '';
                            width: 0;
                            height: 0;
                            border-radius: 50%;
                            background-color: transparent;
                            transition: all 0.3s ease;
                        }
                    }

                    .radio-label {
                        font-size: 28px;
                        color: #3D3D3D;
                        font-weight: 400;
                        user-select: none;
                    }

                    // 选中状态
                    &.active {
                        .radio-custom {
                            border-color: #007aff;
                            background-color: #007aff;

                            // 对勾图标
                            &::before {
                                content: '';
                                position: absolute;
                                width: 6px;
                                height: 10px;
                                border: 2px solid #fff;
                                border-top: none;
                                border-left: none;
                                transform: rotate(45deg);
                                margin-top: -2px;
                            }
                        }
                    }

                    &:active {
                        opacity: 0.7;
                    }
                }
            }

            // 联系方式输入框
            .contact-inputs {
                display: flex;
                flex-direction: column;
                gap: 16px;
                margin-top: 24px;
            }

            .name-input-wrapper {
                position: relative;
                width: 100%;
                margin-bottom: 0;

                // 当有错误提示时，增加底部间距，避免被下一个输入框遮挡
                &.has-error {
                    margin-bottom: 40px;
                }
            }

            .phone-input-wrapper {
                position: relative;
                width: 100%;
                margin-bottom: 0;

                // 当有错误提示时，增加底部间距，避免被下一个元素遮挡
                &.has-error {
                    margin-bottom: 40px;
                }
            }

            .form-input {
                width: 100%;
                height: 64px;
                padding: 0 16px;
                background-color: #f8f8f8;
                border: 1px solid #e0e0e0;
                border-radius: 8px;
                font-size: 28px;
                color: #3D3D3D;
                outline: none;
                transition: all 0.3s ease;
                box-sizing: border-box;

                &::placeholder {
                    color: #999;
                }

                &:focus {
                    border-color: #007aff;
                    background-color: #fff;
                }

                &.input-error {
                    border-color: #f56c6c;
                    background-color: #fffef0;
                }
            }

            .error-tip {
                position: absolute;
                top: 100%;
                left: 0;
                margin-top: 8px;
                font-size: 24px;
                color: #f56c6c;
                line-height: 1.4;
                z-index: 10;
                background: #ffffff;
                padding: 4px 0;
                white-space: nowrap;
            }

            // 提交按钮样式已移到外层，作为固定定位
        }

    }

    // 提交按钮 - 固定定位，悬浮在底部
    .submit-btn-wrapper {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        width: 100%;
        padding: 24px;
        padding-bottom: calc(24px + env(safe-area-inset-bottom, 0px)); // 适配安全区域
        box-sizing: border-box;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #ffffff;
        z-index: 1000;
        box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);

        .submit-btn {
            width: 100%;
            max-width: 702px;
            height: 96px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #eaeaea;
            color: #999;
            font-size: 32px;
            font-weight: 500;
            border-radius: 8px;
            cursor: not-allowed;
            transition: all 0.3s ease;
            user-select: none;
            border: none;

            &.submit-btn-active {
                background: #349DFF;
                color: #fff;
                cursor: pointer;

                &:active {
                    opacity: 0.8;
                    transform: scale(0.98);
                }
            }
        }
    }

    // 历史记录悬浮按钮
    .floating-history-btn {
        position: fixed;
        right: 24px;
        bottom: calc(180px + env(safe-area-inset-bottom, 0px)); // 避开底部提交按钮
        width: 100px;
        height: 100px;
        background: url('@/assets/img/feedbackList_icon.png') no-repeat center center;
        background-size: 100% 100%;
        z-index: 1001;
        cursor: pointer;
        transition: transform 0.2s ease, opacity 0.2s ease;

      

        &:active {
            transform: scale(0.95);
            opacity: 0.9;
        }
    }
}
</style>

<style lang="scss">
// 全局消息提示样式
.simple-message {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%) scale(0.8);
    padding: 20px 32px;
    background-color: rgba(0, 0, 0, 0.8);
    color: #fff;
    border-radius: 8px;
    font-size: 28px;
    z-index: 9999;
    opacity: 0;
    transition: all 0.3s ease;
    pointer-events: none;
    max-width: 80%;
    text-align: center;

    &.show {
        opacity: 1;
        transform: translate(-50%, -50%) scale(1);
    }

    &.simple-message-success {
        background-color: rgba(76, 175, 80, 0.9);
    }

    &.simple-message-error {
        background-color: rgba(244, 67, 54, 0.9);
    }

    &.simple-message-info {
        background-color: rgba(33, 150, 243, 0.9);
    }
}
</style>