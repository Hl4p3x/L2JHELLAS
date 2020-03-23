SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `castle`
-- ---------------------------

CREATE TABLE `castle` (
  `id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(25) NOT NULL,
  `taxPercent` int(11) NOT NULL DEFAULT '15',
  `treasury` int(11) NOT NULL DEFAULT '0',
  `siegeDate` decimal(20,0) NOT NULL DEFAULT '0',
  `siegeDayOfWeek` int(11) NOT NULL DEFAULT '7',
  `siegeHourOfDay` int(11) NOT NULL DEFAULT '20',
  `showNpcCrest` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`name`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

-- ----------------------------
-- Records of `castle`
-- ----------------------------
INSERT IGNORE INTO `castle` VALUES 
(1,'Gludio',0,0,0,7,20,false),
(2,'Dion',0,0,0,7,20,false),
(3,'Giran',0,0,0,1,16,false),
(4,'Oren',0,0,0,1,16,false),
(5,'Aden',0,0,0,7,20,false),
(6,'Innadril',0,0,0,1,16,false),
(7,'Goddard',0,0,0,1,16,false),
(8,'Rune',0,0,0,7,20,false),
(9,'Schuttgart',0,0,0,7,20,false);