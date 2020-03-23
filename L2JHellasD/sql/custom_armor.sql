SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `custom_armor`
-- ----------------------------

CREATE TABLE `custom_armor` (
  `item_id` int(11) NOT NULL DEFAULT '0',
  `name` varchar(70) DEFAULT NULL,
  `bodypart` varchar(15) NOT NULL DEFAULT '',
  `crystallizable` varchar(5) NOT NULL DEFAULT '',
  `armor_type` varchar(5) NOT NULL DEFAULT '',
  `weight` int(5) NOT NULL DEFAULT '0',
  `material` varchar(15) NOT NULL DEFAULT '',
  `crystal_type` varchar(4) NOT NULL DEFAULT '',
  `avoid_modify` int(1) NOT NULL DEFAULT '0',
  `duration` int(3) NOT NULL DEFAULT '0',
  `p_def` int(3) NOT NULL DEFAULT '0',
  `m_def` int(2) NOT NULL DEFAULT '0',
  `mp_bonus` int(3) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `crystal_count` int(4) DEFAULT NULL,
  `sellable` varchar(5) DEFAULT NULL,
  `dropable` varchar(5) DEFAULT NULL,
  `destroyable` varchar(5) DEFAULT NULL,
  `tradeable` varchar(5) DEFAULT NULL,
  `item_skill_id` decimal(11,0) NOT NULL DEFAULT '0',
  `item_skill_lvl` decimal(11,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';