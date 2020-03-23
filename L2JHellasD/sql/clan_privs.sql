SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `clan_privs`
-- ---------------------------

CREATE TABLE `clan_privs` (
  `clan_id` int(11) NOT NULL DEFAULT '0',
  `rank` int(11) NOT NULL DEFAULT '0',
  `party` int(11) NOT NULL DEFAULT '0',
  `privs` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`clan_id`,`rank`,`party`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';