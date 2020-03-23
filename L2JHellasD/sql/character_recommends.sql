SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `character_recommends`
-- ---------------------------

CREATE TABLE `character_recommends` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `target_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`char_id`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';