SET FOREIGN_KEY_CHECKS=0;
-- ---------------------------
-- Table structure for `auction_watch`
-- ---------------------------

CREATE TABLE `auction_watch` (
  `charObjId` int(11) NOT NULL DEFAULT '0',
  `auctionId` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`charObjId`,`auctionId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';