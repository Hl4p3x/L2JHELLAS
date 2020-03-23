SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `character_friends`
-- ---------------------------

CREATE TABLE `character_friends` (
  `char_id` int(11) NOT NULL DEFAULT '0',
  `friend_id` int(11) NOT NULL DEFAULT '0',
  `friend_name` varchar(35) NOT NULL DEFAULT '',
  PRIMARY KEY (`char_id`,`friend_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';