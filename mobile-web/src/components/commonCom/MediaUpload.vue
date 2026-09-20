<!-- 媒体上传组件（图片/视频） -->
<template>
    <div class="media-upload">
        <!-- 从相册选择 -->
        <input 
            ref="fileInput"
            type="file" 
            accept="image/*,video/mp4,video/quicktime,video/x-msvideo,video/webm,video/3gpp,video/x-matroska,video/m4v"
            multiple
            @change="handleFileSelect"
            class="file-input"
        />
        
        <!-- 文件列表预览（包含上传按钮） -->
        <div class="file-list">
            <!-- 已上传的文件 -->
            <div 
                class="file-item" 
                v-for="(item, index) in internalFileList" 
                :key="item.id"
            >
                <!-- 图片预览 -->
                <div class="preview-image" v-if="item.type === 'image'" @click.stop="previewImage(index)">
                    <img :src="item.url" :alt="item.name" />
                    <!-- 上传状态遮罩 -->
                    <div class="upload-overlay" v-if="item.uploading || item.uploadError">
                        <div class="upload-status" v-if="item.uploading">
                            <div class="upload-spinner"></div>
                            <div class="upload-text">上传中...</div>
                        </div>
                        <div class="upload-error" v-if="item.uploadError">
                            <div class="error-icon">!</div>
                            <div class="error-text">上传失败</div>
                        </div>
                    </div>
                    <div class="delete-btn" @click.stop="confirmDelete(index)">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                            <path d="M18 6L6 18M6 6L18 18" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                    </div>
                </div>
                
                <!-- 视频预览 -->
                <div class="preview-video" v-else-if="item.type === 'video'" @click.stop="previewVideo(index)">
                    <!-- 优先使用本地URL（objectUrl或url），这样封面生成时使用的video元素和显示的video元素是同一个 -->
                    <!-- 如果本地URL不可用，再使用服务器URL -->
                    <video 
                        :src="item.objectUrl || item.url || item.serverUrl" 
                        :poster="item.poster" 
                        preload="metadata"
                        @loadedmetadata="handleVideoMetadata($event, index)"
                    ></video>
                    <!-- 如果没有封面，显示背景图片 -->
                    <div class="video-placeholder-bg" v-if="!item.poster && !item.checking && !item.uploading && !item.uploadError">
                        <img src="@/assets/img/videoUploadBck.png" alt="视频" />
                    </div>
                    <div class="video-overlay" :class="{ 'has-poster': item.poster }">
                        <div class="play-icon" v-if="!item.uploading && !item.uploadError && !item.checking && item.poster">
                            <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                                <path d="M8 5v14l11-7z" fill="#fff"/>
                            </svg>
                        </div>
                        <!-- 检查中提示 -->
                        <div class="upload-status" v-if="item.checking">
                            <div class="upload-spinner"></div>
                            <div class="upload-text">检查中...</div>
                        </div>
                        <!-- 上传中提示 -->
                        <div class="upload-status" v-if="item.uploading">
                            <div class="upload-spinner"></div>
                            <div class="upload-text">上传中...</div>
                        </div>
                        <!-- 上传失败提示 -->
                        <div class="upload-error" v-if="item.uploadError">
                            <div class="error-icon">!</div>
                            <div class="error-text">上传失败</div>
                        </div>
                        <!-- 始终显示时长，如果没有则显示默认值 -->
                        <!-- <div class="video-duration">
                            {{ item.duration ? formatDuration(item.duration) : '--:--' }}
                        </div> -->
                    </div>
                    <div class="delete-btn" @click.stop="confirmDelete(index)">
                        <svg width="16" height="16" viewBox="0 0 24 24" fill="none">
                            <path d="M18 6L6 18M6 6L18 18" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
                        </svg>
                    </div>
                </div>
            </div>
            
            <!-- 上传按钮（放在文件列表的网格中） -->
            <div class="file-item upload-btn-item" v-if="internalFileList.length < maxFiles" @click="triggerFileInput">
                <div class="upload-btn">
                    <div class="upload-icon-wrapper">
                        <img src="@/assets/img/uploadIcon.png" alt="上传" class="upload-icon" />
                    </div>
                </div>
            </div>
        </div>
        
        <!-- 删除确认弹框 -->
        <div class="delete-confirm-modal" v-if="deleteConfirmIndex !== null" @click="cancelDelete">
            <div class="delete-confirm-content" @click.stop>
                <div class="delete-confirm-title">确认删除</div>
                <div class="delete-confirm-message">确定要删除这个文件吗？</div>
                <div class="delete-confirm-btns">
                    <button class="delete-confirm-btn cancel-btn" @click="cancelDelete">取消</button>
                    <button class="delete-confirm-btn confirm-btn" @click="confirmDeleteFile">确认删除</button>
                </div>
            </div>
        </div>
        
        <!-- 预览模态框 -->
        <div class="preview-modal" v-if="previewItem" @click="closePreview">
            <div class="preview-content" @click.stop>
                <!-- 关闭按钮 -->
                <div class="preview-close" @click="closePreview">
                    <svg width="32" height="32" viewBox="0 0 24 24" fill="none">
                        <path d="M18 6L6 18M6 6L18 18" stroke="#fff" stroke-width="2" stroke-linecap="round"/>
                    </svg>
                </div>
                
                <!-- 图片预览 -->
                <div class="preview-image-container" v-if="previewItem.type === 'image'">
                    <img :src="previewItem.url" :alt="previewItem.name" />
                </div>
                
                <!-- 视频预览 -->
                <div class="preview-video-container" v-else-if="previewItem.type === 'video'">
                    <video 
                        ref="previewVideoPlayer"
                        :src="previewItem.url" 
                        :poster="previewItem.poster"
                        controls
                        playsinline
                        webkit-playsinline
                        x5-playsinline
                        preload="metadata"
                        @click.stop
                        @ended="closePreview"
                        @error="handlePreviewVideoError"
                    ></video>
                    <!-- 加载提示 -->
                    <div class="video-loading-tip" v-if="previewVideoLoading">
                        <div class="loading-spinner"></div>
                        <div class="loading-text">加载中...</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import http from '@/utils/request.js';

export default {
    name: 'MediaUpload',
    props: {
        // 文件列表（v-model）
        modelValue: {
            type: Array,
            default: () => []
        },
        // 最大文件数
        maxFiles: {
            type: Number,
            default: 6
        }
    },
    emits: ['update:modelValue', 'change', 'upload-success', 'upload-delete', 'upload-status-change'],
    data() {
        return {
            // 内部文件列表
            internalFileList: [],
            // 预览项
            previewItem: null,
            // 删除确认索引
            deleteConfirmIndex: null,
            // 文件ID计数器
            fileIdCounter: 0,
            // 支持的视频格式
            videoMimeTypes: [
                'video/mp4',
                'video/quicktime',  // mov
                'video/x-msvideo',   // avi
                'video/webm',
                'video/3gpp',        // 3gp
                'video/x-matroska',  // mkv
                'video/m4v',
                'video/avi',
                'video/x-flv',
                'video/x-ms-wmv'
            ],
            // 支持的视频文件扩展名
            videoExtensions: [
                '.mp4', '.mov', '.avi', '.webm', '.3gp', 
                '.mkv', '.m4v', '.flv', '.wmv', '.mpg', 
                '.mpeg', '.m2v', '.f4v', '.rm', '.rmvb'
            ],
            // 是否为微信浏览器
            isWeChatBrowser: false
        }
    },
    watch: {
        modelValue: {
            immediate: true,
            handler(newVal) {
                // 只有当外部传入的值与内部值不同时才更新，避免循环更新
                // 通过比较长度和 ID 列表来判断是否相同
                if (newVal.length !== this.internalFileList.length) {
                    this.internalFileList = [...newVal];
                    return;
                }
                
                const newIds = newVal.map(item => item.id).sort().join(',');
                const currentIds = this.internalFileList.map(item => item.id).sort().join(',');
                if (newIds !== currentIds) {
                    this.internalFileList = [...newVal];
                }
            }
        },
        internalFileList: {
            deep: true,
            handler(newVal) {
                this.$emit('update:modelValue', [...newVal]);
                this.$emit('change', [...newVal]);
            }
        }
    },
    mounted() {
        // 检测是否为微信浏览器
        this.detectWeChatBrowser();
        // 监听键盘事件
        window.addEventListener('keydown', this.handleKeyDown);
    },
    beforeUnmount() {
        // 关闭预览
        this.closePreview();
        // 清理所有 ObjectURL，避免内存泄漏
        this.internalFileList.forEach(file => {
            if (file.url && file.url.startsWith('blob:')) {
                URL.revokeObjectURL(file.url);
            }
            if (file.objectUrl) {
                URL.revokeObjectURL(file.objectUrl);
            }
        });
        // 恢复背景滚动
        document.body.style.overflow = '';
        // 移除键盘事件监听
        window.removeEventListener('keydown', this.handleKeyDown);
    },
    methods: {
        /**
         * 检测是否为微信浏览器
         */
        detectWeChatBrowser() {
            const ua = navigator.userAgent.toLowerCase();
            this.isWeChatBrowser = /micromessenger/.test(ua);
            console.log('浏览器检测:', {
                userAgent: ua,
                isWeChatBrowser: this.isWeChatBrowser
            });
        },
        
        /**
         * 文件上传请求
         * @param {string} url - 请求地址
         * @param {FormData} formData - FormData 对象
         * @param {object} config - 额外配置（如 headers, params, timeout, signal 等）
         * @returns {Promise} 请求结果
         */
        upload(url, formData, config = {}) {
            // 使用 axios 实例直接调用，支持完整的配置选项（包括 timeout 和 signal）
            return http.axios.post(url, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data',
                    ...config.headers
                },
                params: config.params || {},
                timeout: config.timeout || undefined, // 传递超时设置
                signal: config.signal || undefined, // 传递取消信号
                ...config
            });
        },
        
        // 检查是否为视频文件（通过 MIME 类型和扩展名）
        isVideoFile(file) {
            // 首先检查 MIME 类型
            if (file.type) {
                const mimeType = file.type.toLowerCase();
                if (mimeType.startsWith('video/')) {
                    console.log('通过MIME类型识别为视频:', file.name, mimeType);
                    return true;
                }
                // 检查是否在支持的 MIME 类型列表中
                if (this.videoMimeTypes.includes(mimeType)) {
                    console.log('通过MIME类型列表识别为视频:', file.name, mimeType);
                    return true;
                }
            }
            
            // 如果 MIME 类型不标准或为空，通过文件扩展名判断
            const fileName = file.name.toLowerCase();
            const isVideo = this.videoExtensions.some(ext => fileName.endsWith(ext));
            if (isVideo) {
                console.log('通过文件扩展名识别为视频:', file.name, 'MIME类型:', file.type || '未知');
            } else {
                console.warn('无法识别为视频文件:', file.name, 'MIME类型:', file.type || '未知', '扩展名:', fileName.substring(fileName.lastIndexOf('.')));
            }
            return isVideo;
        },
        
        // 检查是否为图片文件
        isImageFile(file) {
            if (file.type) {
                return file.type.startsWith('image/');
            }
            // 通过扩展名判断
            const fileName = file.name.toLowerCase();
            const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp', '.svg'];
            return imageExtensions.some(ext => fileName.endsWith(ext));
        },
        
        // 触发文件选择（从相册）
        triggerFileInput() {
            if (this.$refs.fileInput) {
                this.$refs.fileInput.click();
            }
        },
        
        // 处理文件选择
        handleFileSelect(event) {
            const files = Array.from(event.target.files || []);
            const remainingSlots = this.maxFiles - this.internalFileList.length;
            const filesToProcess = files.slice(0, remainingSlots);
            
            filesToProcess.forEach(file => {
                this.processFile(file);
            });
            
            // 清空 input，以便可以重复选择同一文件
            if (this.$refs.fileInput) {
                this.$refs.fileInput.value = '';
            }
        },
        
        // 处理单个文件
        processFile(file) {
            console.log('处理文件:', file.name, '类型:', file.type, '大小:', file.size);
            
            const isImage = this.isImageFile(file);
            const isVideo = this.isVideoFile(file);
            
            console.log('文件类型判断结果 - 图片:', isImage, '视频:', isVideo);
            
            if (!isImage && !isVideo) {
                console.warn('文件类型不支持:', file.name, 'MIME类型:', file.type);
                this.showMessage('只能上传图片或视频文件！', 'error');
                return;
            }
            
            // 验证文件大小
            if (isImage && file.size > 10 * 1024 * 1024) {
                this.showMessage('图片大小不能超过 10MB！', 'error');
                return;
            }
            
            // 视频大小限制已移除，改为通过时长限制
            
            if (isImage) {
                this.processImage(file);
            } else if (isVideo) {
                // 对于视频，立即创建占位项让用户看到，然后检查时长
                const objectUrl = URL.createObjectURL(file);
                const placeholderItem = {
                    id: ++this.fileIdCounter,
                    name: file.name,
                    url: objectUrl,
                    poster: null,
                    type: 'video',
                    duration: 0,
                    raw: file,
                    objectUrl: objectUrl,
                    isError: false,
                    posterGenerated: false,
                    needPoster: true,
                    uploading: false,
                    checking: true // 标记正在检查时长
                };
                
                // 立即添加到列表，让用户看到占位格子
                console.log('立即创建视频占位项:', file.name);
                this.internalFileList.push(placeholderItem);
                
                // 立即触发状态更新，通知父组件有文件正在处理（检查中）
                this.checkAndEmitUploadStatus();
                
                // 然后检查时长和处理视频
                this.checkVideoDuration(file, placeholderItem, objectUrl);
            }
        },
        
        // 检查视频时长
        checkVideoDuration(file, fileItem, objectUrl) {
            const video = document.createElement('video');
            video.preload = 'metadata';
            video.muted = true;
            
            // 添加更多属性以提高兼容性
            video.playsInline = true;
            video.setAttribute('webkit-playsinline', 'true');
            video.setAttribute('x5-playsinline', 'true');
            
            video.src = objectUrl;
            
            let isResolved = false; // 防止重复处理
            let timeoutId = null;
            
            // 处理元数据加载完成的函数
            const handleMetadataLoaded = () => {
                if (isResolved) return;
                isResolved = true;
                
                if (timeoutId) {
                    clearTimeout(timeoutId);
                    timeoutId = null;
                }
                
                console.log('视频元数据加载完成 - 检查时长:', {
                    fileName: file.name,
                    duration: video.duration,
                    videoWidth: video.videoWidth,
                    videoHeight: video.videoHeight,
                    readyState: video.readyState
                });
                
                // 检查视频时长（最大1分钟）
                if (video.duration && video.duration > 60){
                    // 时长超限：删除占位项
                    const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                    if (fileIndex !== -1) {
                        if (fileItem.objectUrl) {
                            URL.revokeObjectURL(fileItem.objectUrl);
                        }
                        this.internalFileList.splice(fileIndex, 1);
                    }
                    URL.revokeObjectURL(objectUrl);
                    video.src = '';
                    this.showMessage('视频时长不能超过 1 分钟！', 'error');
                    return;
                }
                
                // 时长符合要求，继续处理视频
                this.continueVideoProcessing(file, fileItem, video);
            };
            
            // 处理超时的情况
            const handleTimeout = () => {
                if (isResolved) return;
                isResolved = true;
                
                console.warn('视频元数据加载超时，跳过时长检查直接处理:', file.name);
                
                // 超时时，跳过时长检查，直接处理视频
                // 服务器端可能也会检查时长，这里只是客户端预检查
                const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                if (fileIndex !== -1) {
                    this.internalFileList[fileIndex].checking = false;
                    this.internalFileList[fileIndex].duration = video.duration || 0;
                    // 触发响应式更新
                    this.$forceUpdate();
                }
                
                // 清理视频元素
                video.src = '';
                
                // 直接调用 processVideo，不等待元数据
                this.processVideo(file, fileItem);
            };
            
            // 设置超时（增加到15秒，给移动端更多时间）
            const timeoutDuration = this.isWeChatBrowser ? 15000 : 12000;
            timeoutId = setTimeout(handleTimeout, timeoutDuration);
            
            // 监听多种事件以提高兼容性
            video.onloadedmetadata = () => {
                console.log('onloadedmetadata 触发:', file.name);
                handleMetadataLoaded();
            };
            
            video.onloadeddata = () => {
                console.log('onloadeddata 触发:', file.name, 'duration:', video.duration);
                // 如果 onloadedmetadata 没触发，但 onloadeddata 触发了，也尝试处理
                if (!isResolved && video.duration !== undefined && !isNaN(video.duration)) {
                    handleMetadataLoaded();
                }
            };
            
            video.oncanplay = () => {
                console.log('oncanplay 触发:', file.name, 'duration:', video.duration);
                // 如果前面的都没触发，oncanplay 时也尝试处理
                if (!isResolved && video.duration !== undefined && !isNaN(video.duration)) {
                    handleMetadataLoaded();
                }
            };
            
            video.onerror = (e) => {
                if (isResolved) return;
                isResolved = true;
                
                if (timeoutId) {
                    clearTimeout(timeoutId);
                    timeoutId = null;
                }
                
                console.error('视频加载错误:', {
                    fileName: file.name,
                    error: video.error,
                    errorCode: video.error ? video.error.code : 'unknown'
                });
                
                // 视频加载错误：删除占位项
                const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                if (fileIndex !== -1) {
                    if (fileItem.objectUrl) {
                        URL.revokeObjectURL(fileItem.objectUrl);
                    }
                    this.internalFileList.splice(fileIndex, 1);
                }
                URL.revokeObjectURL(objectUrl);
                video.src = '';
                this.showMessage('视频文件无法读取，请检查文件格式！', 'error');
            };
        },
        
        // 继续处理视频（从时长检查后调用）
        continueVideoProcessing(file, fileItem, video) {
            // 更新占位项的时长标记
            const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
            if (fileIndex !== -1) {
                this.internalFileList[fileIndex].checking = false;
                this.internalFileList[fileIndex].duration = video.duration || 0;
                // 触发响应式更新
                this.$forceUpdate();
            }
            
            // 清理临时视频元素
            video.src = '';
            
            // 继续处理视频（注意：不要在这里 revokeObjectURL，因为 processVideo 还需要使用）
            this.processVideo(file, fileItem);
        },
        
        // 处理图片
        processImage(file) {
            const reader = new FileReader();
            
            reader.onload = async (e) => {
                const fileItem = {
                    id: ++this.fileIdCounter,
                    name: file.name,
                    url: e.target.result,
                    type: 'image',
                    raw: file,
                    uploading: false
                };
                // 先添加到列表（但不显示成功提示）
                this.internalFileList.push(fileItem);
                
                // 立即触发状态更新（虽然 uploading 还是 false，但 fileItem 已创建，后续 uploadFile 会更新）
                // 实际上在 uploadFile 开始时会设置为 true，这里主要是为了保持一致性
                
                // 立即上传文件（只有上传成功才显示提示）
                await this.uploadFile(fileItem);
            };
            
            reader.onerror = () => {
                this.showMessage('图片读取失败，请重试！', 'error');
            };
            
            reader.readAsDataURL(file);
        },
        
        // 处理视频
        processVideo(file, fileItem) {
            console.log('开始处理视频文件:', file.name, 'MIME类型:', file.type, '大小:', file.size, '微信浏览器:', this.isWeChatBrowser);
            
            // 使用已创建的 fileItem（从 checkVideoDuration 传入）
            if (!fileItem) {
                console.error('processVideo: fileItem 不能为空');
                return;
            }
            
            const video = document.createElement('video');
            video.preload = 'metadata';
            video.playsInline = true; // iOS 兼容性
            video.muted = true; // 某些浏览器需要静音才能自动加载
            
            // 微信浏览器特殊处理：添加更多属性
            if (this.isWeChatBrowser) {
                video.setAttribute('webkit-playsinline', 'true');
                video.setAttribute('x5-playsinline', 'true');
                video.setAttribute('x5-video-player-type', 'h5');
                video.setAttribute('x5-video-player-fullscreen', 'true');
                video.setAttribute('x5-video-orientation', 'portraint');
            }
            
            // 使用 fileItem 已有的 objectUrl
            const objectUrl = fileItem.objectUrl;
            let timeoutId = null;
            let isProcessed = true; // 文件项已创建
            let posterGenerationAttempted = false; // 标记是否已尝试生成封面
            
            const self = this;
            
            // 立即开始上传（错误处理已在 uploadFile 方法中统一处理）
            self.uploadFile(fileItem).catch(err => {
                // 错误已经在 uploadFile 的 catch 块中处理了
                console.error('视频上传失败（已在 uploadFile 中处理）:', err);
            });
            
            // 更新视频封面的函数
            const updateVideoPoster = (poster) => {
                if (!fileItem || !poster) {
                    console.warn('更新封面失败: fileItem或poster为空', { fileItem: !!fileItem, poster: !!poster });
                    return;
                }
                
                const fileIndex = self.internalFileList.findIndex(f => f.id === fileItem.id);
                if (fileIndex !== -1) {
                    console.log('更新视频封面:', file.name, '封面大小:', poster.length, '封面前100字符:', poster.substring(0, 100));
                    self.internalFileList[fileIndex].poster = poster;
                    self.internalFileList[fileIndex].posterGenerated = true;
                    self.internalFileList[fileIndex].needPoster = false;
                    // 更新时长
                    if (video.duration) {
                        self.internalFileList[fileIndex].duration = video.duration;
                    }
                    // 使用Vue的响应式更新
                    self.$set(self.internalFileList, fileIndex, { ...self.internalFileList[fileIndex] });
                    // 触发响应式更新
                    self.$forceUpdate();
                    console.log('封面更新完成，当前poster:', self.internalFileList[fileIndex].poster ? '已设置' : '未设置');
                } else {
                    console.warn('更新封面失败: 找不到文件项', fileItem.id);
                }
            };
            
            // 处理错误的函数
            const handleError = (errorMessage) => {
                console.error('视频处理错误:', errorMessage, '文件:', file.name, '类型:', file.type);
                
                // 显示错误消息
                self.showMessage(errorMessage, 'error');
                
                // 标记为错误并移除文件项
                if (fileItem) {
                    const fileIndex = self.internalFileList.findIndex(f => f.id === fileItem.id);
                    if (fileIndex !== -1) {
                        // 延迟移除错误的文件项（给用户一点时间看到错误提示）
                        setTimeout(() => {
                            const currentIndex = self.internalFileList.findIndex(f => f.id === fileItem.id);
                            if (currentIndex !== -1) {
                                // 释放 ObjectURL
                                if (fileItem.objectUrl) {
                                    URL.revokeObjectURL(fileItem.objectUrl);
                                }
                                self.internalFileList.splice(currentIndex, 1);
                            }
                        }, 2000);
                    }
                }
                
                // 清空视频源
                if (video.src) {
                    video.src = '';
                }
            };
            
            // 尝试生成视频封面（支持重试）
            const tryCaptureFrame = (retryCount = 0) => {
                // 如果已经生成封面，不再尝试
                if (!fileItem || fileItem.posterGenerated) {
                    return;
                }
                
                // 如果已经尝试过但失败了，且重试次数用完，不再尝试
                if (posterGenerationAttempted && retryCount === 0) {
                    return;
                }
                
                const maxRetries = self.isWeChatBrowser ? 8 : 5;
                
                try {
                    // 检查视频是否准备好
                    if (video.readyState >= 2 && video.videoWidth > 0 && video.videoHeight > 0) {
                        // 确保视频当前时间在有效范围内
                        if (video.duration && video.currentTime >= video.duration) {
                            video.currentTime = 0.1;
                        }
                        
                        // 等待视频帧渲染
                        const waitTime = self.isWeChatBrowser ? 500 : 200;
                        setTimeout(() => {
                            try {
                                const canvas = document.createElement('canvas');
                                canvas.width = video.videoWidth;
                                canvas.height = video.videoHeight;
                                const ctx = canvas.getContext('2d');
                                
                                // 尝试绘制视频帧
                                ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
                                
                                // 直接生成封面，不检查内容（因为第一帧可能是黑色，但仍然有效）
                                // 只要canvas能正常绘制，就认为封面有效
                                const poster = canvas.toDataURL('image/jpeg', 0.8);
                                
                                // 验证生成的base64数据是否有效
                                if (poster && poster.length > 100 && poster.startsWith('data:image')) {
                                    console.log('封面生成成功:', file.name, '重试次数:', retryCount, '封面大小:', poster.length);
                                    posterGenerationAttempted = true;
                                    updateVideoPoster(poster);
                                    // 封面已生成，但不清空视频源，因为列表中的视频元素可能还需要使用
                                    // 延迟清空，给列表中的视频元素时间加载
                                    setTimeout(() => {
                                        if (video.src && video.src.startsWith('blob:')) {
                                            video.src = '';
                                        }
                                    }, 2000);
                                } else {
                                    throw new Error('生成的封面数据无效');
                                }
                            } catch (drawError) {
                                console.warn('绘制视频帧失败:', drawError, '重试次数:', retryCount);
                                if (retryCount < maxRetries) {
                                    // 重试：跳转到不同的时间点
                                    const retryTime = Math.min((retryCount + 1) * 0.3, video.duration ? video.duration - 0.1 : 1);
                                    video.currentTime = retryTime;
                                    setTimeout(() => tryCaptureFrame(retryCount + 1), 500);
                                } else {
                                    console.warn('封面生成失败，已达到最大重试次数');
                                    posterGenerationAttempted = true;
                                }
                            }
                        }, waitTime);
                    } else {
                        // 视频帧还没准备好，等待一下再试
                        if (retryCount < maxRetries) {
                            const waitTime = self.isWeChatBrowser ? 1000 : 500;
                            setTimeout(() => {
                                if (fileItem && !fileItem.posterGenerated) {
                                    tryCaptureFrame(retryCount + 1);
                                }
                            }, waitTime);
                        } else {
                            console.warn('视频帧一直未准备好，放弃生成封面');
                            posterGenerationAttempted = true;
                        }
                    }
                } catch (e) {
                    console.warn('生成视频封面异常:', e, '重试次数:', retryCount);
                    if (retryCount < maxRetries) {
                        setTimeout(() => tryCaptureFrame(retryCount + 1), 500);
                    } else {
                        posterGenerationAttempted = true;
                    }
                }
            };
            
            // 设置超时，防止某些格式无法加载时一直等待
            // 微信浏览器需要更长的超时时间，因为视频处理可能较慢
            // 根据文件大小和浏览器类型动态调整超时时间
            let timeoutDuration;
            if (this.isWeChatBrowser) {
                // 微信浏览器：小文件15秒，大文件（>10MB）30秒
                timeoutDuration = file.size > 10 * 1024 * 1024 ? 30000 : 15000;
            } else {
                // 其他浏览器：小文件10秒，大文件（>10MB）20秒
                timeoutDuration = file.size > 10 * 1024 * 1024 ? 20000 : 10000;
            }
            let lastReadyState = video.readyState;
            let readyStateCheckCount = 0;
            
            // 定期检查 readyState 是否在变化（说明视频正在加载）
            const readyStateCheckInterval = setInterval(() => {
                if (isProcessed) {
                    clearInterval(readyStateCheckInterval);
                    return;
                }
                const currentReadyState = video.readyState;
                readyStateCheckCount++;
                
                // 如果 readyState 在变化，说明视频正在加载
                if (currentReadyState !== lastReadyState) {
                    console.log(`视频加载中: readyState 从 ${lastReadyState} 变为 ${currentReadyState} (检查次数: ${readyStateCheckCount})`);
                    lastReadyState = currentReadyState;
                }
                
                // 如果 readyState 一直为 0 且检查了多次，可能是真的无法加载
                if (readyStateCheckCount >= 5 && currentReadyState === 0) {
                    console.warn('视频 readyState 一直为 0，可能无法加载');
                }
            }, 1000); // 每秒检查一次
            
            timeoutId = setTimeout(() => {
                clearInterval(readyStateCheckInterval);
                
                if (!fileItem || fileItem.posterGenerated) {
                    return;
                }
                
                const currentReadyState = video.readyState;
                const hasError = video.error !== null;
                
                console.warn('视频加载超时（但视频项已创建）:', {
                    fileName: file.name,
                    fileSize: file.size,
                    fileType: file.type,
                    readyState: currentReadyState,
                    readyStateCheckCount: readyStateCheckCount,
                    lastReadyState: lastReadyState,
                    hasError: hasError,
                    videoError: video.error,
                    networkState: video.networkState
                });
                
                // 如果 readyState 还是 0 且没有错误事件触发，说明可能是格式不支持
                if (currentReadyState === 0 && !hasError) {
                    // 检查是否有网络状态问题
                    if (video.networkState === 3) { // NETWORK_NO_SOURCE
                        console.warn('视频格式可能不支持，但视频项已创建，继续尝试生成封面');
                        // 不删除视频项，继续尝试生成封面
                    } else {
                        console.warn('视频加载超时，但视频项已创建，继续尝试生成封面');
                    }
                    // 不清空 video.src，继续尝试生成封面
                } else if (currentReadyState > 0) {
                    // readyState > 0 说明视频在加载，继续尝试生成封面
                    console.log('视频正在加载中，readyState:', currentReadyState, '继续尝试生成封面');
                    tryCaptureFrame();
                }
                // 不触发错误，因为视频项已经创建，继续尝试生成封面
            }, timeoutDuration);
            
            video.onloadedmetadata = () => {
                console.log('视频元数据加载完成:', {
                    fileName: file.name,
                    duration: video.duration,
                    videoWidth: video.videoWidth,
                    videoHeight: video.videoHeight,
                    readyState: video.readyState,
                    isWeChatBrowser: self.isWeChatBrowser
                });
                
                if (timeoutId) {
                    clearTimeout(timeoutId);
                    timeoutId = null;
                }
                
                // 更新视频时长
                if (fileItem && video.duration) {
                    const fileIndex = self.internalFileList.findIndex(f => f.id === fileItem.id);
                    if (fileIndex !== -1) {
                        self.internalFileList[fileIndex].duration = video.duration;
                    }
                }
                
                // 再次检查视频时长（双重保险，因为已经在 checkVideoDuration 中检查过）
                if (video.duration && video.duration > 60) {
                    if (fileItem) {
                        const fileIndex = self.internalFileList.findIndex(f => f.id === fileItem.id);
                        if (fileIndex !== -1) {
                            if (fileItem.objectUrl) {
                                URL.revokeObjectURL(fileItem.objectUrl);
                            }
                            self.internalFileList.splice(fileIndex, 1);
                        }
                    }
                    self.showMessage('视频时长不能超过 1 分钟！', 'error');
                    video.src = '';
                    return;
                }
                
                // 尝试跳转到第一帧来生成封面
                video.currentTime = 0.1;
            };
            
            video.onloadeddata = () => {
                console.log('视频数据加载完成:', {
                    fileName: file.name,
                    readyState: video.readyState,
                    videoWidth: video.videoWidth,
                    videoHeight: video.videoHeight
                });
                
                // 尝试生成封面
                if (fileItem && !fileItem.posterGenerated) {
                    console.log('视频数据加载完成，尝试生成封面');
                    tryCaptureFrame();
                }
            };
            
            video.oncanplay = () => {
                console.log('视频可以播放:', {
                    fileName: file.name,
                    readyState: video.readyState
                });
                
                if (timeoutId) {
                    clearTimeout(timeoutId);
                    timeoutId = null;
                }
                
                // 如果视频项已创建但没有封面，尝试生成封面
                if (fileItem && !fileItem.posterGenerated) {
                    console.log('视频可以播放，尝试生成封面');
                    tryCaptureFrame();
                }
            };
            
            video.onprogress = () => {
                if (isProcessed) return;
                console.log('视频加载进度:', {
                    fileName: file.name,
                    readyState: video.readyState,
                    networkState: video.networkState,
                    buffered: video.buffered.length > 0 ? video.buffered.end(0) : 0
                });
            };
            
            video.onerror = (e) => {
                if (isProcessed) return;
                
                // 获取更详细的错误信息
                const errorCode = video.error ? video.error.code : 'unknown';
                const errorMessages = {
                    1: '视频加载中止',
                    2: '视频网络错误',
                    3: '视频解码错误',
                    4: '视频格式不支持'
                };
                const errorDetail = errorMessages[errorCode] || '视频格式不支持或文件已损坏';
                
                console.error('视频加载错误:', {
                    error: e,
                    errorCode: errorCode,
                    errorDetail: errorDetail,
                    fileName: file.name,
                    fileType: file.type,
                    videoError: video.error
                });
                
                handleError(`${errorDetail}，请尝试其他格式！`);
                video.src = '';
            };
            
            video.src = objectUrl;
        },
        
        // 上传文件到服务器
        async uploadFile(fileItem) {
            // 设置上传状态为 true
            const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
            if (fileIndex !== -1) {
                this.internalFileList[fileIndex].uploading = true;
                // 创建 AbortController 用于取消请求
                if (!this.internalFileList[fileIndex].abortController) {
                    this.internalFileList[fileIndex].abortController = new AbortController();
                }
                this.checkAndEmitUploadStatus();
            }
            
            // 获取 AbortController（如果已存在）
            const abortController = fileItem.abortController || new AbortController();
            if (!fileItem.abortController) {
                // 如果 fileItem 中没有，更新到列表中
                const currentFileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                if (currentFileIndex !== -1) {
                    this.internalFileList[currentFileIndex].abortController = abortController;
                }
            }
            
            try {
                // 根据文件类型选择目录
                const directory = fileItem.type === 'image' 
                    ? 'feedback/images' 
                    : 'feedback/videos';
                
                // 创建 FormData
                const formData = new FormData();
                formData.append('file', fileItem.raw); // 使用原始 File 对象
                
                console.log(`开始上传文件: ${fileItem.name}, 类型: ${fileItem.type}, 目录: ${directory}`);
                
                // 调用上传接口，增加重试机制以提高稳定性（特别是视频）
                let response;
                let retryCount = 0;
                const maxRetries = fileItem.type === 'video' ? 2 : 1; // 视频允许重试2次
                
                // 根据文件类型和大小设置超时时间
                // 视频文件需要更长的超时时间，特别是大文件
                // 默认10秒（来自 request.js），图片保持默认，视频根据大小动态调整
                let timeoutMs;
                if (fileItem.type === 'video') {
                    // 视频：小文件（<20MB）60秒，大文件120秒
                    timeoutMs = fileItem.raw.size > 20 * 1024 * 1024 ? 120000 : 60000;
                } else {
                    // 图片：默认30秒
                    timeoutMs = 30000;
                }
                
                console.log(`上传文件超时设置: ${fileItem.name}, 类型: ${fileItem.type}, 大小: ${(fileItem.raw.size / 1024 / 1024).toFixed(2)}MB, 超时: ${timeoutMs/1000}秒`);
                
                while (retryCount <= maxRetries) {
                    // 检查是否已取消
                    if (abortController.signal.aborted) {
                        console.log(`文件 ${fileItem.name} 上传已取消`);
                        return; // 直接返回，不抛出错误
                    }
                    
                    try {
                        response = await this.upload('/admin-api/infra/file/upload', formData, {
                            params: {
                                directory: false // 文件目录
                            },
                            timeout: timeoutMs, // 设置超时时间
                            signal: abortController.signal // 传递取消信号
                            // headers 中的 tenant-id 和 Authorization 已经在 request.js 的拦截器中自动添加
                        });
                        break; // 成功则跳出循环
                    } catch (uploadError) {
                        // 如果是取消错误，直接返回，不重试
                        if (uploadError.name === 'CanceledError' || uploadError.name === 'AbortError' || abortController.signal.aborted) {
                            console.log(`文件 ${fileItem.name} 上传已取消`);
                            return; // 直接返回，不抛出错误
                        }
                        
                        retryCount++;
                        if (retryCount > maxRetries) {
                            throw uploadError; // 重试次数用完，抛出错误
                        }
                        console.warn(`文件 ${fileItem.name} 上传失败，正在重试 (${retryCount}/${maxRetries})...`, uploadError);
                        // 等待一段时间后重试（视频等待更久）
                        await new Promise(resolve => setTimeout(resolve, fileItem.type === 'video' ? 2000 : 1000));
                    }
                }
                
                // 检查是否已取消（在获取响应后再次检查）
                if (abortController.signal.aborted) {
                    console.log(`文件 ${fileItem.name} 上传已取消（响应返回后）`);
                    return; // 直接返回，不处理响应
                }
                
                console.log(`文件 ${fileItem.name} 上传接口返回:`, response);
                
                // 接口返回格式: { code: 200, msg: 'success', data: '文件URL' }
                if (response.code === 200 || response.code === 0) {
                    // 再次检查是否已取消（在更新状态前）
                    if (abortController.signal.aborted) {
                        console.log(`文件 ${fileItem.name} 上传已取消（处理响应前）`);
                        return;
                    }
                    
                    // 更新 fileList 中对应文件项，添加 serverUrl 字段
                    const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                    if (fileIndex !== -1) {
                        this.internalFileList[fileIndex].serverUrl = response.data;
                        this.internalFileList[fileIndex].uploading = false;
                        // 清理 AbortController
                        this.internalFileList[fileIndex].abortController = null;
                        // 如果还没有封面，尝试从服务器URL生成封面（对于视频）
                        if (fileItem.type === 'video' && !this.internalFileList[fileIndex].poster) {
                            // 触发响应式更新
                            this.$forceUpdate();
                        }
                    }
                    this.checkAndEmitUploadStatus();
                    
                    // 简化的上传结果，只包含 fileUrl, fileName, fileType
                    const uploadResult = {
                        fileUrl: response.data,
                        fileName: fileItem.name,
                        fileType: fileItem.type
                    };
                    
                    console.log(`文件 ${fileItem.name} 上传成功，URL: ${response.data}`);
                    console.log('上传结果:', uploadResult);
                    
                    // 触发上传成功事件，传递简化的结果
                    this.$emit('upload-success', uploadResult);
                    
                    this.showMessage(`${fileItem.name} 上传成功`, 'success');
                } else {
                    throw new Error(response.msg || '上传失败');
                }
            } catch (error) {
                // 如果是取消错误，不显示错误提示，直接返回
                if (error.name === 'CanceledError' || error.name === 'AbortError' || abortController.signal.aborted) {
                    console.log(`文件 ${fileItem.name} 上传已取消`);
                    // 清理上传状态
                    const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                    if (fileIndex !== -1) {
                        this.internalFileList[fileIndex].uploading = false;
                        this.internalFileList[fileIndex].abortController = null;
                        this.checkAndEmitUploadStatus();
                    }
                    return; // 直接返回，不显示错误提示
                }
                
                console.error(`文件 ${fileItem.name} 上传失败:`, error);
                // 设置上传状态为 false，并标记为上传失败
                const fileIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                if (fileIndex !== -1) {
                    this.internalFileList[fileIndex].uploading = false;
                    this.internalFileList[fileIndex].uploadError = true;
                    this.internalFileList[fileIndex].errorMessage = error.message || '上传失败，请重试';
                    // 清理 AbortController
                    this.internalFileList[fileIndex].abortController = null;
                    this.checkAndEmitUploadStatus();
                    // 触发响应式更新
                    this.$forceUpdate();
                    
                    // 5秒后自动删除失败的文件项（给用户时间看到错误提示）
                    setTimeout(() => {
                        const currentIndex = this.internalFileList.findIndex(f => f.id === fileItem.id);
                        if (currentIndex !== -1 && this.internalFileList[currentIndex].uploadError) {
                            // 释放 ObjectURL
                            if (this.internalFileList[currentIndex].objectUrl) {
                                URL.revokeObjectURL(this.internalFileList[currentIndex].objectUrl);
                            } else if (this.internalFileList[currentIndex].url && this.internalFileList[currentIndex].url.startsWith('blob:')) {
                                URL.revokeObjectURL(this.internalFileList[currentIndex].url);
                            }
                            this.internalFileList.splice(currentIndex, 1);
                        }
                    }, 5000);
                }
                this.showMessage(`${fileItem.name} 上传失败: ${error.message || '未知错误'}`, 'error');
                // 不再抛出错误，因为已经在 UI 上显示了错误状态
                // throw error;
            }
        },
        
        // 检查并发送上传状态
        checkAndEmitUploadStatus() {
            // 检查是否有文件正在上传或正在检查（视频检查时长也算处理中）
            const hasUploading = this.internalFileList.some(file => 
                file.uploading === true || file.checking === true
            );
            this.$emit('upload-status-change', hasUploading);
        },
        
        // 确认删除（显示确认弹框）
        confirmDelete(index) {
            this.deleteConfirmIndex = index;
        },
        
        // 取消删除
        cancelDelete() {
            this.deleteConfirmIndex = null;
        },
        
        // 确认删除文件
        confirmDeleteFile() {
            if (this.deleteConfirmIndex !== null) {
                const index = this.deleteConfirmIndex;
                this.deleteConfirmIndex = null;
                this.removeFile(index);
            }
        },
        
        // 移除文件
        async removeFile(index) {
            // 先获取文件信息，避免删除后无法获取
            if (index < 0 || index >= this.internalFileList.length) {
                console.warn('删除索引无效:', index);
                return;
            }
            
            const file = this.internalFileList[index];
            if (!file) {
                console.warn('文件不存在:', index);
                return;
            }
            
            // 如果文件正在上传，先取消上传请求
            if (file.uploading && file.abortController) {
                console.log(`取消文件 ${file.name} 的上传请求`);
                file.abortController.abort();
                // 清理 AbortController
                file.abortController = null;
            }
            
            // 如果文件已上传，先调用删除接口进行持久化删除
            if (file.serverUrl) {
                try {
                    // 调用删除接口，URL 参数通过 query string 传递
                    const deleteUrl = `/admin-api/infra/file/delete-by-url?url=${encodeURIComponent(file.serverUrl)}`;
                    const response = await http.delete(deleteUrl);
                    
                    console.log(`文件 ${file.name} 删除接口返回:`, response);
                    
                    if (response.code === 200 || response.code === 0) {
                        console.log(`文件 ${file.name} 已从服务器删除`);
                    } else {
                        console.warn(`文件 ${file.name} 服务器删除失败:`, response.msg);
                        // 即使服务器删除失败，也继续从页面删除
                    }
                } catch (error) {
                    console.error(`文件 ${file.name} 删除接口调用失败:`, error);
                    // 即使接口调用失败，也继续从页面删除
                }
            }
            
            // 释放 ObjectURL
            if (file.objectUrl) {
                URL.revokeObjectURL(file.objectUrl);
            } else if (file.url && file.url.startsWith('blob:')) {
                URL.revokeObjectURL(file.url);
            }
            
            // 从列表中移除（先移除，确保UI立即更新）
            this.internalFileList.splice(index, 1);
            
            // 更新上传状态（因为可能取消了正在上传的文件）
            this.checkAndEmitUploadStatus();
            
            // 无论文件是否已上传，都触发删除事件，以便父组件同步更新
            // 使用 fileId 或 serverUrl 作为唯一标识
            const deleteInfo = {
                fileUrl: file.serverUrl || null,
                fileName: file.name,
                fileType: file.type,
                fileId: file.id
            };
            this.$emit('upload-delete', deleteInfo);
            
            // 强制触发响应式更新
            this.$forceUpdate();
            
            this.showMessage('已删除', 'success');
        },
        
        // 处理视频元数据加载（用于更新时长）
        handleVideoMetadata(event, index) {
            const video = event.target;
            const fileItem = this.internalFileList[index];
            
            if (fileItem && video.duration) {
                // 更新视频时长
                if (!fileItem.duration || fileItem.duration === 0) {
                    fileItem.duration = video.duration;
                    // 触发响应式更新
                    this.$forceUpdate();
                }
                
                // 如果没有封面，尝试生成封面
                if (!fileItem.poster && video.readyState >= 2 && video.videoWidth > 0 && video.videoHeight > 0) {
                    // 确保视频当前时间在有效范围内
                    if (video.duration && video.currentTime >= video.duration) {
                        video.currentTime = 0.1;
                    }
                    
                    // 等待视频帧渲染
                    setTimeout(() => {
                        try {
                            const canvas = document.createElement('canvas');
                            canvas.width = video.videoWidth;
                            canvas.height = video.videoHeight;
                            const ctx = canvas.getContext('2d');
                            ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
                            const poster = canvas.toDataURL('image/jpeg', 0.8);
                            
                            // 验证生成的base64数据是否有效
                            if (poster && poster.length > 100 && poster.startsWith('data:image')) {
                                fileItem.poster = poster;
                                fileItem.posterGenerated = true;
                                this.$forceUpdate();
                                console.log('从服务器视频生成封面成功:', fileItem.name, '封面大小:', poster.length);
                            } else {
                                console.warn('从服务器视频生成的封面数据无效:', poster ? poster.length : 'null');
                            }
                        } catch (e) {
                            console.warn('从服务器视频生成封面失败:', e);
                        }
                    }, 300);
                }
            }
        },
        
        // 格式化视频时长
        formatDuration(seconds) {
            if (!seconds || seconds === 0) return '--:--';
            const mins = Math.floor(seconds / 60);
            const secs = Math.floor(seconds % 60);
            return `${mins}:${secs.toString().padStart(2, '0')}`;
        },
        
        // 预览图片
        previewImage(index) {
            const file = this.internalFileList[index];
            // 只有上传成功（有 serverUrl）才允许预览
            if (!file.serverUrl) {
                this.showMessage('文件正在上传中，请稍候...', 'info');
                return;
            }
            this.previewItem = { ...file };
            // 阻止背景滚动
            document.body.style.overflow = 'hidden';
        },
        
        // 预览视频
        previewVideo(index) {
            const file = this.internalFileList[index];
            // 只有上传成功（有 serverUrl）才允许预览
            if (!file.serverUrl) {
                this.showMessage('文件正在上传中，请稍候...', 'info');
                return;
            }
            // 优先使用本地已加载的URL（objectUrl或url），避免重新加载
            // 如果本地URL不可用，再使用服务器URL
            let previewUrl = file.objectUrl || file.url;
            if (!previewUrl || (previewUrl.startsWith('blob:') && !file.objectUrl)) {
                // 如果本地blob URL已失效，使用服务器URL
                previewUrl = file.serverUrl;
                this.previewVideoLoading = true;
            } else {
                // 使用本地URL，不需要加载
                this.previewVideoLoading = false;
            }
            
            // 使用本地URL创建预览项，这样就不需要重新加载
            const previewItem = {
                ...file,
                url: previewUrl,
                // 保留服务器URL作为备用
                serverUrl: file.serverUrl
            };
            this.previewItem = previewItem;
            // 阻止背景滚动
            document.body.style.overflow = 'hidden';
            // 视频预览时自动播放
            this.$nextTick(() => {
                if (this.$refs.previewVideoPlayer) {
                    const video = this.$refs.previewVideoPlayer;
                    // 监听加载事件
                    video.oncanplay = () => {
                        this.previewVideoLoading = false;
                        video.play().catch(err => {
                            console.warn('视频自动播放失败:', err);
                        });
                    };
                    
                    video.onloadeddata = () => {
                        this.previewVideoLoading = false;
                    };
                    
                    // 如果视频已经加载，直接播放
                    if (video.readyState >= 3) {
                        this.previewVideoLoading = false;
                        video.play().catch(err => {
                            console.warn('视频自动播放失败:', err);
                        });
                    }
                }
            });
        },
        
        // 处理预览视频加载错误
        handlePreviewVideoError(event) {
            const video = event.target;
            console.warn('预览视频加载失败，尝试使用服务器URL:', this.previewItem);
            
            // 如果本地URL加载失败，尝试使用服务器URL
            if (this.previewItem && this.previewItem.serverUrl && video.src !== this.previewItem.serverUrl) {
                video.src = this.previewItem.serverUrl;
                this.previewVideoLoading = true;
                
                // 监听加载完成
                video.oncanplay = () => {
                    this.previewVideoLoading = false;
                    video.play().catch(err => {
                        console.warn('视频自动播放失败:', err);
                    });
                };
                
                video.onerror = () => {
                    this.previewVideoLoading = false;
                    this.showMessage('视频加载失败，请检查网络连接', 'error');
                };
            } else {
                this.previewVideoLoading = false;
                this.showMessage('视频加载失败', 'error');
            }
        },
        
        // 关闭预览
        closePreview() {
            // 停止视频播放
            if (this.$refs.previewVideoPlayer) {
                this.$refs.previewVideoPlayer.pause();
                this.$refs.previewVideoPlayer.currentTime = 0;
                // 清理事件监听
                this.$refs.previewVideoPlayer.oncanplay = null;
                this.$refs.previewVideoPlayer.onerror = null;
            }
            this.previewItem = null;
            this.previewVideoLoading = false;
            // 恢复背景滚动
            document.body.style.overflow = '';
        },
        
        // 键盘事件处理（ESC 关闭预览）
        handleKeyDown(event) {
            if (event.key === 'Escape' && this.previewItem) {
                this.closePreview();
            }
        },
        
        // 显示消息提示（简单实现，不依赖 Element Plus）
        showMessage(message, type = 'info') {
            // 创建简单的提示框
            const messageEl = document.createElement('div');
            messageEl.className = `simple-message simple-message-${type}`;
            messageEl.textContent = message;
            document.body.appendChild(messageEl);
            
            // 触发动画
            setTimeout(() => {
                messageEl.classList.add('show');
            }, 10);
            
            // 自动移除
            setTimeout(() => {
                messageEl.classList.remove('show');
                setTimeout(() => {
                    if (document.body.contains(messageEl)) {
                        document.body.removeChild(messageEl);
                    }
                }, 300);
            }, 2000);
        }
    }
}
</script>

<style lang="scss" scoped>
.media-upload {
    width: 100%;
    
    .file-input {
        position: absolute;
        width: 0;
        height: 0;
        opacity: 0;
        overflow: hidden;
        pointer-events: none;
    }
    
    // 文件列表
    .file-list {
        display: grid;
        grid-template-columns: repeat(6, 1fr);
        gap: 16px;
    }

    .file-item {
        position: relative;
        width: 85%;
        padding-bottom: 85%; // 保持 1:1 比例
        border-radius: 8px;
        overflow: hidden;
        background-color: #f5f5f5;
        
        // 上传按钮项样式
        &.upload-btn-item {
            background-color: transparent;
            cursor: pointer;
        }
    }

    .preview-image,
    .preview-video {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        cursor: pointer;
        transition: transform 0.2s;
        
        &:active {
            transform: scale(0.98);
        }
        
        img,
        video {
            width: 100%;
            height: 100%;
            object-fit: cover;
            display: block;
            pointer-events: none;
        }
    }
    
    .preview-image {
        .upload-overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.6);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 2;
            
            .upload-status {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                gap: 8px;
                
                .upload-spinner {
                    width: 28px;
                    height: 28px;
                    border: 2.5px solid rgba(255, 255, 255, 0.3);
                    border-top-color: #fff;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                }
                
                .upload-text {
                    color: #fff;
                    font-size: 18px;
                    font-weight: 500;
                    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
                }
            }
            
            .upload-error {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                gap: 6px;
                background-color: rgba(244, 67, 54, 0.9);
                padding: 10px 14px;
                border-radius: 8px;
                
                .error-icon {
                    width: 28px;
                    height: 28px;
                    background-color: #fff;
                    color: #f44336;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 18px;
                    font-weight: bold;
                }
                
                .error-text {
                    color: #fff;
                    font-size: 18px;
                    font-weight: 500;
                    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
                }
            }
        }
    }

    .preview-video {
        // 确保视频元素有背景色
        background-color: #000;
        
        video {
            background-color: #000;
            // 如果没有封面，确保视频元素可见
            &[poster=""] {
                background-color: transparent;
            }
        }
        
        // 视频占位背景（背景图片）
        .video-placeholder-bg {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: #f5f5f5;
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 0;
            overflow: hidden;
            
            img {
                width: 100%;
                height: 100%;
                object-fit: cover;
            }
        }
        
        .video-overlay {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            // 默认透明，当没有封面时不显示遮罩
            background-color: rgba(0, 0, 0, 0);
            display: flex;
            align-items: center;
            justify-content: center;
            flex-direction: column;
            z-index: 1;
            transition: background-color 0.3s;
            
            // 当有封面时，显示半透明黑色遮罩以突出播放按钮
            &.has-poster {
                background-color: rgba(0, 0, 0, 0.4);
            }
            
            .play-icon {
                width: 30px;
                height: 30px;
                background-color: rgba(0, 0, 0, 0.1);
                border-radius: 50%;
                display: flex;
                align-items: center;
                justify-content: center;
                margin-bottom: 8px;
                flex-shrink: 0;
            }
            
            .video-duration {
                color: #fff;
                font-size: 24px;
                font-weight: 500;
                text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
                white-space: nowrap;
            }
            
            // 上传状态
            .upload-status {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                gap: 8px;
                
                .upload-spinner {
                    width: 24px;
                    height: 24px;
                    border: 2.5px solid rgba(255, 255, 255, 0.3);
                    border-top-color: #fff;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                }
                
                .upload-text {
                    color: #fff;
                    font-size: 16px;
                    font-weight: 500;
                    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
                }
            }
            
            // 上传错误状态
            .upload-error {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                gap: 6px;
                background-color: rgba(244, 67, 54, 0.8);
                padding: 10px 14px;
                border-radius: 8px;
                
                .error-icon {
                    width: 28px;
                    height: 28px;
                    background-color: #fff;
                    color: #f44336;
                    border-radius: 50%;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    font-size: 16px;
                    font-weight: bold;
                }
                
                .error-text {
                    color: #fff;
                    font-size: 16px;
                    font-weight: 500;
                    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
                }
            }
        }
    }

    .delete-btn {
        position: absolute;
        top: 2px;
        right: 2px;
        width: 20px;
        height: 20px;
        background-color: #FF3434;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: all 0.2s;
        z-index: 10;
        
        &:active {
            transform: scale(0.9);
            background-color: #ff0000;
        }
        
        svg {
            display: block;
        }
    }

    // 上传按钮（在文件列表中）
    .upload-btn {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        border: none;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        background-color: #eaeaea;
        cursor: pointer;
        transition: all 0.2s;
        
        &:active {
            opacity: 0.7;
            transform: scale(0.98);
        }
        
        .upload-icon-wrapper {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        
        .upload-icon {
            width: 40px;
            height: 40px;
            object-fit: contain;
            display: block;
        }
    }
    
    // 删除确认弹框
    .delete-confirm-modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 10000;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        align-items: center;
        justify-content: center;
        animation: fadeIn 0.3s ease;
        
        .delete-confirm-content {
            background-color: #fff;
            border-radius: 16px;
            padding: 40px 32px 32px;
            width: 80%;
            max-width: 500px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            animation: slideUp 0.3s ease;
        }
        
        .delete-confirm-title {
            font-size: 36px;
            font-weight: 600;
            color: #333;
            text-align: center;
            margin-bottom: 24px;
        }
        
        .delete-confirm-message {
            font-size: 28px;
            color: #666;
            text-align: center;
            margin-bottom: 40px;
            line-height: 1.5;
        }
        
        .delete-confirm-btns {
            display: flex;
            gap: 20px;
        }
        
        .delete-confirm-btn {
            flex: 1;
            height: 80px;
            border: none;
            border-radius: 8px;
            font-size: 28px;
            font-weight: 500;
            cursor: pointer;
            transition: all 0.2s;
            
            &.cancel-btn {
                background-color: #f5f5f5;
                color: #666;
                
                &:active {
                    background-color: #e0e0e0;
                    transform: scale(0.98);
                }
            }
            
            &.confirm-btn {
                background-color: #FF3434;
                color: #fff;
                
                &:active {
                    background-color: #ff0000;
                    transform: scale(0.98);
                }
            }
        }
    }
    
    // 预览模态框
    .preview-modal {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        bottom: 0;
        z-index: 9999;
        background-color: rgba(0, 0, 0, 0.95);
        display: flex;
        align-items: center;
        justify-content: center;
        animation: fadeIn 0.3s ease;
        
        .preview-content {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        
        .preview-close {
            position: absolute;
            top: 20px;
            right: 20px;
            width: 48px;
            height: 48px;
            background-color: rgba(0, 0, 0, 0.6);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            z-index: 10000;
            transition: all 0.2s;
            
            &:active {
                transform: scale(0.9);
                background-color: rgba(0, 0, 0, 0.8);
            }
            
            svg {
                display: block;
            }
        }
        
        .preview-image-container {
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            overflow: auto;
            -webkit-overflow-scrolling: touch;
            
            img {
                max-width: 100%;
                max-height: 100%;
                object-fit: contain;
                user-select: none;
                -webkit-user-select: none;
                -webkit-touch-callout: none;
                touch-action: pan-x pan-y pinch-zoom;
            }
        }
        
        .preview-video-container {
            position: relative;
            width: 100%;
            height: 100%;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 0;
            
            video {
                max-width: 100%;
                max-height: 100%;
                width: 100%;
                height: auto;
                object-fit: contain;
                background-color: #000;
            }
            
            .video-loading-tip {
                position: absolute;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                z-index: 10;
                
                .loading-spinner {
                    width: 40px;
                    height: 40px;
                    border: 3px solid rgba(255, 255, 255, 0.3);
                    border-top-color: #fff;
                    border-radius: 50%;
                    animation: spin 1s linear infinite;
                }
                
                .loading-text {
                    margin-top: 12px;
                    color: #fff;
                    font-size: 28px;
                    text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
                }
            }
        }
        
        @keyframes spin {
            to {
                transform: rotate(360deg);
            }
        }
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }
    to {
        opacity: 1;
    }
}

@keyframes slideUp {
    from {
        transform: translateY(20px);
        opacity: 0;
    }
    to {
        transform: translateY(0);
        opacity: 1;
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
}
</style>

