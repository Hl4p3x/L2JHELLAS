SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `tvt_teams`
-- -----------------------------

CREATE TABLE `tvt_teams` (
  `teamId` int(4) NOT NULL DEFAULT '0',
  `teamName` varchar(255) NOT NULL DEFAULT '',
  `teamX` int(11) NOT NULL DEFAULT '0',
  `teamY` int(11) NOT NULL DEFAULT '0',
  `teamZ` int(11) NOT NULL DEFAULT '0',
  `teamColor` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`teamId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

-- ----------------------------
-- Records of `tvt_teams`
-- ----------------------------
INSERT INTO `tvt_teams` VALUES 
('0', 'Wolfs', '148179', '45841', '-3413', '16711680'),
('1', 'Beasts', '150787', '45822', '-3413', '255'),
('2', 'Vampires', '149496', '47826', '-3413', '26367');