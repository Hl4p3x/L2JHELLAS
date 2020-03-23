SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `heroes_diary`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `heroes_diary` (
  `char_id` int(10) unsigned NOT NULL,
  `time` bigint(13) unsigned NOT NULL DEFAULT '0',
  `action` tinyint(2) unsigned NOT NULL DEFAULT '0',
  `param` int(11) unsigned NOT NULL DEFAULT '0',
  KEY `char_id` (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';