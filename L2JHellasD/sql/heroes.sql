SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `heroes`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `heroes` (
  `char_id` decimal(11,0) NOT NULL DEFAULT '0',
  `class_id` decimal(3,0) NOT NULL DEFAULT '0',
  `count` decimal(3,0) NOT NULL DEFAULT '0',
  `played` decimal(1,0) NOT NULL DEFAULT '0',
  `active` tinyint NOT NULL DEFAULT 0,
  `message` varchar(300) NOT NULL DEFAULT '',
  PRIMARY KEY  (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';