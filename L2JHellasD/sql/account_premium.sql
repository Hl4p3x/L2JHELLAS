SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `account_premium`
-- ----------------------------

CREATE TABLE `account_premium` (
  `account_name` varchar(45) NOT NULL DEFAULT '',
  `premium_service` int(1) NOT NULL DEFAULT '1',
  `enddate` decimal(20,0) NOT NULL DEFAULT '0',
  PRIMARY KEY (`account_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';