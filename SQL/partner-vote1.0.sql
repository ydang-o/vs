-- MySQL dump 10.13  Distrib 8.3.0, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: partner_vote_db
-- ------------------------------------------------------
-- Server version	8.3.0

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `partner`
--

DROP TABLE IF EXISTS `partner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `partner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '合伙人ID',
  `user_id` bigint NOT NULL COMMENT '关联用户ID',
  `level` tinyint NOT NULL COMMENT '合伙人层级：1总部一级 2总部二级 3分部一级 4分部二级',
  `invest_ratio` decimal(5,2) NOT NULL COMMENT '出资比例（%）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1正常 0禁用',
  `bind_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关联时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`),
  CONSTRAINT `fk_partner_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COMMENT='合伙人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `partner`
--

LOCK TABLES `partner` WRITE;
/*!40000 ALTER TABLE `partner` DISABLE KEYS */;
INSERT INTO `partner` VALUES (1,1,1,30.00,1,'2025-12-29 21:11:36'),(2,2,2,20.00,1,'2025-12-29 21:11:36'),(3,3,4,10.00,1,'2025-12-29 21:11:36');
/*!40000 ALTER TABLE `partner` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `proposal`
--

DROP TABLE IF EXISTS `proposal`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `proposal` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '议案ID',
  `title` varchar(200) NOT NULL COMMENT '议案标题',
  `proposal_no` varchar(50) NOT NULL COMMENT '议案编号',
  `content` text NOT NULL COMMENT '议案正文',
  `status` tinyint NOT NULL COMMENT '状态：0草稿 1待发布 2已发布 3已关联投票 4已结束',
  `creator_id` bigint NOT NULL COMMENT '创建人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_proposal_no` (`proposal_no`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COMMENT='议案表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `proposal`
--

LOCK TABLES `proposal` WRITE;
/*!40000 ALTER TABLE `proposal` DISABLE KEYS */;
INSERT INTO `proposal` VALUES (1,'关于新项目投资的议案','PA-2025-001','议案正文内容',2,1,'2025-12-29 21:11:36',NULL);
/*!40000 ALTER TABLE `proposal` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '登录用户名',
  `real_name` varchar(50) NOT NULL COMMENT '姓名',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `password` varchar(255) NOT NULL COMMENT '登录密码（加密）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1正常 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COMMENT='系统用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'zhangsan','张三','13800000001','xxx',1,'2025-12-29 21:11:36',NULL),(2,'lisi','李四','13800000002','xxx',1,'2025-12-29 21:11:36',NULL),(3,'wangwu','王五','13800000003','xxx',1,'2025-12-29 21:11:36',NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vote_delegate`
--

DROP TABLE IF EXISTS `vote_delegate`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote_delegate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '委托ID',
  `vote_task_id` bigint NOT NULL COMMENT '投票任务ID',
  `from_partner_id` bigint NOT NULL COMMENT '委托人ID',
  `to_partner_id` bigint NOT NULL COMMENT '被委托人ID',
  `proof_file` varchar(255) DEFAULT NULL COMMENT '委托证明文件路径',
  `status` tinyint DEFAULT '1' COMMENT '状态：1有效 0无效',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '委托时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_delegate` (`vote_task_id`,`from_partner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COMMENT='委托投票表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vote_delegate`
--

LOCK TABLES `vote_delegate` WRITE;
/*!40000 ALTER TABLE `vote_delegate` DISABLE KEYS */;
INSERT INTO `vote_delegate` VALUES (1,1,3,1,'/upload/delegate_001.pdf',1,'2025-12-29 21:11:36');
/*!40000 ALTER TABLE `vote_delegate` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vote_record`
--

DROP TABLE IF EXISTS `vote_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '投票记录ID',
  `vote_task_id` bigint NOT NULL COMMENT '投票任务ID',
  `partner_id` bigint NOT NULL COMMENT '投票人合伙人ID',
  `vote_option` tinyint NOT NULL COMMENT '投票选项：1同意 2反对 3弃权',
  `vote_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '投票时间',
  `is_delegate` tinyint DEFAULT '0' COMMENT '是否委托投票：0否 1是',
  `delegate_partner_id` bigint DEFAULT NULL COMMENT '被委托人ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COMMENT='投票记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vote_record`
--

LOCK TABLES `vote_record` WRITE;
/*!40000 ALTER TABLE `vote_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `vote_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vote_task`
--

DROP TABLE IF EXISTS `vote_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '投票任务ID',
  `proposal_id` bigint NOT NULL COMMENT '关联议案ID',
  `start_time` datetime NOT NULL COMMENT '投票开始时间',
  `end_time` datetime NOT NULL COMMENT '投票截止时间',
  `vote_type` tinyint NOT NULL COMMENT '投票方式：1匿名 2实名',
  `vote_strategy` tinyint NOT NULL COMMENT '投票策略：1人数票 2出资票 3组合',
  `pass_rate` decimal(5,2) DEFAULT '50.00' COMMENT '通过阈值%',
  `status` tinyint DEFAULT '1' COMMENT '状态：1投票中 2已结束',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `fk_vote_task_proposal` (`proposal_id`),
  CONSTRAINT `fk_vote_task_proposal` FOREIGN KEY (`proposal_id`) REFERENCES `proposal` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3 COMMENT='投票任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vote_task`
--

LOCK TABLES `vote_task` WRITE;
/*!40000 ALTER TABLE `vote_task` DISABLE KEYS */;
INSERT INTO `vote_task` VALUES (1,1,'2025-12-29 21:11:36','2025-12-31 21:11:36',2,3,50.00,1,'2025-12-29 21:11:36');
/*!40000 ALTER TABLE `vote_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vote_task_partner`
--

DROP TABLE IF EXISTS `vote_task_partner`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vote_task_partner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `vote_task_id` bigint NOT NULL COMMENT '投票任务ID',
  `partner_id` bigint NOT NULL COMMENT '合伙人ID',
  `level_weight` int NOT NULL COMMENT '层级票权（人数票用）',
  `invest_ratio` decimal(5,2) NOT NULL COMMENT '出资比例（出资票用）',
  `has_voted` tinyint DEFAULT '0' COMMENT '是否已投票：0否 1是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_partner` (`vote_task_id`,`partner_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3 COMMENT='投票任务参与人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vote_task_partner`
--

LOCK TABLES `vote_task_partner` WRITE;
/*!40000 ALTER TABLE `vote_task_partner` DISABLE KEYS */;
INSERT INTO `vote_task_partner` VALUES (1,1,1,4,30.00,0),(2,1,2,3,20.00,0),(3,1,3,1,10.00,0);
/*!40000 ALTER TABLE `vote_task_partner` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-29 22:22:08
