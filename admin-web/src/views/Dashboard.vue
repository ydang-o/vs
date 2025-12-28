<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>总用户数</span>
            </div>
          </template>
          <div class="card-value">120</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>合伙人数</span>
            </div>
          </template>
          <div class="card-value">45</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>进行中投票</span>
            </div>
          </template>
          <div class="card-value text-primary">3</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>累计议案</span>
            </div>
          </template>
          <div class="card-value">28</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-card class="mt-4" header="最近活动">
      <el-empty description="暂无最近活动数据" />
    </el-card>
  </div>
</template>

<style scoped>
.card-header {
  font-size: 14px;
  color: #6B7280;
}
.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #1F2937;
}
.text-primary {
  color: #3B82F6;
}
.mt-4 {
  margin-top: 24px;
}
</style>
