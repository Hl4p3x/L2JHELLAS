SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `ctf_teams`
-- ----------------------------

CREATE TABLE `ctf_teams` (
  `teamId` int(4) NOT NULL DEFAULT '0',
  `teamName` varchar(255) NOT NULL DEFAULT '',
  `teamX` int(11) NOT NULL DEFAULT '0',
  `teamY` int(11) NOT NULL DEFAULT '0',
  `teamZ` int(11) NOT NULL DEFAULT '0',
  `teamColor` int(11) NOT NULL DEFAULT '0',
  `flagX` int(11) NOT NULL DEFAULT '0',
  `flagY` int(11) NOT NULL DEFAULT '0',
  `flagZ` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`teamId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';