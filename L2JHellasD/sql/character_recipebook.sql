SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `character_recipebook`
-- ---------------------------

CREATE TABLE `character_recipebook` (
  `char_id` decimal(11,0) NOT NULL DEFAULT '0',
  `id` decimal(11,0) NOT NULL DEFAULT '0',
  `type` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';