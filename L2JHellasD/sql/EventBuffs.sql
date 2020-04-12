CREATE TABLE IF NOT EXISTS `event_buffs` (
  `player` varchar(30) NOT NULL,
  `buffs` varchar(200) NOT NULL,
  PRIMARY KEY (`player`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;