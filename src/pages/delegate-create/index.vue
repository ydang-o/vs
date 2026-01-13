<template>
  <view class="container">
    <view class="form-card">
      <view class="form-item">
        <text class="label">选择受托人</text>
        <picker 
          @change="handlePartnerChange" 
          :value="partnerIndex" 
          :range="partnerList" 
          range-key="name"
        >
          <view class="picker-value">
            {{ partnerList[partnerIndex] ? partnerList[partnerIndex].name : '请选择合伙人' }}
            <text class="arrow">></text>
          </view>
        </picker>
      </view>

      <view class="form-item vertical">
        <text class="label">委托证明材料</text>
        <view class="upload-area" @click="chooseImage">
          <image 
            v-if="proofFile" 
            :src="proofFile" 
            mode="aspectFit" 
            class="preview-image"
          ></image>
          <view v-else class="upload-placeholder">
            <text class="plus">+</text>
            <text class="tip">点击上传证明图片</text>
          </view>
        </view>
      </view>
    </view>

    <view class="btn-area">
      <button class="submit-btn" @click="handleSubmit" :loading="submitting">提交委托</button>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const voteTaskId = ref(null)
const partnerList = ref([])
const partnerIndex = ref(-1)
const proofFile = ref('')
const submitting = ref(false)
const uploadedPath = ref('') // Server path

onLoad((options) => {
  if (options.taskId) {
    voteTaskId.value = options.taskId
    fetchPartners()
  }
})

const fetchPartners = () => {
  request({
    url: '/user/partner/list',
    method: 'GET',
    data: {
      status: 1 // Only active partners
    }
  }).then(res => {
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      let list = []
      if (Array.isArray(res.data)) {
        list = res.data
      } else if (res.data && Array.isArray(res.data.list)) {
        list = res.data.list
      } else if (res.data && Array.isArray(res.data.records)) {
        list = res.data.records
      }
      
      partnerList.value = list.map(p => ({
        id: p.id,
        name: p.name,
        avatar: p.avatar
      })).filter(p => p.id !== uni.getStorageSync('userInfo').id)
    }
  }).catch(err => {
    console.error('Fetch partners failed', err)
    uni.showToast({ title: '获取合伙人列表失败', icon: 'none' })
  })
}

const handlePartnerChange = (e) => {
  partnerIndex.value = e.detail.value
}

const chooseImage = () => {
  uni.chooseImage({
    count: 1,
    success: (res) => {
      const tempFilePath = res.tempFilePaths[0]
      proofFile.value = tempFilePath
      uploadFile(tempFilePath)
    }
  })
}

const uploadFile = (filePath) => {
  uni.showLoading({ title: '上传中...' })
  const token = uni.getStorageSync('token')
  
  uni.uploadFile({
    url: 'http://119.29.249.72:8080/user/file/upload', 
    filePath: filePath,
    name: 'file',
    header: {
      'authentication': token
    },
    success: (uploadRes) => {
      uni.hideLoading()
      console.log('Upload raw response:', uploadRes.data)
      try {
        const res = JSON.parse(uploadRes.data)
        if (res.code === 0 || res.code === 1 || res.code === 200) {
          // Handle Map<String, String> response
          let path = ''
          const data = res.data
          
          if (typeof data === 'string') {
            path = data
          } else if (data && typeof data === 'object') {
             // Try common keys first
             if (data.path) path = data.path
             else if (data.url) path = data.url
             else if (data.filePath) path = data.filePath
             else if (data.file) path = data.file
             else {
               // Take the first string value found
               const values = Object.values(data)
               if (values.length > 0 && typeof values[0] === 'string') {
                 path = values[0]
               }
             }
          }
          
          if (path) {
            uploadedPath.value = path
            uni.showToast({ title: '上传成功' })
          } else {
             uploadedPath.value = ''
             console.error('Path extraction failed:', res)
             uni.showToast({ title: '无法获取文件路径', icon: 'none' })
          }
        } else {
          uni.showToast({ title: '上传失败: ' + (res.msg || '未知错误'), icon: 'none' })
        }
      } catch (e) {
        console.error('Parse upload response failed', e)
        uni.showToast({ title: '上传响应解析失败', icon: 'none' })
      }
    },
    fail: (err) => {
      uni.hideLoading()
      console.error('Upload failed', err)
      uni.showToast({ title: '上传请求失败', icon: 'none' })
    }
  })
}

const handleSubmit = () => {
  if (partnerIndex.value < 0) {
    uni.showToast({ title: '请选择受托人', icon: 'none' })
    return
  }
  if (!uploadedPath.value) {
    uni.showToast({ title: '请上传证明材料', icon: 'none' })
    return
  }

  submitting.value = true
  const selectedPartner = partnerList.value[partnerIndex.value]
  const userInfo = uni.getStorageSync('userInfo')

  request({
    url: '/user/vote/task/delegate/create',
    method: 'POST',
    data: {
      voteTaskId: Number(voteTaskId.value),
      toPartnerId: Number(selectedPartner.id),
      fromPartnerId: Number(userInfo.id), 
      proofFile: uploadedPath.value
    }
  }).then(res => {
    submitting.value = false
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      uni.showToast({ title: '委托提交成功' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.msg || '提交失败', icon: 'none' })
    }
  }).catch(err => {
    submitting.value = false
    console.error(err)
    uni.showToast({ title: '提交请求失败', icon: 'none' })
  })
}
</script>

<style>
.container {
  padding: 30rpx;
  background-color: #F8F9FA;
  min-height: 100vh;
}

.form-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 0 30rpx;
  margin-bottom: 40rpx;
}

.form-item {
  display: flex;
  align-items: center;
  padding: 30rpx 0;
  border-bottom: 1px solid #eee;
}

.form-item:last-child {
  border-bottom: none;
}

.form-item.vertical {
  flex-direction: column;
  align-items: flex-start;
  gap: 20rpx;
}

.label {
  font-size: 30rpx;
  color: #333;
  width: 200rpx;
}

.picker-value {
  flex: 1;
  font-size: 30rpx;
  color: #666;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 450rpx; /* Approximate width */
}

.arrow {
  color: #999;
}

.upload-area {
  width: 100%;
  height: 300rpx;
  background-color: #F3F4F6;
  border-radius: 12rpx;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #999;
}

.plus {
  font-size: 60rpx;
  margin-bottom: 10rpx;
}

.tip {
  font-size: 24rpx;
}

.preview-image {
  width: 100%;
  height: 100%;
}

.submit-btn {
  background-color: #3B82F6;
  color: white;
  border-radius: 40rpx;
  font-size: 32rpx;
}
</style>
