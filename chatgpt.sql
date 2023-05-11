/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 80033
Source Host           : localhost:3306
Source Database       : chatgpt

Target Server Type    : MYSQL
Target Server Version : 80033
File Encoding         : 65001

Date: 2023-05-11 15:56:35
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for chatgpt
-- ----------------------------
DROP TABLE IF EXISTS `chatgpt`;
CREATE TABLE `chatgpt` (
  `id` int NOT NULL AUTO_INCREMENT,
  `content` longtext,
  `chatgpt` longtext,
  `external_userid` varchar(255) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `duration` int DEFAULT NULL,
  `token` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `external_userid` (`external_userid`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=495 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of chatgpt
-- ----------------------------

-- ----------------------------
-- Table structure for cursor
-- ----------------------------
DROP TABLE IF EXISTS `cursor`;
CREATE TABLE `cursor` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cursor` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci DEFAULT NULL,
  `token` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of cursor
-- ----------------------------
INSERT INTO `cursor` VALUES ('1', '', '');

-- ----------------------------
-- Table structure for knowledge
-- ----------------------------
DROP TABLE IF EXISTS `knowledge`;
CREATE TABLE `knowledge` (
  `id` int NOT NULL AUTO_INCREMENT,
  `knowledge` longtext,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of knowledge
-- ----------------------------
INSERT INTO `knowledge` VALUES ('1', '');
