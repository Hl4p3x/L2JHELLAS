SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `forums`
-- ----------------------------

CREATE TABLE `forums` (
  `forum_id` int(8) NOT NULL AUTO_INCREMENT,
  `forum_name` varchar(255) NOT NULL DEFAULT '',
  `forum_parent` int(8) NOT NULL DEFAULT '0',
  `forum_post` int(8) NOT NULL DEFAULT '0',
  `forum_type` int(8) NOT NULL DEFAULT '0',
  `forum_perm` int(8) NOT NULL DEFAULT '0',
  `forum_owner_id` int(8) NOT NULL DEFAULT '0',
  UNIQUE KEY `forum_id` (`forum_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

-- ----------------------------
-- Records of `forums`
-- ----------------------------8/
INSERT IGNORE INTO `forums` VALUES
(1, 'NormalRoot', 0, 0, 0, 1, 0),
(2, 'ClanRoot', 0, 0, 0, 0, 0),
(3, 'MemoRoot', 0, 0, 0, 0, 0),
(4, 'MailRoot', 0, 0, 0, 0, 0);