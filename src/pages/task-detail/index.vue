<template>
  <view class="container">
    <view class="detail-card" v-if="task && task.proposal">
      <view class="header">
        <text class="title">{{ task.proposal.title }}</text>
        <text class="delegate-hint text-orange" v-if="task.voteTask && task.voteTask.delegateeName">
          (您已委托 {{ task.voteTask.delegateeName }} 投票)
        </text>
        <text class="delegate-hint text-blue" v-if="task.voteTask && task.voteTask.delegatorName">
          ({{ task.voteTask.delegatorName }} 委托您投票)
        </text>
        <view class="meta">
          <text>截止时间：{{ task.voteTask.endTime }}</text>
        </view>
      </view>
      
      <view class="content">
        <text class="desc">{{ task.proposal.content || '暂无详细描述' }}</text>
        <image 
          v-if="task.proposal.imageUrl" 
          class="detail-image" 
          :src="task.proposal.imageUrl" 
          mode="widthFix" 
          @click="previewImage(task.proposal.imageUrl)"
        ></image>
      </view>

      <!-- Statistics Area for All Users -->
      <view class="stats-card" v-if="statistics">
        <view class="stat-row main-stat">
          <view class="stat-item">
            <text class="stat-num">{{ statistics.voted }}</text>
            <text class="stat-label">已投票</text>
          </view>
          <view class="stat-divider"></view>
          <view class="stat-item">
            <text class="stat-num">{{ statistics.unvoted }}</text>
            <text class="stat-label">未投票</text>
          </view>
        </view>
        <view class="stat-row sub-stat">
          <view class="stat-item">
            <text class="stat-val text-green">{{ statistics.agree }}</text>
            <text class="stat-label">同意</text>
          </view>
          <view class="stat-item">
            <text class="stat-val text-red">{{ statistics.reject }}</text>
            <text class="stat-label">反对</text>
          </view>
          <view class="stat-item">
            <text class="stat-val text-gray">{{ statistics.abstain }}</text>
            <text class="stat-label">弃权</text>
          </view>
        </view>
      </view>
      
      <view class="action-area">
        <view class="vote-area" v-if="task.voteTask.canVote && !isExpired">
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

        <!-- <view class="delegate-area" v-if="task.voteTask.canDelegateVote && !isExpired">
           <view class="section-title">委托投票</view>
           <button class="delegate-btn" @click="handleDelegateVote">处理委托投票</button>
        </view> -->
        
        <view class="result-area" v-if="task.voteTask.hasVoted || isExpired">
          <text class="result-text" v-if="task.voteTask.hasVoted">您已完成投票</text>
          <text class="result-text" v-else-if="isExpired">投票已截止</text>
          <button class="view-result-btn" @click="viewResult">查看结果</button>
        </view>

        <!-- Admin View: Voter Details -->
        <view class="admin-area" v-if="isAdmin && task.voteTask.voterList && task.voteTask.voterList.length > 0">
          <view class="section-title">投票人详情 (管理员可见)</view>
          <view class="voter-list">
            <view class="voter-header">
              <text class="th">姓名</text>
              <text class="th">状态</text>
            </view>
            <view class="voter-item" v-for="(voter, idx) in task.voteTask.voterList" :key="idx">
              <text class="voter-name">{{ voter.realName || voter.name || voter.username || '未知' }}</text>
              <text class="voter-status" :class="getVoteColorClass(voter.voteOption)">
                {{ getVoteText(voter.voteOption) }}
              </text>
            </view>
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { onLoad, onPullDownRefresh } from '@dcloudio/uni-app'
import request from '@/utils/request.js'

const task = ref(null)
const taskId = ref(null)
const isAdmin = ref(false)

const statistics = computed(() => {
  if (!task.value || !task.value.voteTask) return null
  
  const vt = task.value.voteTask
  
  let agree = vt.agreeCount || 0
  let reject = vt.opposeCount || vt.rejectCount || 0
  let abstain = vt.abstainCount || vt.waiverCount || 0
  
  // Calculate from list if explicit counts are missing
  if (agree === 0 && reject === 0 && abstain === 0 && vt.voterList && vt.voterList.length > 0) {
    vt.voterList.forEach(v => {
      if (v.voteOption === 1) agree++
      else if (v.voteOption === 2) reject++
      else if (v.voteOption === 3) abstain++
    })
  }
  
  const total = vt.totalCount || 0
  const voted = vt.votedCount || (agree + reject + abstain)
  const unvoted = total > voted ? total - voted : 0
  
  return {
    agree,
    reject,
    abstain,
    voted,
    unvoted,
    total
  }
})

const isExpired = computed(() => {
  if (!task.value || !task.value.voteTask) return false
  const timeStr = task.value.voteTask.endTime
  if (!timeStr || timeStr === '进行中') return false
  try {
    const target = new Date(timeStr.replace(/-/g, '/')).getTime()
    return Date.now() > target
  } catch (e) {
    return false
  }
})

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
    const userInfo = uni.getStorageSync('userInfo')
    // As per user requirement: Only 'admin' username can see admin view
    if (userInfo && userInfo.username === 'admin') {
      isAdmin.value = true
    }
    fetchDetail(options.id)
  }
})

onPullDownRefresh(() => {
  if (taskId.value) {
    fetchDetail(taskId.value)
  } else {
    uni.stopPullDownRefresh()
  }
})

const fetchDetail = (id) => {
  const token = uni.getStorageSync('token')
  if (!token) {
    uni.reLaunch({ url: '/pages/login/index' })
    return
  }

  // Use the same endpoint for both Admin and User
  // Admin and User: /user/h5/vote/task/detail/{id} (id is VoteTask ID)
  const url = `/user/h5/vote/task/detail/${id}`

  request({
    url: url,
    method: 'GET'
  }).then(res => {
    uni.stopPullDownRefresh()
    console.log(`Fetching detail from endpoint:`, url)
    
    if (res.code === 0 || res.code === 1 || res.code === 200) {
      const payload = res.data || {}
      const BASE_URL = 'http://119.29.249.72:8080'
      const resolveImageUrl = (path) => {
        if (!path) return ''
        // If it contains the view controller, it's likely correct (check if needs base url)
        if (path.indexOf('/user/file/view') !== -1) {
             if (path.startsWith('http')) return path
             return BASE_URL + path
        }
        
        // If it starts with http (full URL) but missing controller
        if (path.startsWith('http')) {
             // If it matches our BASE_URL, strip it and fix
             if (path.startsWith(BASE_URL)) {
                 const relative = path.replace(BASE_URL, '')
                 return `${BASE_URL}/user/file/view?filePath=${relative}`
             }
             // External URL? Return as is
             return path
        }
        
        // Relative path, add base url and controller
        return `${BASE_URL}/user/file/view?filePath=${path}`
      }
      
      // Unified Response Mapping
      // Check if it's the new flat structure
      const isFlatStructure = payload.voteTaskId !== undefined && payload.title !== undefined
      
      if (isFlatStructure) {
          const stats = payload.voteStats || {}
          task.value = {
              proposal: {
                  title: payload.title,
                  content: payload.content || '暂无详细描述',
                  imageUrl: resolveImageUrl(payload.imageUrl)
              },
              voteTask: {
                  voteTaskId: payload.voteTaskId,
                  endTime: payload.endTime || '进行中',
                  canVote: payload.canVote,
                  canDelegateVote: payload.canDelegateVote,
                  hasVoted: payload.hasVoted,
                  voterList: payload.voteRecords || [],
                  // Statistics
                  agreeCount: stats.agreeCount || 0,
                  rejectCount: stats.rejectCount || 0,
                  abstainCount: stats.abstainCount || 0,
                  votedCount: stats.votedCount || 0,
                  totalCount: stats.partnerCount || 0
              }
          }
      } else {
          // Old structure fallback or other variations
          const proposalData = payload.proposal || payload.Proposal
          const voteTaskData = payload.voteTask || payload.VoteTask
    
          task.value = {
            proposal: {
              title: proposalData ? proposalData.title : (payload.title || ''),
              content: proposalData ? (proposalData.content || '暂无详细描述') : (payload.content || '暂无详细描述'),
              imageUrl: resolveImageUrl(proposalData ? proposalData.imageUrl : payload.imageUrl)
            },
            voteTask: {
              voteTaskId: voteTaskData ? voteTaskData.voteTaskId : id,
              endTime: voteTaskData ? (voteTaskData.endTime || '进行中') : (payload.endTime || '进行中'),
              // Relaxed status check: allow if status is 1 or undefined (backward compatibility)
              canVote: voteTaskData ? ((voteTaskData.status === 1 || voteTaskData.status === undefined) && !voteTaskData.hasVoted) : (payload.canVote),
              canDelegateVote: voteTaskData ? voteTaskData.canDelegateVote : (payload.canDelegateVote),
              hasVoted: voteTaskData ? voteTaskData.hasVoted : (payload.hasVoted),
              votedCount: voteTaskData ? (voteTaskData.votedCount || 0) : 0,
              totalCount: voteTaskData ? (voteTaskData.totalCount || 0) : 0,
              voterList: voteTaskData ? (voteTaskData.voterList || []) : [],
              delegateeName: voteTaskData ? voteTaskData.delegateeName : payload.delegateeName,
              delegatorName: voteTaskData ? voteTaskData.delegatorName : payload.delegatorName,
              isProxy: (voteTaskData && voteTaskData.isProxy !== undefined) ? voteTaskData.isProxy : (payload.isProxy || 0),
              fromPartnerId: (voteTaskData && voteTaskData.fromPartnerId) ? voteTaskData.fromPartnerId : (payload.fromPartnerId || payload.delegatorPartnerId)
            }
          }
      }
      
      // If admin, fetch additional stats details (still needed for detailed voter list if not present in main detail)
      if (isAdmin.value) {
        fetchAdminStats(id)
      }
    }
  }).catch(err => {
    uni.stopPullDownRefresh()
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

const fetchAdminStats = (id) => {
  request({
    url: `/user/h5/vote/task/detail/${id}`,
    method: 'GET'
  }).then(res => {
    if (res.code === 0 || res.code === 1 || res.code === 200) {
       const data = res.data || {}
       if (task.value && task.value.voteTask) {
         // Merge voter list from admin endpoint
         if (data.voteRecords) {
           task.value.voteTask.voterList = data.voteRecords
         }
         // Merge stats
         const stats = data.voteStats
         if (stats) {
           task.value.voteTask.agreeCount = stats.agreeCount || 0
           task.value.voteTask.rejectCount = stats.rejectCount || 0
           task.value.voteTask.abstainCount = stats.abstainCount || 0
           task.value.voteTask.votedCount = stats.votedCount || 0
           task.value.voteTask.totalCount = stats.partnerCount || 0
         }
       }
    }
  }).catch(err => {
    console.error('Failed to fetch admin stats:', err)
  })
}

const getVoteText = (option) => {
  // If option is already a string (e.g. "同意"), return it
  if (typeof option === 'string' && option) return option
  // If option is null/undefined/empty string, it means not voted
  if (!option) return '未投票'
  
  const map = {
    1: '同意',
    2: '反对',
    3: '弃权'
  }
  return map[option] || '未知'
}

const getVoteColorClass = (option) => {
  // Handle string options
  if (typeof option === 'string') {
    if (option === '同意') return 'text-green'
    if (option === '反对') return 'text-red'
    if (option === '弃权') return 'text-gray'
    return 'text-gray' // Default/Unvoted
  }
  
  const map = {
    1: 'text-green',
    2: 'text-red',
    3: 'text-gray'
  }
  return map[option] || 'text-gray'
}

const previewImage = (url) => {
  if (!url) return
  uni.previewImage({
    urls: [url]
  })
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
    // Relaxed check logic aligned with delegate-create:
    // code=1: Success (even if data is null)
    // code=200: Success
    // code=0: Success ONLY if data exists (to filter out business errors with code=0 like "Already delegated")
    if (res.code === 1 || res.code === 200 || (res.code === 0 && res.data)) {
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

.stats-card {
  background-color: #F3F4F6;
  border-radius: 12rpx;
  padding: 20rpx;
  margin-top: 20rpx;
}

.stat-row {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.main-stat {
  padding-bottom: 20rpx;
  border-bottom: 1px solid #E5E7EB;
  margin-bottom: 20rpx;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}

.stat-divider {
  width: 1px;
  height: 60rpx;
  background-color: #D1D5DB;
}

.stat-num {
  font-size: 36rpx;
  font-weight: bold;
  color: #111827;
}

.stat-val {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 4rpx;
}

.stat-label {
  font-size: 24rpx;
  color: #6B7280;
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

.detail-image {
  width: 100%;
  margin-top: 20rpx;
  border-radius: 8rpx;
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

.admin-area {
  margin-top: 40rpx;
  padding-top: 30rpx;
  border-top: 1px solid #eee;
}

/* Admin List Styles */
.admin-list-container {
  margin-top: 24rpx;
  background-color: #fff;
  border-radius: 16rpx;
  padding: 24rpx;
  box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  height: 600rpx; /* Fixed height for scroll area */
}

.list-header {
  font-size: 32rpx;
  font-weight: bold;
  margin-bottom: 20rpx;
  color: #111827;
  flex-shrink: 0;
}

.list-content {
  flex: 1;
  overflow-y: auto;
}

/* Hide scrollbar for Chrome, Safari and Opera */
.list-content::-webkit-scrollbar {
  display: none;
}

/* Hide scrollbar for IE, Edge and Firefox */
.list-content {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}

.voter-item {
  display: flex;
  justify-content: space-between;
  padding: 16rpx 0;
  border-bottom: 1px dashed #E5E7EB;
  font-size: 28rpx;
}

.voter-item:last-child {
  border-bottom: none;
}

.voter-name {
  color: #374151;
}

.text-green { color: #10B981; }
.text-red { color: #EF4444; }
.text-gray { color: #9CA3AF; }
.text-orange { color: #F59E0B; }
.text-blue { color: #3B82F6; }
.delegate-hint {
  font-size: 26rpx;
  margin-bottom: 10rpx;
  display: block;
  font-weight: 500;
}
</style>
