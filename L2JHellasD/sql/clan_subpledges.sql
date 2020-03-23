SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `clan_subpledges`
-- ---------------------------

CREATE TABLE `clan_subpledges` (
  `clan_id` int(11) NOT NULL DEFAULT '0',
  `sub_pledge_id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(45) DEFAULT NULL,
  `leader_name` varchar(35) DEFAULT NULL,
  PRIMARY KEY (`clan_id`,`sub_pledge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';