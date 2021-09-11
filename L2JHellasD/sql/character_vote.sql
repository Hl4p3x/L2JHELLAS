CREATE TABLE `character_vote` (
  `ip` varchar(20) NOT NULL,
  `site` varchar(20) NOT NULL,
  `time` bigint(50) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ip`,`site`)
);