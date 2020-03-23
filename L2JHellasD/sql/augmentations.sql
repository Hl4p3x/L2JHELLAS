SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `augmentations`
-- ---------------------------

CREATE TABLE `augmentations` (
  `item_id` int(11) NOT NULL DEFAULT '0',
  `attributes` int(11) DEFAULT '0',
  `skill` int(11) DEFAULT '0',
  `level` int(11) DEFAULT '0',
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';