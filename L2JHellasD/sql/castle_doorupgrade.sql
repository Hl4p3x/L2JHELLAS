SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `castle_doorupgrade`
-- ---------------------------

CREATE TABLE `castle_doorupgrade` (
  `doorId` int(11) NOT NULL DEFAULT '0',
  `hp` int(11) NOT NULL DEFAULT '0',
  `pDef` int(11) NOT NULL DEFAULT '0',
  `mDef` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`doorId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';