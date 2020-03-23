SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `games`
-- ----------------------------

CREATE TABLE `games` (
  `id` int(11) NOT NULL DEFAULT '0',
  `idnr` int(11) NOT NULL DEFAULT '0',
  `number1` int(11) NOT NULL DEFAULT '0',
  `number2` int(11) NOT NULL DEFAULT '0',
  `prize` int(11) NOT NULL DEFAULT '0',
  `newprize` int(11) NOT NULL DEFAULT '0',
  `prize1` int(11) NOT NULL DEFAULT '0',
  `prize2` int(11) NOT NULL DEFAULT '0',
  `prize3` int(11) NOT NULL DEFAULT '0',
  `enddate` decimal(20,0) NOT NULL DEFAULT '0',
  `finished` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`,`idnr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';