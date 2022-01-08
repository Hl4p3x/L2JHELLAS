SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `custom_npc`
-- ----------------------------

CREATE TABLE `custom_npc` (
  `id` decimal(11,0) NOT NULL DEFAULT '0',
  `idTemplate` int(11) NOT NULL DEFAULT '0',
  `name` varchar(200) DEFAULT NULL,
  `serverSideName` int(1) DEFAULT '0',
  `title` varchar(45) DEFAULT '',
  `serverSideTitle` int(1) DEFAULT '0',
  `class` varchar(200) DEFAULT NULL,
  `collision_radius` decimal(5,2) DEFAULT NULL,
  `collision_height` decimal(5,2) DEFAULT NULL,
  `level` decimal(2,0) DEFAULT NULL,
  `sex` varchar(6) DEFAULT NULL,
  `type` varchar(20) DEFAULT NULL,
  `attackrange` int(11) DEFAULT NULL,
  `hp` decimal(8,0) DEFAULT NULL,
  `mp` decimal(5,0) DEFAULT NULL,
  `hpreg` decimal(8,2) DEFAULT NULL,
  `mpreg` decimal(5,2) DEFAULT NULL,
  `str` decimal(7,0) DEFAULT NULL,
  `con` decimal(7,0) DEFAULT NULL,
  `dex` decimal(7,0) DEFAULT NULL,
  `int` decimal(7,0) DEFAULT NULL,
  `wit` decimal(7,0) DEFAULT NULL,
  `men` decimal(7,0) DEFAULT NULL,
  `exp` decimal(9,0) DEFAULT NULL,
  `sp` decimal(8,0) DEFAULT NULL,
  `patk` decimal(5,0) DEFAULT NULL,
  `pdef` decimal(5,0) DEFAULT NULL,
  `matk` decimal(5,0) DEFAULT NULL,
  `mdef` decimal(5,0) DEFAULT NULL,
  `atkspd` decimal(3,0) DEFAULT NULL,
  `aggro` decimal(6,0) DEFAULT NULL,
  `matkspd` decimal(4,0) DEFAULT NULL,
  `rhand` decimal(4,0) DEFAULT NULL,
  `lhand` decimal(4,0) DEFAULT NULL,
  `armor` decimal(1,0) DEFAULT NULL,
  `walkspd` decimal(3,0) DEFAULT NULL,
  `runspd` decimal(3,0) DEFAULT NULL,
  `faction_id` varchar(40) DEFAULT NULL,
  `faction_range` decimal(4,0) DEFAULT NULL,
  `isUndead` int(11) DEFAULT '0',
  `absorb_level` decimal(2,0) DEFAULT '0',
  `absorb_type` enum('FULL_PARTY','LAST_HIT','PARTY_ONE_RANDOM') NOT NULL DEFAULT 'LAST_HIT',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci ROW_FORMAT=COMPRESSED COMMENT='L2jHellas Table';

-- ----------------------------
-- Records of `custom_npc`
-- ----------------------------
INSERT INTO `custom_npc` VALUES
('50', '30767', 'Sofia', '1', 'Buffer', '1', 'Monster2.queen_of_cat', '8.00', '22.00', '70', 'etc', 'L2Buffer', '40', '3892', '1567', '23.00', '1.00', '40', '40', '40', '40', '40', '40', '0', '0', '2314', '2341', '324', '234', '234', '0', '333', '0', '0', '0', '65', '123', null, '0', '0', '0', 'LAST_HIT'),
('51', '30767', 'Maria', '1', 'Buffer', '1', 'Monster2.queen_of_cat', '8.00', '22.00', '70', 'etc', 'L2NpcBuffer', '40', '3892', '1567', '23.00', '1.00', '40', '40', '40', '40', '40', '40', '0', '0', '2314', '2341', '324', '234', '234', '0', '333', '0', '0', '0', '65', '123', null, '0', '0', '0', 'LAST_HIT'),
('52', '31324', 'Camila', '1', 'Buffer', '1', 'NPC.a_casino_FDarkElf', '8.00', '23.00', '70', 'female', 'L2SchemeBuffer', '40', '3892', '1567', '23.00', '1.00', '40', '40', '40', '40', '40', '40', '0', '0', '2314', '2341', '324', '234', '234', '0', '333', '0', '0', '0', '65', '123', null, '0', '0', '0', 'LAST_HIT'),
('61', '31309', 'Napoleon', '1', 'Vote Manager', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2VoteManager', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('100', '31309', 'Dorian', '1', 'Shop', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2Merchant', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('50017', '30080', 'Louisa', '1', 'GateKeeper', '1', 'NPC.a_teleporter_FHuman', '8.00', '25.00', '70', 'female', 'L2Teleporter', '40', '3892', '1567', '23.00', '1.00', '40', '40', '40', '40', '40', '40', '0', '0', '2314', '2341', '324', '234', '234', '0', '333', '0', '0', '0', '65', '123', null, '0', '0', '0', 'LAST_HIT'),
('50007', '31324', 'Andromeda', '1', 'Wedding Manager', '1', 'NPC.a_casino_FDarkElf', '8.00', '23.00', '70', 'female', 'L2WeddingManager', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '316', '0', '0', '55', '132', null, '0', '1', '0', 'LAST_HIT'),
('70007', '31309', 'Paul', '1', 'Casino', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2Casino', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('70008', '31309', 'Zaon', '1', 'Donate Manager', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2Donate', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('70009', '31309', 'Philip', '1', 'Boss Info', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2BossSpawn', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('70010', '31309', 'Aioria', '1', 'Event Manager', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2EventManager', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('70011', '31309', 'Lee', '1', 'Noble Manager', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2CharNobles', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('70013', '31309', 'Achievement', '1', 'Manager', '1', 'NPC.a_traderD_Mhuman', '8.00', '25.30', '70', 'male', 'L2Achievements', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('70014', '32083', 'MassInformer', '1', 'Siege', '1', 'NPC.a_traderD_Mhuman', '10.00', '15.00', '70', 'male', 'L2CastleManager', '40', '2444', '2444', '0.00', '0.00', '10', '10', '10', '10', '10', '10', '0', '0', '500', '500', '500', '500', '278', '0', '333', '9376', '0', '0', '30', '120', '', '0', '0', '0', 'LAST_HIT'),
('70016', '31361', 'Spammer', '1', 'Server', '1', 'NPC.a_trader_MDarkElf', '8.00', '22.50', '70', 'male', 'L2NpcWalker', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '151', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT'),
('9103', '31772', 'Zone', '0', '', '1', 'LineageNPC.heroes_obelisk_dwarf', '23.00', '80.00', '78', 'etc', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '151', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT'),
('9104', '35062', 'Blue Flag', '1', '', '1', 'Deco.flag_a', '21.00', '82.00', '1', 'etc', 'L2Npc', '40', '158000', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '151', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT'),
('9105', '35062', 'Red Flag', '1', '', '1', 'Deco.flag_a', '21.00', '82.00', '1', 'etc', 'L2Npc', '40', '158000', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '151', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT'),
('9106', '32027', 'Blue Flag Holder', '0', '', '0', 'NpcEV.grail_brazier_b', '9.50', '29.00', '78', 'male', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '151', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT'),
('9107', '31361', 'Red Flag Holder', '0', '', '0', 'NpcEV.grail_brazier_b', '9.50', '29.00', '78', 'male', 'L2Npc', '40', '3862', '1493', '11.85', '2.78', '40', '43', '30', '21', '20', '10', '0', '0', '1314', '470', '780', '382', '278', '0', '333', '151', '0', '0', '55', '132', '', '0', '1', '0', 'LAST_HIT');




