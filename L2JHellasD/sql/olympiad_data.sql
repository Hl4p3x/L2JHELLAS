SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `olympiad_data`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `olympiad_data` (
  `id` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `current_cycle` MEDIUMINT UNSIGNED NOT NULL DEFAULT 1,
  `period` MEDIUMINT UNSIGNED NOT NULL DEFAULT 0,
  `olympiad_end` bigint(13) unsigned NOT NULL DEFAULT '0',
  `validation_end` bigint(13) unsigned NOT NULL DEFAULT '0',
  `next_weekly_change` bigint(13) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';