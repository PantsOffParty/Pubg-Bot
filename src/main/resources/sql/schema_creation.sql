CREATE DATABASE  IF NOT EXISTS `god_bot`;
USE `god_bot`;

DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
  `player_name` varchar(25) NOT NULL,
  `account_id` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`player_name`)
);

DROP TABLE IF EXISTS `win`;
CREATE TABLE `win` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `win_date` datetime DEFAULT CURRENT_TIMESTAMP,
  `x_coord` float NOT NULL,
  `y_coord` float NOT NULL,
  `win_image` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
);
