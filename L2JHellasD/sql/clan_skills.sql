SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `clan_skills`
-- ---------------------------

CREATE TABLE `clan_skills` (
  `clan_id` int(11) NOT NULL DEFAULT '0',
  `skill_id` int(11) NOT NULL DEFAULT '0',
  `skill_level` int(5) NOT NULL DEFAULT '0',
  `skill_name` varchar(26) DEFAULT NULL,
  PRIMARY KEY (`clan_id`,`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';