SET FOREIGN_KEY_CHECKS=0;
-- olympiad update
ALTER TABLE `characters`
MODIFY COLUMN `hero`  tinyint(1) UNSIGNED NULL DEFAULT 0 AFTER `death_penalty_level`;

ALTER TABLE `heroes`
CHANGE COLUMN `char_name` `class_id`  decimal(3,0) NOT NULL DEFAULT 0 AFTER `char_id`,
CHANGE COLUMN `class_id` `count`  decimal(3,0) NOT NULL DEFAULT 0 AFTER `class_id`,
CHANGE COLUMN `count` `played`  decimal(1,0) NOT NULL DEFAULT 0 AFTER `count`,
CHANGE COLUMN `played` `active`  tinyint(1) NOT NULL DEFAULT 0 AFTER `played`,
ADD COLUMN `message`  varchar(300) NOT NULL AFTER `active`;

CREATE TABLE IF NOT EXISTS `olympiad_data` (
  `id` TINYINT UNSIGNED NOT NULL DEFAULT 0,
  `current_cycle` MEDIUMINT UNSIGNED NOT NULL DEFAULT 1,
  `period` MEDIUMINT UNSIGNED NOT NULL DEFAULT 0,
  `olympiad_end` bigint(13) unsigned NOT NULL DEFAULT '0',
  `validation_end` bigint(13) unsigned NOT NULL DEFAULT '0',
  `next_weekly_change` bigint(13) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

CREATE TABLE IF NOT EXISTS `olympiad_fights` (
  `charOneId` int(10) unsigned NOT NULL,
  `charTwoId` int(10) unsigned NOT NULL,
  `charOneClass` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `charTwoClass` tinyint(3) unsigned NOT NULL DEFAULT '0',
  `winner` tinyint(1) unsigned NOT NULL DEFAULT '0',
  `start` bigint(13) unsigned NOT NULL DEFAULT '0',
  `time` bigint(13) unsigned NOT NULL DEFAULT '0',
  `classed` tinyint(1) unsigned NOT NULL DEFAULT '0',
  KEY `charOneId` (`charOneId`),
  KEY `charTwoId` (`charTwoId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

CREATE TABLE IF NOT EXISTS `olympiad_nobles_eom` (
  `char_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `class_id` tinyint(3) unsigned NOT NULL DEFAULT 0,
  `olympiad_points` int(10) NOT NULL DEFAULT 0,
  `competitions_done` smallint(3) NOT NULL DEFAULT 0,
  `competitions_won` smallint(3) NOT NULL DEFAULT 0,
  `competitions_lost` smallint(3) NOT NULL DEFAULT 0,
  `competitions_drawn` smallint(3) NOT NULL DEFAULT 0,
  PRIMARY KEY (`char_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

ALTER TABLE `olympiad_nobles`
MODIFY COLUMN `char_id`  int(11) UNSIGNED NOT NULL DEFAULT 0 FIRST ,
MODIFY COLUMN `class_id`  tinyint(3) NOT NULL DEFAULT 0 AFTER `char_id`,
CHANGE COLUMN `char_name` `olympiad_points`  int(10) NOT NULL AFTER `class_id`,
CHANGE COLUMN `olympiad_points` `competitions_done`  smallint(3) NOT NULL AFTER `olympiad_points`,
CHANGE COLUMN `competitions_done` `competitions_won`  smallint(3) NOT NULL AFTER `competitions_done`,
ADD COLUMN `competitions_lost`  smallint(3) NOT NULL AFTER `competitions_won`,
ADD COLUMN `competitions_drawn`  smallint(3) NOT NULL AFTER `competitions_lost`;