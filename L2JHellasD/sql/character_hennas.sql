SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `character_hennas`
-- ---------------------------

CREATE TABLE `character_hennas` (
  `char_obj_id` int(11) NOT NULL DEFAULT '0',
  `symbol_id` int(11) DEFAULT NULL,
  `slot` int(11) NOT NULL DEFAULT '0',
  `class_index` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`char_obj_id`,`slot`,`class_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';