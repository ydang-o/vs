// utils/mockData.js

const users = [
  {
    id: 1,
    username: 'admin',
    password: '123',
    name: '系统管理员',
    role: 'admin',
    status: 'normal',
    phone: '13800000000'
  },
  {
    id: 2,
    username: 'partner1',
    password: '123',
    name: '张三',
    role: 'partner',
    status: 'normal',
    phone: '13900000001',
    level: '总部一级',
    shareRatio: 10.00
  },
  {
    id: 3,
    username: 'partner2',
    password: '123',
    name: '李四',
    role: 'partner',
    status: 'normal',
    phone: '13900000002',
    level: '分部一级',
    shareRatio: 5.00
  }
];

const tasks = [
  {
    id: 101,
    title: '关于2026年第一季度战略规划的议案',
    status: 'published', // published, ended
    deadline: '2025-12-30 18:00:00',
    type: '实名',
    strategy: '人数票+出资票',
    content: '这里是议案的详细内容...',
    votedCount: 5,
    totalCount: 10
  },
  {
    id: 102,
    title: '关于新增合伙人的议案',
    status: 'ended',
    deadline: '2025-11-01 12:00:00',
    type: '匿名',
    strategy: '人数票',
    content: '详细内容...',
    votedCount: 10,
    totalCount: 10
  }
];

module.exports = {
  users,
  tasks
};
