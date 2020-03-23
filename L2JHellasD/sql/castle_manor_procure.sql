SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `castle_manor_procure`
-- ---------------------------

CREATE TABLE `castle_manor_procure` (
  `castle_id` int(11) NOT NULL DEFAULT '0',
  `crop_id` int(11) NOT NULL DEFAULT '0',
  `can_buy` int(11) NOT NULL DEFAULT '0',
  `start_buy` int(11) NOT NULL DEFAULT '0',
  `price` int(11) NOT NULL DEFAULT '0',
  `reward_type` int(11) NOT NULL DEFAULT '0',
  `period` int(11) NOT NULL DEFAULT '1',
  PRIMARY KEY (`castle_id`,`crop_id`,`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';