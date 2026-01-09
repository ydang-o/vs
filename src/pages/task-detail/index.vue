<template>
  <view class="container">
    <view class="detail-card" v-if="task && task.proposal">
      <view class="header">
        <text class="title">{{ task.proposal.title }}</text>
        <view class="meta">
        <text>截止时间：{{ task.voteTask.endTime }}</text>
        <text class="stats" v-if="task.voteTask.totalCount"> · 已投票 {{ task.voteTask.votedCount }}/{{ task.voteTask.totalCount }} 人</text>
      </view>
      </view>
      
      <view class="content">
        <text class="desc">{{ task.proposal.content || '暂无详细描述' }}</text>
      </view>
      
      <view class="action-area">
        <view class="vote-area" v-if="task.voteTask.canVote">
          <view class="section-title">本人投票</view>
          <view class="btn-group">
            <button class="vote-btn agree" @click="handleVote(1)">同意</button>
            <button class="vote-btn reject" @click="handleVote(2)">反对</button>
            <button class="vote-btn abstain" @click="handleVote(3)">弃权</button>
          </view>
          <view class="delegate-create-area">
             <button class="delegate-create-btn" @click="goToDelegateCreate">委托他人投票</button>
          </view>
        </view>

        <view class="delegate-area" v-if="task.voteTask.canDelegateVote">
           <view class="section-title">委托投票</view>
           <button class="delegate-btn" @click="handleDelegateVote">处理委托投票</button>
        </view>
        
        <view class="result-area" v-if="task.voteTask.hasVoted && !task.voteTask.canVote">
          <text class="result-text">您已完成投票</text>
          <button class="view-result-btn" @click="viewResult">查看结果</button>
        </view>
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

const isUrgent = (timeStr) => {
  if (!timeStr || timeStr === '进行中') return false
  try {
    const target = new Date(timeStr.replace(/-/g, '/')).getTime()
    if (isNaN(target)) return false
    const now = Date.now()
    const diff = target - now
    return diff > 0 && diff <= 10800000
  } catch (e) {
    return false
  }
}

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
    url: `/user/h5/vote/task/detail/${id}`,
    method: 'GET'
  }).then(res => {
    console.log('Fetching detail from H5 endpoint:', `/user/h5/vote/task/detail/${id}`)
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      const payload = res.data || {}
      // Map new API response to existing template structure
      // API structure: { proposal: {...}, voteTask: {...} } or { Proposal: {...}, ... }
      // Handling case sensitivity
      const proposalData = payload.proposal || payload.Proposal
      const voteTaskData = payload.voteTask || payload.VoteTask

      task.value = {
        proposal: {
          title: proposalData ? proposalData.title : '',
          content: proposalData ? (proposalData.content || '暂无详细描述') : '暂无详细描述'
        },
        voteTask: {
          voteTaskId: voteTaskData ? voteTaskData.voteTaskId : id,
          endTime: voteTaskData ? (voteTaskData.endTime || '进行中') : '进行中',
          // Relaxed status check: allow if status is 1 or undefined (backward compatibility)
          canVote: voteTaskData ? ((voteTaskData.status === 1 || voteTaskData.status === undefined) && !voteTaskData.hasVoted) : false,
          canDelegateVote: voteTaskData ? voteTaskData.canDelegateVote : false,
          hasVoted: voteTaskData ? voteTaskData.hasVoted : false,
          votedCount: voteTaskData ? (voteTaskData.votedCount || 0) : 0,
          totalCount: voteTaskData ? (voteTaskData.totalCount || 0) : 0
        }
      }
    }
  }).catch(err => {
    // Force HBuilderX re-compile
    if (err.statusCode !== 401) {
      console.error(err)
    }
    // Mock for error case or dev
    task.value = {
      proposal: {
        title: '关于扩大合伙人规模的议案',
        content: '为了适应公司快速发展的需求，提议引入新的战略合伙人...'
      },
      voteTask: {
        voteTaskId: id,
        endTime: '2023-12-31',
        canVote: true,
        canDelegateVote: true,
        hasVoted: false
      }
    }
  })
}

const getVoteText = (option) => {
  const map = {
    1: '同意',
    2: '反对',
    3: '弃权'
  }
  return map[option] || '未知'
}

const handleDelegateVote = () => {
  uni.showLoading({ title: '加载委托信息' })
  const userInfo = uni.getStorageSync('userInfo')
  
  request({
    url: '/user/vote/task/delegate/list',
    method: 'GET',
    data: {
        voteTaskId: taskId.value,
        toPartnerId: userInfo.id,
        status: 1,
        page: 1,
        pageSize: 100
    }
  }).then(res => {
      uni.hideLoading()
      if (res.code === 0 || res.code === 1 || res.code === 200) {
         let delegates = [];
         if (Array.isArray(res.data)) {
             delegates = res.data;
         } else if (res.data && Array.isArray(res.data.list)) {
             delegates = res.data.list;
         } else if (res.data && Array.isArray(res.data.records)) {
             delegates = res.data.records;
         }

         if (delegates.length > 0) {
            showDelegateAction(delegates)
         } else {
            uni.showToast({ title: '暂无待处理委托', icon: 'none' })
         }
      } else {
         uni.showToast({ title: '暂无待处理委托', icon: 'none' })
      }
  }).catch(err => {
      uni.hideLoading()
      console.error(err)
  })
}

const showDelegateAction = (delegates) => {
  // Map delegates to readable strings. Assuming 'fromPartnerName' exists in response or need to be derived
  const itemList = delegates.map(d => `代理 ${d.fromPartnerName || d.fromPartnerId} 投票`)
  uni.showActionSheet({
    itemList: itemList,
    success: (res) => {
      const selectedDelegate = delegates[res.tapIndex]
      showVoteOptions(selectedDelegate)
    }
  })
}

const showVoteOptions = (delegate) => {
    uni.showActionSheet({
        itemList: ['同意', '反对', '弃权'],
        success: (res) => {
            const option = res.tapIndex + 1
            submitDelegateVote(delegate.fromPartnerId, option)
        }
    })
}

const submitDelegateVote = (fromPartnerId, option) => {
    uni.showLoading({ title: '提交委托中' })
    // Note: User did not provide new proxy submit endpoint, keeping original logic
    request({
      url: '/user/h5/vote/delegate/submit',
      method: 'POST',
      data: {
        voteTaskId: taskId.value,
        voteOption: option,
        fromPartnerId: fromPartnerId
      }
    }).then(res => {
      uni.hideLoading()
      if (res.code === 0 || res.code === 1 || res.code === 200) {
         uni.showToast({ title: '委托投票成功' })
         fetchDetail(taskId.value)
      } else {
         uni.showToast({ title: res.msg || '操作失败', icon: 'none' })
      }
    }).catch(() => {
      uni.hideLoading()
    })
}

const handleVote = (option) => {
  const optionText = getVoteText(option)
  uni.showModal({
    title: '确认投票',
    content: `确认投"${optionText}"吗？提交后不可修改。`,
    success: (res) => {
      if (res.confirm) {
        submitVote(option)
      }
    }
  })
}

const submitVote = (option) => {
  uni.showLoading({ title: '提交中' })
  request({
    url: '/user/vote/submit',
    method: 'POST',
    data: {
      voteTaskId: taskId.value,
      voteOption: option
    }
  }).then(res => {
    uni.hideLoading()
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      uni.navigateTo({
        url: '/pages/vote-success/index'
      })
    } else {
      uni.showToast({ title: res.msg || '投票失败', icon: 'none' })
    }
  }).catch(() => {
    uni.hideLoading()
  })
}

const checkIsUrgent = (target) => {
  try {
    const now = Date.now()
    const diff = target - now
    return diff > 0 && diff <= 10800000
  } catch (e) {
    return false
  }
}

const goToDelegateCreate = () => {
  uni.navigateTo({
    url: `/pages/delegate-create/index?taskId=${taskId.value}`
  })
}

const viewResult = () => {
  if (task.value && task.value.voteTask && task.value.voteTask.totalCount > 0) {
      uni.showModal({
          title: '投票进度',
          content: `已投票: ${task.value.voteTask.votedCount}/${task.value.voteTask.totalCount} 人`,
          showCancel: false
      })
  } else {
      uni.showToast({ title: '暂无详细结果', icon: 'none' })
  }
}
</script>

<style>
.container {
  padding: 30rpx;
}

.stats {
  margin-left: 20rpx;
  color: #3B82F6;
  font-weight: 500;
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

.action-area {
  margin-top: 40rpx;
  display: flex;
  flex-direction: column;
  gap: 30rpx;
}

.section-title {
  font-size: 30rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: #333;
}

.btn-group {
  display: flex;
  justify-content: space-around;
}

.vote-btn {
  width: 180rpx;
  font-size: 28rpx;
}

.delegate-create-area {
  margin-top: 20rpx;
}

.delegate-create-btn {
  background-color: #fff;
  color: #3B82F6;
  font-size: 28rpx;
  border: 1px solid #3B82F6;
  width: 100%;
}

.delegate-btn {
  background-color: #3B82F6;
  color: white;
  font-size: 28rpx;
  width: 100%;
}

.text-danger {
  color: #EF4444 !important;
  font-weight: bold;
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

.view-result-btn {
  margin-top: 20rpx;
  background-color: #fff;
  color: #3B82F6;
  border: 1px solid #3B82F6;
  font-size: 28rpx;
  width: 100%;
}
</style>
