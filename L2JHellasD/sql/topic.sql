SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `topic`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `topic` (
  `topic_id` int(8) NOT NULL DEFAULT '0',
  `topic_forum_id` int(8) NOT NULL DEFAULT '0',
  `topic_name` varchar(255) NOT NULL DEFAULT '',
  `topic_date` decimal(20,0) NOT NULL DEFAULT '0',
  `topic_ownername` varchar(255) NOT NULL DEFAULT '0',
  `topic_ownerid` int(8) NOT NULL DEFAULT '0',
  `topic_type` int(8) NOT NULL DEFAULT '0',
  `topic_reply` int(8) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';