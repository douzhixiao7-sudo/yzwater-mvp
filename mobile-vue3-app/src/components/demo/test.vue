<template>
    <div class="test-upload">
        <div class="header">
            <h1>图片/视频上传 Demo</h1>
        </div>
        
        <div class="upload-area">
            <input 
                ref="fileInput"
                type="file" 
                accept="image/*,video/*"
                multiple
                @change="handleFileSelect"
                class="file-input"
            />
            <button @click="triggerFileInput" class="upload-btn">选择文件</button>
            <button @click="uploadAll" class="upload-all-btn" v-if="fileList.length > 0">上传所有文件</button>
        </div>
        
        <div class="file-list" v-if="fileList.length > 0">
            <div class="file-item" v-for="(item, index) in fileList" :key="index">
                <div class="preview" v-if="item.type === 'image'">
                    <img :src="item.url" :alt="item.name" />
                </div>
                <div class="preview" v-else-if="item.type === 'video'">
                    <video :src="item.url" controls></video>
                </div>
                <div class="file-info">
                    <p>{{ item.name }}</p>
                    <p class="file-size">{{ formatSize(item.size) }}</p>
                </div>
                <button @click="removeFile(index)" class="delete-btn">删除</button>
            </div>
        </div>
        
        <div class="upload-result" v-if="uploadResult">
            <h3>上传结果：</h3>
            <pre>{{ JSON.stringify(uploadResult, null, 2) }}</pre>
        </div>
    </div>
</template>

<script>
import http from '@/utils/request.js';

export default {
    name: 'TestUpload',
    data() {
        return {
            fileList: [],
            uploadResult: null
        }
    },
    methods: {
        // 触发文件选择
        triggerFileInput() {
            this.$refs.fileInput.click();
        },
        
        // 处理文件选择
        handleFileSelect(event) {
            const files = Array.from(event.target.files || []);
            
            files.forEach(file => {
                const isImage = file.type.startsWith('image/');
            const isVideo = file.type.startsWith('video/');
                
                if (!isImage && !isVideo) {
                    alert('只能上传图片或视频文件！');
                    return;
                }
                
                const fileItem = {
                    name: file.name,
                    size: file.size,
                    type: isImage ? 'image' : 'video',
                    raw: file,
                    url: URL.createObjectURL(file)
                };
                
                this.fileList.push(fileItem);
            });
            
            // 清空 input
            event.target.value = '';
        },
        
        // 删除文件
        removeFile(index) {
            const file = this.fileList[index];
            if (file.url) {
                URL.revokeObjectURL(file.url);
            }
            this.fileList.splice(index, 1);
        },
        
        // 格式化文件大小
        formatSize(bytes) {
            if (bytes === 0) return '0 B';
            const k = 1024;
            const sizes = ['B', 'KB', 'MB', 'GB'];
            const i = Math.floor(Math.log(bytes) / Math.log(k));
            return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
        },
        
        // 上传文件
        async uploadFile(fileItem) {
            try {
                const formData = new FormData();
                formData.append('file', fileItem.raw);
                
                const directory = fileItem.type === 'image' ? 'feedback/images' : 'feedback/videos';
                
                // 使用 post 方法上传文件
                const response = await http.post('/admin-api/infra/file/upload', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    },
                    params: {
                        directory: directory
                    }
                });
                
                return response;
            } catch (error) {
                console.error('上传失败:', error);
                throw error;
            }
        },
        
        // 上传所有文件
        async uploadAll() {
            if (this.fileList.length === 0) {
                alert('请先选择文件！');
                return;
            }
            
            const results = [];
            
            for (const fileItem of this.fileList) {
                try {
                    const response = await this.uploadFile(fileItem);
                    results.push({
                        fileName: fileItem.name,
                        fileType: fileItem.type,
                        status: 'success',
                        fileUrl: response.data
                    });
                } catch (error) {
                    results.push({
                        fileName: fileItem.name,
                        fileType: fileItem.type,
                        status: 'failed',
                        error: error.message
                    });
                }
            }
            
            this.uploadResult = results;
        }
    },
    
    beforeUnmount() {
        // 清理所有 ObjectURL
        this.fileList.forEach(file => {
            if (file.url) {
                URL.revokeObjectURL(file.url);
            }
        });
    }
}
</script>

<style lang="scss" scoped>
.test-upload {
    padding: 20px;
    max-width: 800px;
    margin: 0 auto;
    
    .header {
        text-align: center;
        margin-bottom: 30px;
        
        h1 {
            font-size: 24px;
            color: #333;
        }
    }
    
    .upload-area {
        margin-bottom: 30px;
        text-align: center;
        
        .file-input {
            display: none;
        }
        
        .upload-btn, .upload-all-btn {
            padding: 12px 24px;
            background-color: #409eff;
            color: #fff;
            border: none;
            border-radius: 4px;
            font-size: 16px;
            cursor: pointer;
            margin: 0 10px;
            
            &:hover {
                background-color: #66b1ff;
            }
        }
        
        .upload-all-btn {
            background-color: #67c23a;
            
            &:hover {
                background-color: #85ce61;
            }
        }
    }
    
    .file-list {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
        gap: 20px;
        margin-bottom: 30px;
        
        .file-item {
            border: 1px solid #ddd;
    border-radius: 8px;
            padding: 10px;
            background-color: #fff;
            
            .preview {
                width: 100%;
                height: 150px;
                margin-bottom: 10px;
                border-radius: 4px;
    overflow: hidden;
                background-color: #f5f5f5;
                
                img, video {
                    width: 100%;
                    height: 100%;
                    object-fit: cover;
                }
            }
            
            .file-info {
                margin-bottom: 10px;
                
                p {
                    margin: 5px 0;
                    font-size: 14px;
                    color: #666;
                    
                    &.file-size {
        color: #999;
                        font-size: 12px;
                    }
                }
            }
            
            .delete-btn {
                width: 100%;
                padding: 8px;
                background-color: #f56c6c;
                color: #fff;
                border: none;
                border-radius: 4px;
                font-size: 14px;
                cursor: pointer;
                
                &:hover {
                    background-color: #f78989;
                }
            }
        }
    }
    
    .upload-result {
        margin-top: 30px;
        padding: 20px;
        background-color: #f5f5f5;
        border-radius: 8px;
        
        h3 {
            margin-bottom: 10px;
            font-size: 18px;
            color: #333;
        }
        
        pre {
            background-color: #fff;
            padding: 15px;
            border-radius: 4px;
            overflow-x: auto;
            font-size: 12px;
            line-height: 1.5;
        }
    }
}
</style>
