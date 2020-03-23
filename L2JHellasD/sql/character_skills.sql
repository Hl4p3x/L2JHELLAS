SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for character_skills
-- ---------------------------

CREATE TABLE `character_skills` (
  `char_obj_id` int(11) NOT NULL DEFAULT '0',
  `skill_id` int(11) NOT NULL DEFAULT '0',
  `skill_level` varchar(5) DEFAULT NULL,
  `skill_name` varchar(40) DEFAULT NULL,
  `class_index` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`char_obj_id`,`skill_id`,`class_index`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';