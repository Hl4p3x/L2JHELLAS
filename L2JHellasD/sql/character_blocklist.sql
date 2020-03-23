SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `character_blocklist`
-- ---------------------------

CREATE TABLE `character_blocklist` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `blocked_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`char_id`,`blocked_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';