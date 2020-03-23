SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `olympiad_nobles`
-- ----------------------------

CREATE TABLE IF NOT EXISTS `olympiad_nobles` (
  `char_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `class_id` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `olympiad_points` int(10) NOT NULL DEFAULT 0,
  `competitions_done` smallint(3) NOT NULL DEFAULT 0,
  `competitions_won` smallint(3) NOT NULL DEFAULT 0,
  `competitions_lost` smallint(3) NOT NULL DEFAULT 0,
  `competitions_drawn` smallint(3) NOT NULL DEFAULT 0,
  PRIMARY KEY (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';