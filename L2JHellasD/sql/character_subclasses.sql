SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `character_subclasses`
-- ---------------------------

CREATE TABLE `character_subclasses` (
  `char_obj_id` decimal(11,0) NOT NULL DEFAULT '0',
  `class_id` int(2) NOT NULL DEFAULT '0',
  `exp` decimal(20,0) NOT NULL DEFAULT '0',
  `sp` decimal(11,0) NOT NULL DEFAULT '0',
  `level` int(2) NOT NULL DEFAULT '40',
  `class_index` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`char_obj_id`,`class_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';