SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `grandboss_data`
-- ----------------------------

CREATE TABLE `grandboss_data` (
  `boss_id` int(11) NOT NULL DEFAULT '0',
  `loc_x` int(11) NOT NULL DEFAULT '0',
  `loc_y` int(11) NOT NULL DEFAULT '0',
  `loc_z` int(11) NOT NULL DEFAULT '0',
  `heading` int(11) NOT NULL DEFAULT '0',
  `respawn_time` bigint(20) NOT NULL DEFAULT '0',
  `currentHP` decimal(8,0) DEFAULT NULL,
  `currentMP` decimal(8,0) DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '0',
  PRIMARY KEY (`boss_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

-- ----------------------------
-- Records of `grandboss_data`
-- ----------------------------
INSERT IGNORE INTO `grandboss_data` VALUES
(25512, 96080, -110822, -3343, 0, 0, 0, 0, 0),		-- Dr Chaos
(29001, -21610, 181594, -5734, 0, 0, 0, 0, 0),		-- Queen Ant
(29006, 17726, 108915, -6480, 0, 0, 0, 0, 0),		-- Core
(29014, 55024, 17368, -5412, 10126, 0, 0, 0, 0),	-- Orfen
(29019, 185708, 114298, -8221, 32768, 0, 0, 0, 0),	-- Antharas
(29020, 115762, 17116, 10077, 8250, 0, 0, 0, 0),	-- Baium
(29022, 55312, 219168, -3223, 0, 0, 0, 0, 0),		-- Zaken
(29028, -105200,-253104,-15264, 0, 0, 0, 0, 0),		-- Valakas
(29045, 0, 0, 0, 0, 0, 0, 0, 0),					-- Frintezza
(29046, 174231, -88006, -5115, 0, 0, 0, 0, 0),		-- Scarlet Van Halisha (85)
(29047, 174231, -88006, -5115, 0, 0, 0, 0, 0),		-- Scarlet Van Halisha (85)
(29062, 0, 0, 0, 0,0, 0, 0, 1),                     -- IceFairySirra
(29056, 102722, -127892, -2768, 0,0, 0, 0, 1),
(29065, 27549, -6638, -2008, 0, 0, 0, 0, 0);		-- Sailren