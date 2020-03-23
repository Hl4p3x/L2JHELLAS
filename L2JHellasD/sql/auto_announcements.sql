SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `auto_announcements`
-- ----------------------------

CREATE TABLE `auto_announcements` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `announcement` varchar(255) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL DEFAULT '',
  `delay` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';