SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `grandboss_list`
-- ----------------------------

CREATE TABLE `grandboss_list` (
  `player_id` decimal(11,0) NOT NULL,
  `zone` decimal(11,0) NOT NULL,
  PRIMARY KEY (`player_id`,`zone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';