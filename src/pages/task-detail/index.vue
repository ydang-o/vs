<template>
  <view class="container">
    <view class="detail-card" v-if="task">
      <view class="header">
        <text class="title">{{ task.title }}</text>
        <view class="meta">
          <text>截止时间：{{ task.endTime }}</text>
        </view>
      </view>
      
      <view class="content">
        <text class="desc">{{ task.description || '暂无详细描述' }}</text>
      </view>
      
      <view class="vote-area" v-if="task.status === 'active'">
        <button class="vote-btn agree" @click="handleVote(1)">同意</button>
        <button class="vote-btn reject" @click="handleVote(2)">反对</button>
        <button class="vote-btn abstain" @click="handleVote(3)">弃权</button>
      </view>
      
      <view class="result-area" v-else>
        <text class="result-text">投票已结束</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const task = ref(null)
const taskId = ref(null)

onLoad((options) => {
  if (options.id) {
    taskId.value = options.id
    fetchDetail(options.id)
  }
})

const fetchDetail = (id) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }

  request({
    url: `/api/vote/task/detail/${id}`,
    method: 'GET'
  }).then(res => {
    if (res.code === 1 || res.code === 200) {
      task.value = res.data
    }
  }).catch(err => {
    if (err.statusCode !== 401) {
      console.error(err)
    }
    // Mock
    task.value = {
      id: id,
      title: '关于扩大合伙人规模的议案',
      description: '为了适应公司快速发展的需求，提议引入新的战略合伙人...',
      endTime: '2023-12-31',
      status: 'active'
    }
  })
}

const handleVote = (option) => {
  uni.showLoading({ title: '提交中' })
  
  request({
    url: '/api/vote/submit',
    method: 'POST',
    data: {
      voteTaskId: taskId.value,
      voteOption: option
    }
  }).then(res => {
    uni.hideLoading()
    if (res.code === 1 || res.code === 200) {
      uni.showToast({ title: '投票成功' })
      setTimeout(() => {
        uni.navigateBack()
      }, 1500)
    } else {
      uni.showToast({ title: res.msg || '投票失败', icon: 'none' })
    }
  }).catch(() => {
    uni.hideLoading()
  })
}
</script>

<style>
.container {
  padding: 30rpx;
}

.detail-card {
  background-color: #fff;
  border-radius: 16rpx;
  padding: 40rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
}

.title {
  font-size: 36rpx;
  font-weight: bold;
  color: #111;
  margin-bottom: 20rpx;
  display: block;
}

.meta {
  color: #666;
  font-size: 24rpx;
  padding-bottom: 30rpx;
  border-bottom: 1px solid #eee;
}

.content {
  padding: 40rpx 0;
  min-height: 200rpx;
}

.desc {
  font-size: 28rpx;
  color: #333;
  line-height: 1.6;
}

.vote-area {
  display: flex;
  justify-content: space-around;
  margin-top: 40rpx;
}

.vote-btn {
  width: 180rpx;
  font-size: 28rpx;
}

.agree {
  background-color: #10B981;
  color: white;
}

.reject {
  background-color: #EF4444;
  color: white;
}

.abstain {
  background-color: #9CA3AF;
  color: white;
}
</style>
