SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `raid_events_spawnlist`
-- ----------------------------

CREATE TABLE `raid_event_spawnlist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `location` varchar(40) NOT NULL DEFAULT '',
  `raid_locX` int(9) NOT NULL,
  `raid_locY` int(9) NOT NULL,
  `raid_locZ` int(9) NOT NULL,
  `player_locX` int(9) NOT NULL,
  `player_locY` int(9) NOT NULL,
  `player_locZ` int(9) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

-- ----------------------------
-- Records of `raid_event_spawnlist`
-- ----------------------------
INSERT INTO `raid_event_spawnlist` VALUES 
('1', 'Test', '-93287', '-251026', '-3336', '-94347', '-251026', '-3136'),
('2', 'Test', '-87131', '-257755', '-3336', '-88020', '-257755', '-3136'),
('3', 'Test', '174167', '-75329', '-5107', '174085', '-76703', '-5007'),
('4', 'Test', '174252', '-88483', '-5139', '174242', '-86548', '-5007'),
('5', 'Test', '174091', '-82305', '-5123', '174103', '-80650', '-5007');