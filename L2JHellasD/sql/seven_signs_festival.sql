SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `seven_signs_festival`
-- ----------------------------

CREATE TABLE `seven_signs_festival` (
  `festivalId` int(1) NOT NULL DEFAULT '0',
  `cabal` varchar(4) NOT NULL DEFAULT '',
  `cycle` int(4) NOT NULL DEFAULT '0',
  `date` bigint(50) DEFAULT '0',
  `score` int(5) NOT NULL DEFAULT '0',
  `members` varchar(255) NOT NULL DEFAULT '',
  PRIMARY KEY (`festivalId`,`cabal`,`cycle`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

-- ----------------------------
-- Records of `seven_signs_festival`
-- ----------------------------
INSERT IGNORE INTO `seven_signs_festival` VALUES 
(0, "dawn", 1, 0, 0, ""),
(1, "dawn", 1, 0, 0, ""),
(2, "dawn", 1, 0, 0, ""),
(3, "dawn", 1, 0, 0, ""),
(4, "dawn", 1, 0, 0, ""),
(0, "dusk", 1, 0, 0, ""),
(1, "dusk", 1, 0, 0, ""),
(2, "dusk", 1, 0, 0, ""),
(3, "dusk", 1, 0, 0, ""),
(4, "dusk", 1, 0, 0, "");