SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `dm`
-- ----------------------------

CREATE TABLE `dm` (
  `eventName` varchar(255) NOT NULL DEFAULT '',
  `eventDesc` varchar(255) NOT NULL DEFAULT '',
  `joiningLocation` varchar(255) NOT NULL DEFAULT '',
  `minlvl` int(4) NOT NULL DEFAULT '0',
  `maxlvl` int(4) NOT NULL DEFAULT '0',
  `npcId` int(8) NOT NULL DEFAULT '0',
  `npcX` int(11) NOT NULL DEFAULT '0',
  `npcY` int(11) NOT NULL DEFAULT '0',
  `npcZ` int(11) NOT NULL DEFAULT '0',
  `rewardId` int(11) NOT NULL DEFAULT '0',
  `rewardAmount` int(11) NOT NULL DEFAULT '0',
  `color` int(11) NOT NULL DEFAULT '0',
  `playerX` int(11) NOT NULL DEFAULT '0',
  `playerY` int(11) NOT NULL DEFAULT '0',
  `playerZ` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';